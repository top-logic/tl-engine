/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.table.FilterState;

/**
 * {@link FilterState} of a {@link BooleanColumnFilter}: which of the three logical values
 * ({@code true} / {@code false} / no-value) are accepted.
 *
 * <p>
 * The filter is inactive (empty) when nothing or everything is accepted, since neither
 * constrains the rows.
 * </p>
 *
 * @param acceptTrue
 *        Whether {@code true} cells match.
 * @param acceptFalse
 *        Whether {@code false} cells match.
 * @param acceptNull
 *        Whether {@code null} (no value) cells match.
 */
public record BooleanFilterState(boolean acceptTrue, boolean acceptFalse, boolean acceptNull) implements FilterState {

	@Override
	public boolean isEmpty() {
		int selected = (acceptTrue ? 1 : 0) + (acceptFalse ? 1 : 0) + (acceptNull ? 1 : 0);
		return selected == 0 || selected == 3;
	}

}
