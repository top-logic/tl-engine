/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import java.util.Collection;

import com.top_logic.layout.dnd.TableDropActionOp.TableDropAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * Base class for {@link TableDropTarget} implementations handling with business objects.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class BusinessObjectTableDrop implements TableDropTarget {

	@Override
	public void handleDrop(TableDropEvent event) {
		Collection<Object> droppedObjects = TLTreeModelUtil.getInnerBusinessObjects(event.getData());
		Object referenceRow = getReferenceRow(event);

		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordAction(() -> record(event, referenceRow));
		}

		handleDrop(droppedObjects, referenceRow);
	}

	private TableDropAction record(TableDropEvent event, Object referenceRow) {
		return TableDropAction.create(event.getTarget(), event.getSource(), referenceRow, event.getDragDataName());
	}

	@Override
	public boolean canDrop(TableDropEvent event) {
		return canDrop(TLTreeModelUtil.getInnerBusinessObjects(event.getData()), getReferenceRow(event));
	}

	/**
	 * Returns the row before which the dragged object was dropped.
	 */
	protected Object getReferenceRow(TableDropEvent event) {
		TableViewModel viewModel = event.getTarget().getViewModel();

		switch (event.getPos()) {
			case ONTO:
			case ABOVE: {
				return viewModel.getRowObject(event.getRefRow());
			}
			case BELOW: {
				if (viewModel.getDisplayedRows().size() > event.getRefRow() + 1) {
					return viewModel.getRowObject(event.getRefRow() + 1);
				} else {
					return null;
				}
			}
		}

		throw new IllegalArgumentException("No such position: " + event.getPos());
	}

	/**
	 * Handles the drop operation for the given objects right before the referenced row.
	 * 
	 * @implSpec The implementor has to care about commit handling.
	 */
	public abstract void handleDrop(Collection<?> droppedObjects, Object referenceRow);

	/**
	 * True if the dragged objects can be dropped right before the referenced row.
	 */
	public abstract boolean canDrop(Collection<?> draggedObjects, Object referenceRow);
}
