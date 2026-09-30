/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link TableViewControl} group command: which column the table groups its
 * rows by.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Group by column '{column}'")
public interface GroupArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/**
	 * The name of the column to group the rows by, {@code null} or empty to show the rows
	 * ungrouped.
	 */
	@Name(COLUMN)
	@Nullable
	String getColumn();

	/** @see #getColumn() */
	void setColumn(String value);

}
