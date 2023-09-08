/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.SimpleDisplayValue;
import com.top_logic.layout.basic.ResourceText;

/**
 * Test for {@link ResourceText}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestResourceText extends BasicTestCase {

	public void testWithExitingResourceGerman() throws IOException {
		/* Note: English translation can not be tested within same method, because resources are
		 * cached. */
		testWithExitingResource(Locale.GERMAN, "Deutsche Übersetzung für Key1");
	}

	public void testWithExistingResourceEnglish() throws IOException {
		/* Note: German translation can not be tested within same method, because resources are
		 * cached. */
		testWithExitingResource(Locale.ENGLISH, "English translation for Key1");
	}

	public void testFallbackWithExistingResourceGerman() throws IOException {
		/* Note: English translation can not be tested within same method, because resources are
		 * cached. */
		testFallbackWithExistingResource(Locale.GERMAN, "Deutsche Übersetzung für Key1", new SimpleDisplayValue() {

			@Override
			public String get(DisplayContext context) {
				fail("Translation is present; fallback must not be accessed");
				throw new AssertionError();
			}
		});
	}

	public void testFallbackWithExistingResourceEnglish() throws IOException {
		/* Note: German translation can not be tested within same method, because resources are
		 * cached. */
		testFallbackWithExistingResource(Locale.ENGLISH, "English translation for Key1", new SimpleDisplayValue() {

			@Override
			public String get(DisplayContext context) {
				fail("Translation is present; fallback must not be accessed");
				throw new AssertionError();
			}
		});
	}

	public void testFallbackWithNonExistingResourceGerman() throws IOException {
		/* Note: English translation can not be tested within same method, because resources are
		 * cached. */
		testFallbackWithNonExistingResource(Locale.GERMAN, "Deutsche Übersetzung für Key1");
	}

	public void testFallbackWithNonExistingResourceEnglish() throws IOException {
		/* Note: German translation can not be tested within same method, because resources are
		 * cached. */
		testFallbackWithNonExistingResource(Locale.ENGLISH, "English translation for Key1");
	}

	private void testFallbackWithNonExistingResource(Locale locale, String expectedTranslation)
			throws IOException {
		DisplayContext interaction = interactionForLocale(locale);
		assertEquals("Precondition for resource text test violated.", expectedTranslation,
			interaction.getResources().getString(I18NConstants.TEST_RESOURCE_TEXT_KEY1));
		assertEquals("Precondition for resource text test violated.", null,
			interaction.getResources().getString(I18NConstants.TEST_RESOURCE_TEXT_NOT_TRANSLATED_KEY, null));
		DisplayValue fallback = new ResourceText(I18NConstants.TEST_RESOURCE_TEXT_KEY1);
		ResourceText resourceText = new ResourceText(I18NConstants.TEST_RESOURCE_TEXT_NOT_TRANSLATED_KEY, fallback);
		// Test simple get.
		assertEquals(expectedTranslation, resourceText.get(interaction));
		// Test streaming.
		StringBuilder out = new StringBuilder();
		resourceText.append(interaction, out);
		assertEquals("Streaming API not correct.", expectedTranslation, out.toString());
	}

	private void testFallbackWithExistingResource(Locale locale, String expectedTranslation, DisplayValue fallback)
			throws IOException {
		DisplayContext interaction = interactionForLocale(locale);
		assertEquals("Precondition for resource text test violated.", expectedTranslation,
			interaction.getResources().getString(I18NConstants.TEST_RESOURCE_TEXT_KEY1));
		ResourceText resourceText = new ResourceText(I18NConstants.TEST_RESOURCE_TEXT_KEY1, fallback);
		// Test simple get.
		assertEquals(expectedTranslation, resourceText.get(interaction));
		// Test streaming.
		StringBuilder out = new StringBuilder();
		resourceText.append(interaction, out);
		assertEquals("Streaming API not correct.", expectedTranslation, out.toString());
	}

	private void testWithExitingResource(Locale locale, String expectedTranslation) throws IOException {
		testFallbackWithExistingResource(locale, expectedTranslation, null);
	}

	private DisplayContext interactionForLocale(Locale locale) {
		DisplayContext interaction = (DisplayContext) ThreadContextManager.getInteraction();
		interaction.getSubSessionContext().setCurrentLocale(locale);
		return interaction;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestResourceText}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestResourceText.class);
	}

}
