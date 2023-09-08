/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.table.TableModel;

/**
 * Implementation of the event listener handling defined in {@link TableModel}.
 * 
 * <p>
 * A derived concrete {@link TableModel} implementation must use
 * {@link #fireTableModelEvent(int, int, int)} to inform its observers about changes of the model.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTableModel implements TableModel, OnColumnChange {
	
	private final Header _header;

	private HashSet/*<TableModelListener>*/ listeners;

	public AbstractTableModel(TableConfiguration config, List<String> columnNames, boolean allVisible) {
		assert config != null : "No table configuration.";

		_header = new Header(config, columnNames, allVisible, this);
	}

	@Override
	public void handleColumnsChanged() {
		// Hook for subclasses.
	}

	@Override
	public Header getHeader() {
		return _header;
	}

	@Override
	public final int getColumnIndex(String columnName) {
		Column column = _header.getColumn(columnName);
		if (column == null) {
			return -1;
		}
		return column.getIndex();
	}

	@Override
	public final TableConfiguration getTableConfiguration() {
		return _header.getTableConfiguration();
	}

	@Override
	public final int getColumnCount() {
		return _header.getColumnNames().size();
	}

	@Override
	public final String getColumnName(int columnIndex) {
		return _header.getColumns().get(columnIndex).getName();
	}

	@Override
	public final List<String> getColumnNames() {
		return _header.getColumnNames();
	}

	@Override
	public void setColumns(List<String> newColumns) throws VetoException {
		ArrayList<String> oldColumns = new ArrayList<>(getColumnNames());
		newColumns = initColumns(newColumns);

		fireTableColumnEvent(oldColumns, newColumns);
	}

	/**
	 * Updates the column index without sending events.
	 * 
	 * @return The (potentially adjusted) list of new column names.
	 */
	protected final List<String> initColumns(List<String> newColumns) {
		_header.setVisibleColumns(newColumns);
		return _header.getColumnNames();
	}

	/**
	 * Checks, whether this model is observed by any {@link TableModelListener}s.
	 */
	protected final boolean hasTableModelListeners() {
		return listeners != null && !listeners.isEmpty(); 
	}

	/**
	 * Triggers an update for the given range of rows.
	 * 
	 * <p>
	 * A call to this method is necessary, if this model is a view of external
	 * data that cannot be observed directly. If this external data changes, the
	 * table view has to be informed about the change to forward the update to
	 * its view.
	 * </p>
	 * 
	 * @param firstRow
	 *     The first row that has been externally modified. 
	 * @param lastRow
	 *     The last row that has been externally modified. 
	 */
	@Override
	public void updateRows(int firstRow, int lastRow) {
		fireTableModelEvent(firstRow, lastRow, TableModelEvent.UPDATE);
	}

	/**
	 * Informs all listeners of this model about the occurrence of a {@link TableModelEvent}.
	 * 
	 * <p>
	 * If this model {@link #hasTableModelListeners() has listeners}, a new {@link TableModelEvent}
	 * is constructed and sent to all currently registered listeners.
	 * </p>
	 * 
	 * @param firstRow
	 *        See {@link TableModelEvent#getFirstRow()}.
	 * @param lastRow
	 *        See {@link TableModelEvent#getLastRow()}.
	 * @param type
	 *        See {@link TableModelEvent#getType()}.
	 */
	protected void fireTableModelEvent(int firstRow, int lastRow, int type) {
		if (! hasTableModelListeners()) return;
		
		assert type != TableModelEvent.COLUMNS_UPDATE : "Fire columns update using fireTableColumnEvent(...)";
		fireTableModelEvent(new TableModelEvent(this, firstRow, lastRow, type));
	}

	/**
	 * Informs all listeners of this model about the occurrence of a {@link TableModelEvent} of type
	 * {@link TableModelEvent#COLUMNS_UPDATE}.
	 * 
	 * <p>
	 * If this model {@link #hasTableModelListeners() has listeners}, a new
	 * {@link TableModelColumnsEvent} is constructed and sent to all currently registered listeners.
	 * </p>
	 * 
	 * @param oldColumns
	 *        See {@link TableModelColumnsEvent#oldColumns()}.
	 * @param newColumns
	 *        See {@link TableModelColumnsEvent#newColumns()}.
	 */
	protected void fireTableColumnEvent(List<String> oldColumns, List<String> newColumns) {
		if (!hasTableModelListeners())
			return;

		fireTableModelEvent(new TableModelColumnsEvent(this, oldColumns, newColumns));
	}

	private void fireTableModelEvent(TableModelEvent event) {
		if (listeners == null) return;
		
        // Copy to prevent concurrent modification exceptions in case a listener
        // tries to remove itself as response to an event.
        TableModelListener[] currentListeners = 
            (TableModelListener[]) listeners.toArray(new TableModelListener[listeners.size()]);
        
		for (int n = 0, cnt = currentListeners.length; n < cnt; n++) {
			currentListeners[n].handleTableModelEvent(event);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		assert listener != null : "Will not add a \"null\"-listener.";
		
		if (listeners == null) {
			listeners = new HashSet();
		}
		
		listeners.add(listener);
	}

	@Override
	public boolean removeTableModelListener(TableModelListener listener) {
		if (! hasTableModelListeners()) return false;
		
		return listeners.remove(listener);
	}
	
	@Override
	public final ColumnConfiguration getColumnDescription(String aColumnName) {
		Column column = _header.getColumn(aColumnName);
		assert column != null : "No such column: " + aColumnName;
		return column.getConfig();
	}

	@Override
	public Object getValueAt(int rowIndex, String columnName) {
		return getValueAt(rowIndex, getColumnIndex(columnName));
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, String columnName) {
		setValueAt(aValue, rowIndex, getColumnIndex(columnName));
	}

	@Override
	public boolean isDisplayedEmpty() {
		return getRowCount() == 0;
	}

	@Override
	public boolean isDisplayed(Object rowObject) {
		return getRowOfObject(rowObject) != NO_ROW;
	}

}
