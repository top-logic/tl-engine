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
 * Typed arguments of the {@link TableViewControl} {@code expand} command: which tree/group row to
 * expand or collapse, and the target state.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Set row {rowIndex} expanded to {expanded}")
public interface ExpandArguments extends ReactCommand {

	/** @see #getRowIndex() */
	String ROW_INDEX = SelectRowArguments.ROW_INDEX;

	/** @see #isExpanded() */
	String EXPANDED = "expanded";

	/**
	 * Zero-based index of the tree/group row to expand or collapse.
	 */
	@Name(ROW_INDEX)
	@Mandatory
	int getRowIndex();

	/**
	 * {@code true} to expand the row, {@code false} to collapse it.
	 */
	@Name(EXPANDED)
	@Mandatory
	boolean isExpanded();

}
