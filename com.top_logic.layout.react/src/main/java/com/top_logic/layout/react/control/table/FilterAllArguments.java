/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactFilterListControl} {@code all} command: whether to check or
 * to uncheck every value of the list.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Check all filter values: {checked}")
public interface FilterAllArguments extends ReactCommand {

	/** @see #isChecked() */
	String CHECKED = "checked";

	/**
	 * Whether every value is to be checked - shown - or unchecked - hidden.
	 */
	@Name(CHECKED)
	@Mandatory
	boolean isChecked();

}
