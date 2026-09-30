/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link FormatDefinition} that allows to specify a separate format for parsing the value entered
 * by a user.
 */
@Label("Format with separate parser")
public class NormalizingFormatDefinition<C extends NormalizingFormatDefinition.Config<?>> extends FormatDefinition<C> {

	/**
	 * Configuration options for {@link NormalizingFormatDefinition}.
	 */
	@TagName("normalizing-format")
	public interface Config<I extends NormalizingFormatDefinition<?>> extends FormatDefinition.Config<I> {

		/**
		 * The format used to format objects for display.
		 */
		PolymorphicConfiguration<FormatDefinition<?>> getFormat();

		/**
		 * The format used to parse inputs, if {@link #getFormat()} fails to parse the input.
		 * 
		 * <p>
		 * The format can be specified as alternative (simplified) format to make user input more
		 * convenient. E.g. when formatting a value with a currency symbol, it is best not to expect
		 * the user to enter that symbol, but complete the value with the currency symbol after the
		 * user has entered the plain number. To accomplish that, use a format with a currenc symbol
		 * and a parsing format without it.
		 * </p>
		 */
		PolymorphicConfiguration<FormatDefinition<?>> getParser();

	}

	private FormatDefinition<?> _format;

	private FormatDefinition<?> _parser;

	/**
	 * Creates a {@link NormalizingFormatDefinition}.
	 */
	public NormalizingFormatDefinition(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);

		_format = context.getInstance(config.getFormat());
		_parser = context.getInstance(config.getParser());
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		Format parser = _parser.newFormat(globalConfig, timeZone, locale);
		Format format = _format.newFormat(globalConfig, timeZone, locale);

		return new Format() {
			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				return format.format(obj, toAppendTo, pos);
			}

			@Override
			public Object parseObject(String source, ParsePosition pos) {
				Object result = format.parseObject(source, pos);
				if (pos.getErrorIndex() >= 0) {
					pos.setIndex(0);
					pos.setErrorIndex(-1);
					result = parser.parseObject(source, pos);
					if (pos.getErrorIndex() < 0) {
						// Accept, even if not all content has been parsed. The allows to skip
						// appended units not understood by the simplified parsing format.
						pos.setIndex(source.length());
					}
				} else {
					// Accept, even if not all content has been parsed. The allows to skip
					// appended units not understood by the simplified parsing format.
					pos.setIndex(source.length());
				}
				return result;
			}
		};
	}

	@Override
	public String getPattern() {
		return _format.getPattern();
	}

}
