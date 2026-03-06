/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;

/**
 * Lean number input control that renders via the {@code TLNumberInput} React component.
 *
 * <p>
 * Works with plain {@link Number} values instead of {@code FormField} objects. Supports
 * configurable decimal places.
 * </p>
 */
public class ViewNumberInputControl extends ReactControl {

	/** State key for the field value. */
	private static final String VALUE = "value";

	/** State key for whether the field is editable. */
	private static final String EDITABLE = "editable";

	/** State key for the configuration map. */
	private static final String CONFIG = "config";

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewNumberInputControl}.
	 *
	 * @param value
	 *        The initial numeric value, may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 * @param decimalPlaces
	 *        The number of decimal places. When greater than zero, a {@code "decimal"} config key
	 *        is sent to the React component.
	 */
	public ViewNumberInputControl(Number value, boolean editable, int decimalPlaces) {
		super(null, "TLNumberInput");
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));
		if (decimalPlaces > 0) {
			putState(CONFIG, Collections.singletonMap("decimal", Integer.valueOf(decimalPlaces)));
		}
	}

	/**
	 * Sets the numeric value.
	 *
	 * @param value
	 *        The new value, may be {@code null}.
	 */
	public void setValue(Number value) {
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
		Number newValue = (Number) arguments.get(VALUE);
		if (_valueCallback != null) {
			_valueCallback.valueChanged(newValue);
		}
	}
}
