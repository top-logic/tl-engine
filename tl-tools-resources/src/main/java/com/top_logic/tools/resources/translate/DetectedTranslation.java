/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources.translate;

/**
 * Result of a translation with automatic detection of the source language.
 *
 * @see Translator#translateDetecting(String, String)
 *
 * @param text
 *        The translated text.
 * @param sourceLanguage
 *        The detected language of the original text, normalized like the languages in
 *        {@link Translator#getSupportedLanguages()} (e.g. <code>de</code>, <code>en</code>).
 *        <code>null</code>, if the {@link Translator} does not report the detected language.
 * @param targetLanguage
 *        The language the text was translated to, as requested by the caller.
 */
public record DetectedTranslation(String text, String sourceLanguage, String targetLanguage) {

	// Pure value.

}
