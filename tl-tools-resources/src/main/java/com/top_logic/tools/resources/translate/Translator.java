/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources.translate;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * API for automatic translations.
 */
public interface Translator {

	/**
	 * Dummy {@link Translator} that supports no languages at all.
	 */
	Translator NONE = new Translator() {

		@Override
		public String translate(String text, String sourceLanguage, String targetLanguage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> getSupportedLanguages() {
			return Collections.emptyList();
		}
	};

	/**
	 * Translates the given text to the given target language.
	 *
	 * @param text
	 *        The text to translate.
	 * @param targetLanguage
	 *        The requested target language.
	 * @return The text translated to the given target language.
	 */
	default String translate(String text, String targetLanguage) {
		return translate(text, null, targetLanguage);
	}

	/**
	 * Translates the given text to the given target language.
	 *
	 * @param text
	 *        The text to translate.
	 * @param targetLanguage
	 *        The requested target language.
	 * @return The text translated to the given target language.
	 */
	default String translate(String text, Locale targetLanguage) {
		return translate(text, targetLanguage.getLanguage());
	}

	/**
	 * Translates the given text from the given source language to the given target language.
	 *
	 * @param text
	 *        The text to translate.
	 * @param sourceLanguage
	 *        The language of the given text, <code>null</code> to use auto-detection.
	 * @param targetLanguage
	 *        The requested target language.
	 * @return The text translated to the given target language.
	 */
	default String translate(String text, Locale sourceLanguage, Locale targetLanguage) {
		return translate(text, sourceLanguage == null ? null : sourceLanguage.getLanguage(),
			targetLanguage.getLanguage());
	}

	/**
	 * Translates the given text from the given source language to the given target language.
	 *
	 * @param text
	 *        The text to translate.
	 * @param sourceLanguage
	 *        The language of the given text, <code>null</code> to use auto-detection.
	 * @param targetLanguage
	 *        The requested target language.
	 * @return The text translated to the given target language.
	 */
	String translate(String text, String sourceLanguage, String targetLanguage);

	/**
	 * Lists supported languages.
	 */
	List<String> getSupportedLanguages();

	/**
	 * Whether the given language is within {@link #getSupportedLanguages()}.
	 */
	default boolean isSupported(String language) {
		return getSupportedLanguages().contains(language);
	}

	/**
	 * Whether the given language is within {@link #getSupportedLanguages()}.
	 */
	default boolean isSupported(Locale language) {
		return isSupported(language.getLanguage());
	}

}
