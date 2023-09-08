/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

import java.util.Iterator;

import com.top_logic.dob.meta.MOStructure;

/**
 * Default implementation of {@link TableContent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTableContent implements TableContent {

	private MOStructure _table;

	private Iterable<RowValue> _rows;

	/**
	 * Creates a new {@link DefaultTableContent}.
	 * 
	 * @param table
	 *        The name of the table.
	 * @param rows
	 *        The acual content.
	 */
	public DefaultTableContent(MOStructure table, Iterable<RowValue> rows) {
		_table = table;
		_rows = rows;
	}

	@Override
	public MOStructure getTable() {
		return _table;
	}

	@Override
	public Iterator<RowValue> getRows() {
		return _rows.iterator();
	}

	@Override
	public String toString() {
		return "Table: " + _table + ", content: " + _rows;
	}

}

