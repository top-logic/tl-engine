/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

/**
 * The value carried by a {@link ScriptedFilter} column cell: the cell {@code value} together with
 * its owning {@code row}.
 *
 * <p>
 * Pairing both lets the match closure filter by the value with the full row available as context,
 * while keeping the filter free of any knowledge of the column attribute - the column's accessor
 * (which knows the attribute) produces the pair, the filter merely consumes it.
 * </p>
 *
 * @param value
 *        The column's cell value (the attribute value).
 * @param row
 *        The row business object the value was read from.
 */
public record ScriptedCell(Object value, Object row) {
	// Pure data carrier.
}
