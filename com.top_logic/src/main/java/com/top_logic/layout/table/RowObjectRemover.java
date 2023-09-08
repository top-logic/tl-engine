/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;

/**
 * The RowObjectCreator does cleanup work after a existing row object was removed from a
 * table. It is useful for the TableListControl to remove rows from the table.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public interface RowObjectRemover {

    /**
     * Does cleanup work after removing a row object from a table.
     *
     * @param aRowObject
     *            the row object which was removed from the table
     * @param aControl
     *            the control which removed a row.
     */
	void removeRow(Object aRowObject, Control aControl);

	/**
	 * Decides whether the given row is allowed to get removed or not.
	 *
	 * @param row
	 *        the line number of the selected row
	 * @param data
	 *        {@link TableData} to get more information about the row to remove
	 * @param control
	 *        the control which wants to know whether the row is allowed to get removed
	 * @return Either <code>null</code>, when removing is allowed or a {@link ResKey} with the
	 *         disabled reason, when removing is not allowed.
	 */
	default ResKey allowRemoveRow(int row, TableData data, Control control) {
		return null;
	}

}
