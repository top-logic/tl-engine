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

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.wysiwyg.ui.HtmlFunctions;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Test for calling {@link HtmlFunctions#text(String, Object)} and
 * {@link HtmlFunctions#images(StructuredText)} from TL-Script.
 */
@SuppressWarnings("javadoc")
public class TestHtmlFunctionsScript extends AbstractSearchExpressionTest {

	private static final String LOGO = "logo.png";

	private static final String SOURCE = "<p><img src=\"logo.png\"/></p>";

	public void testWithoutImages() throws Exception {
		StructuredText text = (StructuredText) execute(search("html -> htmlText($html)"), SOURCE);

		assertEquals(SOURCE, text.getSourceCode());
		assertTrue(text.getImages().isEmpty());
	}

	public void testNamedImages() throws Exception {
		BinaryData logo = image(LOGO);
		StructuredText text =
			(StructuredText) execute(search("html -> files -> htmlText($html, images: $files)"), SOURCE,
				List.of(logo));

		assertEquals("<p><img src=\"" + REF_ID_PREFIX + LOGO + "\"></p>", text.getSourceCode());
		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	public void testDictImages() throws Exception {
		BinaryData logo = image("other.png");
		StructuredText text =
			(StructuredText) execute(search("html -> file -> htmlText($html, images: {'logo.png': $file})"),
				SOURCE, logo);

		assertEquals("<p><img src=\"" + REF_ID_PREFIX + LOGO + "\"></p>", text.getSourceCode());
		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	public void testRoundTrip() throws Exception {
		BinaryData logo = image(LOGO);
		StructuredText original = HtmlFunctions.text(SOURCE, List.of(logo));
		StructuredText text = (StructuredText) execute(
			search("text -> htmlText('<div>' + htmlSource($text) + '</div>', images: htmlImages($text))"),
			original);

		assertEquals("<div><p><img src=\"" + REF_ID_PREFIX + LOGO + "\"></p></div>", text.getSourceCode());
		assertEquals(Map.of(LOGO, logo), text.getImages());
	}

	private static BinaryData image(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "image/png", name);
	}

	/**
	 * The test suite, run against the default database only.
	 *
	 * <p>
	 * {@link HtmlFunctions#text(String, Object)} builds its result in memory without accessing the
	 * database, so running the script against each configured database tests nothing more.
	 * </p>
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestHtmlFunctionsScript.class,
			SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE));
	}

}
