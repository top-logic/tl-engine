/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.wysiwyg.I18NHtmlControlProvider;
import com.top_logic.layout.react.wysiwyg.WysiwygControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * Tests how a formatted text field is displayed where it has no room, see
 * {@link WysiwygControlProvider} and {@link I18NHtmlControlProvider}.
 */
public class TestWysiwygPreview extends TestCase {

	private static final String HTML =
		"<h1>Title &amp; more</h1>\n<p>First   paragraph,<br/>next&nbsp;line.</p><ul><li>One</li><li>Two</li></ul>";

	private static final String PLAIN = "Title & more First paragraph, next line. One Two";

	/**
	 * A formatted text editor always needs more room than a single line.
	 */
	public void testIsLarge() {
		assertTrue(wysiwyg().isLarge(FieldSpec.of(StructuredText.class, "Text")));
		assertTrue(new I18NHtmlControlProvider().isLarge(FieldSpec.of(I18NStructuredText.class, "Text")));
	}

	/**
	 * The plain text on a single line stands for a formatted text.
	 */
	public void testPreview() {
		WysiwygControlProvider provider = wysiwyg();
		FieldSpec field = FieldSpec.of(StructuredText.class, "Text");

		assertEquals("", provider.previewText(field, null));
		assertEquals(PLAIN, provider.previewText(field, new StructuredText(HTML)));
	}

	/**
	 * The plain text of the internationalized formatted text stands for it.
	 */
	public void testI18NPreview() {
		I18NHtmlControlProvider provider = new I18NHtmlControlProvider();
		FieldSpec field = FieldSpec.of(I18NStructuredText.class, "Text");

		assertEquals("", provider.previewText(field, null));
		I18NStructuredText value = new I18NStructuredText(Map.of(Locale.ENGLISH, new StructuredText(HTML)));
		assertEquals(PLAIN, provider.previewText(field, value));
	}

	private static WysiwygControlProvider wysiwyg() {
		return new WysiwygControlProvider(null, TypedConfiguration.newConfigItem(WysiwygControlProvider.Config.class));
	}

	/**
	 * Test suite providing the resources the language of the user is resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestWysiwygPreview.class, ResourcesModule.Module.INSTANCE));
	}

}
