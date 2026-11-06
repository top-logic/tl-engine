/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;

import junit.framework.Test;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.model.search.expr.config.operations.BinaryFunctions;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.util.error.TopLogicException;

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
		Object result =
			evalWithoutErrors("binarySize(base64Decode('SGVsbG8=', name: 'greeting.txt', contentType: 'text/plain'))");

		// TL-Script represents all numbers as floating point values.
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

	public void testTextUtf8ByDefault() throws Exception {
		BinaryData data = BinaryDataFactory.createBinaryData("Grüße, Äpfel".getBytes(StandardCharsets.UTF_8),
			"text/plain", "greeting.txt");

		assertEquals("Grüße, Äpfel", eval("data -> binaryText($data)", data));
	}

	public void testTextExplicitCharset() throws Exception {
		BinaryData data = BinaryDataFactory.createBinaryData("Grüße".getBytes(StandardCharsets.ISO_8859_1),
			"text/plain; charset=UTF-8", "greeting.txt");

		// The explicit encoding wins over the one of the content type.
		assertEquals("Grüße", eval("data -> binaryText($data, 'ISO-8859-1')", data));
		assertEquals("Grüße", eval("data -> $data.binaryText(charset: 'ISO-8859-1')", data));
	}

	public void testTextCharsetFromContentType() throws Exception {
		BinaryData data = BinaryDataFactory.createBinaryData("Grüße".getBytes(StandardCharsets.ISO_8859_1),
			"text/plain; " + BinaryFunctions.CHARSET_PARAMETER + "=ISO-8859-1", "greeting.txt");

		assertEquals("Grüße", eval("data -> binaryText($data)", data));
		assertEquals("Grüße", eval("data -> binaryText($data, '')", data));
	}

	public void testTextKeepsEscapeSequences() throws Exception {
		String contents = "a\\0b\\nc\\\\d\"e";
		BinaryData data = BinaryDataFactory.createBinaryData(contents.getBytes(StandardCharsets.UTF_8),
			"text/plain", "escapes.txt");

		Object result = eval("data -> binaryText($data)", data);

		assertEquals(contents, result);
		assertEquals("No escape sequence must be interpreted.", contents.length(), ((String) result).length());
	}

	public void testTextNoValue() throws Exception {
		assertNull(eval("binaryText(null)"));
		assertNull(eval("binaryText(null, 'UTF-8')"));
	}

	public void testTextUnknownCharset() throws Exception {
		BinaryData data =
			BinaryDataFactory.createBinaryData("x".getBytes(StandardCharsets.UTF_8), "text/plain", "x.txt");

		assertUnknownCharset(() -> eval("data -> binaryText($data, 'no-such-charset')", data));
		assertUnknownCharset(() -> eval("data -> binaryText($data, 'illegal charset name')", data));

		BinaryData wrongType = BinaryDataFactory.createBinaryData("x".getBytes(StandardCharsets.UTF_8),
			"text/plain; charset=no-such-charset", "x.txt");
		assertUnknownCharset(() -> eval("data -> binaryText($data)", wrongType));
	}

	private interface Evaluation {
		Object run() throws Exception;
	}

	private static void assertUnknownCharset(Evaluation evaluation) throws Exception {
		try {
			Object result = evaluation.run();
			fail("An unknown character encoding must be reported, got: " + result);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_UNKNOWN_CHARSET__NAME, ex.getErrorKey().plain());
		}
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
