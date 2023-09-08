/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * Configurable {@link Mapping} replacing in text content using regular expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RegexPatternMapping implements Mapping<String, String> {

	private Pattern _pattern;

	private String _replacement;

	/**
	 * Configuration options for {@link RegexPatternMapping}.
	 */
	@TagName("regex-replace")
	public interface Config<I extends RegexPatternMapping> extends PolymorphicConfiguration<I> {

		/**
		 * The pattern to match.
		 */
		@Format(RegExpValueProvider.class)
		Pattern getPattern();

		/**
		 * The replacement to insert replacing each matched region of the input.
		 */
		String getReplacement();

	}

	/**
	 * Creates a {@link RegexPatternMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RegexPatternMapping(InstantiationContext context, Config<?> config) {
		_pattern = config.getPattern();
		_replacement = config.getReplacement();
	}

	@Override
	public String map(String input) {
		Matcher matcher = _pattern.matcher(input);

		if (matcher.find()) {
			StringBuffer buffer = new StringBuffer();

			do {
				matcher.appendReplacement(buffer, _replacement);
			} while (matcher.find());

			matcher.appendTail(buffer);

			return buffer.toString();
		} else {
			return input;
		}
	}

}
