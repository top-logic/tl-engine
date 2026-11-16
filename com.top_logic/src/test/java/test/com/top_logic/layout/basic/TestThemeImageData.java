/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataURI;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Test for {@link ThemeImage#data(com.top_logic.basic.io.binary.BinaryDataSource)}.
 */
@SuppressWarnings("javadoc")
public class TestThemeImageData extends BasicTestCase {

	private static final String PNG_URI_PREFIX = BinaryDataURI.PREFIX + "image/png" + BinaryDataURI.BASE64_SEPARATOR;

	public void testEncodedForm() throws IOException {
		byte[] png = png(4, 3);
		ThemeImage image = ThemeImage.data(BinaryDataFactory.createBinaryData(png, "image/png", "picture.png"));
		assertNotNull(image);

		String encoded = image.toEncodedForm();
		assertTrue(encoded, encoded.startsWith(PNG_URI_PREFIX));
		assertEquals(PNG_URI_PREFIX + Base64.getEncoder().encodeToString(png), encoded);
		assertSame(image, image.resolve());
	}

	public void testDecodeRoundTrip() throws IOException {
		ThemeImage image = ThemeImage.data(BinaryDataFactory.createBinaryData(png(2, 2), "image/png"));

		ThemeImage decoded = ThemeImage.internalDecode(image.toEncodedForm());
		assertEquals(image, decoded);
		assertEquals(image.hashCode(), decoded.hashCode());
		assertEquals(image.toEncodedForm(), decoded.toEncodedForm());
	}

	public void testSvg() {
		BinaryData svg = BinaryDataFactory.createBinaryData("<svg xmlns='http://www.w3.org/2000/svg'/>".getBytes(),
			"image/svg+xml");
		ThemeImage image = ThemeImage.data(svg);
		assertNotNull(image);
		assertTrue(image.toEncodedForm().startsWith(BinaryDataURI.PREFIX + "image/svg+xml"));
	}

	public void testNoImage() {
		assertNull(ThemeImage.data(BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 }, "application/pdf")));
		assertNull(ThemeImage.data(null));
	}

	public void testTooLarge() {
		byte[] large = new byte[ThemeImage.MAX_DATA_IMAGE_SIZE + 1];
		assertNull(ThemeImage.data(BinaryDataFactory.createBinaryData(large, "image/png")));

		byte[] limit = new byte[ThemeImage.MAX_DATA_IMAGE_SIZE];
		assertNotNull(ThemeImage.data(BinaryDataFactory.createBinaryData(limit, "image/png")));
	}

	public void testOtherFormsUnchanged() {
		ThemeImage css = ThemeImage.internalDecode("css:fas fa-car-side");
		assertEquals("css:fas fa-car-side", css.toEncodedForm());
		assertFalse(css.equals(ThemeImage.internalDecode(PNG_URI_PREFIX + "AAAA")));

		assertEquals("/icons/some.png", ThemeImage.internalDecode("/icons/some.png").toEncodedForm());
	}

	public void testWrite() throws IOException {
		ThemeImage image = ThemeImage.data(BinaryDataFactory.createBinaryData(png(2, 2), "image/png"));

		StringWriter buffer = new StringWriter();
		TagWriter out = new TagWriter(buffer);
		image.writeWithCss(DummyDisplayContext.newInstance(), out, "tl-icon");
		out.flushBuffer();
		String html = buffer.toString();

		assertTrue(html, html.startsWith("<img"));
		assertTrue(html, html.contains("src=\"" + image.toEncodedForm() + "\""));
		assertTrue(html, html.contains("class=\"tl-icon\""));
	}

	private static byte[] png(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xFFFF0000);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buffer);
		return buffer.toByteArray();
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestThemeImageData.class));
	}

}
