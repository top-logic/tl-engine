/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FixedOptionsListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;

/**
 * An adaptor that provides a {@link TableModel} view of the current selection
 * of a {@link SelectField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SelectionTableModel extends ObjectTableViewModel implements
		ValueListener, FixedOptionsListener {

	private static class NOOPTableModelListener implements TableModelListener {
	
		private static NOOPTableModelListener INSTANCE = new NOOPTableModelListener();
	
		@Override
		public void handleTableModelEvent(TableModelEvent event) {
			// NOOP
		}
	
	}

	private SelectField selectField;

	private boolean hasFixedFilter;

	private Filter fixedFilter;

	private boolean isSelectFieldUpdate;

	/**
	 * Constructs a new {@link ObjectTableModel} that is backed by the current
	 * selection of a {@link SelectField}.
	 * 
	 * <p>
	 * The row objects of this {@link ObjectTableModel} are the currently
	 * selected options of the given {@link SelectField}. The column values are
	 * accessed from this row objects with the given {@link Accessor} instance.
	 * </p>
	 */
	public SelectionTableModel(SelectField selectField,
			String[] columnNames, TableConfiguration aCDM) {
		super(columnNames, aCDM, new ArrayList<>(), selectField.hasCustomOrder());

		this.selectField = selectField;
		this.fixedFilter = selectField.getFixedOptions();
		this.hasFixedFilter = this.fixedFilter != null;
		this.isSelectFieldUpdate = true;
		
		internalSetRowObjects(selectField.getSelection());
	}

	/**
	 * Attaches this model as listener to its {@link SelectField}.
	 * 
	 * <p>
	 * While being attached, this {@link TableModel} stays in sync with the
	 * currently selected options of the {@link #selectField}. If the value
	 * (the selection) of this model's select field changes, this model
	 * generates the corresponding {@link TableModelEvent}s that describe the
	 * change. A {@link TableControl} view that observes this model can
	 * translate these events into updates of its display.
	 * </p>
	 */
	@Override
	protected void internalAttach() {
		this.selectField.addValueListener(this);
		this.selectField.addListener(SelectField.FIXED_FILTER_PROPERTY, this);
		internalSetRowObjects(selectField.getSelection());
	}

	@Override
	protected void internalDetach() {
		this.selectField.removeValueListener(this);
		this.selectField.removeListener(SelectField.FIXED_FILTER_PROPERTY, this);
	}
	
	/**
	 * The underlying {@link SelectField} upon which this {@link TableModel} is
	 * based.
	 */
	public SelectField getField() {
		return selectField;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (isSelectFieldUpdate && hasTableModelListeners()) {
			internalSetRowObjects(selectField.getSelection());
		}
	}

	@Override
	public Bubble handleFixedOptionsChanged(SelectField sender, Filter oldValue, Filter newValue) {
		// the selectField's filter has (been) changed programmatically,
		// let's remember the filter locally.
		if (sender == this.selectField) {
			this.fixedFilter = newValue;
			this.hasFixedFilter = this.fixedFilter != null;
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Overridden to delegate the decision to the
	 * {@link SelectField#getFixedOptions() fixed option filter} of the
	 * underlying select field.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#canRemove(int)
	 */
	@Override
	public boolean canRemove(int row) {
		Object rowObject = getRowObject(row);
		return canRemove(rowObject);
	}

	private boolean canRemove(Object rowObject) {
		return (!hasFixedFilter) || (!fixedFilter.accept(rowObject));
	}

	/**
	 * Overridden to enforce the fixed row filter configuration.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int removedRow) {
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			if (canRemove(removedRow)) {
				List<Object> newSelection = internalRemoveRows(removedRow, removedRow + 1);
				super.removeRow(removedRow);
				setFieldSelection(newSelection);
			}
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void removeRowObject(Object rowObject) {
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			if (canRemove(rowObject)) {
				int target = getRowOfObject(rowObject);
				List<Object> newSelection = internalRemoveRows(target, target + 1);
				super.removeRowObject(rowObject);
				setFieldSelection(newSelection);
			}
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void removeRows(int start, int stop) {
		List rows = getDisplayedRows();
		if (stop <= start) {
			if (stop < start) {
				throw new IllegalArgumentException("Stop (" + stop + ") < start (" + start + ").");
			} else {
				return;
			}
		}
		if (start < 0 || stop > rows.size()) {
			throw new IllegalArgumentException(
				"Row indices, that shall be removed, are out of range! Range: [0, " + (rows.size() - 1)
					+ "], RowIndices: [" + start + ", " + stop + "]");
		}

		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			boolean canRemove = true;
			for (int i = start; i < stop - 1; i++) {
				canRemove &= canRemove(i);
			}

			if (canRemove) {
				List<Object> newSelection = internalRemoveRows(start, stop);
				super.removeRows(start, stop);
				setFieldSelection(newSelection);
			}
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	private List<Object> internalRemoveRows(int start, int stop) {
		List<Object> selection = getFieldSelectionCopy();
		for (int i = stop - 1; i >= start; i--) {
			removeFromSelection(selection, i);
		}
		return selection;
	}

	private void removeFromSelection(List<Object> selection, int visibleRow) {
		int target = getAllRowsId(visibleRow);
		selection.remove(target);
	}

	@Override
	public void moveRow(int from, int to) {
		assert selectField
			.hasCustomOrder() : "Tried to moveRow, but the underlying SelectField has CustomOrder not set! That means, the move will be ignored, eventually.";
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			int source = getAllRowsId(from);
			int target = getAllRowsId(to);
			super.moveRow(from, to);
			if (from != to) {
				List<Object> selection = getFieldSelectionCopy();
				Object rowObject = selection.remove(source);
				selection.add(target, rowObject);
				setFieldSelection(selection);
			}
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	private List<Object> getFieldSelectionCopy() {
		List<Object> selection = new ArrayList<>(selectField.getSelection());
		return selection;
	}

	private void setFieldSelection(List<Object> selection) {
		selectField.setAsSelection(selection);
	}

	private int getAllRowsId(int visibleRowId) {
		return selectField.getSelection().indexOf(getRowObject(visibleRowId));
	}

	@Override
	public void setRowObjects(List newRowObjects) {
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			super.setRowObjects(newRowObjects);
			setFieldSelection(newRowObjects);
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void addRowObject(Object rowObject) {
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			super.addRowObject(rowObject);
			List<Object> selection = getFieldSelectionCopy();
			selection.add(rowObject);
			setFieldSelection(selection);
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void addAllRowObjects(List newRows) {
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			super.addAllRowObjects(newRows);
			List<Object> selection = getFieldSelectionCopy();
			selection.addAll(newRows);
			setFieldSelection(selection);
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void insertRowObject(int row, Object rowObject) {
		if (row < 0 || row > getDisplayedRows().size()) {
			throw new IndexOutOfBoundsException(
				"Row must neither be negative nor greater than number of displayed rows.");
		}
		
		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			int target = getAllRowsId(row);
			super.insertRowObject(row, rowObject);
			List<Object> selection = getFieldSelectionCopy();
			selection.add(target, rowObject);
			setFieldSelection(selection);
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}

	@Override
	public void insertRowObject(int row, List newRows) {
		if (newRows.isEmpty()) {
			return;
		}
		List displayedRows = getDisplayedRows();
		int size = displayedRows.size();
		if (row < 0 || row > size) {
			throw new IndexOutOfBoundsException(
				"Row must neither be negative nor greater than number of displayed rows.");
		}

		boolean wasSelectFieldUpdate = isSelectFieldUpdate;
		isSelectFieldUpdate = false;
		try {
			int target = getAllRowsId(row);
			super.insertRowObject(row, newRows);
			List<Object> selection = getFieldSelectionCopy();
			selection.addAll(target, newRows);
			setFieldSelection(selection);
		} finally {
			isSelectFieldUpdate = wasSelectFieldUpdate;
		}
	}
	
	private void internalSetRowObjects(List<?> rowObjects) {
		super.setRowObjects(rowObjects);
	}

	/**
	 * Marker of the end of the code block, which modifies the {@link SelectionTableModel}. After
	 * that the model will not be modifiable.
	 * 
	 * @param tableModel
	 *        - the modified {@link SelectionTableModel}
	 */
	@Deprecated
	public static void endModification(TableModel tableModel) {
		// For explanation take a look at beginForcedModification()
		tableModel.removeTableModelListener(SelectionTableModel.NOOPTableModelListener.INSTANCE);
	}

	/**
	 * Marker of the begin of the code block, which modifies the {@link SelectionTableModel}, to
	 * ensure the model is modifiable.
	 * <p>
	 * <b>Note:</b> After model modification {@link #endModification} must be called. Otherwise
	 * strange effects in (GUI-) update handling may occur.
	 * </p>
	 * 
	 * @param tableModel
	 *        - {@link SelectionTableModel} to modify
	 */
	@Deprecated
	public static void beginModification(TableModel tableModel) {
		// SelectionTableModel needs a TableModelListener as observer, in case it should be
		// modified. Because the TableViewModel, which usually acts as observer,
		// is potentially not existent, it is necessary to add a noop listener.
		tableModel.addTableModelListener(SelectionTableModel.NOOPTableModelListener.INSTANCE);
	}
}