/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.text;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.TimeZones;

/**
 * Extension of {@link AbstractMessageFormat} that sets the time zone for created
 * {@link DateFormat}, and tries to find {@link Format formatter} for custom pattern.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLMessageFormat extends AbstractMessageFormat {

	/**
	 * Creates a {@link TLMessageFormat}.
	 * 
	 */
	public TLMessageFormat(String pattern, Locale locale) {
		super(pattern, locale);
	}

	@Override
	protected DateFormat createDateFormat(int style) {
		DateFormat dateFormat = super.createDateFormat(style);
		dateFormat.setTimeZone(TimeZones.systemTimeZone());
		return dateFormat;
	}

	@Override
	protected DateFormat createTimeFormat(int style) {
		DateFormat timeFormat = super.createTimeFormat(style);
		timeFormat.setTimeZone(ThreadContext.getTimeZone());
		return timeFormat;
	}

	@Override
	protected DateFormat createDateTimeInstance(int timeStyle, int dateStyle) {
		DateFormat dateTimeFormat = super.createDateTimeInstance(timeStyle, dateStyle);
		dateTimeFormat.setTimeZone(ThreadContext.getTimeZone());
		return dateTimeFormat;
	}

	@Override
	protected SimpleDateFormat createSimpleDateFormat(String pattern) throws IllegalArgumentException {
		SimpleDateFormat dateFormat = super.createSimpleDateFormat(pattern);
		dateFormat.setTimeZone(TimeZones.systemTimeZone());
		return dateFormat;
	}

	@Override
	protected Format createCustomFormat(int oldMaxOffset, String formatPattern) {
		Format format = getConfiguredFormat(formatPattern);
		if (format != null) {
			return format;
		}
		return super.createCustomFormat(oldMaxOffset, formatPattern);
	}

	private Format getConfiguredFormat(String formatPattern) {
		Formatter formatter = FormatterService.getFormatter(ThreadContext.getTimeZone(), getLocale());
		Format format = formatter.getFormat(formatPattern);
		if (format != null) {
			return format;
		}
		// May have leading or trailing blanks.
		return formatter.getFormat(formatPattern.trim());
	}

}

