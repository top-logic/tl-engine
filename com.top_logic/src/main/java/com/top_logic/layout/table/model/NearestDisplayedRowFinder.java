/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.table.TableModel;
import com.top_logic.util.Utils;

/**
 * Encapsulated the algorithm for {@link TableModel#findNearestDisplayedRow(Object)}.
 * <p>
 * <em>The caller has to check whether the target row is displayed and call this only if it is not displayed.</em>
 * This method has to iterate through all of them to check that, which is very inefficient.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NearestDisplayedRowFinder<T> {

	private final List<? extends T> _displayedRows;

	private final Collection<? extends T> _allRows;

	private final Comparator<? super T> _comparator;

	/**
	 * Creates a {@link NearestDisplayedRowFinder}.
	 * 
	 * @param displayedRows
	 *        Is allowed to be null, which is normalized to the empty {@link List}.
	 * @param allRows
	 *        Is allowed to be null, which is normalized to the empty {@link List}.
	 * @param comparator
	 *        Is not allowed to be null.
	 */
	public NearestDisplayedRowFinder(List<? extends T> displayedRows, Collection<? extends T> allRows,
			Comparator<? super T> comparator) {

		_displayedRows = nonNull(displayedRows);
		_allRows = nonNull(allRows);
		// Fail early
		_comparator = checkNonNull(comparator);
	}

	private static <S> S checkNonNull(S object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}

	/**
	 * Finds the nearest displayed row for the target row.
	 * 
	 * @param targetRow
	 *        Is allowed to be null, only if null is a valid row object. Must not be contained in
	 *        the displayed rows.
	 */
	public Maybe<T> find(T targetRow) {
		if (getDisplayedRows().isEmpty()) {
			return Maybe.none();
		}
		int insertPosition = Collections.<T> binarySearch(getDisplayedRows(), targetRow, getComparator());
		if (insertPosition < 0) {
			insertPosition = (-insertPosition) - 1;
		}
		/* Objects are inserted "between" existing elements. But the "insert position" is the
		 * element *after* that "in between position". And the "nearest position" might be the
		 * element *before* that "in between position". That decision is encoded in the
		 * "toNearestPosition" method. */
		int nearestPosition = TableUtil.toNearestPosition(insertPosition);
		List<? extends T> compareEqualRows = getCompareEqualRows(targetRow, nearestPosition);
		if (compareEqualRows.isEmpty()) {
			return Maybe.<T> toMaybeButTreatNullAsValidValue(getDisplayedRows().get(nearestPosition));
		}
		if (compareEqualRows.size() == 1) {
			T rowObject = getDisplayedRows().get(0);
			return Maybe.<T> toMaybeButTreatNullAsValidValue(rowObject);
		}
		return findNearestDisplayedRow(targetRow, compareEqualRows);
	}

	/**
	 * Finds the first and last compare-equal element in the {@link #getDisplayedRows() displayed
	 * elements}.
	 * 
	 * @param nearestPosition
	 *        The position before, at or after a compare-equal element.
	 * @return The objects at both indices are compare-equal to the target row. The first index is
	 *         never larger than the second index, but is equal to it, if there is only a single
	 *         compare-equal element.
	 */
	private List<? extends T> getCompareEqualRows(T targetElement, int nearestPosition) {
		/* The "nearest position" might be before or after the compare-equal rows. Unify these cases
		 * to: If there is a compare-equal row, it is at "start position". */
		int startPosition = findCompareEqualNeighbor(targetElement, nearestPosition);
		int firstEqualRow = findFirstCompareEqualRow(targetElement, startPosition);
		int lastEqualRow = findLastCompareEqualRow(targetElement, startPosition);
		if (firstEqualRow > lastEqualRow) {
			return emptyList();
		}
		return getDisplayedRows().subList(firstEqualRow, lastEqualRow + 1);
	}

	private int findFirstCompareEqualRow(T targetElement, int startPosition) {
		/* Start with startPosition + 1 as the first call to "previous()" would return the element
		 * *before* the specified index. */
		int firstEqualRow = startPosition + 1;
		ListIterator<? extends T> iterator = getDisplayedRows().listIterator(firstEqualRow);
		while (iterator.hasPrevious()) {
			T previousRow = iterator.previous();
			if (isCompareEqual(previousRow, targetElement)) {
				firstEqualRow -= 1;
			} else {
				break;
			}
		}
		return firstEqualRow;
	}

	private int findLastCompareEqualRow(T targetElement, int startPosition) {
		int lastEqualRow = startPosition;
		ListIterator<? extends T> iterator = getDisplayedRows().listIterator(lastEqualRow + 1);
		while (iterator.hasNext()) {
			T nextRow = iterator.next();
			if (isCompareEqual(nextRow, targetElement)) {
				lastEqualRow += 1;
			} else {
				break;
			}
		}
		return lastEqualRow;
	}

	private int findCompareEqualNeighbor(T targetRow, int index) {
		if (index < 0 || index >= getDisplayedRows().size()) {
			throw new IllegalArgumentException("Index (" + index + ") has to be between 0 (inclusive) and "
				+ getDisplayedRows().size() + " (exclusive).");
		}
		if (getDisplayedRows().size() <= 1) {
			return 0;
		}
		T rowAtIndex = getDisplayedRows().get(index);
		if (isCompareEqual(rowAtIndex, targetRow)) {
			return index;
		}
		if (index - 1 >= 0) {
			T previousRow = getDisplayedRows().get(index - 1);
			if (isCompareEqual(previousRow, targetRow)) {
				return index - 1;
			}
		}
		if (index + 1 < getDisplayedRows().size()) {
			T nextRow = getDisplayedRows().get(index + 1);
			if (isCompareEqual(nextRow, targetRow)) {
				return index + 1;
			}
		}
		/* There is no row that is compare-equal to the target row. Therefore, the original index is
		 * returned, as no better index is found. */
		return index;
	}

	private boolean isCompareEqual(T left, T right) {
		return getComparator().compare(left, right) == 0;
	}

	/**
	 * @param displayedRows
	 *        The <em>compare-equal</em> displayed rows.
	 */
	private Maybe<T> findNearestDisplayedRow(T targetRow, List<? extends T> displayedRows) {
		if (displayedRows.isEmpty()) {
			return Maybe.none();
		}
		Iterator<? extends T> displayedRowsIterator = displayedRows.iterator();
		T previousDisplayedRow = displayedRowsIterator.next();
		T nextDisplayedRow = displayedRowsIterator.next();
		for (T currentRow : getAllRows()) {
			if (Utils.equals(currentRow, nextDisplayedRow)) {
				if (!displayedRowsIterator.hasNext()) {
					return Maybe.toMaybeButTreatNullAsValidValue(nextDisplayedRow);
				}
				previousDisplayedRow = nextDisplayedRow;
				nextDisplayedRow = displayedRowsIterator.next();
			}
			if (Utils.equals(currentRow, targetRow)) {
				T nearestDisplayedRow = previousDisplayedRow;
				return Maybe.toMaybeButTreatNullAsValidValue(nearestDisplayedRow);
			}
		}
		/* 'allRows' does not contain the target row. Therefore there is no way to tell what the
		 * nearest displayed row is. */
		return Maybe.none();
	}

	private List<? extends T> getDisplayedRows() {
		return _displayedRows;
	}

	private Collection<? extends T> getAllRows() {
		return _allRows;
	}

	private Comparator<? super T> getComparator() {
		return _comparator;
	}

}
