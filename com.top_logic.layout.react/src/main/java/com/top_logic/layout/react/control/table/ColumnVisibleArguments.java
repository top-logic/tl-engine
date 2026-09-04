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
 * Typed arguments of the {@link ReactColumnSelectControl} {@code columnVisible} command: which
 * column to show or hide.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Set column '{column}' visible: {visible}")
public interface ColumnVisibleArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #isVisible() */
	String VISIBLE = "visible";

	/**
	 * The name of the column to show or hide.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * Whether the column is to be displayed.
	 */
	@Name(VISIBLE)
	@Mandatory
	boolean isVisible();

}
