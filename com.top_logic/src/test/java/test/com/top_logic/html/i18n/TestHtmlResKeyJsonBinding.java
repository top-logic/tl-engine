/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.html.i18n;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.config.json.AbstractJsonConfigurationWriterTest;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.html.i18n.HtmlResKeyJsonBinding;

import test.com.top_logic.ModuleLicenceTestSetup;

/**
 * Test case for {@link HtmlResKeyJsonBinding}.
 */
@SuppressWarnings("javadoc")
public class TestHtmlResKeyJsonBinding extends AbstractJsonConfigurationWriterTest {

	public interface HtmlResKeyItem extends ConfigurationItem {
		HtmlResKey getContent();

		void setContent(HtmlResKey value);
	}

	/**
	 * Tests JSON round-trip for a non-literal {@link HtmlResKey}.
	 */
	public void testNonLiteral() throws Exception {
		HtmlResKeyItem config = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);
		config.setContent(new DefaultHtmlResKey(ResKey.internalCreate("my.html.key")));

		HtmlResKeyItem copy = (HtmlResKeyItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(HtmlResKeyItem.class), config);

		assertNotNull(copy.getContent());
		DefaultHtmlResKey copiedKey = (DefaultHtmlResKey) copy.getContent();
		assertEquals(ResKey.internalCreate("my.html.key"), copiedKey.content());
	}

	/**
	 * Tests JSON round-trip for a literal {@link HtmlResKey}.
	 */
	public void testLiteral() throws Exception {
		HtmlResKeyItem config = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);
		ResKey literalKey = ResKey.builder()
			.add(Locale.ENGLISH, "<b>Hello</b>")
			.add(Locale.GERMAN, "<b>Hallo</b>")
			.build();
		config.setContent(new DefaultHtmlResKey(literalKey));

		HtmlResKeyItem copy = (HtmlResKeyItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(HtmlResKeyItem.class), config);

		assertNotNull(copy.getContent());
		DefaultHtmlResKey copiedKey = (DefaultHtmlResKey) copy.getContent();
		assertTrue(copiedKey.content().isLiteral());
	}

	/**
	 * Tests JSON round-trip for a null {@link HtmlResKey}.
	 */
	public void testNull() throws Exception {
		HtmlResKeyItem config = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);

		HtmlResKeyItem copy = (HtmlResKeyItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(HtmlResKeyItem.class), config);

		assertNull(copy.getContent());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestHtmlResKeyJsonBinding.class));
	}

}
