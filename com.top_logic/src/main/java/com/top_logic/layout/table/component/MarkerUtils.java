/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * A collection of methods to support the marker columns in tables.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MarkerUtils {

    /**
     * Determine all row objects of marked rows
     *
     * @return the row objects of the marked rows, never <code>null</code>.
     */
    public static Collection getMarkedObjects(TableComponent aTableComponent, String aMarkerColumnName) {
        EditableRowTableModel theTableModel = aTableComponent.getTableModel();
        return MarkerUtils.getMarkedObjects(theTableModel, aMarkerColumnName);
    }
    
    
    public static Collection getMarkedObjects(EditableRowTableModel theTableModel, String aMarkerColumnName) {
		List theRowObjects = theTableModel.getDisplayedRows();

        int theMarkerColumn = 0;
        for (theMarkerColumn = 0; theMarkerColumn < theTableModel.getColumnCount(); theMarkerColumn++) {
            if (aMarkerColumnName.equals(theTableModel.getColumnName(theMarkerColumn))) {
                break;
            }
        }
        if (theMarkerColumn >= theTableModel.getColumnCount()) {
            return Collections.EMPTY_LIST;
        }

        List theMarkedObjects = new ArrayList(theRowObjects.size());
        for (int i = 0; i < theTableModel.getRowCount(); i++) {
            BooleanField theField = (BooleanField) theTableModel.getValueAt(i, theMarkerColumn);
            if (theField.getAsBoolean()) {
                theMarkedObjects.add(theTableModel.getRowObject(i));
            }
        }
        return theMarkedObjects;
    }

}

