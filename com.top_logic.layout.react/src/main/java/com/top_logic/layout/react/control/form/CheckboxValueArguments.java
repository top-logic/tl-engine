/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of a checkbox's {@link ReactFormFieldControl#CMD_VALUE_CHANGED} command: the new
 * checked state.
 *
 * <p>
 * A checkbox's value is a boolean, not the text the base field assumes — hence its own typed
 * arguments. It is the boxed {@link Boolean}, because a
 * {@link ReactCheckboxControl#ReactCheckboxControl(com.top_logic.layout.react.ReactContext,
 * com.top_logic.layout.form.model.FieldModel, boolean) tri-state checkbox} also has a state for
 * "no value".
 * </p>
 */
@Label("Set '{target}' to '{value}'")
public interface CheckboxValueArguments extends ReactCommand {

	/** @see #getChecked() */
	String VALUE = "value";

	/**
	 * The new checked state of the checkbox, {@code null} for the tri-state "no value".
	 */
	@Name(VALUE)
	Boolean getChecked();

}
