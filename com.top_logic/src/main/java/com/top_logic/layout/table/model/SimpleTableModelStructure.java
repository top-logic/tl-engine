/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.CollectionUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TupleFactory.Pair;

/**
 * {@link TableModelDataStructure} that does not support
 * {@link TableModelDataStructure#moveRow(int, int)} and has no order in
 * {@link TableModelDataStructure#getAllRows()}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class SimpleTableModelStructure<T> extends LinkedHashMap<T, Integer> implements TableModelDataStructure<T> {

	private ObjectTableModel _table;
	private HashSet<T> _notValidatedRows = new HashSet<>();

	public SimpleTableModelStructure(ObjectTableModel table) {
		_table = table;
	}

	@Override
	public void removeRow(int removedRow) {
		List<T> rows = getRows();
		Object rowObject = rows.remove(removedRow);
		remove(rowObject);
		if (removedRow < rows.size()) {
			movePositions(removedRow + 1, -1);
		}

	}

	@Override
	public int removeRowObject(Object rowObject) {
		Integer position = remove(rowObject);
		if (position == null) {
			return -1;
		}
		int removedRow = position.intValue();
		if (removedRow >= 0) {
			List<T> rows = getRows();
			rows.remove(removedRow);
			if (removedRow < rows.size()) {
				movePositions(removedRow + 1, -1);
			}
		}
		return removedRow;

	}

	@Override
	public void insertRows(int row, List<? extends T> newRows) {
		List<T> rows = getRows();
		if (row < rows.size()) {
			movePositions(row, newRows.size());
		}
		rows.addAll(row, newRows);
		int insertedRow = row;
		for (T rowObject : newRows) {
			put(rowObject, Integer.valueOf(insertedRow++));
		}
	}

	@Override
	public void addRow(T rowObject, int displayedRow) {
		internalAddRow(displayedRow, rowObject);
	}

	private void internalAddRow(int position, T rowObject) {
		List<T> rows = getRows();
		if (position < rows.size()) {
			movePositions(position, 1);
		}
		rows.add(position, rowObject);
		put(rowObject, Integer.valueOf(position));
		_notValidatedRows.add(rowObject);
	}

	/**
	 * Update all positions starting with the given position by the given delta.
	 * 
	 * @param position
	 *        The first position to update.
	 * @param count
	 *        The delta to add to affected positions.
	 */
	private void movePositions(int position, int count) {
		for (Entry<T, Integer> rowEntry : entrySet()) {
			int rowPosition = rowEntry.getValue();
			if (rowPosition >= position) {
				rowEntry.setValue(rowPosition + count);
			}
		}
	}

	@Override
	public int getRowOfObject(Object rowObject) {
		Integer displayIndex = get(rowObject);
		if (displayIndex == null) {
			return -1;
		}
		return displayIndex.intValue();
	}

	@Override
	public boolean containsRow(Object anObject) {
		return containsKey(anObject);
	}

	@Override
	public void removeAll() {
		clear();
	}

	@Override
	public void revalidateOrder() {
		List<T> rows = getRows();
		Collections.sort(rows, getOrder());
		updatePositions(0, rows.size());
	}

	private void updatePositions(int start, int stop) {
		List<? extends T> rows = getRows();
		for (int n = start; n < stop; n++) {
			put(rows.get(n), Integer.valueOf(n));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void revalidateFilter(boolean updateDisplayedRows) {
		List rows = getRows();
		if (updateDisplayedRows) {
			_notValidatedRows.clear();
			rows.clear();
		}
		Filter<? super T> filter = getFilter();
		for (Entry<T, Integer> rowEntry : entrySet()) {
			T row = rowEntry.getKey();
			boolean accepted = filter.accept(row);
			if (updateDisplayedRows) {
				if (accepted) {
					rows.add(row);
				} else {
					rowEntry.setValue(-1);
				}
			}
		}
	}

	@Override
	public boolean isValidated(Object rowObject) {
		return !_notValidatedRows.contains(rowObject);
	}

	@Override
	public void initRowObjects(List<? extends T> newRowObjects) {
		List<T> rows = getRows();
		rows.clear();
		clear();
		Filter<? super T> filter = getFilter();
		for (int i = 0; i < newRowObjects.size(); i++) {
			T rowObject = newRowObjects.get(i);
			if (filter.accept(rowObject)) {
				rows.add(rowObject);
			}
			put(rowObject, Integer.valueOf(-1));
		}
	}

	@Override
	public Collection<T> getAllRows() {
		return Collections.unmodifiableSet(keySet());
	}

	@Override
	public int addRow(T rowObject) {
		int displayedRow = insertPosition(rowObject);
		addRow(rowObject, displayedRow);
		return displayedRow;
	}

	@Override
	public Pair<Integer, Integer> addRows(Collection<? extends T> newRowObjects) {
		/* If the table is empty or smaller than the number of rows being added, it would be faster
		 * to use the method for initializing this data structure, instead of "updating" it. But
		 * that would disable the feature, that new rows are always displayed, even when they don't
		 * match the filter. */
		if (CollectionUtil.isEmptyOrNull(newRowObjects)) {
			return new Pair<>(-1, -1);
		}
		if (newRowObjects.size() == 1) {
			int insertIndex = addRow(CollectionUtil.getFirst(newRowObjects));
			return new Pair<>(insertIndex, insertIndex);
		}
		getRows().addAll(newRowObjects);
		revalidateOrder();
		_notValidatedRows.addAll(newRowObjects);
		Integer firstNewRow = indexOfFirst(getRows(), newRowObjects);
		Integer lastNewRow = indexOfLast(getRows(), newRowObjects);
		checkAddRowsIndexConsistency(firstNewRow, lastNewRow, newRowObjects.size());
		return new Pair<>(firstNewRow, lastNewRow);
	}

	static void checkAddRowsIndexConsistency(int first, int last, int insertSize) {
		if ((first == NOT_FOUND) && (last == NOT_FOUND)) {
			return;
		}
		if ((first == NOT_FOUND) || (last == NOT_FOUND)) {
			throw new RuntimeException("Searches returned inconsistent indexes."
				+ " One of the searches found something, the other not. First: " + first + ", Last: " + last);
		}
		if (first > last) {
			throw new RuntimeException("Searches returned inconsistent indexes."
				+ " The first index is larger than the last. First: " + first + ", Last: " + last);
		}
		if ((last - first) < (insertSize - 1)) {
			throw new RuntimeException("Searches returned inconsistent indexes."
				+ " The results are too close to each other. First: " + first + ", Last: " + last + ", Size: " + insertSize);
		}
	}

	@Override
	public int insertPosition(T rowObject) {
		Comparator<? super T> order = getOrder();
		List<T> rows = getRows();
		return CollectionUtil.insertPosition(rows, rowObject, order);
	}

	@Override
	public void removeRows(int first, int stop) {
		int last = stop - 1;
		List<T> rows = getRows();
		for (int n = last; n >= first; n--) {
			Object rowObject = rows.remove(n);
			remove(rowObject);
		}
		if (stop < rows.size()) {
			movePositions(stop, -(stop - first));
		}
	}

	private List<T> getRows() {
		return _table.rows();
	}

	private Comparator<? super T> getOrder() {
		return _table.getOrder();
	}

	private Filter<? super T> getFilter() {
		return _table.getFilter();
	}

	@Override
	public void moveRow(int from, int to) {
		throw noPriorityTableModel();
	}

	private RuntimeException noPriorityTableModel() {
		throw new IllegalStateException("No move of rows in non priority tables");
	}

}


