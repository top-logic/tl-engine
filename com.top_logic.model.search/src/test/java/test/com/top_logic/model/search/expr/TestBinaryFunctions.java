/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import junit.framework.Test;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.model.search.expr.config.operations.BinaryFunctions;

/**
 * Tests for the TL-Script functions accessing the properties of a binary value.
 *
 * @see com.top_logic.model.search.expr.config.operations.BinaryFunctions
 */
@SuppressWarnings("javadoc")
public class TestBinaryFunctions extends AbstractSearchExpressionTest {

	public void testContentType() throws Exception {
		Object result =
			eval("binaryContentType(base64Decode('SGVsbG8=', name: 'greeting.txt', contentType: 'text/plain'))");

		assertEquals("text/plain", result);
	}

	public void testContentTypeChained() throws Exception {
		Object result =
			eval("base64Decode('SGVsbG8=', name: 'logo.png', contentType: 'image/png').binaryContentType()");

		assertEquals("image/png", result);
	}

	public void testSize() throws Exception {
		Object result = eval("binarySize(base64Decode('SGVsbG8=', name: 'greeting.txt', contentType: 'text/plain'))");

		// TL-Script normalizes all numbers to floating point values.
		assertEquals(Double.valueOf(5), result);
	}

	public void testConstructedBinary() throws Exception {
		Object result = eval("binaryContentType(binary(name: 'icon.svg', data: '<svg/>',"
			+ " contentType: 'image/svg+xml'))");

		// The content type of a constructed text value carries the character encoding as parameter.
		assertTrue("Unexpected content type: " + result, ((String) result).startsWith("image/svg+xml"));
	}

	public void testDistinguishesValues() throws Exception {
		Object image = eval("base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png').binaryContentType()");
		Object document =
			eval("base64Decode('SGVsbG8sIFdvcmxkIQ==', name: 'a.pdf', contentType: 'application/pdf')"
				+ ".binaryContentType()");

		assertEquals("image/png", image);
		assertEquals("application/pdf", document);

		Object imageSize = eval("base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png').binarySize()");
		Object documentSize =
			eval("base64Decode('SGVsbG8sIFdvcmxkIQ==', name: 'a.pdf', contentType: 'application/pdf').binarySize()");

		assertEquals(Double.valueOf(5), imageSize);
		assertEquals(Double.valueOf(13), documentSize);
	}

	public void testNoValue() throws Exception {
		assertNull(eval("binaryContentType(null)"));
		assertNull(eval("binarySize(null)"));
		assertNull(eval("binaryImageSize(null)"));
	}

	public void testImageSize() throws Exception {
		BinaryData png = BinaryDataFactory.createBinaryData(png(37, 23), "image/png", "image.png");

		Object result = eval("image -> binaryImageSize($image)", png);

		assertEquals(Map.of(
			BinaryFunctions.IMAGE_WIDTH, Double.valueOf(37),
			BinaryFunctions.IMAGE_HEIGHT, Double.valueOf(23)), result);
	}

	public void testImageSizeOfNonImage() throws Exception {
		assertNull(eval("binaryImageSize(base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png'))"));
	}

	private static byte[] png(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buffer);
		return buffer.toByteArray();
	}

	public static Test suite() {
		return suite(TestBinaryFunctions.class);
	}

}
