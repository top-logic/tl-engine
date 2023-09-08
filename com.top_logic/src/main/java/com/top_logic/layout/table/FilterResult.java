/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * State, that indicates filter results of a group of table columns, that are considered to have
 * active filters. Basically, a filter result encodes row value acceptance, with behalf to column
 * filter evaluation. Acceptance may be undefined, due to inapplicability of a given column filter
 * to a given row.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterResult {
	
	static final Object NO_COUNT = new Object();			
	static final Object COUNT_ALL = new Object();			

	private int _activeFilterCount;
	private BitSet _activeFilterResults;
	private BitSet _activeFilterApplicability;
	private Map<ColumnFilterHolder, Object> _filterCountState;
	private Object _evaluatedRow;

	/**
	 * Creates a new {@link FilterResult}
	 * @param filterCount
	 *        - amount of active column filters
	 * @param filterResults
	 *        - filter results (true / false) of the column filters
	 * @param filterApplicability
	 *        - whether a column filter could be applied to a given row, or not. Must have the same
	 *        order and size as filterResults
	 */
	public FilterResult(int filterCount, BitSet filterResults, BitSet filterApplicability,
			Map<ColumnFilterHolder, Object> filterCountState, Object evaluatedRow) {
		_activeFilterCount = filterCount;
		_activeFilterResults = filterResults;
		_activeFilterApplicability = filterApplicability;
		_filterCountState = filterCountState;
		_evaluatedRow = evaluatedRow;
	}

	/**
	 * Same as {@link BitSet#or(BitSet)} of filter results and {@link BitSet#and(BitSet)} filter
	 * applicability. Also updates count states, to restrict countable values.
	 */
	public void or(FilterResult rowFilterResult) {
		_activeFilterResults.or(rowFilterResult._activeFilterResults);
		_activeFilterApplicability.and(rowFilterResult._activeFilterApplicability);
		joinCountStates(rowFilterResult);
	}

	private void joinCountStates(FilterResult rowFilterResult) {
		for (Entry<ColumnFilterHolder, Object> otherSingleState : rowFilterResult.getFilterCountState().entrySet()) {
			ColumnFilterHolder otherFilter = otherSingleState.getKey();
			Object otherCountValue = otherSingleState.getValue();
			Object myCountValue = _filterCountState.get(otherSingleState.getKey());
			if (myCountValue == null) {
				_filterCountState.put(otherFilter, otherCountValue);
			} else {
				updateExistingCountStates(otherFilter, otherCountValue, myCountValue);
			}
		}
	}

	private void updateExistingCountStates(ColumnFilterHolder otherFilter, Object otherCountValue,
			Object myCountValue) {
		if (!myCountValue.equals(otherCountValue)) {
			if (myCountValue != COUNT_ALL) {
				if (myCountValue != NO_COUNT && otherCountValue != COUNT_ALL) {
					_filterCountState.put(otherFilter, NO_COUNT);
				}
			} else if (otherCountValue != COUNT_ALL) {
				_filterCountState.put(otherFilter, NO_COUNT);
			}
		}
	}

	/**
	 * Set's all column count states, known by this {@link FilterResult}, to values of the given
	 * {@link FilterResult}.
	 */
	public void updateCountState(FilterResult updateResult) {
		for (Entry<ColumnFilterHolder, Object> otherSingleState : updateResult.getFilterCountState().entrySet()) {
			ColumnFilterHolder otherFilter = otherSingleState.getKey();
			Object otherCountValue = otherSingleState.getValue();
			Object myCountValue = _filterCountState.get(otherSingleState.getKey());
			if (myCountValue != null) {
				updateExistingCountStates(otherFilter, otherCountValue, myCountValue);
			}
		}
	}

	/**
	 * Same as {@link BitSet#and(BitSet)} of filter results and {@link BitSet#and(BitSet)} of filter
	 * applicability. Also implicitly {@link #replaceCountStateFrom(FilterResult)} is called.
	 */
	public void and(FilterResult rowFilterResult) {
		_activeFilterResults.and(rowFilterResult._activeFilterResults);
		_activeFilterApplicability.and(rowFilterResult._activeFilterApplicability);
		replaceCountStateFrom(rowFilterResult);
	}

	/**
	 * Total replacement of this {@link FilterResult}'s column filter count state (which value can
	 * be counted by column filters by the given {@link FilterResult}'s count state.
	 */
	public void replaceCountStateFrom(FilterResult filterResult) {
		_filterCountState = filterResult.getFilterCountState();
	}

	/**
	 * true, if no column filters are available, false otherwise.
	 */
	public boolean hasNoFilters() {
		return _activeFilterCount == 0;
	}

	/**
	 * true, if all column filters have been evaluated, false otherwise.
	 */
	public boolean areAllFilterApplicable() {
		return _activeFilterApplicability.isEmpty();
	}

	/**
	 * true, if at least one filter has been evaluated, false otherwise
	 */
	public boolean hasApplicableFilters() {
		return _activeFilterApplicability.nextClearBit(0) < _activeFilterCount;
	}

	/**
	 * true, if all column filters, that are applicable, has accepted the given row, false
	 *         otherwise.
	 */
	public boolean doAllFilterAccept() {
		return _activeFilterResults.isEmpty();
	}

	/**
	 * if a single active column filter denies evaluated row, false otherwise.
	 */
	public boolean isSingleFilterDenial() {
		int denyingFilterIndex = _activeFilterResults.nextSetBit(0);
		return denyingFilterIndex > -1 && _activeFilterResults.nextSetBit(denyingFilterIndex + 1) == -1;
	}

	boolean isFilterApplicable(int index) {
		return !_activeFilterApplicability.get(index);
	}

	private boolean isSolelyDenyingFilter(int index) {
		return _activeFilterResults.nextSetBit(0) == index && _activeFilterResults.nextSetBit(index + 1) == -1;
	}

	boolean doOtherFiltersAllow(int index) {
		return doAllFilterAccept() || isSolelyDenyingFilter(index);
	}

	/**
	 * true, if this {@link FilterResult} is suitable for match counting for some involved
	 *         column filter, false otherwise.
	 */
	public boolean hasCountableValues() {
		for (Object filterCountState : _filterCountState.values()) {
			if (filterCountState != NO_COUNT) {
				return true;
			}
		}
		return false;
	}

	Map<ColumnFilterHolder, Object> getFilterCountState() {
		return _filterCountState;
	}

	/**
	 * row object, whose filter evaluation led to this {@link FilterResult}.
	 */
	public Object getEvaluatedRow() {
		return _evaluatedRow;
	}
}
