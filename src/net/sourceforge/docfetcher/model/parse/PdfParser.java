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
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.util.annotations.NotNull;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;

import com.google.common.io.Closeables;

/**
 * @author Tran Nam Quang
 */
public final class PdfParser extends StreamParser {
	
	private static final Collection<String> extensions = Collections.singleton("pdf");
	private static final Collection<String> types = MediaType.Col.application("pdf");
	
	PdfParser() {
	}
	
	@Override
	protected ParseResult parse(@NotNull InputStream in,
	                            @NotNull final ParseContext context)
			throws ParseException {
		PDDocument pdfDoc = null;
		try {
			try {
				pdfDoc = PDDocument.load(in);
			}
			catch (InvalidPasswordException e) {
				throw new ParseException(Msg.doc_pw_protected.get());
			}
			catch (RuntimeException e) {
				/*
				 * Bug #1459 with PDFBox 2.0.9: PDFBox throws a
				 * ClassCastException on some files.
				 */
				throw new ParseException(e);
			}
			
			final int pageCount;
			try {
				pageCount = pdfDoc.getNumberOfPages();
			}
			catch (RuntimeException e) {
				/*
				 * Bug #1443 with PDFBox 2.0.9: PDFBox throws an
				 * IllegalArgumentException with a "root cannot be null" error
				 * message on malformed PDF files.
				 */
				throw new ParseException(e);
			}
			StringWriter writer = new StringWriter();
			final StringBuilder annotations = new StringBuilder();
			
			/*
			 * If the PDF file is encrypted, the PDF stripper will automatically
			 * try an empty password.
			 */
			PDFTextStripper stripper = new PDFTextStripper() {
				protected void startPage(PDPage page) throws IOException {
					context.getReporter().subInfo(getCurrentPageNo(), pageCount);
				}
				protected void endPage(PDPage page) throws IOException {
					if (context.getCancelable().isCanceled()) {
						setEndPage(0);
						return;
					}
					try {
						for (PDAnnotation a : page.getAnnotations()) {
							if (a instanceof PDAnnotationMarkup) {
								PDAnnotationMarkup annot = (PDAnnotationMarkup) a;
								String title = annot.getTitlePopup();
								String subject = annot.getSubject();
								String contents = annot.getContents();
								if (title != null) {
									annotations.append(title + " ");
								}
								if (subject != null) {
									annotations.append(subject + " ");
								}
								if (contents != null) {
									annotations.append(contents + " ");
								}
							}
						}
					} catch (IOException e) {
						if (e.getMessage().startsWith("Error: Unknown annotation type")) {
							// Ignore unsupported annotations
							System.err.println(e.getMessage());
						} else {
							throw e;
						}
					}
				}
			};
			
			try {
				stripper.writeText(pdfDoc, writer);
			}
			catch (RuntimeException e) {
				/*
				 * PDFTextStripper.writeText can throw various
				 * RuntimeExceptions, see bugs #3446010, #3448272, #3444887.
				 */
				throw new ParseException(e);
			}
			catch (ExceptionInInitializerError e) {
				/*
				 * Thrown since PDFBox 2.0.9, see bug #1477.
				 */
				throw new ParseException(e);
			}
			
			writer.write(" ");
			writer.write(annotations.toString());
			
			ParseResult result = new ParseResult(writer.getBuffer());
			extractMetadata(pdfDoc, result);
			return result;
		}
		catch (IOException e) {
			throw new ParseException(e);
		}
		finally {
			Closeables.closeQuietly(pdfDoc);
		}
	}
	
	protected Collection<String> getExtensions() {
		return extensions;
	}
	
	protected Collection<String> getTypes() {
		return types;
	}
	
	public String getTypeLabel() {
		return Msg.filetype_pdf.get();
	}
	
	private static void extractMetadata(PDDocument pdfDoc, ParseResult result) {
		PDDocumentInformation information = pdfDoc.getDocumentInformation();
		if (information != null) {
			result.setTitle(information.getTitle());
			result.addAuthor(information.getAuthor());
			result.addMiscMetadata(information.getSubject());
			result.addMiscMetadata(information.getKeywords());
		}
		
		PDDocumentCatalog catalog = pdfDoc.getDocumentCatalog();
		PDMetadata meta = catalog.getMetadata();
		if (meta != null) {
			final DomXmpParser xmpParser;
			try {
				xmpParser = new DomXmpParser();
				XMPMetadata metadata = xmpParser.parse(meta.createInputStream());
				
				DublinCoreSchema dc = metadata.getDublinCoreSchema();
				if (dc != null) {
					result.addMiscMetadata(dc.getDescription());
					List<String> subjects = dc.getSubjects();
					if (subjects != null) {
						for (String subject : dc.getSubjects())
							result.addMiscMetadata(subject);
					}
				}
				
				AdobePDFSchema pdf = metadata.getAdobePDFSchema();
				if (pdf != null) {
					result.addMiscMetadata(pdf.getKeywords());
				}
			}
			catch (XmpParsingException e) {
				// Ignore
			}
			catch (IOException e) {
				// Ignore
			}
			catch (RuntimeException e) {
				// ClassCastException, see bug #1465 and #1469
			}
		}
	}

}
