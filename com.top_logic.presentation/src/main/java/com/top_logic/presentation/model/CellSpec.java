/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * A cell of a {@link TableSpec}.
 *
 * @param text
 *        The text of the cell, <code>null</code> for an empty cell.
 * @param fill
 *        The fill, <code>null</code> for the fill of the table style.
 */
public record CellSpec(TextSpec text, PaintSpec fill) {

	/**
	 * A cell without text.
	 */
	public static final CellSpec EMPTY = new CellSpec(null, null);

}
