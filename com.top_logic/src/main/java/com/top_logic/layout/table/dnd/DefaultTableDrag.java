/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.layout.table.TableData;

/**
 * {@link TableDragSource} that allows dragging each row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTableDrag implements TableDragSource {

	/**
	 * Singleton {@link DefaultTableDrag} instance.
	 */
	public static final DefaultTableDrag INSTANCE = new DefaultTableDrag();

	/**
	 * Creates a {@link DefaultTableDrag}.
	 */
	protected DefaultTableDrag() {
		// Singleton constructor.
	}

	@Override
	public boolean dragEnabled(TableData data) {
		return true;
	}

	@Override
	public boolean dragEnabled(TableData data, Object rowObject) {
		return true;
	}

	@Override
	public Object getDragObject(TableData tableData, int row) {
		return tableData.getViewModel().getRowObject(row);
	}

}
