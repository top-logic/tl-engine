/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * {@link FormatDefinition} that wraps a number format with a value adjustment (division by 100.0).
 */
public class PercentFormatDefinition extends FormatDefinition<PercentFormatDefinition.Config<?>> {

	/**
	 * Configuration options for {@link PercentFormatDefinition}.
	 */
	public interface Config<I extends PercentFormatDefinition> extends FormatDefinition.Config<I> {
		/**
		 * The number format for parsing the percent value.
		 * 
		 * <p>
		 * The parsed value is afterwards divided by 100 to create the percentage value.
		 * </p>
		 */
		PolymorphicConfiguration<FormatDefinition<?>> getFormat();
	}

	private FormatDefinition<?> _format;

	/**
	 * Creates a {@link PercentFormatDefinition}.
	 */
	public PercentFormatDefinition(InstantiationContext context, Config<?> config)
			throws ConfigurationException {
		super(context, config);

		_format = context.getInstance(config.getFormat());
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		Format inner = _format.newFormat(globalConfig, timeZone, locale);
		return new NumberFormat() {
			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				Number value = (Number) inner.parseObject(source, parsePosition);
				return value.doubleValue() / 100.0;
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
				return inner.format(number * 100.0, toAppendTo, pos);
			}

			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
				return inner.format(number * 100.0, toAppendTo, pos);
			}
		};
	}

	@Override
	public String getPattern() {
		return _format.getPattern();
	}

}
