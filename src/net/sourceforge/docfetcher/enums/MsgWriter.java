/*******************************************************************************
 * Copyright (c) 2018 Nam-Quang Tran.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nam-Quang Tran - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.enums;

import java.io.File;
import java.io.IOException;

import net.sourceforge.docfetcher.util.ConfLoader;
import net.sourceforge.docfetcher.util.Util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * This class is used for writing the English GUI strings from the Msg enum to a
 * a Resource.properties file, for loading into common translation tools.
 */
public class MsgWriter {

	public static void main(String[] args) throws IOException {
		File outFile = new File("build/Resource.properties");
		writeTo(outFile);
		Util.println("File written: " + outFile.getAbsolutePath());
	}
	
	public static void writeTo(File outFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Msg msg : Msg.values()) {
			String key = ConfLoader.convert(msg.name(), true);
			String value = convert(msg.get(), false);
			String comments = msg.getComment();
			
			if (!comments.isEmpty())
				sb.append("# " + comments + Util.LS);
			sb.append(key + "=" + value);
			sb.append(Util.LS);
		}
		Files.write(sb.toString(), outFile, Charsets.UTF_8);
	}
	
	/**
	 * @see ConfLoader#convert(String,boolean)
	 */
	private static String convert(String input, boolean escapeSpace) {
		StringBuilder out = new StringBuilder(input.length() * 2);
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			switch(c) {
			case ' ':
				if (i == 0 || escapeSpace)
					out.append('\\');
				out.append(' ');
				break;
			case '\t': out.append("\\t"); break;
			case '\n': out.append("\\n"); break;
			case '\r': out.append("\\r"); break;
			case '\f': out.append("\\f"); break;
			case '\\': // Fall through
			case '#': out.append('\\'); out.append(c); break;
			default: out.append(c);
			}
		}
		return out.toString();
	}
	
}
