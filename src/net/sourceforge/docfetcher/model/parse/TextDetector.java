/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.docfetcher.model.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;

/**
 * Content type detection of plain text documents. This detector looks at the
 * beginning of the document input stream and considers the document to be a
 * text document if no ASCII (ISO-Latin-1, UTF-8, etc.) control bytes are found.
 * As a special case some control bytes (up to 2% of all characters) are also
 * allowed in a text document if it also contains no or just a few (less than
 * 10%) characters above the 7-bit ASCII range.
 * <p>
 * This class is a modified version of
 * {@code org.apache.tika.detect.TextDetector} from Apache Tika 0.10.
 * 
 * @author Tran Nam Quang
 */
final class TextDetector {

	/**
	 * The number of bytes from the beginning of the document stream to test for
	 * control bytes.
	 */
    private static final int NUMBER_OF_BYTES_TO_TEST = 512;

	/**
	 * Lookup table for all the ASCII/ISO-Latin/UTF-8/etc. control bytes in the
	 * range below 0x20 (the space character). If an entry in this table is
	 * <code>true</code> then that byte is very unlikely to occur in a plain
	 * text document.
	 * <p>
	 * The contents of this lookup table are based on the following definition
	 * from section 4 of the "Content-Type Processing Model" Internet-draft (<a
	 * href="http://webblaze.cs.berkeley.edu/2009/mime-sniff/mime-sniff.txt"
	 * >draft-abarth-mime-sniff-01</a>).
	 * 
	 * <pre>
	 * +-------------------------+
	 * | Binary data byte ranges |
	 * +-------------------------+
	 * | 0x00 -- 0x08            |
	 * | 0x0B                    |
	 * | 0x0E -- 0x1A            |
	 * | 0x1C -- 0x1F            |
	 * +-------------------------+
	 * </pre>
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/TIKA-154">TIKA-154</a>
	 */
    private static final boolean[] IS_CONTROL_BYTE = new boolean[0x20];

    static {
        Arrays.fill(IS_CONTROL_BYTE, true);
        IS_CONTROL_BYTE[0x09] = false; // tabulator
        IS_CONTROL_BYTE[0x0A] = false; // new line
        IS_CONTROL_BYTE[0x0C] = false; // new page
        IS_CONTROL_BYTE[0x0D] = false; // carriage return
        IS_CONTROL_BYTE[0x1B] = false; // escape
    }
    
    private TextDetector() {
	}

	/**
	 * Looks at the beginning of the document input stream to determine whether
	 * the document is text or not. The input stream must support mark and
	 * reset.
	 * 
	 * @return true if the input stream suggests a text document, false
	 *         otherwise
	 */
	public static boolean isText(@NotNull InputStream input) throws IOException {
    	Util.checkThat(input.markSupported());
        input.mark(NUMBER_OF_BYTES_TO_TEST);
        try {
            int chars = 0;
            int controls = 0;
            int asciis = 0;
            int ch = input.read();
            while (ch != -1 && chars < NUMBER_OF_BYTES_TO_TEST) {
                if (ch < IS_CONTROL_BYTE.length && IS_CONTROL_BYTE[ch]) {
                    controls++;
                }
                else if (ch < 127) {
                    asciis++;
                }
                ch = input.read();
                chars++;
            }
            if (chars == 0) {
                // Empty document, so treat it as binary
                // See https://issues.apache.org/jira/browse/TIKA-483
                return false;
            }
            else if (controls == 0) {
                // No control characters, so treat it as text
                return true;
            }
            else if (controls < chars * 2 / 100
                    && asciis > chars * 90 / 100) {
                // Almost plain text (< 2% control, > 90% ASCII range)
                // See https://issues.apache.org/jira/browse/TIKA-688
                return true;
            }
            return false;
        }
        finally {
            input.reset();
        }
    }

}
