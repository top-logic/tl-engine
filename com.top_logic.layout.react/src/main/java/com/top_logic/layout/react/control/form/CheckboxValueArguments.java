/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of a checkbox's {@link ReactFormFieldControl#CMD_VALUE_CHANGED} command: the new
 * checked state.
 *
 * <p>
 * A checkbox's value is a {@code boolean}, not the text the base field assumes — hence its own typed
 * arguments. The recorder-step rendering is supplied by {@link ReactCheckboxControl} from the
 * field's name and this state, not from a {@code @Label} template.
 * </p>
 */
public interface CheckboxValueArguments extends ReactCommandArguments {

	/** @see #isChecked() */
	String VALUE = "value";

	/**
	 * The new checked state of the checkbox.
	 */
	@Name(VALUE)
	boolean isChecked();

}
