/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * Base class for {@link ConfiguredFilter}s, which shall be part of a global {@link TableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractGlobalFilter implements Filter<Object> {

	private TableViewModel _tableViewModel;

	/**
	 * Creates a {@link AbstractGlobalFilter}.
	 * 
	 * @param tableViewModel
	 *        See {@link #getTableViewModel()}
	 */
	public AbstractGlobalFilter(TableViewModel tableViewModel) {
		this._tableViewModel = tableViewModel;
	}

	/**
	 * {@link TableViewModel}, to which this {@link ConfiguredFilter} belongs to.
	 */
	public final TableViewModel getTableViewModel() {
		return _tableViewModel;
	}

	/**
	 * value provided by {@link TableViewModel#getMappedValueAt(Object, String)}.
	 */
	protected final Object getMappedColumnValue(Object rowObject, String columnName) {
		return _tableViewModel.getMappedValueAt(rowObject, columnName);
	}

	/**
	 * value, provided by the accessor of the given column
	 */
	protected final Object getColumnValue(Object rowObject, String columnName) {
		return _tableViewModel.getValueAt(rowObject, columnName);
	}

	/**
	 * {@link Accessor}, for the specified column, may be null, if none specified.
	 */
	protected final ColumnConfiguration getColumnConfiguration(String columnName) {
		return _tableViewModel.getColumnDescription(columnName);
	}
}
