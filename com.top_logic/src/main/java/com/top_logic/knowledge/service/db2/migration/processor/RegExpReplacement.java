/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * Mapping that replaces all parts in the source string that match a given regular expression.
 * 
 * @see StringReplacement Replacement of a simple string by another one.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RegExpReplacement extends AbstractConfiguredInstance<RegExpReplacement.Config> implements Mapping<String, String> {

	/**
	 * Typed configuration interface definition for {@link RegExpReplacement}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("reg-exp-replacement")
	public interface Config extends PolymorphicConfiguration<RegExpReplacement> {

		/** Configuration name of {@link #getSearchPattern()}. */
		String SEARCH_PATTERN = "search-pattern";

		/** Configuration name of {@link #getReplacement()}. */
		String REPLACEMENT = "replacement";

		/**
		 * {@link Pattern} to which the part of a string must correspond in order to be replaced.
		 */
		@Format(RegExpValueProvider.class)
		@Name(SEARCH_PATTERN)
		@Mandatory
		Pattern getSearchPattern();

		/**
		 * String that is used to replace the parts of the source string the match the given
		 * {@link #getSearchPattern()}.
		 * 
		 * @apiNote The replacement must be a regular expression as described in
		 *          {@link Matcher#replaceAll(String)}.
		 */
		@Name(REPLACEMENT)
		@Mandatory
		String getReplacement();

	}

	/**
	 * Create a {@link RegExpReplacement}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public RegExpReplacement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String map(String input) {
		if (StringServices.isEmpty(input)) {
			return input;
		}

		return getConfig().getSearchPattern().matcher(input).replaceAll(getConfig().getReplacement());
	}

}

