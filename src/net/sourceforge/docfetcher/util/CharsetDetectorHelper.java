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

package net.sourceforge.docfetcher.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;

import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.NotThreadSafe;
import net.sourceforge.docfetcher.util.annotations.Nullable;

import org.mozilla.universalchardet.UniversalDetector;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

/**
 * @author Tran Nam Quang
 */
public final class CharsetDetectorHelper {
	
	@NotNull public static String charsetOverride = "";
	
	@Nullable private static UniversalDetector charsetDetector;
	
	private CharsetDetectorHelper() {
	}
	
	@NotNull
	@NotThreadSafe
	public static Properties load(@NotNull File propsFile) throws IOException {
		byte[] bytes = Files.toByteArray(propsFile);
		String contents = toString(bytes);
		Properties props = new Properties();
		props.load(new StringReader(contents));
		return props;
	}
	
	@NotNull
	@NotThreadSafe
	public static String toString(@NotNull InputStream in)
			throws IOException {
		byte[] bytes = ByteStreams.toByteArray(in);
		return toString(bytes);
	}
	
	@NotNull
	@NotThreadSafe
	public static String toString(@NotNull File file)
			throws IOException {
		byte[] bytes = Files.toByteArray(file);
		return toString(bytes);
	}
	
	@NotNull
	@NotThreadSafe
	public static String toString(@NotNull byte[] bytes)
			throws IOException {
		if (!charsetOverride.trim().isEmpty()) {
			try {
				return new String(bytes, charsetOverride.trim());
			} catch (UnsupportedCharsetException e) {
				throw new IOException("Charset not found: " + charsetOverride.trim());
			}
		}
		
		if (charsetDetector == null)
			charsetDetector = new UniversalDetector(null);
		
		byte[] buf = new byte[4096];
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
		
		int nread;
		while ((nread = byteIn.read(buf)) > 0 && !charsetDetector.isDone())
			charsetDetector.handleData(buf, 0, nread);
		charsetDetector.dataEnd();
		String charsetName = charsetDetector.getDetectedCharset();
		charsetDetector.reset();
		
		try {
			return charsetName == null
				? new String(bytes, Charsets.ISO_8859_1)
				: new String(bytes, charsetName);
		}
		catch (UnsupportedEncodingException e) {
			/*
			 * Bug #1410: DocFetcher fails to parse text files with HZ-GB-2312
			 * encoding, since the latter is not supported by the JRE.
			 */
			return new String(bytes, Charsets.ISO_8859_1);
		}
	}

}
