/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.layout.basic.AbstractAttachable;

/**
 * The <tt>ObjectTableViewModel</tt> is an extension of the
 * {@link ObjectTableModel} which handles the possibility that the model is
 * additionally a view. It will be checked if this view observes its model,
 * before the <tt>get</tt>-methods can be called.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ObjectTableViewModel extends ObjectTableModel {

	/**
	 * Delegation to {@link AbstractAttachable} for handling the attachment.
	 */
	private final AbstractAttachable attachmentDelegate = new AbstractAttachable() {
		@Override
		protected void internalAttach() {
			ObjectTableViewModel.this.internalAttach();
		}

		@Override
		protected void internalDetach() {
			ObjectTableViewModel.this.internalDetach();
		}
	};

	public ObjectTableViewModel(String[] columnNames, TableConfiguration aCDM, List rows, boolean priorityTable) {
		super(columnNames, aCDM, rows, priorityTable);
	}

	public ObjectTableViewModel(String[] columnNames, TableConfiguration aCDM, List rows) {
		super(columnNames, aCDM, rows);
	}

	/**
	 * This method has to be override to specify what shall happens if the view
	 * starts to observe its model.
	 * 
	 */
	protected abstract void internalAttach();

	/**
	 * This method has to be override to specify what shall happens if the view
	 * stops to observe its model.
	 * 
	 */
	protected abstract void internalDetach();

	/**
	 * This method returns the attach-state of this view.
	 * 
	 */
	protected final boolean isAttached() {
		return attachmentDelegate.isAttached();
	}

	/**
	 * This method throws an {@link IllegalStateException} if the view is
	 * detached.
	 * 
	 */
	protected final void checkAttachException() {
		if (!attachmentDelegate.isAttached())
			throw new IllegalStateException(
					"View does not observe its model. State is possibly not up-to-date!");
	}

	/**
	 * If this view is attached the corresponding method from
	 * {@link ObjectTableModel} will be called. Otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#containsRowObject(java.lang.Object)
	 */
	@Override
	public boolean containsRowObject(Object anObject) {
		checkAttachException();
		return super.containsRowObject(anObject);
	}

	/**
	 * If this view is attached the corresponding method from
	 * {@link ObjectTableModel} will be called. Otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#getRowObject(int)
	 */
	@Override
	public Object getRowObject(int row) {
		checkAttachException();
		return super.getRowObject(row);
	}

	/**
	 * If this view is attached the corresponding method from
	 * {@link ObjectTableModel} will be called. Otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#getRowOfObject(java.lang.Object)
	 */
	@Override
	public int getRowOfObject(Object rowObject) {
		checkAttachException();
		return super.getRowOfObject(rowObject);
	}

	/**
	 * If this view is attached the corresponding method from
	 * {@link ObjectTableModel} will be called. Otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		checkAttachException();
		return super.getRowCount();
	}

	/**
	 * If this view is attached the corresponding method from
	 * {@link ObjectTableModel} will be called. Otherwise an
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @see com.top_logic.layout.table.model.ObjectTableModel#getValueAt(int,
	 *      int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		checkAttachException();
		return super.getValueAt(rowIndex, columnIndex);
	}

	/**
	 * If the last {@link TableModelListener} is removed, this view stops to
	 * observe its model.
	 * 
	 * @see com.top_logic.layout.table.model.AbstractTableModel#removeTableModelListener(com.top_logic.layout.table.model.TableModelListener)
	 */
	@Override
	public boolean removeTableModelListener(TableModelListener listener) {
		boolean result = super.removeTableModelListener(listener);
		if (!hasTableModelListeners()) {
			attachmentDelegate.detach();
		}
		return result;
	}

	/**
	 * If the first {@link TableModelListener} is added, the view starts to
	 * observe its model.
	 * 
	 * @see com.top_logic.layout.table.model.AbstractTableModel#addTableModelListener(com.top_logic.layout.table.model.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener listener) {
		attachmentDelegate.attach();
		super.addTableModelListener(listener);
	}
}
