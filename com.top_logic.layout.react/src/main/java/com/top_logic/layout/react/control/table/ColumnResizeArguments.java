/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link TableViewControl} {@code columnResize} command: which column to
 * resize and its new width.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Resize column '{column}' to {width}")
public interface ColumnResizeArguments extends ReactCommandArguments {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #getWidth() */
	String WIDTH = "width";

	/**
	 * The name of the column to resize.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * The new column width in pixels.
	 */
	@Name(WIDTH)
	@Mandatory
	int getWidth();

}
