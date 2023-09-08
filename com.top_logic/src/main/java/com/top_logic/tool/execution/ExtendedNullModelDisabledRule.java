/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * This class uses for table components the getTableModel to check if the model is null and
 * for all other components the getModel method.
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ExtendedNullModelDisabledRule extends NullModelDisabled {

	/** Name under which this rule is found in {@link ExecutabilityRuleManager}. */
	public static final String RULE_ID = "ExtendedNullModelDisabled";

    /** The single instance of this class. */
    public static final ExtendedNullModelDisabledRule EXTENDED_NULL_RULE = new ExtendedNullModelDisabledRule();


    /**
     * The framework uses this constructor to create new
     * instances of {@link ExtendedNullModelDisabledRule}s.
     */
    public ExtendedNullModelDisabledRule() {
        super();
    }


    /**
     * Overridden to handle table components. The model of the table is the table model
     * that isn't returned by the getModel method. It isn't possible to use the getModel
     * method.
     *
     * @see com.top_logic.tool.execution.NullModelDisabled#isExecutable(LayoutComponent, Object, Map)
     */
    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        if (aComponent instanceof TableComponent) {
            return handleTableComponent((TableComponent)aComponent);
        }
        return super.isExecutable(aComponent, model, someArguments);
    }

    /**
     * This method returns the executable state of the table component.
     *
     * @param aTableComponent
     *            A {@link TableComponent} must NOT be <code>null</code>.
     * @return Returns a {@link ExecutableState}.
     */
    protected ExecutableState handleTableComponent(TableComponent aTableComponent) {
        EditableRowTableModel tableModel = aTableComponent.getTableModel();
        return tableModel != null ? ExecutableState.EXECUTABLE : NullModelDisabled.EXEC_STATE_DISABLED;
    }

}
