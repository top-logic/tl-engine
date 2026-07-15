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

/**
 * The serializable, per-user view state of a table: column order, widths, frozen count,
 * sort, filters, grouping, expansion, selection and paging.
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

	private List<String> _columnOrder = new ArrayList<>();

	private Map<String, Integer> _widths = new LinkedHashMap<>();

	private int _frozenCount;

	private List<SortColumn> _sort = new ArrayList<>();

	private Map<String, FilterState> _filters = new LinkedHashMap<>();

	private GroupSpec _grouping = GroupSpec.NONE;

	private Set<Object> _expanded = new LinkedHashSet<>();

	private Selection _selection = Selection.none(SelectionMode.SINGLE);

	private int _pageSize = SHOW_ALL;

	private int _page;

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

}
