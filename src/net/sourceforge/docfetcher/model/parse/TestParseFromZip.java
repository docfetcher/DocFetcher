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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import net.sourceforge.docfetcher.TestFiles;
import net.sourceforge.docfetcher.util.Util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.EmptyFileException;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.junit.Test;

import com.google.common.io.Closeables;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TVFS;

/**
 * This unit test checks that all the libraries used for parsing files are
 * capable of handling input streams from zip archive entries.
 * 
 * @author Tran Nam Quang
 */
public final class TestParseFromZip {
	
	@Test
	public void testZippedOffice() throws Exception {
		new ZipAndRun(TestFiles.doc) {
			protected void handleInputStream(InputStream in) throws Exception {
				POIFSReader reader = new POIFSReader();
				reader.registerListener(new POIFSReaderListener() {
					public void processPOIFSReaderEvent(POIFSReaderEvent event) {
						// Nothing
					}
				}, "\005SummaryInformation"); //$NON-NLS-1$
				reader.read(in);
			}
		};
		new ZipAndRun(TestFiles.doc) {
			protected void handleInputStream(InputStream in) throws Exception {
				WordExtractor extractor = null;
				try {
					extractor = new WordExtractor(in);
					extractor.getText();
				} finally {
					Closeables.closeQuietly(extractor);
				}
			}
		};
	}
	
	@Test(expected=EmptyFileException.class)
	public void testZippedOfficeFail() throws Exception {
		// This will fail because we're trying to read the same InputStream twice
		new ZipAndRun(TestFiles.doc) {
			protected void handleInputStream(InputStream in) throws Exception {
				POIFSReader reader = new POIFSReader();
				reader.registerListener(new POIFSReaderListener() {
					public void processPOIFSReaderEvent(POIFSReaderEvent event) {
						// Nothing
					}
				}, "\005SummaryInformation"); //$NON-NLS-1$
				reader.read(in);
				WordExtractor extractor = null;
				try {
					extractor = new WordExtractor(in);
					extractor.getText();
				} finally {
					Closeables.closeQuietly(extractor);
				}
			}
		};
	}
	
	@Test
	public void testZippedOffice2007() throws Exception {
		new ZipAndRun(TestFiles.docx) {
			protected void handleInputStream(InputStream in) throws Exception {
				int length = ExtractorFactory.createExtractor(in).getText().length();
				assertEquals(659, length);
			}
		};
		new ZipAndRun(TestFiles.docx) {
			protected void handleInputStream(InputStream in) throws Exception {
				OPCPackage pkg = OPCPackage.open(in);
				pkg.getPackageProperties();
				Closeables.closeQuietly(pkg);
			}
		};
	}
	
	@Test(expected=IOException.class)
	public void testZippedOffice2007Fail() throws Exception {
		// This will fail because we're trying to read the same InputStream twice
		new ZipAndRun(TestFiles.docx) {
			protected void handleInputStream(InputStream in) throws Exception {
				int length = ExtractorFactory.createExtractor(in).getText().length();
				assertEquals(659, length);
				OPCPackage pkg = OPCPackage.open(in);
				pkg.getPackageProperties();
				Closeables.closeQuietly(pkg);
			}
		};
	}
	
	@Test
	public void testZippedHtml() throws Exception {
		new ZipAndRun(TestFiles.html) {
			protected void handleInputStream(InputStream in) throws Exception {
				Source source = new Source(in);
				source.fullSequentialParse();
				TextExtractor textExtractor = source.getTextExtractor();
				textExtractor.setIncludeAttributes(true);
				assertTrue(textExtractor.toString().contains("HTML file"));
			}
		};
	}
	
	@Test
	public void testZippedPdf() throws Exception {
		new ZipAndRun(TestFiles.multi_page_pdf) {
			protected void handleInputStream(InputStream in) throws Exception {
				PDDocument pdfDoc = PDDocument.load(in);
				PDFTextStripper stripper = new PDFTextStripper();
				StringWriter writer = new StringWriter();
				stripper.setSortByPosition(true);
				stripper.writeText(pdfDoc, writer); // Will handle encryption with empty password
				PDDocumentInformation pdInfo = pdfDoc.getDocumentInformation();
				ParseResult result = new ParseResult(writer.getBuffer())
					.setTitle(pdInfo.getTitle())
					.addAuthor(pdInfo.getAuthor())
					.addMiscMetadata(pdInfo.getSubject())
					.addMiscMetadata(pdInfo.getKeywords());
				String expectedContents = Util.join(Util.LS, "page 1", "page 2", "page 3");
				String actualContents = result.getContent().toString().trim();
				assertEquals(expectedContents, actualContents);
			}
		};
	}
	
	private static abstract class ZipAndRun {
		public ZipAndRun(TestFiles testFile) throws Exception {
			TFile src = new TFile(testFile.getPath());
			File dir = Util.createTempDir();
			TFile archive = new TFile(dir, "archive.zip");
			archive.mkdir();
			TFile dst = new TFile(archive, src.getName());
			src.cp(dst);
			InputStream in = new TFileInputStream(dst);
			try {
				handleInputStream(in);
			}
			finally {
				Closeables.closeQuietly(in);
				/*
				 * On Windows 7, the archive must be unmounted before the
				 * directory can be deleted.
				 */
				TVFS.umount(archive);
				Util.deleteRecursively(dir);
			}
		}
		protected abstract void handleInputStream(InputStream in) throws Exception;
	}

}
