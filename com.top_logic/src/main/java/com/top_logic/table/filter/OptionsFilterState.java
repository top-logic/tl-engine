/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.Set;

import com.top_logic.table.FilterState;

/**
 * {@link FilterState} of an {@link OptionsColumnFilter}: the set of selected option values.
 *
 * @param selected
 *        The selected option values; a row matches if its value is in this set.
 */
public record OptionsFilterState(Set<Object> selected) implements FilterState {

	/**
	 * Creates an {@link OptionsFilterState} with a defensive, immutable copy.
	 */
	public OptionsFilterState {
		selected = Set.copyOf(selected);
	}

	@Override
	public boolean isEmpty() {
		return selected.isEmpty();
	}

}
