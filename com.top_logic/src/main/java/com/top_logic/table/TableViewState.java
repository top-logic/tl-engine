/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.table.filter.TextFilterState;

/**
 * The serializable, per-user view state of a table: column order and selection, widths, frozen
 * count, sort, filters, the free-text search, grouping, expansion, selection and paging.
 *
 * <p>
 * This single value object is both the personalization payload persisted via a
 * {@link ViewStateStore} and the seed sent to the UI client, replacing the scattered
 * {@code save*}/{@code load*} methods and {@code PagingModel} of the legacy
 * {@code TableViewModel}. It is a mutable holder; the {@link TableView} mutates it in
 * response to UI commands.
 * </p>
 */
public class TableViewState {

	/** Sentinel {@link #getPageSize() page size} meaning "show all rows" / virtual scroll. */
	public static final int SHOW_ALL = 0;

	/** The {@link #getDensity() density} that follows the surrounding application. */
	public static final String DENSITY_DEFAULT = "";

	/** The {@link #getDensity() density} with the usual row height. */
	public static final String DENSITY_NORMAL = "normal";

	/** The {@link #getDensity() density} with reduced row height. */
	public static final String DENSITY_COMPACT = "compact";

	private List<String> _columnOrder = new ArrayList<>();

	private Set<String> _hiddenColumns = new LinkedHashSet<>();

	private Map<String, Integer> _widths = new LinkedHashMap<>();

	private int _frozenCount;

	private List<SortColumn> _sort = new ArrayList<>();

	private Map<String, FilterState> _filters = new LinkedHashMap<>();

	private TextFilterState _search;

	private GroupSpec _grouping = GroupSpec.NONE;

	private Set<Object> _expanded = new LinkedHashSet<>();

	private Selection _selection = Selection.none(SelectionMode.SINGLE);

	private int _pageSize = SHOW_ALL;

	private int _page;

	private String _density = DENSITY_DEFAULT;

	private boolean _striped;

	private boolean _columnLines;

	/**
	 * The visible columns, in display order (by {@link Column#name() name}).
	 */
	public List<String> getColumnOrder() {
		return _columnOrder;
	}

	/**
	 * @see #getColumnOrder()
	 */
	public void setColumnOrder(List<String> columnOrder) {
		_columnOrder = columnOrder;
	}

	/**
	 * The columns the user removed from the display, by {@link Column#name() name}.
	 *
	 * <p>
	 * A column missing from {@link #getColumnOrder()} is not enough to tell apart the two ways a
	 * column can be absent: the user hid it, or it did not yet exist when this state was persisted.
	 * Only the ones named here stay hidden when the state is restored; anything else the table
	 * defines is appended as a new column.
	 * </p>
	 */
	public Set<String> getHiddenColumns() {
		return _hiddenColumns;
	}

	/**
	 * @see #getHiddenColumns()
	 */
	public void setHiddenColumns(Set<String> hiddenColumns) {
		_hiddenColumns = hiddenColumns;
	}

	/**
	 * Per-column display widths in pixels, keyed by column name.
	 */
	public Map<String, Integer> getWidths() {
		return _widths;
	}

	/**
	 * @see #getWidths()
	 */
	public void setWidths(Map<String, Integer> widths) {
		_widths = widths;
	}

	/**
	 * The number of leading columns that are frozen (fixed).
	 */
	public int getFrozenCount() {
		return _frozenCount;
	}

	/**
	 * @see #getFrozenCount()
	 */
	public void setFrozenCount(int frozenCount) {
		_frozenCount = frozenCount;
	}

	/**
	 * The active multi-column sort order, primary first.
	 */
	public List<SortColumn> getSort() {
		return _sort;
	}

	/**
	 * @see #getSort()
	 */
	public void setSort(List<SortColumn> sort) {
		_sort = sort;
	}

	/**
	 * The active filter state per column, keyed by column name.
	 */
	public Map<String, FilterState> getFilters() {
		return _filters;
	}

