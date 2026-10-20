/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.table.FilterState;

/**
 * The applied state of a {@link ScriptedFilter}: the filter-parameter object (a snapshot of the
 * dialog form's transient instance) plus the resolved values of any configured extra inputs, the
 * variables of the match expression besides the cell value and the row.
 *
 * @param params
 *        The filter-parameter object the match expression reads.
 * @param inputs
 *        The resolved values of the configured extra inputs, in order.
 */
public record ScriptedFilterState(Object params, Object[] inputs) implements FilterState {

	@Override
	public boolean isEmpty() {
		return false;
	}

}
