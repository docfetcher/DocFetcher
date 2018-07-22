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

package net.sourceforge.docfetcher.model.index.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.docfetcher.TestFiles;
import net.sourceforge.docfetcher.model.TreeIndex.IndexingResult;
import net.sourceforge.docfetcher.model.UtilModel;
import net.sourceforge.docfetcher.model.index.IndexingError;
import net.sourceforge.docfetcher.model.index.IndexingInfo;
import net.sourceforge.docfetcher.model.index.IndexingInfo.InfoType;
import net.sourceforge.docfetcher.model.index.IndexingReporter;
import net.sourceforge.docfetcher.util.AppUtil;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.collect.ListMap;
import net.sourceforge.docfetcher.util.collect.ListMap.Entry;

import org.apache.lucene.store.Directory;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.NullOutputStream;

/**
 * @author Tran Nam Quang
 */
public final class FileIndexTest {
	
	static {
		AppUtil.Const.autoInit();
	}
	
	@Test
	public void testClearIndex() {
		File file = TestFiles.archive_zip_rar_7z.get();
		FileIndex index = new FileIndex(null, file);
		CountingReporter reporter = new CountingReporter();
		index.update(reporter, null);
		assertEquals(1, reporter.extractCount);
		index.clear();
		index.update(reporter, null);
		assertEquals(2, reporter.extractCount);
	}
	
	@Test
	public void testNestedUpdate() throws Exception {
		File rarFile = TestFiles.sfx_rar.get();
		File[] files = {
				TestFiles.archive_zip_rar_7z.get(),
				TestFiles.sfx_zip.get(),
				TestFiles.sfx_7z.get(),
				rarFile,
		};
		
		for (File file : files) {
			FileIndex index = new FileIndex(null, file);
			index.getConfig().setDetectExecutableArchives(true);
			index.update(null, null);
			Directory luceneDir = index.getLuceneDir();

			/*
			 * JUnRar doesn't support SFX rar archives, so we're expecting zero
			 * results for those.
			 */
			int expectedResultCount = file == rarFile ? 0 : 1;
			
			UtilModel.assertDocCount(luceneDir, expectedResultCount);
			UtilModel.assertResultCount(luceneDir, "test", expectedResultCount);
		}
	}
	
	/**
	 * Checks that the index update works correctly for HTML pairs.
	 */
	@Test
	public void testHtmlPairUpdate() throws Exception {
		File tempDir = Util.createTempDir();
		File htmlFile = new File(tempDir, "test.html");
		Files.copy(TestFiles.html.get(), htmlFile);
		File htmlDir = new File(tempDir, "test_files");
		File subFile1 = new File(htmlDir, "filename.unsupportedformat");
		File subFile2 = new File(htmlDir, "filename.txt");
		File subFile3 = new File(htmlDir, "simple.7z");
		htmlDir.mkdirs();
		subFile1.createNewFile();
		subFile2.createNewFile();
		Files.copy(TestFiles.simple_7z.get(), subFile3);
		
		FileIndex index = new FileIndex(null, tempDir);
		index.getConfig().setIndexFilenames(false);
		final CountingReporter reporter = new CountingReporter();
		
		// Index update should not detect any changes when nothing was modified
		index.update(reporter, null);
		assertEquals(1, reporter.extractCount);
		index.update(reporter, null);
		assertEquals(1, reporter.extractCount);
		
		// Index update must detect changes when files have been modified
		ListMap<File, Integer> fileMap = ListMap.<File, Integer> create()
			.add(htmlFile, 1)
			.add(subFile1, 0)
			.add(subFile2, 1)
			.add(subFile3, 1);
		int i = 0;
		for (ListMap.Entry<File, Integer> entry : fileMap) {
			reporter.extractCount = 0;
			
			File file = entry.getKey();
			int expectedCount = entry.getValue().intValue();
			file.setLastModified(System.currentTimeMillis() + (i + 1) * 1000);
			
			index.update(reporter, null);
			
			String msg = String.format("On '%s'.", file.getName());
			assertEquals(msg, expectedCount, reporter.extractCount);
			i++;
		}
		
		// Index update must detect change when HTML folder is deleted
		Util.deleteRecursively(htmlDir);
		reporter.extractCount = 0;
		index.update(reporter, null);
		assertEquals(1, reporter.extractCount);
		
		Util.deleteRecursively(tempDir);
	}
	
	@Test
	public void testHtmlPairUpdateInSevenZip() throws Exception {
		File testDir = TestFiles.index_update_html_in_7z.get();
		List<File> files = Arrays.asList(Util.listFiles(testDir));
		Collections.sort(files);
		
		File originalFile = files.get(0);
		File tempDir = Util.createTempDir();
		File target = new File(tempDir, "target.7z");
		
		int[] expectedCounts = { 1, 1, 1, 1, 1, 0 };
		assertEquals(expectedCounts.length, files.size() - 1);
		
		for (boolean reversed : new boolean[] {false, true}) {
			int i = 0;
			for (File modifiedFile : files.subList(1, files.size())) {
				FileIndex index = new FileIndex(null, tempDir);
				index.getConfig().setIndexFilenames(false);
				
				File file1 = reversed ? modifiedFile : originalFile;
				File file2 = reversed ? originalFile : modifiedFile;
				
				Files.copy(file1, target);
				target.setLastModified(System.currentTimeMillis() - 1000);
				index.update(null, null);
				
				Files.copy(file2, target);
				CountingReporter reporter2 = new CountingReporter();
				
				index.update(reporter2, null);
				assertEquals(modifiedFile.getName(), expectedCounts[i], reporter2.extractCount);
				
				Util.deleteContents(tempDir);
				i++;
			}
		}
		
		Util.deleteRecursively(tempDir);
	}
	
