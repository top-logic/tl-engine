/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormat;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link FormatDefinition} creating a predefined date-time {@link DateFormat}.
 * 
 * @since Ticket #12026
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateFormatDefinition extends AbstractDateFormatDefinition<DateFormatDefinition> {

	/**
	 * Configuration of a {@link DateFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractDateFormatDefinition.Config<DateFormatDefinition> {

		/**
		 * Tag name to identify {@link DecimalFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "date";

		/**
		 * The style of the created {@link DateFormat}.
		 */
		DateStyle getStyle();

		@Override
		@BooleanDefault(false)
		boolean getUserTimeZone();

	}

	private final int _style;

	/**
	 * Creates a new {@link DateFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DateFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DateFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_style = config().getStyle().intValue();
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected DateFormat internalCreateFormat(Locale locale) {
		return DateFormat.getDateInstance(_style, locale);
	}

	@Override
	public String getPattern() {
		return null;
	}

}

