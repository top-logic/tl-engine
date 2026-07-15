/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Renders a typed cell value into a UI-neutral {@link CellContent}.
 *
 * <p>
 * Defined in the model tier so that {@link Column} stays toolkit-agnostic; a UI adapter
 * maps the resulting {@link CellContent} onto a concrete control.
 * </p>
 *
 * @param <V>
 *        The cell value type.
 */
@FunctionalInterface
public interface CellRenderer<V> {

	/**
	 * Describes how to display the given cell value.
	 */
	CellContent render(V value);

}
