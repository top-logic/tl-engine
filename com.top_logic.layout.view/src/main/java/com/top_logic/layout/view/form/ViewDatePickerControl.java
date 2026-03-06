/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Map;

import com.top_logic.layout.react.ReactCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;

/**
 * Lean date picker control that renders via the {@code TLDatePicker} React component.
 *
 * <p>
 * Works with plain date values (typically date strings) instead of {@code FormField} objects. Date
 * parsing is deferred to the consumer of the value callback.
 * </p>
 */
public class ViewDatePickerControl extends ReactControl {

	/** State key for the field value. */
	private static final String VALUE = "value";

	/** State key for whether the field is editable. */
	private static final String EDITABLE = "editable";

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewDatePickerControl}.
	 *
	 * @param value
	 *        The initial date value (typically a date string), may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewDatePickerControl(Object value, boolean editable) {
		super(null, "TLDatePicker");
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets the date value.
	 *
	 * @param value
	 *        The new value, may be {@code null}.
	 */
	public void setValue(Object value) {
		putState(VALUE, value);
	}

	/**
	 * Sets whether this field is editable.
	 *
	 * @param editable
	 *        {@code true} for editable, {@code false} for read-only.
	 */
	public void setEditable(boolean editable) {
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets the callback that is invoked when the value changes from the client.
	 *
	 * @param callback
	 *        The callback, or {@code null} to remove.
	 */
	public void setValueCallback(ValueCallback callback) {
		_valueCallback = callback;
	}

	/**
	 * Handles value change events from the client.
	 */
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		if (_valueCallback != null) {
			_valueCallback.valueChanged(arguments.get(VALUE));
		}
	}
}
