/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * Persistence port for the {@link NamedFilter}s a user saved for a table.
 *
 * <p>
 * Mirrors {@link ViewStateStore}: a table's filters are loaded and saved as a whole, keyed by
 * {@link TableId}, and the {@link FilterCodec} the owning table supplies (de)serializes the column
 * states, so the store never has to know a column's value type. Only
 * {@link NamedFilter.Origin#SAVED} filters are persisted - a declared one is part of the table
 * definition, not of a user's data.
 * </p>
 */
public interface NamedFilterStore {

	/**
	 * Loads the filters the user saved for the given table, empty if there are none.
	 *
	 * @param codec
	 *        The column-aware codec used to restore the persisted column states.
	 */
	List<NamedFilter> load(TableId id, FilterCodec codec);

	/**
	 * Persists the given filters as the user's saved filters for the given table, replacing the
	 * ones stored before.
	 *
	 * @param codec
	 *        The column-aware codec used to serialize the column states.
	 */
	void save(TableId id, List<NamedFilter> filters, FilterCodec codec);

}
