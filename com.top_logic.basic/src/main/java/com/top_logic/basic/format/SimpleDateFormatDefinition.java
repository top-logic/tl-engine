/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.time.TimeZones;

/**
 * {@link PatternBasedFormatDefinition} creating a {@link SimpleDateFormat date, date and time, or
 * time format} defined by a pattern.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Custom date format")
public class SimpleDateFormatDefinition extends PatternBasedFormatDefinition<SimpleDateFormatDefinition.Config> {

	/**
	 * Configuration of a {@link SimpleDateFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PatternBasedFormatDefinition.Config<SimpleDateFormatDefinition> {

		/**
		 * Tag name to identify {@link SimpleDateFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "custom-date";

		/**
		 * @see #getUserTimeZone()
		 */
		String USER_TIME_ZONE_PROPERTY = "user-time-zone";

		/**
		 * @see SimpleDateFormat#isLenient()
		 */
		@BooleanDefault(true)
		boolean isLenient();

		/**
		 * Configuration option whether the format should use the {@link TimeZone} of the user.
		 *
		 * @return <code>true</code> to use the users {@link TimeZone}, <code>false</code> to use
		 *         {@link TimeZones#systemTimeZone()}.
		 * 
		 * @see DateFormat#setTimeZone(TimeZone)
		 */
		boolean getUserTimeZone();

		/**
		 * Sets the value of {@link #getUserTimeZone()}
		 */
		void setUserTimeZone(boolean useTimeZone);

	}

	/**
	 * Creates a new {@link SimpleDateFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SimpleDateFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public SimpleDateFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		SimpleDateFormat format = new SimpleDateFormat(getPattern(), locale);
		Config config = getConfig();
		format.setLenient(config.isLenient());
		if (config.getUserTimeZone()) {
			format.setTimeZone(timeZone);
		} else {
			format.setTimeZone(TimeZones.systemTimeZone());
		}
		return format;
	}

}
