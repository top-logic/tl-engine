/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link TableViewControl} {@code select} command: which row to select and
 * the modifier keys that turn the gesture into a toggle or range selection.
 */
public interface SelectRowArguments extends ReactCommandArguments {

	/** @see #getRowIndex() */
	String ROW_INDEX = "rowIndex";

	/** @see #isCtrlKey() */
	String CTRL_KEY = "ctrlKey";

	/** @see #isShiftKey() */
	String SHIFT_KEY = "shiftKey";

	/**
	 * The zero-based index of the row to select, as projected in the table's {@code rows}.
	 */
	@Name(ROW_INDEX)
	@Mandatory
	int getRowIndex();

	/**
	 * Whether to toggle the row's selection (multi-select), as a {@code Ctrl}-click does.
	 */
	@Name(CTRL_KEY)
	boolean isCtrlKey();

	/**
	 * Whether to extend the selection as a range from the anchor (multi-select), as a
	 * {@code Shift}-click does.
	 */
	@Name(SHIFT_KEY)
	boolean isShiftKey();

}
