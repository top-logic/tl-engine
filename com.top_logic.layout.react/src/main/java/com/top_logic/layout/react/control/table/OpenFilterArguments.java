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
 * Typed arguments of the {@link TableViewControl} {@code openFilter} command: the column whose
 * filter to open in the popover at its funnel.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Open filter for column '{column}'")
public interface OpenFilterArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/**
	 * The name of the column whose filter to open.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

}
