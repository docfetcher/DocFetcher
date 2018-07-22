/*******************************************************************************
 * Copyright (c) 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model.index.outlook;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.docfetcher.model.HotColdFileCache;
import net.sourceforge.docfetcher.model.MailResource;
import net.sourceforge.docfetcher.model.Path;
import net.sourceforge.docfetcher.model.Path.PathParts;
import net.sourceforge.docfetcher.model.index.IndexingConfig;
import net.sourceforge.docfetcher.model.parse.ParseException;
import net.sourceforge.docfetcher.util.CheckedOutOfMemoryError;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;

import org.apache.lucene.search.Query;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTMessage;
import com.pff.PSTObject;

/**
 * @author Tran Nam Quang
 */
public final class OutlookMailFactory {
	
	// TODO test
	
	private final HotColdFileCache unpackCache;
	
	public OutlookMailFactory(@NotNull HotColdFileCache unpackCache) {
		this.unpackCache = Util.checkNotNull(unpackCache);
	}
	
	// thrown parse exception has localized error message
	// Path argument should be path to PST file + path to folder + descriptor node ID
	@NotNull
	public MailResource createMail(	@NotNull IndexingConfig config,
									@NotNull Query query,
									boolean isPhraseQuery,
									@NotNull Path emailPath)
			throws ParseException, FileNotFoundException,
			CheckedOutOfMemoryError {
		PathParts leftMiddle_right = emailPath.splitAtLastSeparator();
		try {
			PathParts left_middle = leftMiddle_right.getLeft().splitAtExistingFile();
			long pstId = Long.valueOf(leftMiddle_right.getRight());
			
			String absLeft = left_middle.getLeft().getCanonicalPath();
			PSTFile pstFile = new PSTFile(absLeft);
			PSTMessage email = (PSTMessage) PSTObject.detectAndLoadPSTObject(
				pstFile, pstId);
			
			Path emailId = new Path(Util.joinPath(
				absLeft, left_middle.getRight(), leftMiddle_right.getRight()));
			
			return new OutlookMailResource(
				config, query, isPhraseQuery, unpackCache, emailId, email);
		}
		catch (FileNotFoundException e) {
			throw e; // should not be caught by IOException catch clause
		}
		catch (PSTException e) {
			throw new ParseException(e); // TODO i18n
		}
		catch (IOException e) {
			throw new ParseException(e); // TODO i18n
		}
	}

}
