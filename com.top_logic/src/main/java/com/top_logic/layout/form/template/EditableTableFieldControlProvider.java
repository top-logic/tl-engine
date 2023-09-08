/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.model.DefaultModeModel;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.EditableTableControl;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Provides an {@link EditableTableControl} for {@link TableField} models.
 * 
 * <p>
 * E.g. use with <code>&lt;form:custom name="..." controlProvider="<%= new
 * EditableTableFieldControlProvider() %>"/></code>.
 * </p>
 * 
 * @author <a href=mailto:bu@top-logic.com>dbu</a>
 */
public class EditableTableFieldControlProvider extends DefaultFormFieldControlProvider {
	
	private int sortedColumn;
	private boolean ascending;

	private final boolean rowMove;

	private final boolean selectable;
	
	public EditableTableFieldControlProvider() {
		this(true, false);
	}
	
	public EditableTableFieldControlProvider(boolean rowMove, boolean selectable) {
		this(0, true, rowMove, selectable);
		
	}

	public EditableTableFieldControlProvider(int sortedColumn, boolean ascending, boolean rowMove, boolean selectable) {
		this.sortedColumn = sortedColumn;
		this.ascending = ascending;
		this.rowMove = rowMove;
		this.selectable = selectable;
	}
	
	@Override
	public Control visitTableField(TableField member, Void arg) {
		
		ITableRenderer renderer = createTableRenderer();

		return createEditableControl(member, renderer);
	}

	/**
	 * Create the actual Control.
	 */
	protected EditableTableControl createEditableControl(TableField member, ITableRenderer viewRenderer) {
		EditableTableControl ctrl = 
			new EditableTableControl(member, viewRenderer, new DefaultModeModel(), rowMove);
		
		TableViewModel viewModel = ctrl.getViewModel();
		int sortColumnIdx = viewModel.getSortedColumn();
		if (rowMove) {
			// Necessary to prevent the table from re-sorting moved rows.
			//
			// Necessary to set ColumComparator before setting new mode model,
			// because setModeModel can cause TableViewModel.init() which sorts the
			// rows
			for (int n = 0, cnt = ctrl.getApplicationModel().getColumnCount(); n < cnt; n++) {
				viewModel.setColumnComparator(n, null);
			}
		} else {
			// if no column currently sorted, intialize sortcolumn with preset value
			if (sortColumnIdx < 0) {
				viewModel.setSortedApplicationModelColumn(getSortedColumn(), isAscending());
			}
		}
		ctrl.setModeModel(new FormModeModelAdapter(member));
		
		// Selection is necessary to be able to move rows.
		boolean canSelect = rowMove || selectable;
		
		ctrl.setSelectable(canSelect);
		return ctrl;
	}

	/**
	 * Create the Renderer to render the basic Table.
	 */
	protected ITableRenderer createTableRenderer() {
		return DefaultTableRenderer.newInstance();
	}

	/**
	 * Returns the sortedColumn.
	 */
	public int getSortedColumn() {
		return this.sortedColumn;
	}

	/**
	 * @param sortedColumn The sortedColumn to set.
	 */
	public void setSortedColumn(int sortedColumn) {
		this.sortedColumn = sortedColumn;
	}

	/**
	 * Returns the ascending.
	 */
	public boolean isAscending() {
		return this.ascending;
	}

	/**
	 * @param ascending The ascending to set.
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
}
