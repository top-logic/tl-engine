/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Column-aware (de)serialization of a single column's {@link FilterState} to and from a JSON value
 * model, supplied by the view tier so a {@link ViewStateStore} can persist filters without knowing
 * the column types.
 *
 * <p>
 * The state-only {@link com.top_logic.table.impl.TableViewStateCodec} cannot serialize a
 * {@link FilterState} on its own - the bound value type (and any parser/option identity) lives on
 * the column's {@link ColumnFilter}. The owning {@code TableView} therefore provides this codec,
 * delegating per state to {@link ColumnFilter#toJson(FilterState)} /
 * {@link ColumnFilter#fromJson(Object)} and applying {@link NegatedFilterState inversion}
 * generically.
 * </p>
 */
public interface FilterCodec {

	/** A codec that persists nothing (used when a table has no personalization columns). */
	FilterCodec NONE = new FilterCodec() {
		@Override
		public Object toJson(String column, FilterState state) {
			return null;
		}

		@Override
		public FilterState fromJson(String column, Object json) {
			return null;
		}
	};

	/**
	 * Serializes the given column's filter state to a JSON value model, or {@code null} if it is not
	 * persistable.
	 */
	Object toJson(String column, FilterState state);

	/**
	 * Reconstructs a column's filter state from a value previously produced by
	 * {@link #toJson(String, FilterState)}, or {@code null} if it cannot be restored.
	 */
	FilterState fromJson(String column, Object json);

}
