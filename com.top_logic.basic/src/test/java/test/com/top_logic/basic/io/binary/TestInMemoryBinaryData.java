/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;

/**
 * Tests {@link InMemoryBinaryData}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestInMemoryBinaryData extends TestCase {

	public void testWriteReadString() throws IOException {
		String content = "new content string";
		InMemoryBinaryData memoryData = newInMemoryData();
		try {
			memoryData.write(content.getBytes());
		} finally {
			memoryData.close();
		}
		assertEquals(content, memoryData);
	}

	private void assertEquals(String expected, InMemoryBinaryData actual) throws IOException {
		assertEquals(expected.getBytes().length, actual.getSize());
		InputStream stream = actual.getStream();
		try {
			assertEquals(expected, StreamUtilities.readAllFromStream(stream));
		} finally {
			stream.close();
		}
	}

	public void testWriteReadBinary() throws IOException {
		InMemoryBinaryData memoryData = newInMemoryData();
		try {
			InputStream in = newBinaryStream();
			try {
				StreamUtilities.copyStreamContents(in, memoryData);
			} finally {
				in.close();
			}
		} finally {
			memoryData.close();
		}

		InputStream actual = memoryData.getStream();
		try {
			InputStream expected = newBinaryStream();
			try {
				assertTrue(StreamUtilities.equalsStreamContents(expected, actual));
			} finally {
				expected.close();
			}
		} finally {
			actual.close();
		}
	}

	private InputStream newBinaryStream() {
		return TestInMemoryBinaryData.class
			.getResourceAsStream(TestInMemoryBinaryData.class.getSimpleName() + ".class");
	}

	public void testReuse() throws IOException {
		InMemoryBinaryData memoryData = newInMemoryData();
		try {
			String content1 = "new content string";
			memoryData.write(content1.getBytes());
			assertEquals(content1, memoryData);

			memoryData.reset();

			String content2 = "different content string    ";
			memoryData.write(content2.getBytes());
			assertEquals(content2, memoryData);
		} finally {
			memoryData.close();
		}
	}

	private InMemoryBinaryData newInMemoryData() {
		return new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

}

