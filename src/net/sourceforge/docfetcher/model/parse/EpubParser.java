/*******************************************************************************
 * Copyright (c) 2014 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model.parse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.util.annotations.Nullable;

/**
 * @author Tran Nam Quang
 */
public final class EpubParser extends FileParser {
	
	private static final Collection<String> extensions = Arrays.asList("epub");
	
	private static final Collection<String> types = Arrays.asList(
		MediaType.application("epub+zip")
	);

	protected ParseResult parse(File file, ParseContext context)
			throws ParseException {
		return parse(file, context, false);
	}
	
	protected String renderText(File file, String filename)
			throws ParseException {
		ParseContext context = new ParseContext(filename);
		return parse(file, context, true).getContent().toString();
	}
	
	protected ParseResult parse(File file, ParseContext context, boolean render)
			throws ParseException {
		ZipFile zipFile = null;
		try {
			// Get zip entries
			try {
				zipFile = new ZipFile(file);
			}
			catch (RuntimeException e) {
				/*
				 * Bug #1463: Apparently, on Java 10, feeding a TFile into the
				 * ZipFile constructor can cause an
				 * UnsupportedOperationException.
				 */
				throw new ParseException(e);
			}
			Source containerSource = UtilParser.getSource(zipFile, "META-INF/container.xml"); //$NON-NLS-1$
			Element rootfileEl = containerSource.getNextElement(0, "rootfile"); //$NON-NLS-1$
			maybeThrow(rootfileEl, "No rootfile element in META-INF/container.xml");
			String opfPath = rootfileEl.getAttributeValue("full-path");
			Source opfSource = UtilParser.getSource(zipFile, opfPath);
			
			// Get top-level elements in OPF file
			Element packageEl = getFirstOpfElement(opfSource, "package");
			maybeThrow(packageEl, "No package element in OPF file");
			Element metadataEl = getFirstOpfElement(packageEl, "metadata");
			Element manifestEl = getFirstOpfElement(packageEl, "manifest");
			Element spineEl = getFirstOpfElement(packageEl, "spine");
			maybeThrow(metadataEl, "No metadata element in OPF file");
			maybeThrow(manifestEl, "No manifest element in OPF file");
			maybeThrow(spineEl, "No spine element in OPF file");
			
			// Parse metadata
			String title = UtilParser.extract(metadataEl.getFirstElement("dc:title"));
			Element creatorEl = metadataEl.getFirstElement("dc:creator");
			String creator = null;
			if (creatorEl != null) {
				creator = UtilParser.extract(creatorEl);
			}
			
			// Parse manifest
			Map<String, String> itemIdToRef = new HashMap<String, String>();
			for (Element itemEl : manifestEl.getChildElements()) {
				String id = itemEl.getAttributeValue("id");
				String href = itemEl.getAttributeValue("href");
				itemIdToRef.put(id, href);
			}
			
			// Get spine paths
			List<String> spinePaths = new LinkedList<String>();
			for (Element itemRefEl : spineEl.getChildElements()) {
				String subPath = itemIdToRef.get(itemRefEl.getAttributeValue("idref"));
				if (subPath == null) {
					// Broken spine reference; ignore
					continue;
				}
				try {
					subPath = new java.net.URI(subPath).getPath();
				} catch (Throwable t) {
				}
				File opfParent = new File(opfPath).getParentFile();
				if (opfParent != null) {
					spinePaths.add(new File(opfParent, subPath).getPath());
				} else {
					spinePaths.add(subPath);
				}
			}
			
			StringBuilder contents = new StringBuilder();
			boolean first = true;
			
			// Parse description
			Element descriptionEl = metadataEl.getFirstElement("dc:description");
			if (descriptionEl != null) {
				String description = descriptionEl.getContent().getTextExtractor().toString();
				Source source = new Source(description);
				source.setLogger(null);
				contents.append(UtilParser.render(source));
				first = false;
			}
			
			// Parse spine
			final int spineCount = spinePaths.size();
			int i = 1;
			for (String spinePath : spinePaths) {
				final Source spineSource;
				try {
					spineSource = UtilParser.getSource(zipFile, spinePath);
				} catch (ParseException e) {
					// Ignore missing spine files
					continue;
				}
				Element bodyEl = spineSource.getNextElement(0, HTMLElementName.BODY);
				if (bodyEl == null) {
					// See bug #682
					continue;
				}
				context.getReporter().subInfo(i, spineCount);
				if (!first) {
					contents.append("\n\n");
				}
				if (render) {
					contents.append(UtilParser.render(bodyEl));
				} else {
					contents.append(UtilParser.extract(bodyEl));
				}
				first = false;
				i++;
			}
			
			// Create and return parse result
			return new ParseResult(contents)
				.setTitle(title)
				.addAuthor(creator);
		}
		catch (IOException e) {
			throw new ParseException(e);
		}
		finally {
			UtilParser.closeZipFile(zipFile);
		}
	}
	
	private static <T> T maybeThrow(@Nullable T object, String message) throws ParseException {
		if (object == null) {
			throw new ParseException(message);
		}
		return object;
	}
	
	@Nullable
	private static Element getFirstOpfElement(Segment segment, String tagName) {
		Element el = segment.getFirstElement(tagName);
		if (el != null) return el;
		return segment.getFirstElement("opf:" + tagName);
	}
	
	protected Collection<String> getExtensions() {
		return extensions;
	}

	protected Collection<String> getTypes() {
		return types;
	}

	public String getTypeLabel() {
		return Msg.filetype_epub.get();
	}

}
