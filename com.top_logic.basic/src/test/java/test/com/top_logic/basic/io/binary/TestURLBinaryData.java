/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.io.binary.URLBinaryData;

/**
 * Test case for {@link URLBinaryData}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestURLBinaryData extends TestCase {
	
	public void testEquals() throws IOException {
		File tmp = BasicTestCase.createTestFile("data", ".txt");
		FileOutputStream out = new FileOutputStream(tmp);
		byte[] data = "Hello World!".getBytes("utf-8");
		out.write(data);
		out.close();
		URLBinaryData testedData = new URLBinaryData(new URL("file:" + tmp.getAbsolutePath()));
		InMemoryBinaryData expected = new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM);
		expected.write(data);
		assertEquals(expected, testedData);
		assertEquals(data.length, testedData.getSize());
	}

}
