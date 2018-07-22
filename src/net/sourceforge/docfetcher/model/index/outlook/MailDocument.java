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

import net.sourceforge.docfetcher.model.Document;
import net.sourceforge.docfetcher.model.DocumentType;
import net.sourceforge.docfetcher.util.annotations.NotNull;

/**
 * @author Tran Nam Quang
 */
final class MailDocument extends Document<MailDocument, MailFolder> {
	
	private static final long serialVersionUID = 1L;
	
	public MailDocument(@NotNull MailFolder parent,
	                    @NotNull String name,
	                    @NotNull String displayName,
						long lastModified) {
		super(parent, name, displayName, lastModified);
	}

	protected DocumentType getType() {
		return DocumentType.OUTLOOK;
	}
	
	public boolean isModified(long newLastModified) {
		return getLastModified() != newLastModified;
	}

}
