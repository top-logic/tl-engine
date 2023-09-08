/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.model.export.AccessContext;

/**
 * Table model based on a list of object arrays.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ArrayTableModel<T> extends ObjectTableModel {
    
	private static final class ArrayAccessor implements Accessor<Object[]> {
		private final int _index;

		ArrayAccessor(int index) {
			_index = index;
		}

		@Override
		public Object getValue(Object[] object, String property) {
			return object[_index];
		}

		@Override
		public void setValue(Object[] object, String property, Object value) {
			object[_index] = value;
		}
	}

	/**
	 * Creates a {@link ArrayTableModel}.
	 * 
	 * @param columns
	 *        The columns of this table, must not be <code>null</code>.
	 * @param someRows
	 *        The rows of this table model (object arrays must have the same length as the columns
	 *        array), must not be <code>null</code>.
	 */
	public ArrayTableModel(String[] columns, List<T[]> someRows) {
		this(columns, someRows, TableConfiguration.table());
    }

	/**
	 * Creates a {@link ArrayTableModel}.
	 * 
	 * @param columns
	 *        The columns of this table, must not be <code>null</code>.
	 * @param someRows
	 *        The rows of this table model (object arrays must have the same length as the columns
	 *        array), must not be <code>null</code>.
	 * @param config
	 *        See {@link TableModel#getTableConfiguration()}.
	 */
	public ArrayTableModel(String[] columns, List<T[]> someRows, TableConfiguration config) {
		this(Arrays.asList(columns), someRows, config);
	}

	/**
	 * Creates a {@link ArrayTableModel}.
	 * 
	 * @param columns
	 *        The columns of this table, must not be <code>null</code>.
	 * @param someRows
	 *        The rows of this table model (object arrays must have the same length as the columns
	 *        array), must not be <code>null</code>.
	 * @param config
	 *        See {@link TableModel#getTableConfiguration()}.
	 */
	public ArrayTableModel(List<String> columns, List<T[]> someRows, TableConfiguration config) {
		super(columns, ensureAccessors(config, columns), someRows);
    }

	private static TableConfiguration ensureAccessors(TableConfiguration config, List<String> columns) {
		for (int n = 0, cnt = columns.size(); n < cnt; n++) {
			final int index = n;
			ColumnConfiguration columnConfig = config.declareColumn(columns.get(n));
			if (columnConfig.getAccessor() == null) {
				columnConfig.setAccessor(new ArrayAccessor(index));
			}
		}
		return config;
	}

	@Override
	public AccessContext prepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
		return NoPrepare.INSTANCE;
	}

}

