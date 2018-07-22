/*******************************************************************************
 * Copyright (c) 2012 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.collect.LazyList;

/**
 * @author Tran Nam Quang
 */
public final class IndexLoadingProblems {
	
	private final List<File> obsoleteFiles = new LazyList<File>();
	private final List<CorruptedIndex> corruptedIndexes = new LazyList<CorruptedIndex>();
	private final List<OverflowIndex> overflowIndexes = new LazyList<OverflowIndex> ();
	
	IndexLoadingProblems() {
	}
	
	void addObsoleteFile(@NotNull File file) {
		obsoleteFiles.add(Util.checkNotNull(file));
	}
	
	void addCorruptedIndex(@NotNull CorruptedIndex index) {
		corruptedIndexes.add(Util.checkNotNull(index));
	}
	
	void addOverflowIndex(@NotNull OverflowIndex index) {
		overflowIndexes.add(Util.checkNotNull(index));
	}
	
	@NotNull
	public List<File> getObsoleteFiles() {
		return Collections.unmodifiableList(obsoleteFiles);
	}
	
	@NotNull
	public List<CorruptedIndex> getCorruptedIndexes() {
		return Collections.unmodifiableList(corruptedIndexes);
	}
	
	@NotNull
	public List<OverflowIndex> getOverflowIndexes() {
		return Collections.unmodifiableList(overflowIndexes);
	}
	
	/**
	 * A corrupted index that couldn't be loaded during initialization.
	 */
	public static final class CorruptedIndex {
		@NotNull public final LuceneIndex index;
		@NotNull public final IOException ioException;
		
		public CorruptedIndex(	@NotNull LuceneIndex index,
								@NotNull IOException ioException) {
			this.index = Util.checkNotNull(index);
			this.ioException = Util.checkNotNull(ioException);
		}
	}
	
	/**
	 * An index that couldn't be loaded during initialization due to a
	 * StackOverflowError.
	 */
	public static final class OverflowIndex {
		@NotNull public final File file;
		@NotNull public final StackOverflowError error;
		
		public OverflowIndex(	@NotNull File file,
								@NotNull StackOverflowError error) {
			this.file = Util.checkNotNull(file);
			this.error = Util.checkNotNull(error);
		}
	}

}
