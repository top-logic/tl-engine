/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Set;

/**
 * The current selection: a {@link SelectionMode} and the set of selected {@link Row#key()
 * row keys}.
 *
 * @param mode
 *        Whether single or multiple selection is allowed.
 * @param keys
 *        The keys of the currently selected rows.
 */
public record Selection(SelectionMode mode, Set<Object> keys) {

	/**
	 * Creates a {@link Selection} with a defensive, immutable copy of the key set.
	 */
	public Selection {
		keys = Set.copyOf(keys);
	}

	/**
	 * An empty selection for the given mode.
	 */
	public static Selection none(SelectionMode mode) {
		return new Selection(mode, Set.of());
	}

}
