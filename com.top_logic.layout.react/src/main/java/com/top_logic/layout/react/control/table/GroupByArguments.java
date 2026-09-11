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
 * Typed arguments of the {@link ReactColumnSelectControl} group-by command: which of the offered
 * columns the table is to group its rows by.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Group by column '{column}'")
public interface GroupByArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = GroupArguments.COLUMN;

	/**
	 * The name of the column to group by, {@code null} or empty to group by none.
	 */
	@Name(COLUMN)
	@Nullable
	String getColumn();

}
