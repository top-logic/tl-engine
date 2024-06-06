/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.util.Utils;

/**
 * {@link FormMemberProvider} adding fields for the {@link #MARKER_COLUMN_NAME}.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MarkerFormMemberProvider implements FormMemberProvider {

    public static final String MARKER_COLUMN_NAME = "marker";

    public static final String MARKER_COLUMN_HEAD_FIELD_NAME = "markerHead";

    /**
     * @see com.top_logic.layout.table.component.FormMemberProvider#addFormMembers(java.lang.Object, com.top_logic.layout.form.FormContainer)
     */
    @Override
	public void addFormMembers(Object aModel, FormContainer aFormContainer) {

        FormTableModel theTableModel = (FormTableModel) aModel;

        for (int theMarkerColumn = 0; theMarkerColumn < theTableModel.getColumnCount(); theMarkerColumn++) {
            if (MARKER_COLUMN_NAME.equals(theTableModel.getColumnName(theMarkerColumn))) {
                FormContainer theColumnGroup = theTableModel.getColumnGroup(theMarkerColumn);
                BooleanField theMarkerHeadField = FormFactory.newBooleanField(ColumnConfiguration.COLUMN_CONTROL_TYPE_HEADER);
                theColumnGroup.addMember(theMarkerHeadField);
                Set theCellfields = initFromTableModel(theTableModel, theMarkerColumn);
				theMarkerHeadField.initializeField(determinAccumulatedValue(theCellfields));
                MarkerHeadListener theMarkerHeadListener = new MarkerHeadListener(theCellfields, theMarkerColumn);
                theMarkerHeadField.addValueListener(theMarkerHeadListener);
                MarkerCellListener theMarkerCellListener = new MarkerCellListener(theMarkerHeadField, theCellfields, theMarkerColumn);
                theTableModel.addTableModelListener(theMarkerCellListener);
                theTableModel.addTableModelListener(theMarkerHeadListener);
            }
        }


    }

    private static Set initFromTableModel(FormTableModel theTableModel, int theMarkerColumn) {
        int theRowCount = theTableModel.getRowCount();
        Set theCellfields = new HashSet();
        for (int i = 0; i < theRowCount; i++) {
            theCellfields.add(theTableModel.getValueAt(i, theMarkerColumn));
        }
        return theCellfields;
    }

    private static Boolean determinAccumulatedValue(Collection someFields) {
        if (CollectionUtil.isEmptyOrNull(someFields)) {
            return null;
        }
        Boolean      theResult;
        Iterator     theIt     = someFields.iterator();
        BooleanField theField  = (BooleanField) theIt.next();
        theResult = Boolean.valueOf(theField.getAsBoolean());
        while (theIt.hasNext()) {
            theField = (BooleanField) theIt.next();
            if ( ! Boolean.valueOf(theField.getAsBoolean()).equals(theResult)) {
                theResult = null;
                break;
            }
        }
        return theResult;
    }

    private static class MarkerCellListener implements ValueListener, TableModelListener {
        private BooleanField markerHeadField;
        private Set cellFields;
        private int markerColumn;
        public MarkerCellListener(BooleanField aMarkerField, Set someCellFields, int aMarkerColumn) {
            this.markerHeadField = aMarkerField;
            this.cellFields = someCellFields;
            this.markerColumn = aMarkerColumn;
            for (Iterator theIt = someCellFields.iterator(); theIt.hasNext();) {
                FormField theField = (FormField) theIt.next();
                theField.addValueListener(this);

            }
        }
        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
//            Iterator theIt = this.cellFields.iterator();
//            // there must be at least one (the one triggering the event).
//            Boolean theResult = (Boolean) ((BooleanField) theIt.next()).getValue();
//
//            while (theIt.hasNext()) {
//                BooleanField theField = (BooleanField) theIt.next();
//                Boolean theValue = (Boolean) theField.getValue();
//                if (theValue != theResult) {
//                    theResult = null;
//                }
//            }
            this.markerHeadField.setValue(determinAccumulatedValue(this.cellFields));
        }
        /**
         * @see com.top_logic.layout.table.model.TableModelListener#handleTableModelEvent(com.top_logic.layout.table.model.TableModelEvent)
         */
        @Override
		public void handleTableModelEvent(TableModelEvent aEvent) {
            FormTableModel theTableModel = (FormTableModel) aEvent.getSource();
            if (aEvent.getType() == TableModelEvent.INSERT) {
                for (int i = aEvent.getFirstRow(); i <= aEvent.getLastRow(); i++) {
                    FormField theField = (FormField) theTableModel.getValueAt(i, this.markerColumn);
                    this.cellFields.add(theField);
                    theField.addValueListener(this);
                    this.markerHeadField.setValue(determinAccumulatedValue(this.cellFields));
                }
            } else if (aEvent.getType() == TableModelEvent.DELETE) {
                this.cellFields = MarkerFormMemberProvider.initFromTableModel(theTableModel, this.markerColumn);
                this.markerHeadField.setValue(determinAccumulatedValue(this.cellFields));
            }
        }
    }

    private static class MarkerHeadListener implements ValueListener, TableModelListener {
        private Set cellFields;
        private int markerColumn;
        public MarkerHeadListener(Set someCellFields, int aMarkerColumn) {
            this.cellFields = someCellFields;
            this.markerColumn = aMarkerColumn;
        }
        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {

            if (aNewValue == null) {
                return;
            }

            Iterator theIt = this.cellFields.iterator();
            while (theIt.hasNext()) {
                BooleanField theField = (BooleanField) theIt.next();
                if ( ! Utils.equals(theField.getValue(), aNewValue)) {
                    theField.setValue(aNewValue);
                }
            }
        }
        /**
         * @see com.top_logic.layout.table.model.TableModelListener#handleTableModelEvent(com.top_logic.layout.table.model.TableModelEvent)
         */
        @Override
		public void handleTableModelEvent(TableModelEvent aEvent) {
            FormTableModel theTableModel = (FormTableModel) aEvent.getSource();
            if (aEvent.getType() == TableModelEvent.INSERT) {
                for (int i = aEvent.getFirstRow(); i <= aEvent.getLastRow(); i++) {
                    FormField theField = (FormField) theTableModel.getValueAt(i, this.markerColumn);
                    this.cellFields.add(theField);
                }
            } else if (aEvent.getType() == TableModelEvent.DELETE) {
                this.cellFields = MarkerFormMemberProvider.initFromTableModel(theTableModel, this.markerColumn);
            }
        }
    }

}

