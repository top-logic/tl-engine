/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.util.Utils.*;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TupleFactory.Pair;

/**
 * {@link TableModelDataStructure} that allows movement of rows and contains sorted
 * {@link #getAllRows()}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PriorityTableModelDataStructure<T> extends HashMap<T, RowIndex<T>> implements TableModelDataStructure<T> {

	class AllRows extends AbstractCollection<T> {

		@Override
		public Iterator<T> iterator() {
			return new Iterator<>() {

				final int modCount = currentModCount();

				private RowIndex<T> _next = PriorityTableModelDataStructure.this._head;

				@Override
				public boolean hasNext() {
					return _next != PriorityTableModelDataStructure.this.none();
				}

				@Override
				public T next() {
					checkModCount();
					T result = _next.getRowObject();
					_next = _next.next();
					return result;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Remove not supported");
				}

				private void checkModCount() {
					if (modCount != currentModCount()) {
						throw new ConcurrentModificationException();
					}
				}

				private int currentModCount() {
					return PriorityTableModelDataStructure.this._modCount;
				}

			};
		}

		@Override
		public int size() {
			return PriorityTableModelDataStructure.this.size();
		}

	}

	RowIndex<T> _head = none();

	RowIndex<T> _tail = none();

	int _modCount = 0;

	private final ObjectTableModel _table;

	Collection<T> _allRows = new AllRows();

	private HashSet<T> _notValidatedRows = new HashSet<>();

	public PriorityTableModelDataStructure(ObjectTableModel tableModel) {
		_table = tableModel;
	}

	@Override
	public Collection<T> getAllRows() {
		return _allRows;
	}

	@Override
	public void initRowObjects(List<? extends T> newRowObjects) {
		List<T> rows = getRows();
		rows.clear();
		removeAll();
		Filter<? super T> filter = getFilter();
		int numberVisibleRows = 0;
		for (int i = 0; i < newRowObjects.size(); i++) {
			T rowObject = newRowObjects.get(i);
			int displayRow;
			if (filter.accept(rowObject)) {
				rows.add(rowObject);
				displayRow = numberVisibleRows++;
			} else {
				displayRow = -1;
			}
			append(rowObject, displayRow);
		}

		increaseModCount();
	}

	private void increaseModCount() {
		_modCount++;
	}

	private void append(T rowObject, int displayedIndex) {
		RowIndex<T> index = new RowIndex<>(rowObject, displayedIndex);
		RowIndex<T> clash = put(rowObject, index);
		assert clash == null : "An object must not be contained twice.";
		if (_tail == none()) {
			assert _head == none();
			_tail = _head = index;
		} else {
			assert _head != none();
			ensureSibling(_tail, index);
			_tail = index;
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void revalidateFilter(boolean updateDisplayedRows) {
		List rows = getRows();
		if (updateDisplayedRows) {
			_notValidatedRows.clear();
			rows.clear();
		}
		Filter filter = getFilter();
		RowIndex<T> current = _head;
		int numberVisibleRows = 0;
		while (current != none()) {
			T row = current.getRowObject();
			boolean accepted = filter.accept(row);
			if (updateDisplayedRows) {
				if (accepted) {
					rows.add(row);
					current.setDisplayedIndex(numberVisibleRows++);
				} else {
					current.setDisplayedIndex(-1);
				}
			}
			current = current.next();
		}
	}

	@Override
	public boolean isValidated(Object rowObject) {
		return !_notValidatedRows.contains(rowObject);
	}

	@Override
	public void revalidateOrder() {
		Comparator<? super T> order = getOrder();
		if (order == AbstractObjectTableModel.DEFAULT_ORDER) {
			return;
		}
		sort(order);
		List<T> rows = getRows();
		Collections.sort(rows, order);

		increaseModCount();
	}

	private void sort(Comparator<? super T> order) {
		RowIndex<T>[] indexes = values().toArray(RowIndex.<T> noRows());
		if (indexes.length == 0) {
			_head = _tail = none();
			return;
		}
		Arrays.sort(indexes, new Comparator<RowIndex<T>>() {

			@Override
			public int compare(RowIndex<T> o1, RowIndex<T> o2) {
				int compare = order.compare(o1.getRowObject(), o2.getRowObject());
				if (compare == 0) {
					/* Ensure that objects that are equal to the comparator are are sorted by their
					 * current display order. */
					compare = o1.compareTo(o2);
				}
				return compare;
			}
		});

		int displayIndex = 0;
		int current = 0;
		while (true) {
			RowIndex<T> previous = indexes[current];
			if (previous.getDisplayedIndex() > -1) {
				previous.setDisplayedIndex(displayIndex++);
			}
			current++;
			if (current == indexes.length) {
				break;
			}
			indexes[current].setPrevious(previous);
			previous.setNext(indexes[current]);
		}
		_head = indexes[0];
		_head.setPrevious(none());
		_tail = indexes[indexes.length - 1];
		_tail.setNext(none());
		
	}

	@Override
	public void removeAll() {
		clear();
		_head = _tail = none();

		increaseModCount();
	}

	@Override
	public boolean containsRow(Object anObject) {
		return containsKey(anObject);
	}

	@Override
	public int getRowOfObject(Object rowObject) {
		return nonNullDisplayedIndex(get(rowObject));
	}

	private static int nonNullDisplayedIndex(RowIndex<?> rowIndex) {
		if (rowIndex == null) {
			return -1;
		} else {
			return rowIndex.getDisplayedIndex();
		}
	}

	@Override
	public int addRow(T rowObject) {
		int displayedRow = insertPosition(rowObject);
		addRow(rowObject, displayedRow);
		return displayedRow;
	}

	@Override
	public void addRow(T rowObject, int displayedRow) {
		RowIndex<T> objectToMove = calculateRowObject(getRows(), displayedRow);
		RowIndex<T> insertIndex = new RowIndex<>(rowObject, displayedRow);
		insert(insertIndex, insertIndex, 1, objectToMove, true);
		getRows().add(displayedRow, rowObject);
		put(rowObject, insertIndex);
		_notValidatedRows.add(rowObject);
		increaseModCount();
	}

	@Override
	public Pair<Integer, Integer> addRows(Collection<? extends T> newRowObjects) {
		/* If the table is empty or smaller than the number of rows being added, it would be faster
		 * to use the method for initializing this data structure, instead of "updating" it. But
		 * that would break the feature, that new rows are always displayed, even when they don't
		 * match the filter. */
		if (CollectionUtil.isEmptyOrNull(newRowObjects)) {
			return new Pair<>(-1, -1);
		}
		if (newRowObjects.size() == 1) {
			int insertIndex = addRow(CollectionUtil.getFirst(newRowObjects));
			return new Pair<>(insertIndex, insertIndex);
		}
		if (isEmpty()) {
			return addRowsToEmptyTable(newRowObjects);
		}
		Pair<Integer, Integer> changeRange = updateTableRowsAndUnderlyingMap(newRowObjects);
		_notValidatedRows.addAll(newRowObjects);
		increaseModCount();
		return changeRange;
	}

	private Pair<Integer, Integer> addRowsToEmptyTable(Collection<? extends T> newRowObjects) {
		List<T> combinedRows = new ArrayList<>();
		addNewRowsDirectly(combinedRows, newRowObjects);
		replaceTableRowsDirectly(combinedRows);
		_notValidatedRows.addAll(newRowObjects);
		increaseModCount();
		return new Pair<>(0, combinedRows.size() - 1);
	}

	private Pair<Integer, Integer> updateTableRowsAndUnderlyingMap(Collection<? extends T> newRowObjects) {
		/* It would not be correct to add the new rows to the existing rows and sorting them
		 * afterwards. That would change the order of the existing rows, which is user specified,
		 * not comparator specified. Therefore, the new rows have to be inserted according to the
		 * comparator, but without changing the order of the existing rows. Of course, that means
		 * that there are probably multiple places where a new row might "fit", for example: Between
		 * any two neighboring existing rows, which are smaller and larger than the new row. This
		 * implementation inserts them before the first larger new row. That does not introduce more
		 * disorder, according to the comparator. It might be more intuitive to insert a new row
		 * after the last smaller existing row. But that is harder to implement efficiently, as it
		 * would require to prepend to the list, which is usually slower. */
		List<T> existingRows = new ArrayList<>(getAllRows());
		List<T> newRows = new ArrayList<>(newRowObjects);
		Comparator<? super T> comparator = getOrder();
		newRows.sort(comparator);
		Filter<? super T> filter = getFilter();
		List<T> combinedRows = new ArrayList<>();
		int newRowsCount = newRows.size();
		int existingRowsCount = existingRows.size();
		int newRowsIndex = 0;
		int existingRowsIndex = 0;
		T currentNewRow = newRows.get(newRowsIndex);
		T currentExistingRow = existingRows.get(existingRowsIndex);
		int firstNewRow = -1;
		int lastNewRow = -1;
		while (true) {
			int compare = comparator.compare(currentExistingRow, currentNewRow);
			if (compare > 0) {
				/* The filters are ignored, as new objects should always be displayed, even if they
				 * do not match the filter. See "_notValidatedRows" for details. */
				int combinedRowIndex = combinedRows.size();
				insert(combinedRowIndex, currentExistingRow, currentNewRow);
				combinedRows.add(currentNewRow);
				if (firstNewRow == -1) {
					firstNewRow = combinedRowIndex;
				}
				lastNewRow = combinedRowIndex;
				newRowsIndex += 1;
				if (newRowsIndex >= newRowsCount) {
					addRowsFiltered(combinedRows, existingRows.subList(existingRowsIndex, existingRowsCount), filter);
					break;
				}
				currentNewRow = newRows.get(newRowsIndex);
			} else {
				if (filter.accept(currentExistingRow)) {
					combinedRows.add(currentExistingRow);
				}
				existingRowsIndex += 1;
				if (existingRowsIndex >= existingRowsCount) {
					if (firstNewRow == -1) {
						firstNewRow = combinedRows.size();
					}
					addNewRowsDirectly(combinedRows, newRows.subList(newRowsIndex, newRowsCount));
					lastNewRow = combinedRows.size() - 1;
					break;
				}
				currentExistingRow = existingRows.get(existingRowsIndex);
			}
		}
		SimpleTableModelStructure.checkAddRowsIndexConsistency(firstNewRow, lastNewRow, newRowsCount);
		Pair<Integer, Integer> changeRange = new Pair<>(firstNewRow, lastNewRow);
		replaceTableRowsDirectly(combinedRows);
		return changeRange;
	}

	private void insert(int insertIndex, T before, T newRow) {
		RowIndex<T> newRowIndex = new RowIndex<>(newRow, insertIndex);
		RowIndex<T> clash = put(newRow, newRowIndex);
		if (clash != null) {
			throw new IllegalArgumentException("Tried to add a row which does already exist: " + debug(clash));
		}
		RowIndex<T> nextRowIndex = get(before);
		insert(newRowIndex, newRowIndex, 1, nextRowIndex, false);
	}

	/** Adds the rows to the result {@link List} if the {@link Filter} accepts them. */
	private void addRowsFiltered(List<T> result, Collection<T> remainingRows, Filter<? super T> filter) {
		for (T row : remainingRows) {
			if (filter.accept(row)) {
				result.add(row);
			}
		}
	}

	/** Adds the rows to this {@link Map} and adds them to the given result {@link List}. */
	private void addNewRowsDirectly(List<T> result, Collection<? extends T> newRows) {
		int insertPosition = result.size();
		result.addAll(newRows);
		for (T row : newRows) {
			append(row, insertPosition);
			insertPosition += 1;
		}
	}

	private void replaceTableRowsDirectly(List<T> rows) {
		getRows().clear();
		getRows().addAll(rows);
	}

	@Override
	public int insertPosition(T rowObject) {
		Comparator<? super T> order = getOrder();
		List<T> rows = getRows();
		return CollectionUtil.insertPosition(rows, rowObject, order);
	}

	private RowIndex<T> calculateRowObject(List<? extends T> rows, int displayedRow) {
		assert 0 <= displayedRow && displayedRow <= rows.size();
		if (displayedRow < rows.size()) {
			return get(rows.get(displayedRow));
		} else {
			if (!rows.isEmpty()) {
				Object lastRowObject = rows.get(rows.size() - 1);
				RowIndex<T> indexLastRowObject = get(lastRowObject);
				if (indexLastRowObject != _tail) {
					return indexLastRowObject.next();
				} else {
					return none();
				}
			} else {
				return none();
			}
		}
	}

	private void removeIndex(RowIndex<T> index) {
		assert index != none();
		ensureSibling(index.previous(), index.next());
		if (index == _head) {
			_head = index.next();
		}
		if (index == _tail) {
			_tail = index.previous();
		} else {
			changeDisplayIndex(index.next(), none(), -1);
		}
	}

	private void insert(RowIndex<T> insertedFirst, RowIndex<T> insertedLast, int numberInserted, RowIndex<T> before,
			boolean updateDisplayIndexes) {
		assert insertedFirst != none();
		assert insertedFirst.previous() == none();
		assert insertedLast != none();
		assert insertedLast.next() == none();
		if (before == none()) {
			// append on list
			if (_tail == none()) {
				assert _head == none();
				_head = _tail = insertedFirst;
			} else {
				assert _head != none();
				ensureSibling(_tail, insertedFirst);
				_tail = insertedLast;
			}
		} else {
			if (before == _head) {
				_head = insertedFirst;
			} else {
				RowIndex<T> previous = before.previous();
				assert previous != none() : "Only head has no previous";
				ensureSibling(previous, insertedFirst);
			}
			ensureSibling(insertedLast, before);
			if (updateDisplayIndexes) {
				changeDisplayIndex(before, none(), numberInserted);
			}
		}
	}

	private void changeDisplayIndex(RowIndex<T> start, RowIndex<T> stop, int increment) {
		while (start != stop) {
			if (start.getDisplayedIndex() != -1) {
				start.setDisplayedIndex(start.getDisplayedIndex() + increment);
			}
			start = start.next();
		}
	}

	private void ensureSibling(RowIndex<T> previous, RowIndex<T> next) {
		if (previous != none()) {
			previous.setNext(next);
		}
		if (next != none()) {
			next.setPrevious(previous);
		}
	}

	@Override
	public void insertRows(int row, List<? extends T> newRows) {
		int insertNumber = newRows.size();
		if (insertNumber == 0) {
			return;
		}
		List<T> rows = getRows();
		rows.addAll(row, newRows);
		RowIndex<T> objectToMove = calculateRowObject(rows, row);
		Iterator<? extends T> newRowsIt = newRows.iterator();
		RowIndex<T> firstInserted, lastInserted;
		T rowObject = newRowsIt.next();
		firstInserted = lastInserted = new RowIndex<>(rowObject, row++);
		put(rowObject, lastInserted);
		while (newRowsIt.hasNext()) {
			rowObject = newRowsIt.next();
			RowIndex<T> newLastInserted = new RowIndex<>(rowObject, insertNumber++);
			put(rowObject, newLastInserted);
			ensureSibling(lastInserted, newLastInserted);
			lastInserted = newLastInserted;
		}
		insert(firstInserted, lastInserted, insertNumber, objectToMove, true);

		increaseModCount();
	}

	@Override
	public int removeRowObject(Object rowObject) {
		RowIndex<T> indexRemovedRowObject = removeIndexFor(rowObject);
		if (indexRemovedRowObject == null) {
			return -1;
		}
		int removedRow = indexRemovedRowObject.getDisplayedIndex();
		if (removedRow >= 0) {
			List<T> rows = getRows();
			Object removed = rows.remove(removedRow);
			assert removed == rowObject : "Inconsistent data structure";
		}

		increaseModCount();
		return removedRow;
	}

	@Override
	public void removeRow(int removedRow) {
		Object rowObject = getRows().remove(removedRow);
		RowIndex<T> removedIndex = removeIndexFor(rowObject);
		assert removedIndex != null : "Inconsistent data structure.";

		increaseModCount();
	}

	@Override
	public void removeRows(int first, int stop) {
		int last = stop - 1;
		List<T> rows = getRows();
		for (int n = last; n >= first; n--) {
			Object rowObject = rows.remove(n);
			RowIndex<T> removedIndex = removeIndexFor(rowObject);
			assert removedIndex != null : "Inconsistent data structure.";
			assert removedIndex.getDisplayedIndex() == n;
		}

		increaseModCount();
	}

	private RowIndex<T> removeIndexFor(Object rowObject) {
		RowIndex<T> removedIndex = remove(rowObject);
		if (removedIndex != null) {
			removeIndex(removedIndex);
		}
		return removedIndex;
	}

	@Override
	public void moveRow(int from, int to) {
		List<T> rows = getRows();
		RowIndex<T> fromIndex = get(rows.get(from));
		assert from == fromIndex.getDisplayedIndex();
		RowIndex<T> toIndex = get(rows.get(to));
		assert to == toIndex.getDisplayedIndex();
		if (to > from) {
			moveAfter(fromIndex, toIndex);
		} else {
			moveBefore(fromIndex, toIndex);
		}
		CollectionUtil.moveEntry(getRows(), from, to);
	}

	private void moveBefore(RowIndex<T> fromIndex, RowIndex<T> toIndex) {
		fromIndex.setDisplayedIndex(toIndex.getDisplayedIndex());
		changeDisplayIndex(toIndex, fromIndex, 1);

		if (fromIndex == _tail) {
			_tail = fromIndex.previous();
		}
		if (toIndex == _head) {
			_head = fromIndex;
		}
		ensureSibling(fromIndex.previous(), fromIndex.next());
		RowIndex<T> tmp = toIndex.previous();
		ensureSibling(fromIndex, toIndex);
		ensureSibling(tmp, fromIndex);
	}

	private void moveAfter(RowIndex<T> fromIndex, RowIndex<T> toIndex) {
		fromIndex.setDisplayedIndex(toIndex.getDisplayedIndex());
		changeDisplayIndex(fromIndex.next(), toIndex.next(), -1);
		
		if (fromIndex == _head) {
			_head = fromIndex.next();
		}
		if (toIndex == _tail) {
			_tail = fromIndex;
		}
		ensureSibling(fromIndex.previous(), fromIndex.next());
		RowIndex<T> tmp = toIndex.next();
		ensureSibling(toIndex, fromIndex);
		ensureSibling(fromIndex, tmp);
	}

	RowIndex<T> none() {
		return RowIndex.none();
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

}
