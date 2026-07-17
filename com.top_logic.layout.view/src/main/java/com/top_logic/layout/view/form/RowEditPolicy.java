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
	 */
	SELECTED;

}
