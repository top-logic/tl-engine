/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * A {@link ReactFormFieldControl} for fields holding a point in time: a date, a time of day, or
 * both.
 *
 * <p>
 * The HTML inputs behind these ({@code <input type="date">}, {@code "time"},
 * {@code "datetime-local"}) exchange their value as a locale-independent ISO string. This control
 * translates that string into a {@link Date} on the way in - so the field model (and therefore
 * persistence) receives a typed value rather than a raw {@link String} - and formats the model
 * {@link Date} back to ISO on the way out, so an existing value populates the input. Without this
 * translation the model would receive a {@link String} and storing it fails (the date storage
 * mapping cannot convert a {@link String} to a {@link Date}).
 * </p>
 *
 * <p>
 * In addition to the ISO {@link #VALUE} (consumed by the edit-mode HTML input), the control emits a
 * {@link #DISPLAY_VALUE} holding the value formatted in the current user's locale and time zone (via
 * {@link HTMLFormatter}). The React component shows this localized string in view (read-only) mode,
 * so a date reads as e.g. {@code 01.06.2026} (German) rather than the locale-independent ISO form or
 * the raw Java {@link Date#toString()}. Both forms speak of the same moment: the ISO conversion uses
 * the user's time zone as well, so the value in the input is the one the view mode shows.
 * </p>
 */
public class ReactDatePickerControl extends ReactFormFieldControl {

	/**
	 * Which part of a point in time a field edits.
	 *
	 * <p>
	 * Chosen by the caller from the attribute's type, and decisive for all three representations:
	 * the HTML input the client renders, the ISO form exchanged with it, and the localized form
	 * shown in view mode.
	 * </p>
	 */
	public enum Kind {

		/** A date without a time of day. */
		DATE("date", "yyyy-MM-dd"),

		/** A time of day without a date. */
		TIME("time", "HH:mm", "HH:mm:ss"),

		/** A date with a time of day. */
		DATE_TIME("datetime-local", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss");

		private final String _inputType;

		private final List<String> _patterns;

		private Kind(String inputType, String... patterns) {
			_inputType = inputType;
			_patterns = List.of(patterns);
		}

		/** The {@code type} attribute of the HTML input editing such a value. */
		public String inputType() {
			return _inputType;
		}

		/** The ISO pattern the value is written in. */
		String isoPattern() {
			return _patterns.get(0);
		}

		/**
		 * The patterns accepted when reading a value back, {@link #isoPattern()} first.
		 *
		 * <p>
		 * More than the ISO pattern alone: a browser adds the seconds to a time whose seconds are
		 * not zero, and it is the same value either way.
		 * </p>
		 */
		List<String> parsePatterns() {
			return _patterns;
		}

		/** The format the value is shown in when the field is read-only. */
		public DateFormat displayFormat() {
			return inputFormats().get(0);
		}

		/**
		 * The formats a value may be typed in, e.g. as the bound of a table filter:
		 * {@link #displayFormat()} first, then its shorter variant - someone who leaves the seconds
		 * off a time means the same time.
		 */
		public List<DateFormat> inputFormats() {
			Formatter formatter = HTMLFormatter.getInstance();
			switch (this) {
				case TIME:
					return List.of(formatter.getTimeFormat(), formatter.getShortTimeFormat());
				case DATE_TIME:
					return List.of(formatter.getDateTimeFormat(), formatter.getShortDateTimeFormat());
				case DATE:
				default:
					return List.of(formatter.getDateFormat(), formatter.getShortDateFormat());
			}
		}

	}

	/** State key for the localized, view-mode display string of the value. */
	private static final String DISPLAY_VALUE = "displayValue";

	/** State key naming the HTML input the client renders. */
	private static final String INPUT_TYPE = "inputType";

	private final Kind _kind;

	/**
	 * Creates a {@link ReactDatePickerControl} for a date without a time of day.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactDatePickerControl(ReactContext context, FieldModel model) {
		this(context, model, Kind.DATE);
	}

	/**
	 * Creates a {@link ReactDatePickerControl}.
	 *
	 * @param kind
	 *        Which part of a point in time the field edits.
	 * @see #ReactDatePickerControl(ReactContext, FieldModel)
	 */
	public ReactDatePickerControl(ReactContext context, FieldModel model, Kind kind) {
		super(context, model, "TLDatePicker");
		_kind = kind;
		putState(INPUT_TYPE, kind.inputType());
		// The base constructor seeded the raw Date into the value state; re-emit it as an ISO
		// string so the input can display the initial value, plus a localized string for the
		// read-only (view-mode) display.
		putState(VALUE, formatIso(model.getValue()));
		putState(DISPLAY_VALUE, formatLocalized(model.getValue()));
	}

	@Override
	protected void applyRawClientValue(Object rawValue) {
		FieldModel model = getFieldModel();

		AbstractFieldModel abstractModel =
			model instanceof AbstractFieldModel ? (AbstractFieldModel) model : null;

		if (rawValue == null || rawValue.toString().trim().isEmpty()) {
			if (abstractModel != null) {
				abstractModel.setError(null);
			}
			model.setValue(null);
			return;
		}

		Date parsed = parseIso(rawValue.toString());
		if (parsed == null) {
			// Set error on model so it gets displayed in chrome and as red border on input.
			if (abstractModel != null) {
				abstractModel.setError(I18NConstants.ERROR_INVALID_DATE__VALUE.fill(rawValue.toString()));
			}
			return;
		}
		if (abstractModel != null) {
			abstractModel.setError(null);
		}
		model.setValue(parsed);
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue instanceof Date) {
			return rawValue;
		}
		if (rawValue == null || rawValue.toString().trim().isEmpty()) {
			return null;
		}
		return parseIso(rawValue.toString());
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		// Emit an ISO string so the HTML input can display the value, plus a localized string for
		// the read-only (view-mode) display.
		putState(VALUE, formatIso(newValue));
		putState(DISPLAY_VALUE, formatLocalized(newValue));
	}

	private String formatIso(Object value) {
		if (value instanceof Date) {
			return isoFormat(_kind.isoPattern()).format((Date) value);
		}
		return null;
	}

	private String formatLocalized(Object value) {
		if (value instanceof Date) {
			return _kind.displayFormat().format((Date) value);
		}
		return null;
	}

	private Date parseIso(String value) {
		String trimmed = value.trim();
		for (String pattern : _kind.parsePatterns()) {
			SimpleDateFormat format = isoFormat(pattern);
			format.setLenient(false);
			try {
				return format.parse(trimmed);
			} catch (ParseException ex) {
				// Try the next accepted pattern; reported as an error when none matches.
			}
		}
		return null;
	}

	/**
	 * A format for the locale-independent ISO form exchanged with the HTML input.
	 *
	 * <p>
	 * In the user's time zone, matching the localized display: read in that zone, a value shown as
	 * 14:30 must reach the input as {@code 14:30} and not as the same moment in another zone.
	 * </p>
	 */
	private static SimpleDateFormat isoFormat(String pattern) {
		SimpleDateFormat result = CalendarUtil.newSimpleDateFormat(pattern, Locale.ROOT);
		result.setTimeZone(ThreadContext.getTimeZone());
		return result;
	}

}
