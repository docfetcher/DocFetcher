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

package net.sourceforge.docfetcher.model.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.Nullable;

/**
 * @author Tran Nam Quang
 */
public final class HtmlParser extends StreamParser {
	
	private static final Collection<String> types = Arrays.asList(
		MediaType.text("html"),
		MediaType.application("xhtml+xml"),
		MediaType.application("vnd.wap.xhtml+xml"),
		MediaType.application("x-asp")
	);
	
	HtmlParser() {
	}
	
	@Override
	public ParseResult parse(	InputStream in,
	                         	ParseContext context) throws ParseException {
		Source source;
		try {
			source = UtilParser.getSource(in);
		}
		catch (IOException e) {
			throw new ParseException(e);
		}
		
		source.setLogger(null);
		source.fullSequentialParse();
		
		// Get tags
		Element titleElement = source.getNextElement(0, HTMLElementName.TITLE);
		String title = titleElement == null ?
				"" : CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
		String author = getMetaValue(source, "author");
		String description = getMetaValue(source, "description");
		String keywords = getMetaValue(source, "keywords");
		
		// Get contents
		Element bodyElement = source.getNextElement(0, HTMLElementName.BODY);
		String contents;
		if (bodyElement != null) {
			contents = bodyElement.getContent().getTextExtractor().toString();
		}
		else {
			/*
			 * Certain HTML-like files don't have a body, but contain
			 * extractable text, e.g. Chrome bookmark files. Such files can
			 * indexed by rendering them and processing the resulting text.
			 */
			contents = source.getRenderer().setIncludeHyperlinkURLs(false)
					.toString();
		}
		
		return new ParseResult(contents)
			.setTitle(title)
			.addAuthor(author)
			.addMiscMetadata(description)
			.addMiscMetadata(keywords);
	}
	
	/**
	 * Returns the value of the meta tag with the given name in the specified
	 * HTML source. Returns null if the meta tag does not exist.
	 */
	@Nullable
	private String getMetaValue(@NotNull Source source, @NotNull String key) {
		int pos = 0;
		while (pos < source.length()) {
			StartTag startTag = source.getNextStartTag(pos, "name", key, false); //$NON-NLS-1$
			if (startTag == null) return null;
			if (startTag.getName() == HTMLElementName.META)
				return startTag.getAttributeValue("content"); //$NON-NLS-1$
			pos = startTag.getEnd();
		}
		return null;
	}
	
	@Override
	protected String renderText(InputStream in, String filename)
			throws ParseException {
		try {
			Source source = UtilParser.getSource(in);
			source.setLogger(null);
			return source.getRenderer().setIncludeHyperlinkURLs(false).toString();
		}
		catch (IOException e) {
			throw new ParseException(e);
		}
	}
	
	protected Collection<String> getExtensions() {
		throw new UnsupportedOperationException();
	}
	
	protected Collection<String> getTypes() {
		return types;
	}
	
	public String getTypeLabel() {
		return Msg.filetype_html.get();
	}

}
