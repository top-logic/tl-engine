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
 * Typed arguments of the {@link TableViewControl} {@code sort} command: which column to sort by,
 * the direction, and whether to add to an existing multi-sort.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Sort by '{column}'")
public interface SortArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #getDirection() */
	String DIRECTION = "direction";

	/** @see #getMode() */
	String MODE = "mode";

	/**
	 * The name of the column to sort by.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * Sort direction; {@code "desc"} for descending, otherwise ascending.
	 */
	@Name(DIRECTION)
	String getDirection();

	/**
	 * {@code "add"} to append to the current multi-sort, otherwise replace.
	 */
	@Name(MODE)
	String getMode();

}
