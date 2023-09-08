/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.model.export.AccessContext;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.Preloader;

/**
 * A {@link EditableTableModel} implementation in which each row is represented
 * by a generic {@link Object}.
 * 
 * <p>
 * The values of the columns of each row are accessed through an
 * {@link Accessor} instance that defines the access to a row object, if a
 * column name is given.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
//Since TableModel has no type parameters yet.
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ObjectTableModel extends AbstractObjectTableModel implements EditableRowTableModel {

	/**
	 * List of currently displayed rows matching {@link #filter} sorted by {@link #order}.
	 */
	private final List _rows = new ArrayList();

	/**
	 * Unmodifiable version of {@link #_rows}.
	 */
	private final List _unmodifiableRows = Collections.unmodifiableList(_rows);

	private final TableModelDataStructure _dataStructure;

	/**
	 * Creates a {@link ObjectTableModel} without priority sort.
	 * 
	 * @see ObjectTableModel#ObjectTableModel(String[], TableConfiguration, List, boolean)
	 */
	public ObjectTableModel(String[] columnNames, TableConfiguration config, List rows) {
		this(columnNames, config, rows, false);
	}

	/**
	 * Creates a {@link ObjectTableModel}.
	 * 
	 * @param columnNames
	 *        See {@link #getColumnNames()}
	 * @param config
	 *        See {@link TableModel#getTableConfiguration()}
	 * @param rows
	 *        The row objects.
	 * @param priorityTable
	 *        Whether the table should be a priority table.
	 */
	public ObjectTableModel(String[] columnNames, TableConfiguration config, List rows, boolean priorityTable) {
		this(toList(columnNames), config, rows, priorityTable);
	}

	private static List<String> toList(String[] columnNames) {
		if (columnNames == null) {
			return null;
		}
		return Arrays.asList(columnNames);
	}

	/**
	 * Creates a {@link ObjectTableModel} without priority sort.
	 * 
	 * @see ObjectTableModel#ObjectTableModel(List, TableConfiguration, List, boolean)
	 */
	public ObjectTableModel(List<String> columnNames, TableConfiguration config, List rows) {
		this(columnNames, config, rows, false);
	}

	/**
	 * Creates a {@link ObjectTableModel}.
	 * 
	 * @param columnNames
	 *        See {@link #getColumnNames()}
	 * @param config
	 *        See {@link TableModel#getTableConfiguration()}
	 * @param rows
	 *        The row objects.
	 * @param priorityTable
	 *        Whether the table should be a priority table.
	 */
	public ObjectTableModel(List<String> columnNames, TableConfiguration config, List rows, boolean priorityTable) {
		super(deactivateColumnComparators(config, priorityTable), columnNames);
		if (priorityTable) {
			_dataStructure = new PriorityTableModelDataStructure(this);
		} else {
			_dataStructure = new SimpleTableModelStructure(this);
		}
		initRowObjects(rows);
	}

	private static TableConfiguration deactivateColumnComparators(TableConfiguration config, boolean priorityTable) {
		if (priorityTable) {
			config.getDefaultColumn().setComparator(null);
			for (ColumnConfiguration columnConfiguration : config.getDeclaredColumns()) {
				columnConfiguration.setComparator(null);
			}
		}
		return config;
	}

	@Override
	public Collection getAllRows() {
		return _dataStructure.getAllRows();
	}

	/**
	 * Use elements of newRowObjects as new rows().
	 * 
	 * @param newRowObjects
	 *        is copied to internal model.
	 */
	@Override
	public void setRowObjects(List newRowObjects) {
		initRowObjects(newRowObjects);
		fireTableModelEvent(0, rows().size() - 1, TableModelEvent.INVALIDATE);
	}

	/**
	 * Internally sets the base list of rows without firing events.
	 */
	protected void initRowObjects(List newRowObjects) {
		getFilter().startFilterRevalidation();
		_dataStructure.initRowObjects(newRowObjects);
		getFilter().stopFilterRevalidation();
		revalidateOrder();
	}

	@Override
	protected void revalidateFilter(boolean updateDisplayedRows) {
		getFilter().startFilterRevalidation();
		_dataStructure.revalidateFilter(updateDisplayedRows);
		getFilter().stopFilterRevalidation();
		if (updateDisplayedRows) {
			revalidateOrder();
		}
	}
	
	/**
	 * @see TableModelDataStructure#isValidated(Object)
	 */
	public boolean isValidated(Object rowObject) {
		return _dataStructure.isValidated(rowObject);
	}

	@Override
	public List getDisplayedRows() {
		return _unmodifiableRows;
	}

	List rows() {
		return _rows;
	}

	@Override
	protected void revalidateOrder() {
		_dataStructure.revalidateOrder();
	}

	@Override
	public void clear() {
		List rows = rows();
		int sizeBefore = rows.size();
		_dataStructure.removeAll();

		if (sizeBefore > 0) {
			rows.clear();
			fireTableModelEvent(0, sizeBefore - 1, TableModelEvent.DELETE);
		}
	}

	@Override
	public boolean containsRowObject(Object anObject) {
		return _dataStructure.containsRow(anObject);
	}

	@Override
	public int getRowOfObject(Object rowObject) {
		return _dataStructure.getRowOfObject(rowObject);
	}

	/**
	 * <p>
	 * Finds the previous displayed element. If there is none, returns the next. (In this case, the
	 * "next" element is always the first element in the table.)
	 * </p>
	 * 
	 * @see AbstractObjectTableModel#findNearestDisplayedRow(Object)
	 */
	@Override
	public int findNearestDisplayedRow(Object rowObject) {
		if (isDisplayed(rowObject)) {
			return getRowOfObject(rowObject);
		}
		NearestDisplayedRowFinder<Object> nearestDisplayedRowFinder =
			new NearestDisplayedRowFinder<Object>(getDisplayedRows(), getAllRows(), getOrder());
		Maybe<Object> nearestDisplayedRow = nearestDisplayedRowFinder.find(rowObject);
		if (!nearestDisplayedRow.hasValue()) {
			return NO_ROW;
		}
		return getRowOfObject(nearestDisplayedRow.get());
	}

	@Override
	public Collection<Object> getNecessaryRows(Object rowObject) {
		return Collections.singleton(rowObject);
	}

	@Override
	public void addRowObject(Object rowObject) {
		if (containsRowObject(rowObject)) {
			return;
		}

		int addedRow = _dataStructure.addRow(rowObject);

		fireTableModelEvent(addedRow, addedRow, TableModelEvent.INSERT);
	}

	@Override
	public void addAllRowObjects(List newRows) {
		Collection<Object> filteredRows = filterOutExistingRows(newRows);
		Pair<Integer, Integer> changeRange = _dataStructure.addRows(filteredRows);
		int firstAddedRow = changeRange.getFirst();
		int lastAddedRow = changeRange.getSecond();
		if (changeRange.getFirst() >= 0) {
			fireTableModelEvent(firstAddedRow, lastAddedRow, TableModelEvent.INSERT);
		}
	}

	private Collection<Object> filterOutExistingRows(Collection<?> rows) {
		List<Object> newCollection = list();
		for (Object row : rows) {
			if (!containsRowObject(row)) {
				newCollection.add(row);
			}
		}
		return newCollection;
	}

	@Override
	public void insertRowObject(int row, Object rowObject) {
		if (row < 0 || row > rows().size()) {
			throw new IndexOutOfBoundsException(
				"Row must neither be negative nor greater than number of displayed rows.");
		}

		_dataStructure.addRow(rowObject, row);
		fireTableModelEvent(row, row, TableModelEvent.INSERT);
	}

	@Override
	public void insertRowObject(int row, List newRows) {
		if (newRows.isEmpty()) {
			return;
		}
		List rows = rows();
		int size = rows.size();
		if (row < 0 || row > size) {
			throw new IndexOutOfBoundsException(
				"Row must neither be negative nor greater than number of displayed rows.");
		}

		_dataStructure.insertRows(row, newRows);
		fireTableModelEvent(row, row + newRows.size() - 1, TableModelEvent.INSERT);
	}

	@Override
	public void removeRowObject(Object rowObject) {
		int removedRow = _dataStructure.removeRowObject(rowObject);
		if (removedRow >= 0) {
			fireTableModelEvent(removedRow, removedRow, TableModelEvent.DELETE);
		}
	}

	/**
	 * returns true. each row can be removed. 
	 * 
	 * @see com.top_logic.layout.table.model.EditableTableModel#canRemove(int)
	 */
	@Override
	public boolean canRemove(int rowNr) {
		return true;
	}
	
	@Override
	public void removeRow(int removedRow) {
		List rows = rows();
		if (removedRow >= rows.size() || removedRow < 0) {
			throw new IllegalArgumentException("Row index, that shall be removed, is out of range! Range: [0, " + (rows.size() - 1)
				+ "], RowIndex: " + removedRow);
		}
		
		_dataStructure.removeRow(removedRow);

		fireTableModelEvent(removedRow, removedRow, TableModelEvent.DELETE);
	}

	@Override
	public void removeRows(int start, int stop) {
		List rows = rows();
		if (stop <= start) {
			if (stop < start) {
				throw new IllegalArgumentException("Stop (" + stop + ") < start (" + start + ").");
			} else {
				return;
			}
		}
		if(start < 0 || stop > rows.size()) {
			throw new IllegalArgumentException("Row indices, that shall be removed, are out of range! Range: [0, " + (rows.size() - 1)
				+ "], RowIndices: [" + start + ", " + stop + "]");
		}
		
		_dataStructure.removeRows(start, stop);
		fireTableModelEvent(start, stop - 1, TableModelEvent.DELETE);
	}

	@Override
	public void showRowAt(Object rowObject, int beforeRow) {
		int currentRow = rows().indexOf(rowObject);
		if (currentRow >= 0) {
			moveRow(currentRow, beforeRow > currentRow ? beforeRow - 1 : beforeRow);
		} else {
			insertRowObject(beforeRow, rowObject);
		}
	}

	@Override
	public void moveRow(int from, int to) {
		if (from == to) {
			return;
		}

		_dataStructure.moveRow(from, to);

		int first = Math.min(from, to);
		int last = Math.max(from, to);
		fireTableModelEvent(first, last, TableModelEvent.UPDATE);
	}

	@Override
	public void moveRowUp(int movedRow) {
		if (movedRow == 0) {
			return;
		}
		moveRow(movedRow, movedRow - 1);
	}

	@Override
	public void moveRowDown(int movedRow) {
		if (movedRow == getRowCount() - 1) {
			return;
		}
		moveRow(movedRow, movedRow + 1);
	}

	@Override
	public void moveRowToTop(int movedRow) {
		moveRow(movedRow, 0);
	}

	@Override
	public void moveRowToBottom(int movedRow) {
		moveRow(movedRow, getRowCount() - 1);
	}

	@Override
	public AccessContext prepareRows(Collection<?> accessedObjects, List<String> accessedColumns) {
		if (accessedObjects.size() < 2) {
			return NoPrepare.INSTANCE;
		}
		TableConfiguration tableConfiguration = getTableConfiguration();

		Preloader preloader = new Preloader();
		for (String columnName : accessedColumns) {
			ColumnConfiguration configuration = tableConfiguration.getCol(columnName);
			configuration.getPreloadContribution().contribute(preloader);
		}

		final PreloadContext context = new PreloadContext();
		preloader.prepare(context, accessedObjects);
		return context;
	}

	@Override
	public boolean isFilterCountingEnabled() {
		return true;
	}

}
