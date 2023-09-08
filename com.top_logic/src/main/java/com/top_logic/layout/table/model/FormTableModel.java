/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link DelegatingTableModel} that overlays cells with input elements.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FormTableModel extends DelegatingTableModel {

	private static final String ROWS_CONTAINER_NAME = "rows";

	private static final String COLUMNS_CONTAINER_NAME = "columns";

    /**
     * counter used to ensure unique names for the row groups
     */
	private int nextRowGroupId = 0;

	/**
	 * The {@link FormContainer} all input elements created by this {@link FormTableModel} are
	 * stored in.
	 */
	private final FormContainer formContainer;

	/**
	 * Container for all row groups created by this {@link FormTableModel}.
	 */
	private final FormContainer rowContainer;

	/**
	 * Container for all column groups created by this {@link FormTableModel}.
	 */
	private final FormGroup columnContainer;

    /**
     * Map from row objects to the associated row groups
     *
     * Map < Object, < {@link FormContainer} >
     */
	private final Map<Object, FormGroup> rowGroupByRowObject;

	/** Constructor without table header groups. */
	public FormTableModel(EditableRowTableModel anInner, FormContainer aFormContainer) {
        super(anInner);

		this.formContainer = aFormContainer;

		this.rowGroupByRowObject = new HashMap<>();
		this.rowContainer = new FormGroup(ROWS_CONTAINER_NAME, this.formContainer.getResources());
		this.rowContainer.setStableIdSpecialCaseMarker(this);
		this.formContainer.addMember(this.rowContainer);

		this.columnContainer = new FormGroup(COLUMNS_CONTAINER_NAME, this.formContainer.getResources());
		markTechnical(columnContainer);
		this.formContainer.addMember(this.columnContainer);

		initColumnGroups();
    }

	private void initColumnGroups() {
		EditableRowTableModel tableModel = getInner();
		for (int n = 0, cnt = tableModel.getColumnCount(); n < cnt; n++) {
			String columnName = tableModel.getColumnName(n);

			createColumnGroup(columnName);
		}
    }

	private String getColumnGroupName(String columnName) {
		return columnName.replace('.', '_');
	}

    private synchronized String createRowGroupName() {
		return Integer.toString(nextRowGroupId++);
    }

    /**
     * @see com.top_logic.layout.table.model.ObjectTableModel#getValueAt(int, int)
     */
    @Override
	public Object getValueAt(int aRowIndex, int aColumnIndex) {
		final String columnName = this.getColumnName(aColumnIndex);
		return getValueAt(aRowIndex, columnName);
	}

	@Override
	public Object getValueAt(int rowIndex, String columnName) {
		Object rowObject = this.getInner().getRowObject(rowIndex);
		return getValueAt(rowObject, columnName);
    }

	@Override
	public Object getValueAt(Object rowObject, String columnName) {
		FieldProvider fieldProvider = getFieldProvider(columnName);
		if (fieldProvider == null) {
			return super.getValueAt(rowObject, columnName);
		} else {
			return getFormMember(rowObject, columnName, fieldProvider);
		}
	}

	private FieldProvider getFieldProvider(String columnName) {
		return getColumnDescription(columnName).getFieldProvider();
	}

	private Accessor getAccessor(String columnName) {
		return getColumnDescription(columnName).getAccessor();
	}

	@Override
	public void setColumns(List<String> newColumns) throws VetoException {
		Set<String> oldColumns = new HashSet<>(getInner().getColumnNames());
		super.setColumns(newColumns);

		for (String newColumn : newColumns) {
			if (!oldColumns.remove(newColumn)) {
				// Newly added column.
				createColumnGroup(newColumn);

				// Note: Row fields are created lazily, therefore no change to the row groups is
				// necessary.
			}
		}

		if (!oldColumns.isEmpty()) {
			// All columns not encountered above were removed.
			for (String removedColumn : oldColumns) {
				removeColumnGroup(removedColumn);
			}

			removeRowFields(oldColumns);
		}
	}

	private void removeRowFields(Set<String> removedColumns) {
		assert !removedColumns.isEmpty();

		List<String> removedInputColumns = new ArrayList<>();
		List<FieldProvider> removedFieldProviders = new ArrayList<>();
		List<Accessor> removedAccessors = new ArrayList<>();
		for (String removedColumn : removedColumns) {
			FieldProvider fieldProvider = getFieldProvider(removedColumn);
			if (fieldProvider != null) {
				removedInputColumns.add(removedColumn);
				removedFieldProviders.add(fieldProvider);
				removedAccessors.add(getAccessor(removedColumn));
			}
		}
		
		for (FormGroup rowGroup : rowGroupByRowObject.values()) {
			Object rowObject = getRowObject(rowGroup);

			for (int n = 0, cnt = removedInputColumns.size(); n < cnt; n++) {
				String removedColumn = removedInputColumns.get(n);
				FieldProvider fieldProvider = removedFieldProviders.get(n);
				Accessor accessor = removedAccessors.get(n);

				String fieldName = fieldProvider.getFieldName(rowObject, accessor, removedColumn);
				rowGroup.removeMember(fieldName);
			}
		}
	}

	private void createColumnGroup(String columnName) {
		FormGroup columnGroup = new FormGroup(getColumnGroupName(columnName), columnContainer.getResources());
		markTechnical(columnGroup);
		columnContainer.addMember(columnGroup);
	}

	private void removeColumnGroup(String columnName) {
		columnContainer.removeMember(getColumnGroupName(columnName));
	}

	/**
	 * Returns the {@link FormMember} which is displayed at the given row in the column with the
	 * given name.
	 */
	public final FormMember getFormMember(int rowIndex, String columnName) {
		Object rowObject = this.getInner().getRowObject(rowIndex);
		return getFormMember(rowObject, columnName, getFieldProvider(columnName));
	}

	private FormMember getFormMember(Object rowObject, final String columnName, FieldProvider fieldProvider) {
		Accessor accessor = getAccessor(columnName);
		String theFieldName = fieldProvider.getFieldName(rowObject, accessor, columnName);
		
		FormContainer theRowGroup = this.getRowGroup(rowObject);
		FormMember    theMember;
		if (theRowGroup.hasMember(theFieldName)) {
		    theMember = theRowGroup.getMember(theFieldName);
		} else {
			theMember = fieldProvider.createField(rowObject, accessor, columnName);
			assert theFieldName.equals(theMember.getName()) : "FieldProvider should create FormMember with name '"
				+ theFieldName + "', but was '" + theMember.getName() + "'";
			theMember.setStableIdSpecialCaseMarker(columnName);
		    theRowGroup.addMember(theMember);
		}
		return theMember;
	}

    public FormContainer getColumnGroup(int aColumnIndex) {
		return getColumnGroup(getColumnName(aColumnIndex));
    }

	public FormContainer getColumnGroup(String columnName) {
		return columnContainer.getContainer(getColumnGroupName(columnName));
	}

    /**
     * Returns the row object the specified {@link FormMember} instance has been
     * created for.
     * 
     * @param aMember
     *            the member to resolve the row object for
     * @return the row object the specified member has been created for
     *         <code>null</code> if not found
     */
    public Object getRowObject(final FormMember aMember) {
		final FormContainer rowGroup = aMember.getParent();
		if (rowGroup == null) {
			return null;
        }
		
		return getRowObject(rowGroup);
    }

	private Object getRowObject(final FormContainer rowGroup) {
		return rowGroup.getStableIdSpecialCaseMarker();
	}
    
    /**
     * @param aRowObject  the object a row is based on
     * @return the {@link FormContainer} holding all {@link FormMember}s declared for that row, never <code>null</code>.
     */
    private synchronized FormContainer getRowGroup(Object aRowObject) {
		FormContainer existingRowGroup = this.rowGroupByRowObject.get(aRowObject);
		if (existingRowGroup == null) {
			return createRowGroup(aRowObject);
		} else {
			return existingRowGroup;
        }
    }

	private FormContainer createRowGroup(Object aRowObject) {
		FormGroup rowGroup = new FormGroup(this.createRowGroupName(), this.rowContainer.getResources());
		markTechnical(rowGroup);
		rowGroup.setStableIdSpecialCaseMarker(aRowObject, this);
		this.rowContainer.addMember(rowGroup);
		this.rowGroupByRowObject.put(aRowObject, rowGroup);
		return rowGroup;
	}

	/**
	 * Prevent creating labels for technical containers based on the resource prefixes.
	 * 
	 * <p>
	 * Not doing so might interfere with test cases that identify fields based on their labels.
	 * Since those technical containers are never displayed directly, it is not helpful generating
	 * labels for them.
	 * </p>
	 */
	private void markTechnical(FormGroup group) {
		group.setLabel("");
	}
    
    public FormContainer getFormContainer() {
        return this.formContainer;
    }

    public FormContainer getGroupContainer() {
		return this.rowContainer;
    }

	public FormGroup getColumnContainer() {
		return columnContainer;
	}

    @Override
    public void updateRows(int aFirstRow, int aLastRow) {
    	for (int n = aFirstRow; n <= aLastRow; n++) {
    		Object row = this.getRowObject(n);
    		cleanupRowGroup(row);
    	}
    	super.updateRows(aFirstRow, aLastRow);
    }
    
    /**
     * @see com.top_logic.layout.table.model.DelegatingTableModel#removeRow(int)
     */
    @Override
	public void removeRow(int aRemovedRow) {

        Object theRowObject = this.getRowObject(aRemovedRow);

        cleanupRowGroup(theRowObject);

        super.removeRow(aRemovedRow);
    }

    /**
     * @see com.top_logic.layout.table.model.DelegatingTableModel#removeRowObject(java.lang.Object)
     */
    @Override
	public void removeRowObject(Object aRowObject) {

       cleanupRowGroup(aRowObject);

       super.removeRowObject(aRowObject);
    }
    
    /**
	 * @see com.top_logic.layout.table.model.DelegatingTableModel#setRowObjects(java.util.List)
	 */
	@Override
	public void setRowObjects(List newRowObjects) {
		Collection rowObjects = getAllRows();
		for (Object rowObject : rowObjects) {
			cleanupRowGroup(rowObject);
		}
		super.setRowObjects(newRowObjects);
	}

    /**
	 * Remove the rowgroup corresponding to the given row object
	 */
	private void cleanupRowGroup(Object theRowObject) {
		FormContainer theRowGroup = this.getRowGroup(theRowObject);
		if (theRowGroup != null) {
			this.rowContainer.removeMember(theRowGroup);
			this.rowGroupByRowObject.remove(theRowObject);
		}
	}
}

