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
 * Typed arguments of the {@code columnMove} command of the {@link ReactColumnSelectControl} and of the
 * {@link TableViewControl} (the table header): which column to move, to which position, and whether it is
 * frozen and displayed afterwards.
 *
 * <p>
 * A drag across the sections of the column selection (frozen, movable, hidden) changes the state of the
 * column and its position in one command. The target index is ignored when the column is to be hidden: a
 * hidden column keeps its place.
 * </p>
 *
 * <p>
 * The table header has no zone for hidden columns: there the client always sends {@link #isVisible() visible}, and a
 * request to hide a column changes nothing. The target index counts the displayed columns without the moved one.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Move column '{column}' to position {targetIndex} (frozen: {locked}, visible: {visible}; no position when hidden)")
public interface ColumnMoveArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #getTargetIndex() */
	String TARGET_INDEX = "targetIndex";

	/** @see #isLocked() */
	String LOCKED = "locked";

	/** @see #isVisible() */
	String VISIBLE = "visible";

	/**
	 * The name of the column to move.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * Zero-based index in the whole column list, without the moved column, to move the column to.
	 * Ignored when the column is to be hidden.
	 */
	@Name(TARGET_INDEX)
	@Mandatory
	int getTargetIndex();

	/**
	 * Whether the column is frozen after the move.
	 */
	@Name(LOCKED)
	@Mandatory
	boolean isLocked();

	/**
	 * Whether the column is displayed after the move.
	 */
	@Name(VISIBLE)
	@Mandatory
	boolean isVisible();

}
