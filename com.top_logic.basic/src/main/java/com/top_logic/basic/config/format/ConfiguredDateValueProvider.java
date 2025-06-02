/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.FormatValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.thread.ThreadContext;

/**
 * {@link FormatValueProvider} adapting the {@link XmlDateTimeFormat}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredDateValueProvider extends FormatValueProvider
		implements ConfiguredInstance<ConfiguredDateValueProvider.Config<?>> {

	/**
	 * Configuration options for {@link ConfiguredDateValueProvider}.
	 */
	public interface Config<I extends ConfiguredDateValueProvider> extends PolymorphicConfiguration<I> {
		/**
		 * The date format pattern.
		 * 
		 * @see SimpleDateFormat
		 */
		@Mandatory
		String getDateFormat();

		/**
		 * Optional fixed locale.
		 * 
		 * <p>
		 * If no locale is specified, the locale of the current user is used.
		 * </p>
		 */
		@Nullable
		String getLocale();

		/**
		 * Optional fixed timezone.
		 * 
		 * <p>
		 * If no timezone is specified, the timezone of the current user is used.
		 * </p>
		 */
		@Nullable
		String getTimeZone();
	}

	private final Config<?> _config;

	private final SimpleDateFormat _format;

	/**
	 * Creates a {@link ConfiguredDateValueProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredDateValueProvider(InstantiationContext context, Config<?> config) {
		super(Date.class);
		_config = config;

		String locale = config.getLocale();
		_format = new SimpleDateFormat(config.getDateFormat(),
			locale == null ? ThreadContext.getLocale() : Locale.forLanguageTag(locale));

		String timeZone = config.getTimeZone();
		_format.setTimeZone(timeZone == null ? ThreadContext.getTimeZone() : TimeZone.getTimeZone(timeZone));
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	protected Format format() {
		return _format;
	}

}
