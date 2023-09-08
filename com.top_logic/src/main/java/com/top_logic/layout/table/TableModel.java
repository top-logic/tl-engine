/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.model.export.AccessContext;

/**
 * Common interface for all table models that can be displayed by a
 * {@link TableControl}.
 * 
 * <p>
 * This interface is modeled in the style of
 * {@link javax.swing.table.TableModel}. The following differences exist (which
 * prevented reusing the Swing version of this interface):
 * </p>
 * 
 * <ul>
 * <li>The method {@link javax.swing.table.TableModel#getColumnClass(int)} is
 * not required by implementations of this interface.</li>
 * 
 * <li>The method
 * {@link javax.swing.table.TableModel#isCellEditable(int rowIndex, int columnIndex)}
 * is not required by implementations of this interface.</li>
 * 
 * <li>The method {@link #getColumnName(int)} has slightly different semantics.
 * </li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableModel {

	/**
	 * Represents "no row" when a method returns a row number.
	 */
	int NO_ROW = -1;

	/**
	 * Description of the table header.
	 */
	Header getHeader();

	/**
	 * Index of the column with the given name.
	 * 
	 * <p>
	 * The name is compared case-sensitive.
	 * </p>
	 * 
	 * 
	 * @param columnName
	 *        The column to look up its current index.
	 * @return Index of the column with the given name or {@link #NO_ROW} if no such column exists.
	 *         If non-negative, the result is to be used in {@link #getValueAt(int, int)} as column
	 *         index.
	 */
	int getColumnIndex(String columnName);

	/**
	 * The number of displayed rows.
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
    public int getRowCount();

	/**
	 * Whether there is no row displayed.
	 */
	boolean isDisplayedEmpty();

	/**
	 * All rows matching the current {@link #getFilter()} sorted by {@link #getOrder()}.
	 * 
	 * <p>
	 * Returned rows must not be changed outside.
	 * </p>
	 * 
	 * @see #getAllRows()
	 */
	public List getDisplayedRows();

	/**
	 * All row objects including those not accepted by the current {@link #getFilter()} in no
	 * particular order.
	 * 
	 * <p>
	 * Returned rows must not be changed outside.
	 * </p>
	 * 
	 * @see #getDisplayedRows()
	 */
	public Collection getAllRows();

	/**
	 * The row object at the given row index.
	 * 
	 * @param row
	 *        Index from the {@link #getDisplayedRows()} list.
	 */
	public Object getRowObject(int row);

	/**
	 * Whether the given row is member of {@link #getAllRows()}.
	 * 
	 * @see #isDisplayed(Object)
	 */
	public boolean containsRowObject(Object anObject);

	/**
	 * Whether the given row is a member of {@link #getDisplayedRows()}.
	 * 
	 * @see #containsRowObject(Object)
	 */
	boolean isDisplayed(Object rowObject);

	/**
	 * Index of the given row object in {@link #getDisplayedRows()}.
	 * 
	 * @return The requested index, or {@link #NO_ROW}, if the given object is either not part of
	 *         this model (see {@link #getAllRows()}), or currently hidden by {@link #getFilter()}.
	 */
	public int getRowOfObject(Object rowObject);

	/**
	 * Find the "nearest" displayed row for the given row object.
	 * <p>
	 * If the object itself is displayed, its row is returned.
	 * </p>
	 * <p>
	 * The exact definition of "near" is up to the concrete {@link TableModel}. For example a tree
	 * could try to find a sibling or parent, whereas a plain list of objects might just return the
	 * next or previous row.
	 * </p>
	 * 
	 * @param rowObject
	 *        Is not allowed to be null.
	 * @return {@link #NO_ROW}, if and only if no row is displayed. If there is at least one row,
	 *         this method returns a valid row number.
	 */
	int findNearestDisplayedRow(Object rowObject);

	/**
	 * Returns the rowObjects that need to be visible for rowObject to become visible.
	 * <p>
	 * In plain tables this is just the rowObject itself. But in trees, it is more complex and could
	 * be the list of ancestors, for example.
	 * </p>
	 * 
	 * @param rowObject
	 *        The row that should be made visible. Is allowed to be null, if null is a rowObject in
	 *        this table.
	 * @return The rowObjects that need to be visible for rowObject to be visible. Never null. Might
	 *         be unmodifiable.
	 */
	Collection<Object> getNecessaryRows(Object rowObject);

    /**
	 * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount();
    
    /**
	 * In contrast to the Swing version of this method (see
	 * {@link javax.swing.table.TableModel#getColumnName(int)}), the name
	 * reported for a column is required to be unique over all columns of this
	 * {@link TableModel}.
	 * 
	 * <p>
	 * This change in semantics is required, because the column name is e.g.
	 * used to identify the column for communication with a {@link CellRenderer}.
	 * </p>
	 * 
	 * <p>
	 * Note: A better choice would be to rename this method to
	 * <code>getColumnID(int)</code> to allow to implement this interface and
	 * it's Swing counterpart in the same class.
	 * </p>
	 */
    public String getColumnName(int columnIndex);

	/**
	 * The names of the columns ordered according to the index of the column. May contain
	 *         <code>null</code>.
	 */
	public List<String> getColumnNames();

	/**
	 * Updates the available columns.
	 * @throws VetoException TODO
	 * 
	 * @see #getColumnNames()
	 * @see TableModelEvent#COLUMNS_UPDATE
	 */
	void setColumns(List<String> newColumns) throws VetoException;

    /**
	 * Retrieves the value at the given row and column index.
	 * 
	 * @param rowIndex
	 *        The index of the row to retrieve.
	 * @param columnIndex
	 *        The index of the column to get the value for.
	 * @return The application value in this {@link TableModel} at the given row and column.
	 * 
	 * @see #getColumnIndex(String)
	 */
    public Object getValueAt(int rowIndex, int columnIndex);
    
    /**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex);

	/**
	 * @see #getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, String columnName);

	/**
	 * @see #setValueAt(Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, String columnName);

	/**
	 * Returns value of the object at the given position.
	 * 
	 * @param rowObject
	 *        Name of the the row object to fetch column value from. Must be the base object for a
	 *        row in the table, i.e. an object value for which {@link #containsRowObject(Object)}
	 *        return <code>true</code>.
	 * @param columnName
	 *        Name of the column to get cell value for.
	 * 
	 * @return The object in the given position. Can be <code>null</code>.
	 */
	public Object getValueAt(Object rowObject, String columnName);

    /**
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
     */
    public void addTableModelListener(TableModelListener listener);
    
    /**
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 * 
	 * @return true when Listener actually was removed.
     */
    public boolean removeTableModelListener(TableModelListener listener);
    
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
    public void updateRows(int firstRow, int lastRow);

    /**
     * aColumnName the {@link ColumnConfiguration} declared for the given column, or the default description.
     *         Never <code>null</code>.
     */
    public ColumnConfiguration getColumnDescription(int aCol);

    /**
     * @param aColumnName the name of the column, never <code>null</code>.
     * @return aColumnName the {@link ColumnConfiguration} declared for the given column, or the default description.
     *         Never <code>null</code>.
     */
    public ColumnConfiguration getColumnDescription(String aColumnName);

	/**
	 * The display configuration for this table.
	 */
	TableConfiguration getTableConfiguration();

	/**
	 * Prepares this model for a following bulk access operation.
	 * 
	 * @param accessedRows
	 *        The row objects that will be accessed.
	 * @param accessedColumns
	 *        The columns that will be accessed.
	 * @return A handle to release resources after access.
	 */
	AccessContext prepareRows(Collection<?> accessedRows, List<String> accessedColumns);

	/**
	 * The currently active {@link Filter}.
	 */
	TableRowFilter getFilter();

	/**
	 * Sets the current filter and order.
	 * 
	 * <p>
	 * Note: Updating both in one call is necessary to avoid sorting rows twice (once after updating
	 * the filter and once after updating the order).
	 * </p>
	 * 
	 * @param filter
	 *        See {@link #getFilter()}.
	 * @param order
	 *        See {@link #getOrder()}.
	 */
	void setFilter(TableRowFilter filter, Comparator order);

	/**
	 * Forces revalidation of active or visible filters and therefore updating their actual or
	 * potential match counts. In contrast to {@link #setFilter(TableRowFilter, Comparator)} the
	 * currently displayed rows will not be affected.
	 *
	 */
	void revalidateFilterMatchCount();

	/**
	 * The currently active sort order.
	 */
	Comparator getOrder();

	/**
	 * @see #getOrder()
	 */
	void setOrder(Comparator order);

	/**
	 * Whether filters should count their matches.
	 */
	boolean isFilterCountingEnabled();

}
