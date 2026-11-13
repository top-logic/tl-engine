/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Which rows of a table edited within a form are editable while the form is in edit mode.
 */
public enum RowEditPolicy {

	/**
	 * The table is read-only (the default).
	 */
	NONE,

	/**
	 * All rows are editable.
	 */
	ALL,

	/**
	 * Exactly the selected row is editable.
	 *
	 * <p>
	 * Edits of previously selected rows stay buffered on their row overlays and are committed
	 * together when the form is saved.
	 * </p>
	 *
	 * <p>
	 * A row with errors cannot be left: selecting another row - by a click, a key or adding a row
	 * - checks the edited row, reveals its errors and is refused with them while it has any.
	 * Removing the row and cancelling the edit are always possible.
	 * </p>
	 */
	SELECTED;

}
