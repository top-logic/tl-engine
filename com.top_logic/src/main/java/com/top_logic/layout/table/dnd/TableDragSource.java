/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.table.TableData;

/**
 * Handler that controls dragging data from a table.
 * 
 * @see TableData#getDragSource()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableDragSource {

	/**
	 * Whether dragging is enabled for the given table.
	 * 
	 * @param data
	 *        The {@link TableData} that occurs as potential drag source.
	 */
	boolean dragEnabled(TableData data);

	/**
	 * Whether the given row can be dragged.
	 * 
	 * @param data
	 *        The {@link TableData} that occurs as potential drag source.
	 * @param rowObject
	 *        The drag object that occurs as potential drag source.
	 */
	boolean dragEnabled(TableData data, Object rowObject);

	/**
	 * Retrieves the underlying object being dragged from the given table.
	 * 
	 * @param tableData
	 *        The {@link TableData} from which the drag started.
	 * @param row
	 *        The row from which the drag started.
	 * @return The drag object to be announced in the drop event.
	 */
	Object getDragObject(TableData tableData, int row);

	/**
	 * Tries to create a {@link ModelName} for the object with the given client-side identifier.
	 * 
	 * @param dragSource
	 *        The source model.
	 * @param tableData
	 *        The {@link TableData} from which the drag started.
	 * @param row
	 *        The row from which the drag started.
	 * 
	 * @return {@link ModelName} for the drag data or empty if no such name could be created.
	 */
	default Maybe<? extends ModelName> getDragDataName(Object dragSource, TableData tableData, int row) {
		return ModelResolver.buildModelNameIfAvailable(dragSource, getDragObject(tableData, row));
	}

}
