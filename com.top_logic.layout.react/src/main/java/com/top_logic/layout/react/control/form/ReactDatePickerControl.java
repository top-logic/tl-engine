/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * A {@link ReactFormFieldControl} for date picker fields.
 *
 * <p>
 * The HTML {@code <input type="date">} exchanges its value as an ISO {@code yyyy-MM-dd} string
 * (locale-independent). This control translates that string into a {@link Date} on the way in - so
 * the field model (and therefore persistence) receives a typed value rather than a raw
 * {@link String} - and formats the model {@link Date} back to ISO on the way out, so an existing
 * value populates the input. Without this translation the model would receive a {@link String} and
 * storing it fails (the date storage mapping cannot convert a {@link String} to a {@link Date}).
 * </p>
 *
 * <p>
 * In addition to the ISO {@link #VALUE} (consumed by the edit-mode HTML input), the control emits a
 * {@link #DISPLAY_VALUE} holding the date formatted in the current user's locale and time zone (via
 * {@link HTMLFormatter}). The React component shows this localized string in view (read-only) mode,
 * so a date reads as e.g. {@code 01.06.2026} (German) rather than the locale-independent ISO form or
 * the raw Java {@link Date#toString()}.
 * </p>
 */
public class ReactDatePickerControl extends ReactFormFieldControl {

	/** ISO date pattern used by the HTML {@code <input type="date">} (locale-independent). */
	private static final String ISO_DATE = "yyyy-MM-dd";

	/** State key for the localized, view-mode display string of the date. */
	private static final String DISPLAY_VALUE = "displayValue";

	/**
	 * Creates a new {@link ReactDatePickerControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactDatePickerControl(ReactContext context, FieldModel model) {
		super(context, model, "TLDatePicker");
		// The base constructor seeded the raw Date into the value state; re-emit it as an ISO
		// string so the date input can display the initial value, plus a localized string for
		// the read-only (view-mode) display.
		putState(VALUE, formatIso(model.getValue()));
		putState(DISPLAY_VALUE, formatLocalized(model.getValue()));
	}

	@Override
	@ReactCommand(CMD_VALUE_CHANGED)
	void handleValueChanged(FieldValueArguments args) {
		Object rawValue = args.getValue();
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
		// Emit an ISO string so the <input type="date"> can display the value, plus a localized
		// string for the read-only (view-mode) display.
		putState(VALUE, formatIso(newValue));
		putState(DISPLAY_VALUE, formatLocalized(newValue));
	}

	private static String formatIso(Object value) {
		if (value instanceof Date) {
			return CalendarUtil.newSimpleDateFormat(ISO_DATE, Locale.ROOT).format((Date) value);
		}
		return null;
	}

	private static String formatLocalized(Object value) {
		if (value instanceof Date) {
			return HTMLFormatter.getInstance().getDateFormat().format((Date) value);
		}
		return null;
	}

	private static Date parseIso(String value) {
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat(ISO_DATE, Locale.ROOT);
		format.setLenient(false);
		try {
			return format.parse(value.trim());
		} catch (ParseException ex) {
			return null;
		}
	}

}
