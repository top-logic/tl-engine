/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.layout.table.TableRowFilter;

/**
 * Data structure of an {@link ObjectTableModel} that allows abstraction of priority table and
 * simple table.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
interface TableModelDataStructure<T> {

	/**
	 * Moves the visible row <code>from</code> such that it is placed index <code>to</code> after
	 * operation.
	 */
	void moveRow(int from, int to);

	/**
	 * Removes the objects on the visible row with index <code>first</code> to the row with index
	 * <code>stop-1</code>.
	 */
	void removeRows(int first, int stop);

	/**
	 * Removes the object on the given visible row.
	 */
	void removeRow(int removedRow);

	/**
	 * Removes the given row object, independent of its visibility.
	 */
	int removeRowObject(Object rowObject);

	/**
	 * Inserts all rows at the given visible position.
	 */
	void insertRows(int row, List<? extends T> newRows);

	/**
	 * @see ObjectTableModel#getRowOfObject(Object)
	 */
	int getRowOfObject(Object rowObject);

	/**
	 * @see ObjectTableModel#containsRowObject(Object)
	 */
	boolean containsRow(Object anObject);

	/**
	 * Clears the complete table
	 */
	void removeAll();

	/**
	 * @see ObjectTableModel#revalidateOrder()
	 */
	void revalidateOrder();

	/**
	 * @see ObjectTableModel#revalidateFilter(boolean)
	 */
	void revalidateFilter(boolean updateDisplayedRows);

	/**
	 * true, if the given rowObject has been checked by {@link TableRowFilter}, false
	 *         otherwise.
	 */
	boolean isValidated(Object rowObject);

	/**
	 * Initialises the {@link ObjectTableModel} with the given list of all rows (including
	 * filtered).
	 */
	void initRowObjects(List<? extends T> newRowObjects);

	/**
	 * @see ObjectTableModel#getAllRows()
	 */
	Collection<T> getAllRows();

	/**
	 * Adds the given rowObject to the end of the displayed rows.
	 */
	int addRow(T rowObject);

	/**
	 * Adds the given rowObject to the given position in the displayed rows.
	 */
	void addRow(T rowObject, int displayedRow);

	/**
	 * Adds the given row objects to the rows.
	 * 
	 * @return The index of the first and last new row. If the collection is empty, both are -1.
	 */
	Pair<Integer, Integer> addRows(Collection<? extends T> newRowObjects);

	/**
	 * Returns the position where the given row object would be inserted in the displayed rows.
	 */
	int insertPosition(T rowObject);

}

