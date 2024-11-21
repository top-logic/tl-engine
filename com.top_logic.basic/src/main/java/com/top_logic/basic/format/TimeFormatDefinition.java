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
 * {@link FormatDefinition} creating an official {@link DateFormat time format} that adjusts to the
 * user's locale.
 * 
 * @since Ticket #12026
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Internationalized time format")
public class TimeFormatDefinition extends AbstractDateFormatDefinition<TimeFormatDefinition.Config> {

	/**
	 * Configuration of a {@link TimeFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractDateFormatDefinition.Config<TimeFormatDefinition> {

		/**
		 * Tag name to identify {@link TimeFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "time";

		/**
		 * The style of the created {@link DateFormat}.
		 */
		DateStyle getStyle();

		@Override
		@BooleanDefault(true)
		boolean getUserTimeZone();

	}

	private final int _style;

	/**
	 * Creates a new {@link TimeFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TimeFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public TimeFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_style = config.getStyle().intValue();
	}

	@Override
	protected DateFormat internalCreateFormat(Locale locale) {
		return DateFormat.getTimeInstance(_style, locale);
	}

	@Override
	public String getPattern() {
		return null;
	}

}

