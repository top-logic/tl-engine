/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * This class uses for table components the getTableModel to check if the model is null or
 * empty and for all other components the getModel method.
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class EmptyTableModelDisabledRule extends ExtendedNullModelDisabledRule {

    /** The single instance of this class. */
    public static final EmptyTableModelDisabledRule EMPTY_TABLE_DISABLED_RULE = new EmptyTableModelDisabledRule();


    /**
     * This method returns the executable state of the table component.
     *
     * @param aTableComponent
     *            A {@link TableComponent} must NOT be <code>null</code>.
     * @return Returns a {@link ExecutableState}.
     */
    @Override
	protected ExecutableState handleTableComponent(TableComponent aTableComponent) {
        EditableRowTableModel tableModel = aTableComponent.getTableModel();
        if (tableModel != null && tableModel.getRowCount() > 0) {
            return ExecutableState.EXECUTABLE;
        } else {
            return NullModelDisabled.EXEC_STATE_DISABLED;
        }
    }

}
