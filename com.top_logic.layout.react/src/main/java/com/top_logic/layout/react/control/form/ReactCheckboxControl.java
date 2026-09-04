/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;

/**
 * A {@link ReactFormFieldControl} for checkbox fields.
 */
public class ReactCheckboxControl extends ReactFormFieldControl {

	/** State key telling the client that the checkbox has a third, "no value" state. */
	private static final String TRI_STATE = "triState";

	private final boolean _triState;

	/**
	 * Creates a two-valued {@link ReactCheckboxControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model) {
		this(context, model, false);
	}

	/**
	 * Creates a {@link ReactCheckboxControl}.
	 *
	 * @param triState
	 *        Whether the field has a third state for "no value" (a tri-state boolean). The client
	 *        then shows an unset checkbox as indeterminate and cycles through the states on click:
	 *        checked, unchecked, unset - the order the classic UI uses.
	 * @see #ReactCheckboxControl(ReactContext, FieldModel)
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model, boolean triState) {
		super(context, model, "TLCheckbox");
		_triState = triState;
		if (triState) {
			putState(TRI_STATE, Boolean.TRUE);
		}
	}

	/**
	 * Handles a checkbox toggle: its value is a boolean, so it has its own typed arguments rather
	 * than the base field's text value.
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleChecked(CheckboxValueArguments args) {
		clientValueChanged(parseClientValue(args.getChecked()));
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (_triState && rawValue == null) {
			// The third state is the absence of a value, not "false".
			return null;
		}
		return Boolean.valueOf(Boolean.TRUE.equals(rawValue));
	}

}
