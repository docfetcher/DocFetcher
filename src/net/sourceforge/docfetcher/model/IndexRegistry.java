/*******************************************************************************
 * Copyright (c) 2010, 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.primitives.Longs;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.enums.ProgramConf;
import net.sourceforge.docfetcher.enums.SettingsConf;
import net.sourceforge.docfetcher.model.IndexLoadingProblems.CorruptedIndex;
import net.sourceforge.docfetcher.model.IndexLoadingProblems.OverflowIndex;
import net.sourceforge.docfetcher.model.index.IndexingQueue;
import net.sourceforge.docfetcher.model.index.file.FileFactory;
import net.sourceforge.docfetcher.model.index.outlook.OutlookMailFactory;
import net.sourceforge.docfetcher.model.search.Searcher;
import net.sourceforge.docfetcher.model.search.SourceCodeTokenizer;
import net.sourceforge.docfetcher.util.AppUtil;
import net.sourceforge.docfetcher.util.CharsetDetectorHelper;
import net.sourceforge.docfetcher.util.Event;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.CallOnce;
import net.sourceforge.docfetcher.util.annotations.ImmutableCopy;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.Nullable;
import net.sourceforge.docfetcher.util.annotations.ThreadSafe;
import net.sourceforge.docfetcher.util.annotations.VisibleForPackageGroup;
import net.sourceforge.docfetcher.util.collect.AlphanumComparator;
import net.sourceforge.docfetcher.util.collect.LazyList;
import net.sourceforge.docfetcher.util.concurrent.BlockingWrapper;
import net.sourceforge.docfetcher.util.concurrent.DelayedExecutor;

/**
 * @author Tran Nam Quang
 */
@ThreadSafe
public final class IndexRegistry {

	public interface ExistingIndexesHandler {
		public void handleExistingIndexes(@NotNull List<LuceneIndex> indexes);
	}

	/*
	 * TODO websearch: code convention: Don't access Version elsewhere, don't instantiate
	 * Analyzer+ elsewhere, don't call setMaxClauseCount elsewhere.
	 */
	@VisibleForPackageGroup
	public static final Version LUCENE_VERSION = Version.LUCENE_6_6_3;

	private static Analyzer analyzer = null;
	
	@Nullable
	public static volatile File indexPathOverride = null;

	private static final String SER_FILENAME = "tree-index.ser";
	private static final String NAME_FILENAME = "index-name.txt";
	
	/*
	 * This setting prevents errors that would otherwise occur if the user
	 * enters generic search terms like "*?".
	 */
	static {
		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
	}

	// These events must always be fired under lock!
	// Avoid firing while holding only the read-lock, since it cannot be upgraded to a write-lock
	private final Event<LuceneIndex> evtAdded = new Event<LuceneIndex>();
	private final Event<List<LuceneIndex>> evtRemoved = new Event<List<LuceneIndex>>();

	/**
	 * A map for storing the indexes, along with the last-modified values of the
	 * indexes' ser files. A last-modified value may be null, which indicates
	 * that the corresponding index hasn't been saved yet.
	 */
	private final Map<LuceneIndex, Long> indexes = Maps.newTreeMap(IndexComparator.instance); // guarded by read-write lock