	private static class CountingReporter extends IndexingReporter {
		private int extractCount = 0;
		private int errorCount = 0;
		
		public void info(IndexingInfo info) {
			if (info.is(InfoType.EXTRACTING))
				extractCount++;
		}
		
		public void fail(IndexingError error) {
			errorCount++;
		}
	}
	
	/**
	 * Checks that the index update works correctly after a folder is renamed.
	 */
	@Test
	public void testIndexUpdateAfterFolderRenaming() throws Exception {
		File tempDir = Util.createTempDir();
		
		// Set up subfolder
		File subDir1 = new File(tempDir, "Test1");
		subDir1.mkdir();
		File textFile = new File(subDir1, "test.txt");
		Files.write("Hello World", textFile, Charsets.UTF_8);
		
		// Create index
		FileIndex index = new FileIndex(null, tempDir);
		index.update(null, null);
		UtilModel.assertDocCount(index.getLuceneDir(), 1);
		
		// Rename subfolder, then update index
		File subDir2 = new File(tempDir, "Test2");
		subDir1.renameTo(subDir2);
		index.update(null, null);
		UtilModel.assertDocCount(index.getLuceneDir(), 1);
		
		Util.deleteRecursively(tempDir);
	}
	
	/**
	 * Checks that the index update works correctly after a file is renamed.
	 */
	@Test
	public void testIndexUpdateAfterFileRenaming() throws Exception {
		File tempDir = Util.createTempDir();
		
		File textFile = new File(tempDir, "test.txt");
		Files.write("Hello World", textFile, Charsets.UTF_8);
		
		FileIndex index = new FileIndex(null, tempDir);
		index.update(null, null);
		UtilModel.assertDocCount(index.getLuceneDir(), 1);
		
		textFile.renameTo(new File(tempDir, "test2.txt"));
		
		index.update(null, null);
		UtilModel.assertDocCount(index.getLuceneDir(), 1);
		
		Util.deleteRecursively(tempDir);
	}
	
	/**
	 * Checks that the index update works correctly after an archive entry
	 * (either a file or a folder) inside a 7z archive is renamed.
	 */
	@Test
	public void testIndexUpdateAfterRenamingIn7z() throws Exception {
		File tempDir = Util.createTempDir();
		
		ListMap<String, String> files = ListMap.create();
		files.add("file1.7z", "file2.7z");
		files.add("folder1.7z", "folder2.7z");
		
		for (Entry<String, String> entry : files) {
			TestFiles parent = TestFiles.index_update_rename_in_7z;
			File oldFile = parent.getChild(entry.getKey());
			File newFile = parent.getChild(entry.getValue());
			
			File target = new File(tempDir, "target.7z");
			Files.copy(oldFile, target);
			
			FileIndex index = new FileIndex(null, tempDir);
			index.update(null, null);
			UtilModel.assertDocCount(index.getLuceneDir(), 1);
			
			Files.copy(newFile, target);
			index.update(null, null);
			UtilModel.assertDocCount(index.getLuceneDir(), 1);
		}
		
		Util.deleteRecursively(tempDir);
	}
	
	/**
	 * Checks that the indexing algorithm can properly deal with HTML files
	 * nested inside the HTML folders of other HTML files.
	 */
	@Test
	public void testIndexUpdateOnNestedHtml() throws Exception {
		File dir = TestFiles.index_update_html_in_html.get();
		FileIndex index = new FileIndex(null, dir);
		
		index.update(null, null);
		UtilModel.assertDocCount(index.getLuceneDir(), 2);
		
		CountingReporter countingReporter = new CountingReporter();
		index.update(countingReporter, null);
		assertEquals(0, countingReporter.extractCount);
	}
	
	@Test
	public void testIndexEncryptedZipArchive() {
		File archive = TestFiles.encrypted_zip.get();
		FileIndex index = new FileIndex(null, archive);
		assertEquals(index.update(null, null), IndexingResult.SUCCESS_CHANGED);
	}
	
	@Test
	public void testIndexEncrypted7zArchives() {
		File archive = TestFiles.encrypted_7z_filenames.get();
		FileIndex index = new FileIndex(null, archive);
		index.update(new IndexingReporter() {
			public void fail(IndexingError error) {
				Throwable t = error.getThrowable();
				assertTrue(t instanceof ArchiveEncryptedException);
			}
		}, null);
		
		// J7Zip will print to stdout and stderr
		PrintStream stdOut = System.out;
		PrintStream stdErr = System.err;
		PrintStream nullOut = new PrintStream(new NullOutputStream());
		System.setOut(nullOut);
		System.setErr(nullOut);
		
		archive = TestFiles.encrypted_7z.get();
		index = new FileIndex(null, archive);
		CountingReporter reporter = new CountingReporter();
		index.update(reporter, null);
		assertTrue(reporter.extractCount == 1); // will report file, but fail to unpack it
		assertTrue(reporter.errorCount == 0);
		
		// Must be reset, otherwise the tests will stop printing output
		System.setOut(stdOut);
		System.setErr(stdErr);
	}
	
	// This test should not crash. See bug #3465544.
	@Test
	public void testFakeZipInsideSolid() {
		File archive = TestFiles.fake_zip_file_inside_solid.get();
		FileIndex index = new FileIndex(null, archive);
		index.update(null, null);
	}
	
}
