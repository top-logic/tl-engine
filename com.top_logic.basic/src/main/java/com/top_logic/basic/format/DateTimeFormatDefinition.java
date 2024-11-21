/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormat;
import java.util.Locale;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link FormatDefinition} creating an official {@link DateFormat date-time format} that adjusts to
 * the user's locale.
 * 
 * @since Ticket #12026
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Internationalized date with time format")
public class DateTimeFormatDefinition extends AbstractDateFormatDefinition<DateTimeFormatDefinition.Config> {

	/**
	 * Configuration of a {@link DateTimeFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractDateFormatDefinition.Config<DateTimeFormatDefinition> {

		/**
		 * Tag name to identify {@link DateTimeFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "date-time";

		/**
		 * The style of the created {@link DateFormat}.
		 */
		DateStyle getDateStyle();

		/**
		 * The style of the created {@link DateFormat}.
		 */
		DateStyle getTimeStyle();

		@Override
		@BooleanDefault(true)
		boolean getUserTimeZone();

	}

	private final int _dateStyle;

	private final int _timeStyle;

	/**
	 * Creates a new {@link DateTimeFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DateTimeFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DateTimeFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_dateStyle = config.getDateStyle().intValue();
		_timeStyle = config.getTimeStyle().intValue();
	}

	@Override
	protected DateFormat internalCreateFormat(Locale locale) {
		return DateFormat.getDateTimeInstance(_dateStyle, _timeStyle, locale);
	}

	@Override
	public String getPattern() {
		return null;
	}

}

