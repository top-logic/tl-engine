/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.translation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.tools.resources.translate.deepl.DeepLTranslator;
import com.top_logic.tools.resources.translate.deepl.DeepLTranslator.Builder;

/**
 * A {@link TranslationService} working with the <i>DeepL</i> API.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class DeepLTranslationService extends TranslationService<DeepLTranslationService.Config> {

	/**
	 * Configuration for {@link DeepLTranslationService}.
	 */
	public interface Config extends TranslationService.Config {

		/** Comma-separated list of XML tags that indicate text not to be translated. */
		@Name("ignore-tags")
		@Format(CommaSeparatedStrings.class)
		List<String> getIgnoreTags();

		/** Comma-separated list of XML tags which never split sentences. */
		@Name("non-splitting-tags")
		@Format(CommaSeparatedStrings.class)
		List<String> getNonSplittingTags();

		/** Whether automatic outline detection is enabled. */
		@Name("outline-detection")
		boolean getOutlineDetection();

		/** How input text is split into into sentences. */
		@Name("split-sentences")
		Splitting getSplitSentences();

		/** Comma-separated list of XML tags which always cause sentence splits. */
		@Name("splitting-tags")
		@Format(CommaSeparatedStrings.class)
		List<String> getSplittingTags();

		/** Whether XML processing is enabled. */
		@Name("tag-handling")
		boolean getTagHandling();

		/**
		 * Mapping of translation directions to a glossary name to use.
		 * 
		 * <p>
		 * A translation direction is the source language appended with the target language
		 * separated with white space. The translation from English to German would be specified as
		 * <code>en de</code>.
		 * </p>
		 */
		@EntryTag("glossary")
		@MapBinding(tag = "glossary", key = "direction", attribute = "name")
		Map<LanguagePair, String> getGlossaries();
	}

	/**
	 * Possible values for sentence splitting.
	 * 
	 * @see DeepLTranslator#SPLIT_SENTENCES
	 */
	public enum Splitting {
		/**
		 * Splits on interpunction only, ignoring newlines.
		 */
		INTERPUNCTATION_ONLY("nonewlines"),

		/**
		 * Splits on interpunction and on newlines.
		 */
		INTERPUNCTATION_AND_NEWLINES("1"),

		/**
		 * No splitting at all, whole input is treated as one sentence.
		 */
		NO_SPLITTING("0");

		final String _encoding;

		/**
		 * Creates a {@link Splitting}.
		 */
		private Splitting(String encoding) {
			_encoding = encoding;
		}

		String encode() {
			return _encoding;
		}
	}

	/**
	 * Creates a new {@link DeepLTranslationService}.
	 */
	public DeepLTranslationService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Translator createTranslator() {
		Config config = getConfig();

		Builder builder = new DeepLTranslator.Builder();
		builder.setApiHost(config.getApiHost());
		builder.setApiKey(config.getApiKey());
		builder.setIgnoreTags(config.getIgnoreTags());
		builder.setNonSplittingTags(config.getNonSplittingTags());
		builder.setOutlineDetection(config.getOutlineDetection());
		builder.setSplitSentences(config.getSplitSentences().encode());
		builder.setSplittingTags(config.getSplittingTags());
		builder.setTagHandling(config.getTagHandling());
		builder.setGlossaries(glossaries(config));

		return builder.build();
	}

	private Map<String, Map<String, String>> glossaries(Config config) {
		Map<String, Map<String, String>> result = new HashMap<>();
		for (var entry : config.getGlossaries().entrySet()) {
			result.computeIfAbsent(entry.getKey().getSource(), x -> new HashMap<>()).put(entry.getKey().getTarget(),
				entry.getValue());
		}
		return result;
	}
}
