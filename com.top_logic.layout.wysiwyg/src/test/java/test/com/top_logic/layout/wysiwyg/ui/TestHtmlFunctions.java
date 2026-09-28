/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.wysiwyg.ui.HtmlFunctions;
import com.top_logic.layout.wysiwyg.ui.I18NConstants;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for {@link HtmlFunctions#text(String, Object)}.
 */
@SuppressWarnings("javadoc")
public class TestHtmlFunctions extends TestCase {

	private static final String LOGO = "logo.png";

	private static final String OTHER = "other.png";

	public void testWithoutImages() {
		String source = "<p>Hello <b>World</b>&nbsp;<img src=\"logo.png\"></p>";
		StructuredText text = HtmlFunctions.text(source, null);

		assertEquals(source, text.getSourceCode());
		assertTrue(text.getImages().isEmpty());
	}

	public void testEmptyImages() {
		String source = "<p><img src=\"logo.png\"></p>";
		StructuredText text = HtmlFunctions.text(source, List.of());

		assertEquals(source, text.getSourceCode());
		assertTrue(text.getImages().isEmpty());
	}

	public void testNullSource() {
		assertEquals("", HtmlFunctions.text(null, null).getSourceCode());
	}

	public void testList() {
		BinaryData logo = image(LOGO);
		StructuredText text = HtmlFunctions.text("<p>Logo: <img src=\"logo.png\"/></p>", List.of(logo));

		assertEquals("<p>Logo: <img src=\"" + REF_ID_PREFIX + LOGO + "\"></p>", text.getSourceCode());
		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	public void testMap() {
		BinaryData logo = image(LOGO);
		StructuredText text = HtmlFunctions.text("<p><img src=\"pic\"/></p>", Map.of("pic", logo));

		assertEquals("<p><img src=\"" + REF_ID_PREFIX + "pic\"></p>", text.getSourceCode());
		assertEquals(Map.of("pic", logo), text.getImages());
	}

	public void testSingleImage() {
		BinaryData logo = image(LOGO);
		StructuredText text = HtmlFunctions.text("<img src=\"logo.png\"/>", logo);

		assertEquals("<img src=\"" + REF_ID_PREFIX + LOGO + "\">", text.getSourceCode());
		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	public void testUnreferencedImageDropped() {
		BinaryData logo = image(LOGO);
		StructuredText text = HtmlFunctions.text("<img src=\"logo.png\"/>", List.of(logo, image(OTHER)));

		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	public void testUnknownImageKept() {
		StructuredText text =
			HtmlFunctions.text("<p><img src=\"logo.png\"/><img src=\"https://example.com/x.png\"/></p>",
				List.of(image(LOGO)));

		assertEquals(
			"<p><img src=\"" + REF_ID_PREFIX + LOGO + "\"><img src=\"https://example.com/x.png\"></p>",
			text.getSourceCode());
	}

	public void testHtmlSyntax() {
		BinaryData image = image("x.png");
		StructuredText text = HtmlFunctions.text(
			"<p>a<br>b <img src=\"x.png\"> &nbsp;c</p><ul><li>one<li>two</ul>", List.of(image));

		assertEquals(
			"<p>a<br>b <img src=\"" + REF_ID_PREFIX + "x.png\"> &nbsp;c</p><ul><li>one</li><li>two</li></ul>",
			text.getSourceCode());
		assertEquals(Map.of("x.png", image), text.getImages());
	}

	public void testInvalidImages() {
		assertInvalid("logo.png");
		assertInvalid(List.of(image(LOGO), "other"));
		assertInvalid(Map.of("pic", 42));
	}

	private static void assertInvalid(Object images) {
		try {
			HtmlFunctions.text("<img src=\"logo.png\"/>", images);
			fail("Invalid images accepted: " + images);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_HTML_IMAGES__VALUE.getKey(), ex.getErrorKey().getKey());
		}
	}

	private static BinaryData image(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "image/png", name);
	}

}
