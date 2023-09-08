/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.table.control.TableListControl;

/**
 * The RowObjectCreator is able to create a new row object for a table. It is required for
 * the TableListControl to add new rows to the table.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public interface RowObjectCreator {

    /**
	 * Creates a new row object for the table this {@link RowObjectCreator} is registered at.
	 * 
	 * <p>
	 * The implementation must create the new row object and either return it or directly
	 * {@link TableListControl#addNewRow(Object) add it} to the given control and return
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param aControl
	 *        The {@link TableListControl} the new row should be added to.
	 *
	 * @return The new row object to add, or <code>null</code> if no action should be performed.
	 */
	Object createNewRow(Control aControl);

	/**
	 * Decides whether it is allowed to create a new row below the currently selected row.
	 *
	 * @param row
	 *        the line number of the selected row; may be <code>-1</code> to indicate no row is
	 *        selected
	 * @param data
	 *        {@link TableData} to create row in.
	 * @param control
	 *        the control which wants to know whether new row creation is allowed.
	 * @return <code>null</code> if it is allowed to create a new row below the selected one, a
	 *         {@link ResKey} with the reason otherwise.
	 */
	default ResKey allowCreateNewRow(int row, TableData data, Control control) {
		return null;
	}

}
