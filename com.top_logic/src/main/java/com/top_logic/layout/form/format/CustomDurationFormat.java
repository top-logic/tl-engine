/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.basic.format.PatternBasedFormatDefinition;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * Time duration format for short time durations up to 24h that can be customized through a pattern.
 */
@InApp
@TargetType(value = TLTypeKind.INT)
@Label("Custom time duration")
public class CustomDurationFormat extends PatternBasedFormatDefinition<CustomDurationFormat.Config<?>> {

	/**
	 * Configuration options for {@link CustomDurationFormat}.
	 */
	public interface Config<I extends CustomDurationFormat> extends PatternBasedFormatDefinition.Config<I> {
		/**
		 * The pattern for the time duration.
		 * 
		 * <p>
		 * The pattern may consist of the following letters (all other characters from {@code 'A'}
		 * to {@code 'Z'} and from {@code 'a'} to {@code 'z'} are reserved):
		 * </p>
		 * 
		 * <blockquote>
		 * <table class="striped">
		 * <caption style="display:none">Chart shows pattern letters, date/time component,
		 * presentation, and examples.</caption>
		 * 
		 * <thead>
		 * <tr>
		 * <th scope="col" style="text-align:left">Letter</th>
		 * <th scope="col" style="text-align:left">Time Component</th>
		 * <th scope="col" style="text-align:left">Presentation</th>
		 * </tr>
		 * </thead>
		 * 
		 * <tbody>
		 * <tr>
		 * <th scope="row">{@code H}</th>
		 * <td>Hours (0-23)</td>
		 * <td><a href="#number">Number</a></td>
		 * </tr>
		 * <tr>
		 * <th scope="row">{@code m}</th>
		 * <td>Minutes (0-59)</td>
		 * <td><a href="#number">Number</a></td>
		 * </tr>
		 * <tr>
		 * <th scope="row">{@code s}</th>
		 * <td>Seconds (0-59)</td>
		 * <td><a href="#number">Number</a></td>
		 * </tr>
		 * <tr>
		 * <th scope="row">{@code S}</th>
		 * <td>Milliseconds (0-999)</td>
		 * <td><a href="#number">Number</a></td>
		 * </tr>
		 * </tbody>
		 * </table>
		 * </blockquote>
		 * 
		 * <p>
		 * Pattern letters are usually repeated, as their number determines the exact presentation.
		 * For formatting, the number of pattern letters is the minimum number of digits, and
		 * shorter numbers are zero-padded to this amount. For parsing, the number of pattern
		 * letters is ignored unless it's needed to separate two adjacent fields.
		 * </p>
		 * 
		 * <p>
		 * The pattern <code>HH:mm:ss</code> represents a duration of at most 24h in hours, minutes
		 * and seconds.
		 * </p>
		 * 
		 * @implNote See {@link SimpleDateFormat}.
		 */
		@Override
		String getPattern();
	}

	/**
	 * Creates a {@link CustomDurationFormat} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomDurationFormat(InstantiationContext context, Config<?> config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		String pattern = getConfig().getPattern();

		return new Format() {
			SimpleDateFormat _inner;
			
			{
				_inner = new SimpleDateFormat(pattern, locale);

				// Note: Use the fixed time-zone UTC to get consistent values for hours, minutes,
				// and seconds.
				_inner.setTimeZone(TimeZones.UTC);
			}

			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				return _inner.format(new Date(((Number) obj).longValue()), toAppendTo, pos);
			}

			@Override
			public Object parseObject(String source, ParsePosition pos) {
				Date date = (Date) _inner.parseObject(source, pos);
				return Long.valueOf(date == null ? 0L : date.getTime());
			}
		};
	}

}

