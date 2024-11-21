/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.Format;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * {@link FormatDefinition} creating {@link Format} based on a given pattern
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Custom format definition")
public abstract class PatternBasedFormatDefinition<C extends PatternBasedFormatDefinition.Config<?>>
		extends FormatDefinition<C> {

	/**
	 * Configuration of {@link PatternBasedFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends PatternBasedFormatDefinition<?>> extends FormatDefinition.Config<I> {

		/**
		 * The pattern to create {@link Format} from.
		 */
		@Mandatory
		String getPattern();

	}

	/**
	 * Creates a new {@link PatternBasedFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link PatternBasedFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public PatternBasedFormatDefinition(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public String getPattern() {
		return getConfig().getPattern();
	}


}

