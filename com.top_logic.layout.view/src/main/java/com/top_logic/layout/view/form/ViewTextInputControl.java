/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueChangeHandler;

/**
 * Lean text input control that renders via the {@code TLTextInput} React component.
 *
 * <p>
 * Works with plain {@link String} values instead of {@code FormField} objects.
 * </p>
 */
public class ViewTextInputControl extends ReactControl implements ValueChangeHandler {

	/** State key for the field value. */
	private static final String VALUE = "value";

	/** State key for whether the field is editable. */
	private static final String EDITABLE = "editable";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(ViewFieldValueChanged.INSTANCE);

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewTextInputControl}.
	 *
	 * @param value
	 *        The initial text value, may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewTextInputControl(String value, boolean editable) {
		super(null, "TLTextInput", COMMANDS);
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets the text value.
	 *
	 * @param value
	 *        The new value, may be {@code null}.
	 */
	public void setValue(String value) {
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

	@Override
	public void handleValueChanged(Object rawValue) {
		String newValue = rawValue != null ? rawValue.toString() : null;
		if (_valueCallback != null) {
			_valueCallback.valueChanged(newValue);
		}
	}
}
