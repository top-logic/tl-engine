/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.Format;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configured definition (and factory) for a {@link Format}.
 * 
 * <p>
 * formats are instantiated using {@link #newFormat(FormatConfig, TimeZone, Locale)}.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class FormatDefinition<T> implements ConfiguredInstance<FormatDefinition.Config<T>> {

	/**
	 * Configuration of a {@link FormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<T> extends PolymorphicConfiguration<T> {

		/** Name of the "id" property. */
		String ID_NAME = "id";

		/**
		 * The id under which this {@link FormatDefinition} is known.
		 */
		@Name(ID_NAME)
		String getId();

	}

	private final Config<T> _config;

	/**
	 * Creates a new {@link FormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public FormatDefinition(InstantiationContext context, Config<T> config) throws ConfigurationException {
		_config = config;
	}

	@Override
	public Config<T> getConfig() {
		return _config;
	}

	/**
	 * Creates the {@link Format}.
	 * 
	 * @param config
	 *        The global configuration, that can be used as fallback for local wildcards.
	 * @param timeZone
	 *        The {@link TimeZone} to create {@link Format} for. Usage of this variable depends on
	 *        the kind of the {@link Format}.
	 * @param locale
	 *        The {@link Locale} to create {@link Format} for. Usage of this variable depends on the
	 *        kind of the {@link Format}.
	 */
	public abstract Format newFormat(FormatConfig config, TimeZone timeZone, Locale locale);

	/**
	 * Returns the pattern string used to create {@link Format}
	 * 
	 * @return May be <code>null</code>, in case the {@link Format} that are created are not pattern
	 *         based.
	 */
	public abstract String getPattern();

}

