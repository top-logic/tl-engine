/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link TableViewControl} {@code setViewOptions} command: how the table
 * looks for the person using it.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 *
 * @see com.top_logic.table.TableView#setViewOptions(String, boolean, boolean)
 */
@Label("Set view: density {density}, stripes {striped}, column lines {columnLines}")
public interface SetViewOptionsArguments extends ReactCommand {

	/** @see #getDensity() */
	String DENSITY = "density";

	/** @see #isStriped() */
	String STRIPED = "striped";

	/** @see #isColumnLines() */
	String COLUMN_LINES = "columnLines";

	/**
	 * The row density: empty to follow the surrounding application, {@code normal} or
	 * {@code compact}. Any other value, and none, is taken as empty.
	 */
	@Name(DENSITY)
	@StringDefault("")
	String getDensity();

	/**
	 * Whether every other row is tinted.
	 */
	@Name(STRIPED)
	boolean isStriped();

	/**
	 * Whether a line separates the columns.
	 */
	@Name(COLUMN_LINES)
	boolean isColumnLines();

}
