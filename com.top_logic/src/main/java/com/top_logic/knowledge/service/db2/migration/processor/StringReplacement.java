/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Mapping that replaces all occurrences of a configured {@link String} by another one.
 * 
 * @see RegExpReplacement Replacement of a more complex {@link String} ba using regular expressions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringReplacement extends AbstractConfiguredInstance<StringReplacement.Config>
		implements Mapping<String, String> {

	/**
	 * Typed configuration interface definition for {@link StringReplacement}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("target-replacement")
	public interface Config extends PolymorphicConfiguration<StringReplacement> {

		/** Configuration name for {@link #getTarget()}. */
		String TARGET = "target";

		/** Configuration name for {@link #getReplacement()}. */
		String REPLACEMENT = "replacement";

		/**
		 * The {@link String} whose occurrences have to be replaced.
		 */
		@Name(TARGET)
		@Mandatory
		String getTarget();

		/**
		 * The {@link String} that is used for all occurrences of {@link #getTarget()}.
		 */
		@Name(REPLACEMENT)
		@Mandatory
		String getReplacement();

	}

	/**
	 * Create a {@link StringReplacement}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StringReplacement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String map(String input) {
		if (StringServices.isEmpty(input)) {
			return input;
		}

		return input.replace(getConfig().getTarget(), getConfig().getReplacement());
	}

}

