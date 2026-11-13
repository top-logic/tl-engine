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
import java.util.Random;

import javax.imageio.ImageIO;

import junit.framework.Test;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.basic.ThemeImage;
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

	public void testScaleImage() throws Exception {
		BinaryData png = BinaryDataFactory.createBinaryData(png(400, 200), "image/png", "picture.png");

		BinaryData result = (BinaryData) eval("image -> $image.binaryScaleImage(32, 32)", png);

		assertEquals("image/png", result.getContentType());
		assertEquals("picture.png", result.getName());
		assertSize(32, 16, result);
	}

	public void testScaleImageNoEnlargement() throws Exception {
		BinaryData png = BinaryDataFactory.createBinaryData(png(20, 10), "image/png", "small.png");

		BinaryData result = (BinaryData) eval("image -> binaryScaleImage($image, 32, 32)", png);

		assertSize(20, 10, result);
	}

	public void testScaleImageKeepsTransparency() throws Exception {
		BinaryData png = BinaryDataFactory.createBinaryData(png(400, 400), "image/png", "transparent.png");

		BinaryData result = (BinaryData) eval("image -> binaryScaleImage($image, 32, 32)", png);

		BufferedImage image = ImageIO.read(result.getStream());
		assertEquals(32, image.getWidth());
		assertTrue(image.getColorModel().hasAlpha());
		assertEquals("Transparent pixel must stay transparent.", 0, image.getRGB(31, 16) >>> 24);
		assertEquals("Opaque pixel must stay opaque.", 0xFF, image.getRGB(0, 16) >>> 24);
	}

	public void testScaleImageJpegStaysJpeg() throws Exception {
		BinaryData jpeg = BinaryDataFactory.createBinaryData(encode(photo(400, 300), "jpg"), "image/jpeg", "photo.jpeg");

		BinaryData result = (BinaryData) eval("image -> binaryScaleImage($image, 32, 32)", jpeg);

		assertEquals("image/jpeg", result.getContentType());
		assertEquals("photo.jpg", result.getName());
		assertSize(32, 24, result);
	}

	public void testScaleImageGifBecomesPng() throws Exception {
		BinaryData gif = BinaryDataFactory.createBinaryData(encode(photo(64, 64), "gif"), "image/gif", "anim.gif");

		BinaryData result = (BinaryData) eval("image -> binaryScaleImage($image, 32, 32)", gif);

		assertEquals("image/png", result.getContentType());
		assertEquals("anim.png", result.getName());
		assertSize(32, 32, result);
	}

	public void testScaleImageSvgUnchanged() throws Exception {
		BinaryData svg = BinaryDataFactory.createBinaryData(
			"<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400'/>".getBytes(StandardCharsets.UTF_8),
			"image/svg+xml", "logo.svg");

		assertSame(svg, eval("image -> binaryScaleImage($image, 32, 32)", svg));
	}

	public void testScaleImageUnreadable() throws Exception {
		assertNull(eval("binaryScaleImage(base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png'), 32, 32)"));
		assertNull(eval("binaryScaleImage(null, 32, 32)"));
	}

	public void testScaleImagePhotoToIcon() throws Exception {
		BinaryData jpeg = BinaryDataFactory.createBinaryData(encode(photo(1000, 1000), "png"), "image/png", "photo.png");

		BinaryData result = (BinaryData) eval("image -> binaryScaleImage($image, 32, 32)", jpeg);

		assertSize(32, 32, result);
		assertTrue("Icon too large: " + result.getSize(), result.getSize() < ThemeImage.MAX_DATA_IMAGE_SIZE);
		assertNotNull(ThemeImage.data(result));
	}

	public void testScaleImageInvalidSize() throws Exception {
		BinaryData png = BinaryDataFactory.createBinaryData(png(400, 200), "image/png", "picture.png");
		try {
			Object result = eval("image -> binaryScaleImage($image, 0, 32)", png);
			fail("An invalid size must be reported, got: " + result);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_IMAGE_SIZE__VALUE, ex.getErrorKey().plain());
		}
	}

	private static void assertSize(int width, int height, BinaryData data) throws IOException {
		BufferedImage image = ImageIO.read(data.getStream());
		assertEquals(width, image.getWidth());
		assertEquals(height, image.getHeight());
	}

	/**
	 * An image with random pixels, which compresses badly like a photo.
	 */
	private static BufferedImage photo(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Random random = new Random(42);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.setRGB(x, y, random.nextInt(0x1000000));
			}
		}
		return image;
	}

	private static byte[] encode(BufferedImage image, String format) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(image, format, buffer));
		return buffer.toByteArray();
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
		// Left half opaque, right half transparent.
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width / 2; x++) {
				image.setRGB(x, y, 0xFF0000FF);
			}
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buffer);
		return buffer.toByteArray();
	}

	public static Test suite() {
		return suite(TestBinaryFunctions.class);
	}

}
