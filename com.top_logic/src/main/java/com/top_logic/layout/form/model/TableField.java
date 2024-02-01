/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;


import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.SelectionModel;

/**
 * Adaptor for adding named {@link TableModel}s to {@link FormGroup}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableField extends ConstantField implements TableData, TableDataOwner, Provider<TableViewModel> {
	
	@Inspectable
	private final TableData tableData;

	@Inspectable
	private boolean selectable;

	@Inspectable
	private CheckScope checkScope = CheckScope.NO_CHECK;

	private final ConfigKey _configKey;
	
	protected TableField(String name, Mapping<FormMember, String> configNameMapping,
			boolean updateSelectionOnTableEvents) {
		super(name, !IMMUTABLE);

		_configKey = ConfigKey.field(configNameMapping, this);
		tableData =
			DefaultTableData.createTableDataImplementation(this, this, _configKey, updateSelectionOnTableEvents);
		installEventForward();
    }

	protected TableField(String name, ConfigKey configKey, boolean updateSelectionOnTableEvents) {
		super(name, !IMMUTABLE);
		_configKey = configKey;
		tableData = DefaultTableData.createTableDataImplementation(this, this, configKey, updateSelectionOnTableEvents);
		installEventForward();
	}

	private void installEventForward() {
		getTableData().addListener(TableData.GLOBAL_LISTENER_TYPE, new GenericPropertyListener() {
			@Override
			public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
				TableField.this.firePropertyChanged((EventType) type, sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
		});
	}

	@Override
	public TableData getTableData() {
		return tableData;
	}

	@Override
	public TableDataOwner getOwner() {
		return getTableData().getOwner();
	}

	@Override
	public ToolBar getToolBar() {
		return getTableData().getToolBar();
	}

	@Override
	public void setToolBar(ToolBar newToolBar) {
		getTableData().setToolBar(newToolBar);
	}

	@Override
	public void setTableModel(TableModel tableModel) {
		getTableData().setTableModel(tableModel);
	}

	@Override
	public TableModel getTableModel() {
		return getTableData().getTableModel();
	}
	
	@Override
	public SelectionModel getSelectionModel() {
		return getTableData().getSelectionModel();
	}

	@Override
	public void setSelectionModel(SelectionModel selectionModel) {
		getTableData().setSelectionModel(selectionModel);
	}

	/**
	 * This method can be called only, if this {@link TableField} is part of an {@link FormContext}
	 * 
	 * @see com.top_logic.layout.table.TableData#getViewModel()
	 */
	@Override
	public TableViewModel getViewModel() {
		return getTableData().getViewModel();
	}

	@Override
	public TableDragSource getDragSource() {
		return getTableData().getDragSource();
	}

	@Override
	public List<TableDropTarget> getDropTargets() {
		return getTableData().getDropTargets();
	}

	@Override
	public ContextMenuProvider getContextMenu() {
		return getTableData().getContextMenu();
	}

	@Override
	public void addSelectionVetoListener(SelectionVetoListener listener) {
		getTableData().addSelectionVetoListener(listener);
	}

	@Override
	public void removeSelectionVetoListener(SelectionVetoListener listener) {
		getTableData().removeSelectionVetoListener(listener);
	}

	@Override
	public Collection<SelectionVetoListener> getSelectionVetoListeners() {
		return getTableData().getSelectionVetoListeners();
	}

	@Override
	public void setContextMenu(ContextMenuProvider value) {
		getTableData().getTableModel().getTableConfiguration().setContextMenu(value);
	}

	@Override
	public final TableViewModel get() {
		return getViewModel();
	}

	@Override
	public ResourceView getTableResources() {
		return getTableModel().getTableConfiguration().getResPrefix();
	}

	@Override
	public CheckScope getCheckScope() {
		return checkScope;
	}

	/**
	 * Setter for {@link #getCheckScope()}.
	 */
	public void setCheckScope(CheckScope checkScope) {
		this.checkScope = checkScope;
	}

	public void setSelectable(boolean selectable) {
    	// TODO: Make sure that changing the property is reflected by the view.
		this.selectable = selectable;
	}
	
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * The {@link ConfigKey} which is used to store changes in the configuration of this table in
	 * the personal configuration.
	 */
	public final ConfigKey getConfigKey() {
		return _configKey;
	}

    @Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitTableField(this, arg);
	}

    /** 
     * Create a column description for the given data and append this to the given manager.
     * 
     * @param    aName        The name of the new created description, must not be <code>null</code> or empty.
     * @param    aManager     The manager to append the column description to, must not be <code>null</code>.
     * @param    aProvider    The field provider, must not be <code>null</code>.
     * @param    aControl     The control provider, must not be <code>null</code>.
     */
    protected static void addField(String aName, TableConfiguration aManager, FieldProvider aProvider, ControlProvider aControl) {
		ColumnConfiguration column = aManager.declareColumn(aName);
		column.setFieldProvider(aProvider);
		column.setControlProvider(aControl);
    }
}
