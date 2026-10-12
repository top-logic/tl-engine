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
 * Typed arguments of the command {@code columnLock} of the column selection: which column to freeze or to release.
 *
 * <p>
 * The {@link Label} also serves as template for the label provider of the configuration, which shows a recorded step
 * to people.
 * </p>
 */
@Label("Freeze column '{column}': {locked}")
public interface ColumnLockArguments extends ReactCommand {

	/** @see #getColumn() */
	String COLUMN = "column";

	/** @see #isLocked() */
	String LOCKED = "locked";

	/**
	 * The name of the column to freeze or to release.
	 */
	@Name(COLUMN)
	@Mandatory
	String getColumn();

	/**
	 * Whether the column is to stay at the start of the table while it scrolls sideways.
	 */
	@Name(LOCKED)
	@Mandatory
	boolean isLocked();

}
