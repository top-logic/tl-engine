/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link FormatDefinition} creating predefined {@link NumberFormat}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Predefined number format")
public class NumberFormatDefinition extends FormatDefinition<NumberFormatDefinition.Config> {

	/**
	 * Style definition of the {@link NumberFormat} to create.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static enum NumberStyle implements ExternallyNamed {

		/** @see NumberFormat#getCurrencyInstance(Locale) */
		CURRENCY("currency"),

		/** @see NumberFormat#getIntegerInstance(Locale) */
		INTEGER("integer"),

		/** @see NumberFormat#getNumberInstance(Locale) */
		NUMBER("number"),

		/** @see NumberFormat#getPercentInstance(Locale) */
		PERCENT("percent")

		;

		private final String _externalName;

		NumberStyle(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Throws an {@link UnreachableAssertion} that given style actually does not exist.
		 */
		public static UnreachableAssertion noSuchNumberStyle(NumberStyle style) {
			throw new UnreachableAssertion("Unknown " + NumberStyle.class.getName() + ": " + style);
		}

	}

	/**
	 * Configuration of a {@link NumberFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends FormatDefinition.Config<NumberFormatDefinition> {

		/**
		 * Tag name to identify {@link NumberFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "number";

		/**
		 * The style of the created {@link NumberFormat}.
		 * 
		 * @see NumberStyle
		 */
		@Mandatory
		NumberStyle getStyle();

		/**
		 * The result type the format should produced after parsing.
		 */
		NumberFormatResult getResultType();
	}

	/**
	 * Creates a new {@link NumberFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NumberFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public NumberFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		return getConfig().getResultType().adapt(createFormat(locale));
	}

	private NumberFormat createFormat(Locale locale) throws UnreachableAssertion {
		switch (getConfig().getStyle()) {
			case CURRENCY:
				return NumberFormat.getCurrencyInstance(locale);
			case INTEGER:
				return NumberFormat.getIntegerInstance(locale);
			case NUMBER:
				return NumberFormat.getNumberInstance(locale);
			case PERCENT:
				return NumberFormat.getPercentInstance(locale);
			default:
				throw NumberStyle.noSuchNumberStyle(getConfig().getStyle());
		}
	}

	@Override
	public String getPattern() {
		return null;
	}

}

