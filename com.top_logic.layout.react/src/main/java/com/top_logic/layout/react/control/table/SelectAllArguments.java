/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link TableViewControl} {@code selectAll} command: whether the header
 * checkbox selects all data rows or clears the selection.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select all rows: {selected}")
public interface SelectAllArguments extends ReactCommand {

	/** @see #isSelected() */
	String SELECTED = "selected";

	/**
	 * {@code true} to select all data rows, {@code false} to clear the selection.
	 */
	@Name(SELECTED)
	boolean isSelected();

}
