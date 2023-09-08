/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.layout.table.TableModel;

/**
 * {@link TableModelEvent} for update of columns.
 * 
 * @see TableModelEvent#COLUMNS_UPDATE
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableModelColumnsEvent extends TableModelEvent {

	private final List<String> _oldColumns;

	private final List<String> _newColumns;

	/**
	 * Creates a new {@link TableModelColumnsEvent}.
	 * 
	 * @param source
	 *        The {@link TableModel} whose columns have changed.
	 * @param oldColumns
	 *        The columns before the change.
	 * @param newColumns
	 *        The columns after the change.
	 */
	public TableModelColumnsEvent(TableModel source, List<String> oldColumns, List<String> newColumns) {
		super(source, 0, source.getRowCount(), COLUMNS_UPDATE);
		_oldColumns = oldColumns;
		_newColumns = newColumns;
	}

	/**
	 * The columns of {@link #getSource()} before the columns change.
	 */
	public List<String> oldColumns() {
		return _oldColumns;
	}

	/**
	 * The columns of {@link #getSource()} after the columns change.
	 */
	public List<String> newColumns() {
		return _newColumns;
	}

}

