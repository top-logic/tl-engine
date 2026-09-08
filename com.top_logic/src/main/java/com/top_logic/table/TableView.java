/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

import com.top_logic.table.filter.TextFilterState;

/**
 * The single binding object the UI tier talks to: composes a column model, a
 * {@link RowSource} and a {@link TableViewState} into a windowed, command-driven view.
 *
 * <p>
 * It answers exactly what a viewport needs - column descriptors, a row count, a row
 * window and per-cell content - and accepts the small set of commands a UI issues,
 * which it implements by mutating the {@link TableViewState} and re-deriving the
 * {@link RowSource}. Incremental changes are reported through {@link TableViewListener}.
 * The {@link #state()} is both persisted and used to seed the client.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public interface TableView<R> {

	// ---- structure ----

	/**
	 * The visible columns, in display order.
	 */
	List<ColumnView> columns();

	/**
	 * The number of leading frozen (fixed) columns.
	 */
	int frozenColumnCount();

	/**
	 * All columns the table can display - the {@link #columns() visible} ones in display order,
	 * followed by the hidden ones - each flagged whether it is currently displayed.
	 *
	 * <p>
	 * This is what a column selection UI offers; the choice it produces is applied through
	 * {@link #setColumnOrder(List)}.
	 * </p>
	 */
	List<ColumnOption> columnOptions();

	/**
	 * The columns as the table was defined: those displayed without any personalization, in
	 * declaration order.
	 *
	 * <p>
	 * This is the arrangement a column selection UI restores when the user resets their
	 * personalization, and the state a table starts from before anything is persisted. It does not
	 * cover columns a table only offers - those stay hidden until the user selects them, exactly as
	 * on a first visit.
	 * </p>
	 */
	List<String> defaultColumnOrder();

	// ---- data window ----

	/**
	 * The total number of displayed rows.
	 */
	int rowCount();

	/**
	 * The displayed rows in the half-open index range {@code [from, to)}.
	 */
	List<Row<R>> rows(int from, int to);

	/**
	 * The content of the given row's cell in the named column.
	 */
	CellContent cell(Row<R> row, String column);

	/**
	 * The filter of the named column, or {@code null} if the column is not filterable. Used
	 * by a UI tier (together with {@link #columnMatchCounts} and the current
	 * {@link #state()}) to build a filter editor.
	 */
	ColumnFilter<?> columnFilter(String column);

	/**
	 * Facet counts for the named column, or {@link MatchCounts#NONE} if unavailable.
	 */
	MatchCounts columnMatchCounts(String column);

	// ---- named filters ----

	/**
	 * The filter criteria this table offers under a name: the ones its definition
	 * {@link NamedFilter.Origin#DECLARED declares} first, then the ones the user
	 * {@link #saveNamedFilter(String) saved}.
	 */
	List<NamedFilter> namedFilters();

	/**
	 * The {@link #namedFilters() named filter} whose criteria the table is filtered by, or
	 * {@code null} if it is filtered by none of them.
	 *
	 * <p>
	 * The result is derived from the live filter state, not remembered: it is the first named
	 * filter {@link NamedFilter#matches(java.util.Map, TextFilterState) matching} the current
	 * column filters and search term, so it is found as well when the user reached those criteria
	 * through the filter editors, and it is gone as soon as they change any of them.
	 * </p>
	 */
	NamedFilter activeNamedFilter();

	/**
	 * Filters the table by exactly the criteria of the {@link #namedFilters() named filter} with
	 * the given {@link NamedFilter#id() identifier}.
	 *
	 * <p>
	 * The named filter replaces the whole filter: a column it does not mention ends up unfiltered,
	 * and its search term becomes the table's - applying it means what it says instead of narrowing
	 * whatever was set before. A criterion for a column this table does not have, or does not
	 * filter by, is dropped. An identifier no named filter has leaves the table as it is.
	 * </p>
	 */
	void applyNamedFilter(String id);

	/**
	 * Saves the current column filters and search term as a {@link NamedFilter} of the user's own,
	 * under the given name.
	 *
	 * <p>
	 * Saving under the name of an existing saved filter replaces that filter's criteria, keeping
	 * its {@link NamedFilter#id() identifier}.
	 * </p>
	 *
	 * @param name
	 *        The free-text name the user typed.
	 * @return The saved filter, or {@code null} if this table persists no filters of its own (then
	 *         it offers only the declared ones).
	 */
	NamedFilter saveNamedFilter(String name);

	/**
	 * Deletes the {@link NamedFilter.Origin#SAVED saved} filter with the given
	 * {@link NamedFilter#id() identifier}.
	 *
	 * <p>
	 * A declared filter is part of the table definition and cannot be deleted, so an identifier
	 * naming one - like an identifier naming nothing - leaves the offered filters as they are.
	 * </p>
	 */
	void deleteNamedFilter(String id);

	// ---- commands (UI -> model) ----

	/**
	 * Applies a new sort order.
	 */
	void sort(SortSpec spec);

	/**
	 * Applies (or clears, when {@link FilterState#isEmpty() empty}) the filter of one
	 * column.
	 */
	void filter(String column, FilterState state);

	/**
	 * Applies (or clears, when the term is {@code null} or {@link TextFilterState#isEmpty()
	 * empty}) the cross-column free-text search.
	 *
	 * <p>
	 * The search examines the columns that are currently displayed, so it finds what the user
	 * can see: a row is displayed when the term occurs in the
	 * {@link Column#searchText(Object) searchable text} of at least one of them. It narrows
	 * the rows in addition to the active column {@link #filter(String, FilterState) filters}.
	 * </p>
	 *
	 * @param term
	 *        The text pattern together with its matching flags.
	 */
	void search(TextFilterState term);

	/**
	 * Applies a new grouping.
	 */
	void group(GroupSpec spec);

	/**
	 * Moves a column to a new visible index.
	 */
	void moveColumn(String column, int toIndex);

	/**
	 * Replaces the displayed columns and their order in one step.
	 *
	 * <p>
	 * This is the bulk form of {@link #moveColumn(String, int)} and
	 * {@link #setColumnVisible(String, boolean)} for a column selection UI, which decides
	 * visibility and order together: applying the result column by column would persist and
	 * report every intermediate arrangement.
	 * </p>
	 *
	 * @param columns
	 *        The columns to display, in display order. Names that are no columns of this table,
	 *        and repetitions, are ignored.
	 */
	void setColumnOrder(List<String> columns);

	/**
	 * Sets the display width of a column.
	 */
	void resizeColumn(String column, int width);

	/**
	 * Shows or hides a column.
	 */
	void setColumnVisible(String column, boolean visible);

	/**
	 * Sets the number of leading frozen (fixed) columns.
	 */
	void setFrozenColumnCount(int count);

	/**
	 * Expands or collapses the row with the given {@link Row#key() key}.
	 */
	void setExpanded(Object rowKey, boolean expanded);

	/**
	 * Replaces the current selection.
	 */
	void select(Selection selection);

	/**
	 * Sets the paging window ({@code pageSize == }{@link TableViewState#SHOW_ALL} disables
	 * paging).
	 */
	void window(int page, int pageSize);

	// ---- change notification (model -> UI) ----

	/**
	 * Registers an incremental-change listener.
	 */
	void addListener(TableViewListener listener);

	/**
	 * Unregisters a previously {@link #addListener(TableViewListener) registered} listener.
	 */
	void removeListener(TableViewListener listener);

	// ---- persistence / client seed ----

	/**
	 * The current, serializable view state (for persistence and to seed the client).
	 */
	TableViewState state();

}
