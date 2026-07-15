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
 * Typed arguments of the {@link TableViewControl} {@code columnReorder} command: which column to
 * move and the target position.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Move column '{column}' to position {targetIndex}")
public interface ColumnReorderArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #getTargetIndex() */
	String TARGET_INDEX = "targetIndex";

	/**
	 * The name of the column to move.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * Zero-based index to move the column to.
	 */
	@Name(TARGET_INDEX)
	@Mandatory
	int getTargetIndex();

}