	/*
	 * This read-write lock is used for the index registry, the indexing queue,
	 * the searcher and the folder watcher. With the exception of the searcher,
	 * a read-write lock might not be the best choice for these classes in terms
	 * of efficiency. However, by using the same lock for all classes, we can
	 * avoid potential lock-ordering deadlocks.
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private final File indexParentDir;
	private final IndexingQueue queue;
	private final HotColdFileCache unpackCache;
	private final FileFactory fileFactory;
	private final OutlookMailFactory outlookMailFactory;
	private final BlockingWrapper<Searcher> searcher = new BlockingWrapper<Searcher>();

	@NotNull
	public static Analyzer getAnalyzer() {
		/* The analyzer is created lazily to ensure that the program settings
		 * have already been loaded. */
		if (analyzer == null) {
			resetAnalyzer();
		}
		return analyzer;
	}
	
	public static void resetAnalyzer() {
		switch (SettingsConf.Int.LuceneAnalyzer.get()) {
		case 1:
			analyzer = new SourceCodeAnalyzer(LUCENE_VERSION); break;
		case 2:
			analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.index_ansj); break;
		default:
			analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
		}
	}

	public IndexRegistry(	@NotNull File indexParentDir,
							int cacheSize,
							int reporterCapacity) {
		Util.checkNotNull(indexParentDir);
		this.indexParentDir = indexParentDir;
		this.unpackCache = new HotColdFileCache(cacheSize);
		this.fileFactory = new FileFactory(unpackCache);
		this.outlookMailFactory = new OutlookMailFactory(unpackCache);

		/*
		 * Giving out a reference to the IndexRegistry before it is fully
		 * constructed might be a little dangerous :-/
		 */
		this.queue = new IndexingQueue(this, reporterCapacity);
	}

	@NotNull
	@ThreadSafe
	public File getIndexParentDir() {
		return indexParentDir;
	}

	@NotNull
	@ThreadSafe
	public IndexingQueue getQueue() {
		return queue;
	}

	// Will block until the searcher is available (i.e. after load(...) has finished)
	// do not call this from the GUI thread, otherwise the application might hang
	// May return null if the calling thread was interrupted
	@Nullable
	@ThreadSafe
	public Searcher getSearcher() {
		/*
		 * This method must not be synchronized, otherwise we'll get a deadlock
		 * when this method is called while the load method is running.
		 */
		return searcher.get();
	}

	// Should not be used by clients
	@NotNull
	@ThreadSafe
	public Lock getReadLock() {
		return readLock;
	}

	// Should not be used by clients
	@NotNull
	@ThreadSafe
	public Lock getWriteLock() {
		return writeLock;
	}

	@ThreadSafe
	@VisibleForPackageGroup
	public void addIndex(@NotNull LuceneIndex index) {
		addIndex(index, null);
	}

	@ThreadSafe
	private void addIndex(	@NotNull LuceneIndex index,
							@Nullable Long lastModified) {
		Util.checkNotNull(index);
		Util.checkNotNull(index.getIndexDirPath()); // RAM indexes not allowed
		writeLock.lock();
		try {
			if (indexes.containsKey(index))
				return;
			indexes.put(index, lastModified);
		}
		finally {
			writeLock.unlock();
		}
		evtAdded.fire(index);
	}

	@ThreadSafe
	public void removeIndexes(	@NotNull Collection<LuceneIndex> indexesToRemove,
								boolean deleteFiles) {
		Util.checkNotNull(indexesToRemove);
		if (indexesToRemove.isEmpty())
			return; // Avoid firing event when given collection is empty

		int size = indexesToRemove.size();
		List<LuceneIndex> removed = new ArrayList<LuceneIndex>(size);
		List<PendingDeletion> deletions = deleteFiles
			? new ArrayList<PendingDeletion>(size)
			: null;

		writeLock.lock();
		try {
			for (LuceneIndex index : indexesToRemove) {
				if (!indexes.containsKey(index))
					continue;
				indexes.remove(index);
				if (deleteFiles)
					deletions.add(new PendingDeletion(index));
				removed.add(index);
			}

			/*
			 * This is done with the lock held to avoid releasing and
			 * reacquiring it.
			 */
			if (deletions != null) {
				queue.approveDeletions(deletions);
				searcher.get().approveDeletions(deletions);
			}
		}
		finally {
			writeLock.unlock();
		}

		evtRemoved.fire(removed);
	}

	// Allows attaching change listeners and processing the existing indexes in
	// one atomic operation, i.e. the indexes handler only receives the indexes
	// that will existed when this method is called.
	// Indexes handler and listeners are notified *without* holding the lock.
	// Events may arrive from non-GUI threads; indexes handler runs in the same
	// thread as the client
	// The list of indexes given to the handler is an immutable copy
	@ThreadSafe
	public void addListeners(	@NotNull ExistingIndexesHandler handler,
								@Nullable Event.Listener<LuceneIndex> addedListener,
								@Nullable Event.Listener<List<LuceneIndex>> removedListener) {
		Util.checkNotNull(handler);
		List<LuceneIndex> indexesCopy;
		writeLock.lock();
		try {
			indexesCopy = ImmutableList.copyOf(indexes.keySet());
			if (addedListener != null)
				evtAdded.add(addedListener);
			if (removedListener != null)
				evtRemoved.add(removedListener);
		}
		finally {
			writeLock.unlock();
		}
		handler.handleExistingIndexes(indexesCopy);
	}

	@ThreadSafe
	public void removeListeners(@Nullable Event.Listener<LuceneIndex> addedListener,
								@Nullable Event.Listener<List<LuceneIndex>> removedListener) {
		if (addedListener == null && removedListener == null)
			return;

		/*
		 * The event class is thread-safe; the lock is only used to make this an
		 * atomic operation.
		 */
		writeLock.lock();
		try {
			if (addedListener != null)
				evtAdded.remove(addedListener);
			if (removedListener != null)
				evtRemoved.remove(removedListener);
		}
		finally {
			writeLock.unlock();
		}
	}

	@ImmutableCopy
	@NotNull
	@ThreadSafe
	public List<LuceneIndex> getIndexes() {
		readLock.lock();
		try {
			return ImmutableList.copyOf(indexes.keySet());
		}
		finally {
			readLock.unlock();
		}
	}

	@CallOnce
	@ThreadSafe
	public IndexLoadingProblems load(@NotNull Cancelable cancelable) throws IOException {
		/*
		 * Note: To allow running this method in parallel with other operations,
		 * it is important not to lock the entire method. Otherwise, if a client
		 * tries to attach listeners to the registry while this method is
		 * running, the former would block until the latter has finished,
		 * resulting in a serialization of both operations.
		 */

		// Ensure this method can only be called once
		Util.checkThat(searcher.isNull());

		indexParentDir.mkdirs(); // Needed for the folder watching
		IndexLoadingProblems loadingProblems = new IndexLoadingProblems();
		
		for (File file : Util.listFiles(indexParentDir)) {
			if (cancelable.isCanceled())
				break;
			if (file.isDirectory()) {
				File serFile = new File(file, SER_FILENAME);
				if (serFile.isFile()) {
					/*
					 * Try to load the tree-index.ser. If this fails, we're
					 * probably dealing with a tree-index.ser from DocFetcher
					 * 1.1 beta 1 through DocFetcher 1.1 beta 6, because the
					 * serialization version UID was changed after 1.1 beta 6.
					 */
					try {
						if (!loadIndex(serFile)) {
							loadingProblems.addObsoleteFile(file);
						}
					} catch (StackOverflowError e) {
						loadingProblems.addOverflowIndex(new OverflowIndex(file, e));
					}
				}
				else if (!serFile.exists()) {
					/*
					 * If no tree-index.ser exists and the containing folder has
					 * a name that ends with a timestamp, it's probably an index
					 * folder from DocFetcher 1.0.3 or earlier.
					 */
					if (file.getName().matches(".*?_\\d+"))
						loadingProblems.addObsoleteFile(file);
				}
				// Ignore if tree-index.ser is a directory
			}
			else if (file.isFile()) {
				/*
				 * ScopeRegistry.ser files were used in DocFetcher 1.0.3 and
				 * earlier.
				 */
				if (file.getName().equals("ScopeRegistry.ser"))
					loadingProblems.addObsoleteFile(file);
			}
		}

		LazyList<CorruptedIndex> corruptedIndexes = new LazyList<CorruptedIndex>();
		searcher.set(new Searcher(
			this, fileFactory, outlookMailFactory, corruptedIndexes));
		
		for (CorruptedIndex index : corruptedIndexes)
			loadingProblems.addCorruptedIndex(index);

		// Watch index directory for changes
		try {
			final DelayedExecutor executor = new DelayedExecutor(1000);

			final int watchId = new SimpleJNotifyListener() {
				protected void handleEvent(File targetFile, EventType eventType) {
					if (!targetFile.getName().equals(SER_FILENAME))
						return;
					executor.schedule(new Runnable() {
						public void run() {
							reload();
						}
					});
				}
			}.addWatch(indexParentDir);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						JNotify.removeWatch(watchId);
					}
					catch (JNotifyException e) {
						Util.printErr(e);
					}
				}
			});
		}
		catch (JNotifyException e) {
			Util.printErr(e);
		}

		return loadingProblems;
	}

	/**
	 * Load the given tree index file. Returns whether the file was successfully
	 * loaded.
	 */
	@ThreadSafe
	private boolean loadIndex(@NotNull File serFile) {
		ObjectInputStream in = null;
		try {
			FileInputStream fin = new FileInputStream(serFile);
			FileLock lock = fin.getChannel().lock(0, Long.MAX_VALUE, true);
			LuceneIndex index;
			try {
				/*
				 * Without this BufferedInputStream, there can be noticeable
				 * performance problems if the index resides on a network drive.
				 */
				in = new ObjectInputStream(new BufferedInputStream(fin));
				index = (LuceneIndex) in.readObject();
			}
			finally {
				lock.release();
			}
			//If index can be loaded, load the index name from file
			index.getRootFolder().setDisplayName(loadIndexName(index.getIndexDirPath()));
			addIndex(index, serFile.lastModified());
			return true;
		}
		catch (Exception e) {
			return false;
		}
		finally {
			Closeables.closeQuietly(in);
		}
	}

	private void reload() {
		writeLock.lock();
		try {
			Map<File, LuceneIndex> indexDirMap = Maps.newHashMap();
			for (LuceneIndex index : indexes.keySet())
				indexDirMap.put(index.getIndexDirPath().getCanonicalFile(), index);

			/*
			 * The code below is pretty inefficient if many indexes are added,
			 * modified and/or removed. However, we can assume that these operations
			 * are usually performed one index at a time, so the inefficiency
			 * doesn't really matter.
			 */

			for (File indexDir : Util.listFiles(indexParentDir)) {
				if (!indexDir.isDirectory())
					continue;
				File serFile = new File(indexDir, SER_FILENAME);
				if (!serFile.isFile())
					continue;

				LuceneIndex index = indexDirMap.remove(Util.getAbsFile(indexDir));

				// New index found
				if (index == null) {
					loadIndex(serFile);
				}
				// Existing index; may have been modified
				else {
					Long oldLM = indexes.get(index);
					long newLM = serFile.lastModified();
					if (oldLM != null && oldLM.longValue() != newLM) {
						/*
						 * Remove the old version of the index and add the new
						 * version. Let's just hope it isn't in the queue or being
						 * searched in right now.
						 */
						removeIndexes(Collections.singletonList(index), false);
						loadIndex(serFile);
					}
				}
			}

			// Handle missing indexes
			removeIndexes(indexDirMap.values(), false);
		}
		finally {
			writeLock.unlock();
		}
	}

	@VisibleForPackageGroup
	public void save(@NotNull LuceneIndex index) {
		Util.checkNotNull(index);
		writeLock.lock();
		try {
			File indexDir = index.getIndexDirPath().getCanonicalFile();
			indexDir.mkdirs();
			File serFile = new File(indexDir, SER_FILENAME);

			/*
			 * DocFetcher might have been burned onto a CD-ROM; if so, then just
			 * ignore it.
			 */
			if (serFile.exists() && !serFile.canWrite())
				return;

			ObjectOutputStream out = null;
			try {
				serFile.createNewFile();
				FileOutputStream fout = new FileOutputStream(serFile);
				FileLock lock = fout.getChannel().lock();
				try {
					/*
					 * Without this BufferedOutputStream, there can be noticeable
					 * performance problems if the index resides on a network drive.
					 */
					out = new ObjectOutputStream(new BufferedOutputStream(fout));
					out.writeObject(index);
				}
				finally {
					lock.release();
				}
			}
			catch (StackOverflowError e) {
				AppUtil.showError("Couldn't save index '" + index.getDisplayName() + "': Folder hierarchy "
						+ "is too deep! Please reduce the folder depth and rebuild the index.", true, false);
			}
			catch (IOException e) {
				Util.printErr(e); // The average user doesn't need to know
			}
			finally {
				Closeables.closeQuietly(out);
			}
			
			if (ProgramConf.Bool.AllowIndexRenaming.get()) {
				// If saving the index succeeded, save the indexName in a separate file
				if (!saveIndexName(new File(indexDir, NAME_FILENAME), index.getRootFolder().getDisplayName())) {
					AppUtil.showError(Msg.rename_index_failed.get(), true, false);
				}
			}

			// Update cached last-modified value of index
			indexes.put(index, serFile.lastModified());
		}
		finally {
			writeLock.unlock();
		}
	}

	@NotNull
	@ThreadSafe
	public TreeCheckState getTreeCheckState() {
		// Make local copy of indexes for thread-safety
		List<LuceneIndex> localIndexes = getIndexes();

		TreeCheckState totalState = new TreeCheckState();
		for (LuceneIndex index : localIndexes)
			totalState.add(index.getTreeCheckState());
		return totalState;
	}

	private static class IndexComparator implements Comparator<LuceneIndex> {
		private static final IndexComparator instance = new IndexComparator();

		public int compare(LuceneIndex o1, LuceneIndex o2) {
			int cmp = AlphanumComparator.ignoreCaseInstance.compare(
				o1.getDisplayName(), o2.getDisplayName());
			if (cmp != 0)
				return cmp;
			/*
			 * Bug #3458940: If two LuceneIndex instances have the same name, do
			 * not return 0. Otherwise it would be impossible to hold two
			 * LuceneIndex instances with identical name as keys in a TreeMap,
			 * or as values in a TreeSet.
			 */
			return Longs.compare(o1.getCreated(), o2.getCreated());
		}
	}

	private static class SourceCodeAnalyzer extends Analyzer {
		private Version matchVersion;

		public SourceCodeAnalyzer(Version matchVersion) {
			this.matchVersion = matchVersion;
		}

		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
			final Tokenizer source = new SourceCodeTokenizer();
			TokenStream sink = new StandardFilter(source);
			sink = new LowerCaseFilter(sink);
		    sink = new StopFilter(sink, CharArraySet.EMPTY_SET );
		    //sink = new SourceCodeTokenFilter(matchVersion, sink);
			return new TokenStreamComponents(source, sink);
		}
	}
	
	/**
	 * Get the name of the index by reading the index-name.txt file
	 * @param indexPath
	 * @return indexName
	 */
	@Nullable
	private static String loadIndexName(Path indexPath) throws IOException {
		File f = new File(indexPath + "/" + NAME_FILENAME);
		if(f.exists()) {
			return CharsetDetectorHelper.toString(f).split("\\r?\\n")[0];
		}
		
		return null;
	}
	
	private static boolean saveIndexName(File nameFile, String indexName) {
		// Check if the nameFile exists but is write protected
		if (nameFile.exists() && !nameFile.canWrite())
			return false;
		
		try {
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nameFile), "utf-8"));
			w.write(indexName);
			w.close();
		}
		catch (IOException e){
			Util.printErr(e);
			return false;
		}
		
		return true;
	}

}
