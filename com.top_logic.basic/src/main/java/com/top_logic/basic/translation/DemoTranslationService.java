/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.translation;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.tools.resources.translate.Translator;

/**
 * A {@link TranslationService} that contacts no external service but produces deterministic
 * pseudo-translations by tagging the source text with the target language.
 *
 * <p>
 * Useful for demonstrations and automated tests where a real translation back-end (e.g.
 * {@link DeepLTranslationService}) is unavailable or undesired: it needs no API key and no network,
 * and its output makes the applied translation visible, e.g. {@code "Hello [de]"}. The set of
 * pretended-to-be-supported languages is configurable.
 * </p>
 */
public class DemoTranslationService extends TranslationService<DemoTranslationService.Config> {

	/**
	 * Configuration for {@link DemoTranslationService}.
	 */
	public interface Config extends TranslationService.Config {

		/** Comma-separated list of language codes the demo translator pretends to support. */
		@Name("supported-languages")
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault("de, en, fr, es")
		List<String> getSupportedLanguages();
	}

	/**
	 * Creates a new {@link DemoTranslationService}.
	 */
	public DemoTranslationService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Translator createTranslator() {
		List<String> supportedLanguages = getConfig().getSupportedLanguages();
		return new Translator() {
			@Override
			public String translate(String text, String sourceLanguage, String targetLanguage) {
				if (text == null || text.isEmpty()) {
					return text;
				}
				return text + " [" + targetLanguage + "]";
			}

			@Override
			public List<String> getSupportedLanguages() {
				return supportedLanguages;
			}
		};
	}

}
