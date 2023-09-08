/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.Equality;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.ColumnFilterHolder;
import com.top_logic.layout.table.TableRowFilter;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.util.Utils;

/**
 * {@link AbstractTableModel} that implements the management of columns and visible rows.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
// Since TableModel has no type parameters yet.
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractObjectTableModel extends AbstractTableModel {

	/**
	 * Comparator that is used when no order was set.
	 * 
	 * @see #getOrder()
	 */
	public static final Comparator<Object> DEFAULT_ORDER = Equality.INSTANCE;

	private TableRowFilter filter = new TableRowFilter(Collections.<ColumnFilterHolder> emptyList());

	private Comparator order = DEFAULT_ORDER;

	/**
	 * Creates a {@link AbstractObjectTableModel}.
	 * 
	 * @param config
	 *        See {@link #getTableConfiguration()}.
	 * @param columnNames
	 *        See {@link #getColumnNames()}.
	 */
	public AbstractObjectTableModel(TableConfiguration config, List<String> columnNames) {
		super(config, columnNames, true);
	}

	@Override
	public TableRowFilter getFilter() {
		return filter;
	}

	@Override
	public void setFilter(TableRowFilter filter, Comparator order) {
		this.filter = filter;
		this.order = order;
		revalidateFilter(true);
		fireTableModelEvent(0, getRowCount() - 1, TableModelEvent.INVALIDATE);
	}

	/**
	 * Applies the filter and sort oder after a changes.
	 * 
	 * @see #setFilter(TableRowFilter, Comparator)
	 */
	protected abstract void revalidateFilter(boolean updateDisplayedRows);

	@Override
	public void revalidateFilterMatchCount() {
		revalidateFilter(false);
	}

	@Override
	public Comparator getOrder() {
		return order;
	}

	@Override
	public void setOrder(Comparator order) {
		this.order = order;
		revalidateOrder();
		fireTableModelEvent(0, getRowCount() - 1, TableModelEvent.INVALIDATE);
	}

	/**
	 * Applies the sort order after a change.
	 * 
	 * @see #setOrder(Comparator)
	 */
	protected abstract void revalidateOrder();

	@Override
	public Object getRowObject(int row) {
		return getDisplayedRows().get(row);
	}

	@Override
	public int getRowCount() {
		return getDisplayedRows().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueAt(getDisplayedRows().get(rowIndex), getColumnName(columnIndex));
	}

	/**
	 * @throws UnsupportedOperationException
	 *         if no {@link Accessor} has been defined for the given column
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) throws UnsupportedOperationException {
		String columnName = getColumnName(columnIndex);
		ColumnConfiguration columnDescription = getColumnDescription(columnName);
		Accessor accessor = columnDescription.getAccessor();
		if (accessor == null) {
			throw new UnsupportedOperationException("No accessor defined for column " + columnName);
		}
		CellExistenceTester cellExistenceTester = columnDescription.getCellExistenceTester();
		Object rowObject = getDisplayedRows().get(rowIndex);
		if (!cellExistenceTester.isCellExistent(rowObject, columnName)) {
			return;
		}
		accessor.setValue(rowObject, columnName, aValue);
	}

	@Override
	public Object getValueAt(Object rowObject, String columnName) {
		ColumnConfiguration columnDescription = getColumnDescription(columnName);
		CellExistenceTester cellExistenceTester = columnDescription.getCellExistenceTester();
		if (cellExistenceTester.isCellExistent(rowObject, columnName)) {
			Accessor accessor = columnDescription.getAccessor();
			if (accessor != null) {
				return Utils.createSortedListForDisplay(accessor.getValue(rowObject, columnName));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * aColumnName the {@link ColumnConfiguration} declared for the given column, or the
	 *         default description. Never <code>null</code>.
	 */
	@Override
	public ColumnConfiguration getColumnDescription(int aCol) {
		return this.getColumnDescription(this.getColumnName(aCol));
	}

}
