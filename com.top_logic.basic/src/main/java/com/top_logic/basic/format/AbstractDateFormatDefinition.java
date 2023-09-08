/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.TimeZones;

/**
 * Base class for default date {@link FormatDefinition}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDateFormatDefinition<T> extends FormatDefinition<T> {

	/**
	 * Common configuration options for all {@link AbstractDateFormatDefinition}s.
	 */
	public interface Config<T> extends FormatDefinition.Config<T> {

		/**
		 * @see #getLenientParsing()
		 */
		String LENIENT_PARSING_PROPERTY = "lenient";

		/**
		 * @see #getUserTimeZone()
		 */
		String USER_TIME_ZONE_PROPERTY = "user-time-zone";

		/**
		 * Configured option whether to use lenient parsing for date and time formats by default.
		 * 
		 * @see DateFormat#setLenient(boolean)
		 */
		@Name(LENIENT_PARSING_PROPERTY)
		Decision getLenientParsing();

		/**
		 * @see #getLenientParsing()
		 */
		void setLenientParsing(Decision value);

		/**
		 * Configuration option whether the format should use the {@link TimeZone} of the user.
		 *
		 * @return <code>true</code> to use the users {@link TimeZone}, <code>false</code> to use
		 *         {@link TimeZones#systemTimeZone()}.
		 * 
		 * @see DateFormat#setTimeZone(TimeZone)
		 */
		@Mandatory
		boolean getUserTimeZone();

		/**
		 * Sets the value of {@link #getUserTimeZone()}
		 */
		void setUserTimeZone(boolean useTimeZone);

	}

	/**
	 * Creates a {@link AbstractDateFormatDefinition} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractDateFormatDefinition(InstantiationContext context, Config<T> config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public final DateFormat newFormat(FormatConfig config, TimeZone timeZone, Locale locale) {
		DateFormat result = internalCreateFormat(locale);
		result.setLenient(config().getLenientParsing().toBoolean(config.getLenientParsing()));
		if (config().getUserTimeZone()) {
			result.setTimeZone(timeZone);
		} else {
			result.setTimeZone(TimeZones.systemTimeZone());
		}
		return result;
	}

	private Config<T> config() {
		return (Config<T>) getConfig();
	}

	/**
	 * Actually creates the format.
	 * 
	 * @see #newFormat(FormatConfig, TimeZone, Locale)
	 */
	protected abstract DateFormat internalCreateFormat(Locale locale);

}
