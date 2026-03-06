/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;

/**
 * Lean select/dropdown control that renders via the {@code TLSelect} React component.
 *
 * <p>
 * Works with plain values and a list of option maps instead of {@code FormField} objects. Each
 * option map contains {@code "value"} and {@code "label"} entries.
 * </p>
 */
public class ViewSelectControl extends ReactControl {

	/** State key for the field value. */
	private static final String VALUE = "value";

	/** State key for whether the field is editable. */
	private static final String EDITABLE = "editable";

	/** State key for the list of options. */
	private static final String OPTIONS = "options";

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewSelectControl}.
	 *
	 * @param value
	 *        The initially selected value, may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 * @param options
	 *        The list of option maps. Each map must contain {@code "value"} and {@code "label"}
	 *        entries.
	 */
	public ViewSelectControl(Object value, boolean editable, List<Map<String, Object>> options) {
		super(null, "TLSelect");
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));
		putState(OPTIONS, options);
	}

	/**
	 * Sets the selected value.
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
	 * Updates the list of available options.
	 *
	 * @param options
	 *        The new option maps. Each map must contain {@code "value"} and {@code "label"}
	 *        entries.
	 */
	public void setOptions(List<Map<String, Object>> options) {
		putState(OPTIONS, options);
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
