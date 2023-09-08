/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.stacktrace.internal;

import junit.framework.TestCase;

import com.top_logic.tool.stacktrace.internal.IntRanges;
import com.top_logic.tool.stacktrace.internal.LineNumberEncoding;

/**
 * Test case for {@link LineNumberEncoding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLineNumberConversion extends TestCase {

	public void testEncodeDecode() {
		IntRanges excluded = new IntRanges();
		excluded.addRange(3, 7);
		excluded.addRange(8, 9);
		LineNumberEncoding conversion = new LineNumberEncoding(10, excluded);
		
		conversion.initEncoder();
		conversion.initDecoder();
		
		for (int line = 1; line <= 10; line++) {
			assertEquals(line, conversion.decode(conversion.encode(line)));
		}
	}
}
