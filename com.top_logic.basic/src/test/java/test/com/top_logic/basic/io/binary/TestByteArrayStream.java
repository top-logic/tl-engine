/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.binary.ByteArrayStream;

/**
 * Tests for {@link ByteArrayStream}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestByteArrayStream extends TestCase {

	public void testInOut() throws IOException {
		try (ByteArrayStream byteArrayStream = new ByteArrayStream()) {
			byte[] origInput = new byte[] { 1, 2, 3, 4, 5, 6 };
			byteArrayStream.write(origInput);

			try (ByteArrayInputStream asInput = byteArrayStream.getStream()) {
				byte[] tmp = new byte[origInput.length];
				int numberReadElements = asInput.read(tmp);
				assertEquals("Not all items read.", numberReadElements, origInput.length);
				assertEquals("Aditional content.", -1, asInput.read());
				BasicTestCase.assertEquals("Stream does not deliver original input.", origInput, tmp);
			}

		}
	}

	public void testOrigOut() throws IOException {
		try (ByteArrayStream byteArrayStream = new ByteArrayStream()) {
			byteArrayStream.write(new byte[] { 1, 2, 3, 4, 5, 6 });
			BasicTestCase.assertEquals(byteArrayStream.getOrginalByteBuffer(),
				byteArrayStream.getOrginalByteBuffer());
		}
	}

}

