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
 * A checkbox's value is a {@code boolean}, not the text the base field assumes — hence its own typed
 * arguments.
 * </p>
 */
@Label("Set '{target}' to '{value}'")
public interface CheckboxValueArguments extends ReactCommand {

	/** @see #isChecked() */
	String VALUE = "value";

	/**
	 * The new checked state of the checkbox.
	 */
	@Name(VALUE)
	boolean isChecked();

}
