/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.table.filter.CellExistenceTester;

/**
 * {@link Filter} of table rows.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableRowFilter implements Filter<Object> {
	
	private List<ColumnFilterHolder> columnFilterHolders;
	private List<ColumnFilterHolder> activeFilters;
	private List<ColumnFilterHolder> visibleFilters;

	/**
	 * Create a new {@link TableRowFilter}.
	 */
	public TableRowFilter(List<ColumnFilterHolder> columnFilterHolder) {
		this.columnFilterHolders = columnFilterHolder;
		activeFilters = new ArrayList<>();
		visibleFilters = new ArrayList<>();
	}

	/**
	 * Called at begin of table model / filter revalidation process, used for preparation purposes
	 * (e.g. allocation of caches).
	 */
	public void startFilterRevalidation() {
		for (ColumnFilterHolder columnFilterHolder : columnFilterHolders) {
			TableFilter filter = columnFilterHolder.getFilter();
			filter.startFilterRevalidation();
			if (filter.isActive()) {
				activeFilters.add(columnFilterHolder);
			} else {
				visibleFilters.add(columnFilterHolder);
			}
		}
	}

	/**
	 * Called at the end of table model / filter revalidation process, used for cleanup of caches
	 * and filter model updates.
	 */
	public void stopFilterRevalidation() {
		activeFilters.clear();
		visibleFilters.clear();
		for (ColumnFilterHolder columnFilterHolder : columnFilterHolders) {
			columnFilterHolder.getFilter().stopFilterRevalidation();
		}
	}

	@Override
	public boolean accept(Object value) {
		FilterResult filterResult = check(value);
		boolean allFilterMatching = filterResult.areAllFilterApplicable() && filterResult.doAllFilterAccept();
		boolean isCountable =
			allFilterMatching || (filterResult.areAllFilterApplicable() && filterResult.isSingleFilterDenial());
		if (isCountable) {
			count(filterResult);
		}
		return allFilterMatching;
	}

	/**
	 * Adds a visible {@link TableFilter column filter} to this {@link TableRowFilter}.
	 * 
	 * @throws IllegalArgumentException
	 *         when the given {@link TableFilter column filter} is active. Only inactive column
	 *         filters can be added.
	 */
	public void addVisibleFilter(ColumnFilterHolder columnFilterHolder) {
		TableFilter filter = columnFilterHolder.getFilter();
		if (filter.isActive()) {
			throw new IllegalArgumentException("Active filters cannot be added to table row filter!");
		}
		columnFilterHolders.add(columnFilterHolder);
	}

	/**
	 * Removes a currently visible {@link TableFilter column filter} from this
	 * {@link TableRowFilter}.
	 * 
	 * @throws IllegalArgumentException
	 *         when the given {@link TableFilter column filter} is active. Only inactive column
	 *         filters can be removed.
	 */
	public void removeVisibleFilter(TableFilter columnFilter) {
		if (columnFilter.isActive()) {
			throw new IllegalArgumentException("Active filters cannot be added to table row filter!");
		}
		ColumnFilterHolder columnFilterHolder = getHolderOfFilter(columnFilter);
		if (columnFilterHolder != null) {
			columnFilterHolders.remove(columnFilterHolder);
		}
	}

	private ColumnFilterHolder getHolderOfFilter(TableFilter columnFilter) {
		ColumnFilterHolder columnFilterHolder = null;
		for (ColumnFilterHolder holderCandidate : columnFilterHolders) {
			if (holderCandidate.getFilter() == columnFilter) {
				columnFilterHolder = holderCandidate;
				break;
			}
		}
		return columnFilterHolder;
	}

	/**
	 * true, if the given {@link TableFilter column filter} is already part (visible or
	 *         active) of this {@link TableRowFilter}, false otherwise.
	 */
	public boolean containsFilter(TableFilter columnFilter) {
		for (ColumnFilterHolder columnFilterHolder : columnFilterHolders) {
			if (columnFilterHolder.getFilter() == columnFilter) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Performs filter evaluation on the given value.
	 */
	public FilterResult check(Object value) {
		Map<ColumnFilterHolder, Object> filterCountState = new HashMap<>();
		Pair<BitSet, BitSet> filterMasks = getActiveFilterResultMasks(value, filterCountState);
		getApplicableVisibleFilters(value, filterCountState);
		return new FilterResult(activeFilters.size(), filterMasks.getFirst(), filterMasks.getSecond(),
			filterCountState, value);
	}

	@SuppressWarnings("unchecked")
	private Pair<BitSet, BitSet> getActiveFilterResultMasks(Object value, Map<ColumnFilterHolder, Object> filterCountState) {
		BitSet filterMatchMask = new BitSet(activeFilters.size());
		BitSet filterApplicabilityMask = new BitSet(activeFilters.size());
		int matchMaskIndex = 0;
		for (ColumnFilterHolder columnFilterHolder : activeFilters) {
			CellExistenceTester cellExistenceTester = columnFilterHolder.getCellExistenceTester();
			if (cellExistenceTester.isCellExistent(value, columnFilterHolder.getFilterPosition())) {
				filterApplicabilityMask.clear(matchMaskIndex);
				Object columnValue = columnFilterHolder.getFilterValueMapping().map(value);
				if (!acceptedByFilter(columnValue, columnFilterHolder)) {
					filterMatchMask.set(matchMaskIndex);
					filterCountState.put(columnFilterHolder, columnValue);
				} else {
					filterMatchMask.clear(matchMaskIndex);
					filterCountState.put(columnFilterHolder, FilterResult.COUNT_ALL);
				}
			} else {
				filterApplicabilityMask.set(matchMaskIndex);
			}
			matchMaskIndex++;
		}
		return new Pair<>(filterMatchMask, filterApplicabilityMask);
	}

	@SuppressWarnings("unchecked")
	private void getApplicableVisibleFilters(Object value, Map<ColumnFilterHolder, Object> filterCountState) {
		for (ColumnFilterHolder columnFilterHolder : visibleFilters) {
			CellExistenceTester cellExistenceTester = columnFilterHolder.getCellExistenceTester();
			if (cellExistenceTester.isCellExistent(value, columnFilterHolder.getFilterPosition())) {
				Object columnValue = columnFilterHolder.getFilterValueMapping().map(value);
				acceptedByFilter(columnValue, columnFilterHolder);
				filterCountState.put(columnFilterHolder, columnValue);
			}
		}
	}

	private boolean acceptedByFilter(Object columnValue, ColumnFilterHolder columnFilterHolder) {
		return columnFilterHolder.getFilter().accept(columnValue);
	}

	/**
	 * Counts filter matches, based on the given {@link FilterResult}.
	 */
	public void count(FilterResult filterResult) {
		countActiveFilterMatches(filterResult);
		countVisibleFilterMatches(filterResult);
	}

	@SuppressWarnings("unchecked")
	private void countActiveFilterMatches(FilterResult filterResult) {
		Map<ColumnFilterHolder, Object> filterCountState = filterResult.getFilterCountState();
		for (int i = 0; i < activeFilters.size(); i++) {
			if (filterResult.isFilterApplicable(i)) {
				if (filterResult.doOtherFiltersAllow(i)) {
					ColumnFilterHolder columnFilterHolder = activeFilters.get(i);
					Object cellValue = columnFilterHolder.getFilterValueMapping().map(filterResult.getEvaluatedRow());
					if (isCountableValue(filterCountState, columnFilterHolder, cellValue)) {
						countFilterMatch(columnFilterHolder, filterResult.getEvaluatedRow(), cellValue);
					}
				}
			}
		}
	}

	private boolean isCountableValue(Map<ColumnFilterHolder, Object> filterCountState,
			ColumnFilterHolder columnFilterHolder, Object cellValue) {
		Object singleCountState = filterCountState.get(columnFilterHolder);
		if (!filterCountState.containsKey(columnFilterHolder) || (singleCountState == FilterResult.NO_COUNT)) {
			return false;
		}
		if (singleCountState != FilterResult.COUNT_ALL) {
			return Utils.equals(singleCountState, cellValue);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void countVisibleFilterMatches(FilterResult filterResult) {
		if (filterResult.doAllFilterAccept()) {
			Map<ColumnFilterHolder, Object> filterCountState = filterResult.getFilterCountState();
			for (ColumnFilterHolder columnFilterHolder : visibleFilters) {
				Object cellValue = columnFilterHolder.getFilterValueMapping().map(filterResult.getEvaluatedRow());
				if (isCountableValue(filterCountState, columnFilterHolder, cellValue)) {
					countFilterMatch(columnFilterHolder, filterResult.getEvaluatedRow(), cellValue);
				}
			}
		}
	}

	private void countFilterMatch(ColumnFilterHolder columnFilterHolder, Object rowValue, Object cellValue) {
		TableFilter filter = columnFilterHolder.getFilter();
		CellExistenceTester cellExistenceTester = columnFilterHolder.getCellExistenceTester();
		if (cellExistenceTester.isCellExistent(rowValue, columnFilterHolder.getFilterPosition())) {
			filter.count(cellValue);
		}
	}

	/**
	 * @param initialValue - true, if initial {@link FilterResult} shall represent a positive filter evaluation,
	 * 						 false, when initial filter evaluation shall be negative.
	 * @return initial {@link FilterResult} which can be used for starting point for merge operations of {@link FilterResult}s.
	 */
	public FilterResult getInitialState(boolean initialValue) {
		int size = activeFilters.size();
		BitSet filterMatchMask = new BitSet(size);
		BitSet filterApplicabilityMask = new BitSet(size);

		if (initialValue) {
			filterMatchMask.set(0, size);
		}
		filterApplicabilityMask.set(0, size);

		return new FilterResult(size, filterMatchMask, filterApplicabilityMask,
			getInitialCountState(), null);
	}

	private Map<ColumnFilterHolder, Object> getInitialCountState() {
		Map<ColumnFilterHolder, Object> filterCountState = new HashMap<>();
		addFiltersToState(filterCountState, activeFilters);
		addFiltersToState(filterCountState, visibleFilters);
		return filterCountState;
	}

	private void addFiltersToState(Map<ColumnFilterHolder, Object> filterCountState, List<ColumnFilterHolder> filters) {
		for (ColumnFilterHolder columnFilterHolder : filters) {
			filterCountState.put(columnFilterHolder, FilterResult.COUNT_ALL);
		}
	}
}