	/**
	 * @see #getFilters()
	 */
	public void setFilters(Map<String, FilterState> filters) {
		_filters = filters;
	}

	/**
	 * The term of the active cross-column free-text search, or {@code null} for no search.
	 *
	 * <p>
	 * Only the term is held here - the pattern together with its matching flags. Which columns
	 * it examines is derived where the search is applied, from {@link #getColumnOrder()}: the
	 * search looks into the columns the user currently sees, so showing or hiding a column
	 * changes what it finds.
	 * </p>
	 */
	public TextFilterState getSearch() {
		return _search;
	}

	/**
	 * @see #getSearch()
	 */
	public void setSearch(TextFilterState search) {
		_search = search;
	}

	/**
	 * The active grouping.
	 */
	public GroupSpec getGrouping() {
		return _grouping;
	}

	/**
	 * @see #getGrouping()
	 */
	public void setGrouping(GroupSpec grouping) {
		_grouping = grouping;
	}

	/**
	 * The keys of expanded tree nodes / group headers.
	 */
	public Set<Object> getExpanded() {
		return _expanded;
	}

	/**
	 * @see #getExpanded()
	 */
	public void setExpanded(Set<Object> expanded) {
		_expanded = expanded;
	}

	/**
	 * The current selection.
	 */
	public Selection getSelection() {
		return _selection;
	}

	/**
	 * @see #getSelection()
	 */
	public void setSelection(Selection selection) {
		_selection = selection;
	}

	/**
	 * The page size, or {@link #SHOW_ALL} for virtual scrolling / no paging.
	 */
	public int getPageSize() {
		return _pageSize;
	}

	/**
	 * @see #getPageSize()
	 */
	public void setPageSize(int pageSize) {
		_pageSize = pageSize;
	}

	/**
	 * The zero-based current page (ignored when {@link #getPageSize()} is
	 * {@link #SHOW_ALL}).
	 */
	public int getPage() {
		return _page;
	}

	/**
	 * @see #getPage()
	 */
	public void setPage(int page) {
		_page = page;
	}

	/**
	 * The row density chosen for this table: {@link #DENSITY_DEFAULT} (empty, follows the
	 * surrounding application), {@link #DENSITY_NORMAL} or {@link #DENSITY_COMPACT}.
	 */
	public String getDensity() {
		return _density;
	}

	/**
	 * @see #getDensity()
	 */
	public void setDensity(String density) {
		_density = density;
	}

	/**
	 * Whether every other row is shaded.
	 */
	public boolean isStriped() {
		return _striped;
	}

	/**
	 * @see #isStriped()
	 */
	public void setStriped(boolean striped) {
		_striped = striped;
	}

	/**
	 * Whether the columns are separated by lines.
	 */
	public boolean isColumnLines() {
		return _columnLines;
	}

	/**
	 * @see #isColumnLines()
	 */
	public void setColumnLines(boolean columnLines) {
		_columnLines = columnLines;
	}

	/**
	 * A copy of this state that changes independently of it.
	 *
	 * <p>
	 * The collections are copied, the values they hold are shared: those are immutable.
	 * </p>
	 */
	public TableViewState copy() {
		TableViewState result = new TableViewState();
		result.setColumnOrder(new ArrayList<>(_columnOrder));
		result.setHiddenColumns(new LinkedHashSet<>(_hiddenColumns));
		result.setWidths(new LinkedHashMap<>(_widths));
		result.setFrozenCount(_frozenCount);
		result.setSort(new ArrayList<>(_sort));
		result.setFilters(new LinkedHashMap<>(_filters));
		result.setSearch(_search);
		result.setGrouping(_grouping);
		result.setExpanded(new LinkedHashSet<>(_expanded));
		result.setSelection(_selection);
		result.setPageSize(_pageSize);
		result.setPage(_page);
		result.setDensity(_density);
		result.setStriped(_striped);
		result.setColumnLines(_columnLines);
		return result;
	}

}
