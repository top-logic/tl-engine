/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.config.json.AbstractJsonConfigurationWriterTest;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextJsonBinding;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextJsonBinding;

import test.com.top_logic.ModuleLicenceTestSetup;

/**
 * Test case for {@link StructuredTextJsonBinding} and {@link I18NStructuredTextJsonBinding}.
 */
@SuppressWarnings("javadoc")
public class TestStructuredTextJsonBinding extends AbstractJsonConfigurationWriterTest {

	public interface StructuredTextItem extends ConfigurationItem {
		StructuredText getText();

		void setText(StructuredText value);
	}

	public interface I18NStructuredTextItem extends ConfigurationItem {
		I18NStructuredText getText();

		void setText(I18NStructuredText value);
	}

	/**
	 * Tests JSON round-trip for a {@link StructuredText} with source code only.
	 */
	public void testStructuredTextSourceOnly() throws Exception {
		StructuredTextItem config = TypedConfiguration.newConfigItem(StructuredTextItem.class);
		config.setText(new StructuredText("<p>Hello <b>world</b>!</p>"));

		StructuredTextItem copy = (StructuredTextItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(StructuredTextItem.class), config);

		assertNotNull(copy.getText());
		assertEquals("<p>Hello <b>world</b>!</p>", copy.getText().getSourceCode());
		assertTrue(copy.getText().getImages().isEmpty());
	}

	/**
	 * Tests JSON round-trip for a {@link StructuredText} with images.
	 */
	public void testStructuredTextWithImages() throws Exception {
		StructuredTextItem config = TypedConfiguration.newConfigItem(StructuredTextItem.class);
		Map<String, BinaryData> images = new HashMap<>();
		images.put("logo.png",
			BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3, 4 }, "image/png", "logo.png"));
		images.put("banner.jpg",
			BinaryDataFactory.createBinaryData(new byte[] { 5, 6, 7, 8, 9 }, "image/jpeg", "banner.jpg"));
		config.setText(new StructuredText("<p><img src='logo.png'/></p>", images));

		StructuredTextItem copy = (StructuredTextItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(StructuredTextItem.class), config);

		assertNotNull(copy.getText());
		assertEquals("<p><img src='logo.png'/></p>", copy.getText().getSourceCode());
		assertEquals(2, copy.getText().getImages().size());
		assertBinaryDataEquals(images.get("logo.png"), copy.getText().getImages().get("logo.png"));
		assertBinaryDataEquals(images.get("banner.jpg"), copy.getText().getImages().get("banner.jpg"));
	}

	/**
	 * Tests JSON round-trip for an empty {@link StructuredText}.
	 */
	public void testStructuredTextEmpty() throws Exception {
		StructuredTextItem config = TypedConfiguration.newConfigItem(StructuredTextItem.class);
		config.setText(new StructuredText());

		StructuredTextItem copy = (StructuredTextItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(StructuredTextItem.class), config);

		assertNotNull(copy.getText());
		assertEquals("", copy.getText().getSourceCode());
		assertTrue(copy.getText().getImages().isEmpty());
	}

	/**
	 * Tests JSON round-trip for a null {@link StructuredText}.
	 */
	public void testStructuredTextNull() throws Exception {
		StructuredTextItem config = TypedConfiguration.newConfigItem(StructuredTextItem.class);

		doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(StructuredTextItem.class), config);
	}

	/**
	 * Tests JSON round-trip for an {@link I18NStructuredText} with multiple locales.
	 */
	public void testI18NStructuredText() throws Exception {
		I18NStructuredTextItem config = TypedConfiguration.newConfigItem(I18NStructuredTextItem.class);
		Map<Locale, StructuredText> content = new HashMap<>();
		content.put(Locale.ENGLISH, new StructuredText("<p>Hello</p>"));
		content.put(Locale.GERMAN, new StructuredText("<p>Hallo</p>"));
		config.setText(new I18NStructuredText(content));

		I18NStructuredTextItem copy = (I18NStructuredTextItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(I18NStructuredTextItem.class), config);

		assertNotNull(copy.getText());
		StructuredText enText = copy.getText().localize(Locale.ENGLISH);
		assertNotNull(enText);
		assertEquals("<p>Hello</p>", enText.getSourceCode());
		StructuredText deText = copy.getText().localize(Locale.GERMAN);
		assertNotNull(deText);
		assertEquals("<p>Hallo</p>", deText.getSourceCode());
	}

	/**
	 * Tests JSON round-trip for an {@link I18NStructuredText} with images.
	 */
	public void testI18NStructuredTextWithImages() throws Exception {
		I18NStructuredTextItem config = TypedConfiguration.newConfigItem(I18NStructuredTextItem.class);
		Map<String, BinaryData> images = new HashMap<>();
		images.put("img.png",
			BinaryDataFactory.createBinaryData(new byte[] { 10, 20, 30 }, "image/png", "img.png"));
		Map<Locale, StructuredText> content = new HashMap<>();
		content.put(Locale.ENGLISH, new StructuredText("<p><img src='img.png'/></p>", images));
		content.put(Locale.GERMAN, new StructuredText("<p>Kein Bild</p>"));
		config.setText(new I18NStructuredText(content));

		I18NStructuredTextItem copy = (I18NStructuredTextItem) doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(I18NStructuredTextItem.class), config);

		assertNotNull(copy.getText());
		StructuredText enText = copy.getText().localize(Locale.ENGLISH);
		assertNotNull(enText);
		assertEquals(1, enText.getImages().size());
		assertBinaryDataEquals(images.get("img.png"), enText.getImages().get("img.png"));
	}

	/**
	 * Tests JSON round-trip for a null {@link I18NStructuredText}.
	 */
	public void testI18NStructuredTextNull() throws Exception {
		I18NStructuredTextItem config = TypedConfiguration.newConfigItem(I18NStructuredTextItem.class);

		doReadWrite("config",
			TypedConfiguration.getConfigurationDescriptor(I18NStructuredTextItem.class), config);
	}

	private void assertBinaryDataEquals(BinaryData expected, BinaryData actual) throws IOException {
		assertNotNull(actual);
		assertEquals(expected.getContentType(), actual.getContentType());
		assertTrue(StreamUtilities.equalsStreamContents(expected.getStream(), actual.getStream()));
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestStructuredTextJsonBinding.class));
	}

}
