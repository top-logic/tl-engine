/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

import java.util.Map;

import com.top_logic.dob.meta.MOStructure;

/**
 * Default implementation of {@link RowValue}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultRowValue implements RowValue {

	private Map<String, Object> _values;

	private final MOStructure _table;

	/**
	 * Creates a new {@link DefaultRowValue}.
	 * 
	 * @param table
	 *        See {@link #getTable()}.
	 * @param values
	 *        Value of {@link #getValues()}
	 */
	public DefaultRowValue(MOStructure table, Map<String, Object> values) {
		_table = table;
		_values = values;
	}

	@Override
	public MOStructure getTable() {
		return _table;
	}

	@Override
	public Map<String, Object> getValues() {
		return _values;
	}

	@Override
	public String toString() {
		return "Row: " + _values;
	}
}
