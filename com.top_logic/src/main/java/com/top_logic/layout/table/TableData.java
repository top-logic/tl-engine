/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.check.CheckScoped;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.table.DefaultTableData.NoTableDataOwner;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Implementations of {@link TableData} serve as model for {@link TableControl}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableData extends ToolBarOwner, CheckScoped, NamedModel, TypedAnnotatable, SelectionModelOwner {

	/**
	 * {@link EventType}, for observing {@link TableData#getViewModel()} changes.
	 * 
	 * @see PropertyObservable#addListener(EventType, PropertyListener)
	 */
	EventType<TableDataListener, TableData, TableViewModel> VIEW_MODEL_PROPERTY =
		new EventType<>("tableViewModel") {
			@Override
			public EventType.Bubble dispatch(TableDataListener listener, TableData sender,
					TableViewModel oldValue, TableViewModel newValue) {
				listener.notifyTableViewModelChanged(sender, oldValue, newValue);
				return Bubble.CANCEL_BUBBLE;
			}
		};

	/**
	 * {@link EventType}, for observing {@link TableData#getSelectionModel()} changes.
	 * 
	 * @see PropertyObservable#addListener(EventType, PropertyListener)
	 */
	EventType<TableDataListener, TableData, SelectionModel> SELECTION_MODEL_PROPERTY =
		new EventType<>("selectionModel") {
			@Override
			public EventType.Bubble dispatch(TableDataListener listener, TableData sender,
					SelectionModel oldValue, SelectionModel newValue) {
				listener.notifySelectionModelChanged(sender, oldValue, newValue);
				return Bubble.CANCEL_BUBBLE;
			}
		};

	/**
	 * The (optional) owner holding this {@link TableData}.
	 * 
	 * <p>
	 * The owner is the table component or form field displaying this table. If the
	 * {@link TableData} was constructed anonymously, {@link NoTableDataOwner#INSTANCE} is returned.
	 * </p>
	 * 
	 * @see DefaultTableData#createTableData(TableDataOwner, TableModel, ConfigKey)
	 */
	TableDataOwner getOwner();

	/**
	 * Returns the {@link TableModel} of this {@link TableData}
	 */
	public TableModel getTableModel();

	/**
	 * sets the {@link TableModel} of this {@link TableViewModel}.
	 */
	public void setTableModel(TableModel tableModel);

	/**
	 * The {@link TableViewModel} which is finally displayed.
	 * 
	 * <p>
	 * This property is observable through the property key {@link #VIEW_MODEL_PROPERTY}, see
	 * {@link #addListener(EventType, PropertyListener)}.
	 * </p>
	 */
	public TableViewModel getViewModel();

	/**
	 * The unmodifiable source of I18N keys for table resources.
	 * 
	 * <p>
	 * Short-cut for {@link #getTableModel()}.{@link TableModel#getTableConfiguration()
	 * getTableConfiguration()}. {@link TableConfiguration#getResPrefix() getResPrefix()}
	 * </p>
	 */
	public ResourceView getTableResources();
	
	/**
	 * Sets the selection model of this {@link TableData}.
	 */
	void setSelectionModel(SelectionModel selectionModel);

	/**
	 * The {@link TableDragSource} that controls drags from this table.
	 * 
	 * @see TableConfig#getDragSource()
	 */
	TableDragSource getDragSource();

	/**
	 * The {@link TableDropTarget}'s that controls drop operations in this table.
	 * 
	 * @see TableConfig#getDropTargets()
	 */
	List<TableDropTarget> getDropTargets();

	/**
	 * The {@link ContextMenuProvider} used for table rows.
	 */
	ContextMenuProvider getContextMenu();

	/**
	 * Adds a {@link SelectionVetoListener} to this {@link TableData}.
	 * 
	 * @param listener
	 *        The listener to add.
	 */
	void addSelectionVetoListener(SelectionVetoListener listener);

	/**
	 * Removes the given {@link SelectionVetoListener} from this {@link TableData}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 */
	void removeSelectionVetoListener(SelectionVetoListener listener);

	/**
	 * All attached {@link SelectionVetoListener}.
	 */
	Collection<SelectionVetoListener> getSelectionVetoListeners();

}
