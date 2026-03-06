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
 * Lean checkbox control that renders via the {@code TLCheckbox} React component.
 *
 * <p>
 * Works with plain {@link Boolean} values instead of {@code FormField} objects.
 * </p>
 */
public class ViewCheckboxControl extends ReactControl implements ValueChangeHandler {

	/** State key for the field value. */
	private static final String VALUE = "value";

	/** State key for whether the field is editable. */
	private static final String EDITABLE = "editable";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(ViewFieldValueChanged.INSTANCE);

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewCheckboxControl}.
	 *
	 * @param value
	 *        The initial boolean value.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewCheckboxControl(boolean value, boolean editable) {
		super(null, "TLCheckbox", COMMANDS);
		putState(VALUE, Boolean.valueOf(value));
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets the boolean value.
	 *
	 * @param value
	 *        The new value.
	 */
	public void setValue(boolean value) {
		putState(VALUE, Boolean.valueOf(value));
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
		Boolean newValue = (Boolean) rawValue;
		if (_valueCallback != null) {
			_valueCallback.valueChanged(newValue);
		}
	}
}
