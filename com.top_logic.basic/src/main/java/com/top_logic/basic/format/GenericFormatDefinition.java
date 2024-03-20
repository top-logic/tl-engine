/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link FormatDefinition} that creates the {@link Format} by instantiating a {@link Format} class.
 * It is expected that the class has a {@link String} constructor.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericFormatDefinition extends PatternBasedFormatDefinition<GenericFormatDefinition.Config> {

	private static final Class<?>[] FORMAT_CONSTRUCTOR_SIGNATURE = new Class[] { String.class };

	/**
	 * Configuration of a {@link GenericFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PatternBasedFormatDefinition.Config<GenericFormatDefinition> {

		/**
		 * Tag name to identify {@link GenericFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "generic";

		/**
		 * Class of the {@link Format} to instantiate. It is expected that the class has a
		 * {@link String} constructor.
		 */
		@Mandatory
		Class<? extends Format> getFormatClass();

	}

	/**
	 * Creates a new {@link GenericFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link GenericFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public GenericFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		try {
			// call to check class has correct constructor.
			config.getFormatClass().getConstructor(FORMAT_CONSTRUCTOR_SIGNATURE);
		} catch (NoSuchMethodException ex) {
			StringBuilder error = new StringBuilder();
			error.append("No String constructor in class '");
			error.append(config.getFormatClass().getName());
			error.append("'");
			throw new ConfigurationException(error.toString(), ex);
		}
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		Config config = getConfig();
		try {
			Format format =
				ConfigUtil.newInstance(config.getFormatClass(), FORMAT_CONSTRUCTOR_SIGNATURE, getPattern());
			if (format instanceof DecimalFormat) {
				((DecimalFormat) format).setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
			}
			else if (format instanceof SimpleDateFormat) {
				((SimpleDateFormat) format).setDateFormatSymbols(new DateFormatSymbols(locale));
				((SimpleDateFormat) format).setTimeZone(timeZone);
			}
			return format;
		} catch (ConfigurationException ex) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("Unable to create generic format from class '");
			errorMsg.append(config.getFormatClass());
			errorMsg.append("' with pattern '");
			errorMsg.append(getPattern());
			errorMsg.append("'.");
			throw new ConfigurationError(errorMsg.toString(), ex);
		}
	}

}

