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

package net.sourceforge.docfetcher.model.index.outlook;

import net.sourceforge.docfetcher.model.Folder;
import net.sourceforge.docfetcher.model.Path;
import net.sourceforge.docfetcher.util.annotations.NotNull;

/**
 * This class is intended to be a generics-free middleman for the Folder class,
 * i.e. this class can be used instead of Folder in order to avoid typing out
 * long and cumbersome generic type signatures.
 * 
 * @author Tran Nam Quang
 */
final class MailFolder extends Folder<MailDocument, MailFolder> {
	
	private static final long serialVersionUID = 1L;
	
	private boolean hasDeepContent = false;
	
	public MailFolder(@NotNull MailFolder parent, @NotNull String name) {
		super(parent, name, null);
	}
	
	public MailFolder(@NotNull Path path) {
		super(path, null);
	}
	
	public void setHasDeepContent(boolean hasDeepContent) {
		this.hasDeepContent = hasDeepContent;
	}
	
	public boolean hasDeepContent() {
		return hasDeepContent;
	}

}
