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

package net.sourceforge.docfetcher.model.index;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.enums.ProgramConf;
import net.sourceforge.docfetcher.model.Path;
import net.sourceforge.docfetcher.model.UtilModel;
import net.sourceforge.docfetcher.model.index.file.SolidArchiveFactory;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.Immutable;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.fs.FsDriver;
import de.schlichtherle.truezip.fs.FsDriverProvider;
import de.schlichtherle.truezip.fs.FsScheme;
import de.schlichtherle.truezip.fs.archive.zip.PromptingKeyManagerService;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.fs.sl.FsDriverLocator;
import de.schlichtherle.truezip.key.KeyManagerProvider;
import de.schlichtherle.truezip.key.PromptingKeyProvider;
import de.schlichtherle.truezip.key.PromptingKeyProvider.Controller;
import de.schlichtherle.truezip.key.UnknownKeyException;
import de.schlichtherle.truezip.key.pbe.AesPbeParameters;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

/**
 * Should not be subclassed outside the package group of this class.
 * 
 * @author Tran Nam Quang
 */
public class IndexingConfig implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final List<String> defaultZipExtensions = Arrays.asList("zip", "jar");
	private static final List<String> defaultTextExtensions = Arrays.asList("txt", "java", "cpp", "py");
	private static final List<PatternAction> defaultPatternActions = Arrays.asList(
		new PatternAction(".*\\.class"),
		new PatternAction(".*\\.pyc"));
	
	/**
	 * Don't forget the {@link #skipTarArchives} field when accessing this list!
	 */
	public static final List<String> tarExtensions = Arrays.asList(
		"tar", "tar.gz", "tgz", "tar.bz2", "tb2", "tbz");
	
	private static final Pattern dotSlashPattern = Pattern.compile("\\.\\.?[/\\\\].*");
	
	@Nullable private File tempDir;
	
	@NotNull private List<String> zipExtensions = defaultZipExtensions;
	@NotNull private List<String> textExtensions = defaultTextExtensions;
	@NotNull private List<PatternAction> patternActions = defaultPatternActions;
	
	private boolean htmlPairing = true;
	private boolean detectExecutableArchives = false;
	private boolean indexFilenames = true;
	private boolean storeRelativePaths = false;
	private boolean watchFolders = true;
	private boolean skipTarArchives = false;
	
	public final boolean isDetectExecutableArchives() {
		return detectExecutableArchives;
	}

	public final void setDetectExecutableArchives(boolean detectExecutableArchives) {
		this.detectExecutableArchives = detectExecutableArchives;
	}
	
	public final boolean isIndexFilenames() {
		return indexFilenames;
	}

	public final void setIndexFilenames(boolean indexFilenames) {
		this.indexFilenames = indexFilenames;
	}

	@NotNull
	public final File getTempDir() {
		if (tempDir != null && tempDir.isDirectory() && tempDir.canWrite())
			return tempDir;
		return Util.TEMP_DIR;
	}

	/**
	 * Sets the temporary directory to be used during indexing. The directory
	 * specified by the given path will only be used if it represents an
	 * existing, writable directory.
	 */
	public final void setTempDir(@Nullable File tempDir) {
		this.tempDir = tempDir;
	}

	public final boolean isStoreRelativePaths() {
		return storeRelativePaths;
	}

	public final void setStoreRelativePaths(boolean storeRelativePaths) {
		if (this.storeRelativePaths == storeRelativePaths)
			return;
		this.storeRelativePaths = storeRelativePaths;
		onStoreRelativePathsChanged();
	}
	
	protected void onStoreRelativePathsChanged() {}

	/**
	 * For the given file, a path is returned that can be stored without
	 * breaking program portability. More specifically, this method returns
	 * either an absolute path or a path relative to the current directory,
	 * depending on the value of {@link #isStoreRelativePaths()}.
	 * <p>
	 * On Windows, there is one exception: If the file and the current directory
	 * reside on different drives (e.g. "C:\" and "D:\"), this method returns an
	 * absolute path.
	 * <p>
	 * The separators are always forward slashes (i.e., "/"). This does not
	 * affect portability since forward slashes are valid path separators on
	 * both Windows and Linux.
	 */
	@NotNull
	public final Path getStorablePath(@NotNull File file) {
		return getStorablePath(file, storeRelativePaths);
	}
	
	@NotNull
	public static Path getStorablePath(	@NotNull File file,
										boolean storeRelativePaths) {
		// Path should not start with any of these:
		// ./   ../   .\   ..\
		Util.checkNotNull(file);
		Util.checkThat(!dotSlashPattern.matcher(file.getPath()).matches());
		
		if (storeRelativePaths)
			return new Path(UtilModel.getRelativePathIfPossible(file));
		else
			return new Path(Util.getAbsPath(file));
	}
	
	@NotNull
	public final File createDerivedTempFile(@NotNull String filename)
			throws IndexingException {
		try {
			return Util.createDerivedTempFile(filename, getTempDir());
		}
		catch (IOException e) {
			throw new IndexingException(e);
		}
	}
	
	// Fail if not enough disk space for extraction
	public final void checkDiskSpaceInTempDir(long requiredSpace)
			throws DiskSpaceException {
		File customTempDir = getTempDir();
		long usableSpace = customTempDir.getUsableSpace();
		if (requiredSpace > usableSpace) {
			String msg = Msg.not_enough_diskspace.format(
					Util.getSystemAbsPath(customTempDir),
					toMegabyteString(usableSpace),
					toMegabyteString(requiredSpace)
			);
			throw new DiskSpaceException(msg);
		}
	}

	private static String toMegabyteString(Long bytes) {
		double megabytes = (double) bytes / (1024 * 1024);
		return String.format("%.1f", megabytes);
	}
	
	@NotNull
	public final Collection<String> getHtmlExtensions() {
		return ProgramConf.StrList.HtmlExtensions.get();
	}
	
	public final boolean isHtmlPairing() {
		return htmlPairing;
	}

	public final void setHtmlPairing(boolean htmlPairing) {
		this.htmlPairing = htmlPairing;
	}
	
	@Immutable
	@NotNull
	public final List<String> getTextExtensions() {
		return textExtensions;
	}
	
	public final void setTextExtensions(@NotNull Collection<String> textExtensions) {
		this.textExtensions = immutableUniqueLowerCase(textExtensions);
	}
	
	// Returned collection does not contain 'exe'
	@Immutable
	@NotNull
	public final List<String> getZipExtensions() {
		return zipExtensions;
	}

	public final void setZipExtensions(@NotNull Collection<String> zipExtensions) {
		this.zipExtensions = immutableUniqueLowerCase(zipExtensions);
	}
	
	@NotNull
	private List<String> immutableUniqueLowerCase(@NotNull Collection<String> strings) {
		Util.checkNotNull(strings);
		Set<String> set = Sets.newLinkedHashSet();
		for (String string : strings)
			set.add(string.toLowerCase());
		return ImmutableList.copyOf(set);
	}

	@Immutable
	@NotNull
	public final List<PatternAction> getPatternActions() {
		return patternActions;
	}

	public final void setPatternActions(@NotNull List<PatternAction> patternActions) {
		this.patternActions = Collections.unmodifiableList(patternActions);
	}

	// Returned detector takes 'detect executable archives' setting into account
	@NotNull
	public final TArchiveDetector createZipDetector() {
		/*
		 * Create an extended copy of the default driver map where all
		 * user-defined extensions not known to TrueZIP are associated with the
		 * zip driver.
		 * 
		 * The existing zip driver is replaced by a custom zip driver which does
		 * not show a Swing password prompt when an encrypted zip file is
		 * encountered.
		 */
		
		Map<FsScheme, FsDriver> oldDriverMap = FsDriverLocator.SINGLETON.get();
		FsDriver oldZipDriver = oldDriverMap.get(FsScheme.create("zip"));
		final Map<FsScheme, FsDriver> driverMap = Maps.newHashMap();
		FsDriver zipDriver = new CustomZipDriver();
		
		for (Map.Entry<FsScheme, FsDriver> entry : oldDriverMap.entrySet()) {
			if (entry.getValue() == oldZipDriver) {
				driverMap.put(entry.getKey(), zipDriver);
			} else {
				driverMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		for (String ext : zipExtensions) {
			FsScheme scheme = FsScheme.create(ext);
			if (!driverMap.containsKey(scheme))
				driverMap.put(scheme, zipDriver);
		}
		
		FsDriverProvider driverProvider = new FsDriverProvider() {
			public Map<FsScheme, FsDriver> get() {
				return Collections.unmodifiableMap(driverMap);
			}
		};
		
		Set<String> extensions = new LinkedHashSet<String>();
		extensions.addAll(zipExtensions);
		if (!skipTarArchives)
			extensions.addAll(tarExtensions);
		if (detectExecutableArchives)
			extensions.add("exe");
		return new TArchiveDetector(driverProvider, Util.join("|", extensions));
	}
	
	private static final class CustomZipDriver extends ZipDriver {
        final KeyManagerProvider provider;
        
        public CustomZipDriver() {
            super(IOPoolLocator.SINGLETON);
            this.provider = new PromptingKeyManagerService(new CustomView());
        }

        @Override
        protected KeyManagerProvider getKeyManagerProvider() {
            return provider;
        }
    }
    
    private static final class CustomView
    implements PromptingKeyProvider.View<AesPbeParameters> {
        @Override
        public void promptWriteKey(Controller<AesPbeParameters> controller)
        throws UnknownKeyException {
			throw new UnknownKeyException(new UnsupportedOperationException(
				"Zip encryption is not supported."));
        }
        
        @Override
        public void promptReadKey(  Controller<AesPbeParameters> controller,
                                    boolean invalid)
        throws UnknownKeyException {
			throw new UnknownKeyException(new UnsupportedOperationException(
				"Zip archive is password protected."));
        }
    }

	// Accepts filenames and filepaths
	// Takes 'detect executable archives' setting into account
	public final boolean isArchive(@NotNull String filename) {
		String ext = Util.getExtension(filename);
		if (detectExecutableArchives && ext.equals("exe"))
			return true;
		if (ext.equals("7z") || ext.equals("rar"))
			return true;
		if (!skipTarArchives && tarExtensions.contains(ext))
			return true;
		return zipExtensions.contains(ext);
	}
	
	// Accepts filenames and filepaths
	// Takes 'detect executable archives' setting into account
	@Nullable
	public final SolidArchiveFactory getSolidArchiveFactory(@NotNull String filename) {
		/*
		 * JUnRar does not seem to support SFX RAR archives, but TrueZIP and
		 * J7Zip do support SFX Zip and SFX 7z archives, respectively.
		 */
		String ext = Util.getExtension(filename);
		if (detectExecutableArchives && ext.equals("exe"))
			return SolidArchiveFactory.SevenZip;
		if (ext.equals("7z"))
			return SolidArchiveFactory.SevenZip;
		if (ext.equals("rar"))
			return SolidArchiveFactory.Rar;
		return null;
	}
	
	// Accepts filenames and filepaths
	public final boolean isSolidArchive(@NotNull String filename) {
		return getSolidArchiveFactory(filename) != null;
	}
	
	public final boolean isWatchFolders() {
		return watchFolders;
	}
	
	public final void setWatchFolders(boolean watchFolders) {
		if (this.watchFolders == watchFolders)
			return;
		this.watchFolders = watchFolders;
		onWatchFoldersChanged();
	}
	
	protected void onWatchFoldersChanged() {}
	
	public boolean isSkipTarArchives() {
		return skipTarArchives;
	}
	
	public void setSkipTarArchives(boolean skipTarArchives) {
		this.skipTarArchives = skipTarArchives;
	}

}
