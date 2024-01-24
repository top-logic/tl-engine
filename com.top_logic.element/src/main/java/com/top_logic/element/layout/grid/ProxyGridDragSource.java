/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.dnd.TableDragSource;

/**
 * {@link TableDragSource} that converts grid rows to underlying row objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProxyGridDragSource implements GridDragSource {

	private final TableDragSource _impl;

	private final GridComponent _grid;

	/**
	 * Creates a {@link ProxyGridDragSource}.
	 * 
	 * @param grid
	 *        The context {@link GridComponent}.
	 */
	protected ProxyGridDragSource(TableDragSource impl, GridComponent grid) {
		_impl = impl;
		_grid = grid;
	}

	@Override
	public boolean dragEnabled(TableData data) {
		return _impl.dragEnabled(data);
	}

	@Override
	public Object getDragObject(TableData tableData, int row) {
		Object rowObject = _impl.getDragObject(tableData, row);
		return rowObject(rowObject);
	}

	@Override
	public Maybe<? extends ModelName> getDragDataName(Object dragSource, TableData tableData, int row) {
		Maybe<? extends ModelName> rowName =
			ModelResolver.buildModelNameIfAvailable(_impl.getDragObject(tableData, row));
		if (rowName.hasValue()) {
			return Maybe.some(GridBusinessObjectNaming.newName(rowName.get()));
		}
		return Maybe.none();
	}

	@Override
	public boolean dragEnabled(TableData tableData, Object rowObject) {
		GridComponent grid = _grid;
		if (grid.isEditing()) {
			if (grid.getSelected() == rowObject(rowObject)) {
				return false;
			}
		}
		return _impl.dragEnabled(tableData, rowObject);
	}

	/**
	 * Converts the internal row object into the dragged object.
	 */
	private Object rowObject(Object rowObject) {
		return _grid.getBusinessObjectFromInternalRow(rowObject);
	}

}
