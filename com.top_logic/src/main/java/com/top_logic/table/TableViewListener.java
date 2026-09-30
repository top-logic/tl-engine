/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Notified of incremental changes to a {@link TableView}, so a UI tier can patch its
 * rendering without a full rebuild.
 *
 * <p>
 * All methods have no-op defaults; observers override only the events they care about.
 * </p>
 */
public interface TableViewListener {

	/**
	 * The set, order, widths, sort or frozen state of the columns changed.
	 */
	default void columnsChanged() {
		// Optional.
	}

	/**
	 * The total {@link TableView#rowCount() row count} changed.
	 */
	default void rowCountChanged() {
		// Optional.
	}

	/**
	 * The displayed rows in the half-open range {@code [from, to)} changed.
	 */
	default void rowsChanged(int from, int to) {
		// Optional.
	}

	/**
	 * The filtering of the table changed: its column filters, its search term, or which
	 * {@link TableView#namedFilters() named filters} it offers.
	 *
	 * <p>
	 * The {@link TableView#activeNamedFilter() active named filter} is derived from those
	 * criteria, so it may have changed as well: applying, editing or clearing a filter can make
	 * another one active, or none at all. When this is called, the {@link TableView} already
	 * reports the new state.
	 * </p>
	 */
	default void filterChanged() {
		// Optional.
	}

	/**
	 * The selection changed.
	 */
	default void selectionChanged() {
		// Optional.
	}

}
