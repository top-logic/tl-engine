/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

/**
 * Enhanced {@link EditableTableModel}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface EditableRowTableModel extends EditableTableModel {
    
    /**
     * Use Elements of newRowObjects as new rows.
     * 
     * @param newRowObjects is copied to internal model.
     */
    public void setRowObjects(List newRowObjects);
    
    public void addRowObject(Object rowObject);
    
    public void addAllRowObjects(List newRows);
    
    public void insertRowObject(int row, Object rowObject);
    
    public void insertRowObject(int row, List newRows);
    
    public void removeRowObject(Object rowObject);

	/**
	 * Explicitly show a row at a certain position ignoring current sort order and filter settings.
	 * 
	 * <p>
	 * If the given row object is not yet part of this model, it is added to this model.
	 * </p>
	 * 
	 * @param rowObject
	 *        The row to insert/display/move.
	 * @param beforeRow
	 *        The position in the view model where to show the row. The position is given as row in
	 *        the view model before which the application row should be shown. The value
	 *        {@link #getRowCount()} shows the row at the end of this table.
	 */
	public void showRowAt(Object rowObject, int beforeRow);
    
	/**
	 * Removes the rows from the start index (inclusive) to the stop index (exclusive).
	 * 
	 * @param start
	 *        The index of the first row to remove.
	 * @param stop
	 *        The index after the last row to remove.
	 */
	public void removeRows(int start, int stop);

    /**
     * Removes all rows.
     */
    public void clear();
    
}

