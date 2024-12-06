/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.table.filter.CellExistenceTester;

/**
 * Holds a {@link TableFilter} and the {@link Mapping} of the raw value to the value the TableFilter
 * shall process.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ColumnFilterHolder {
	
	private String filterPosition;
	private Mapping<? super Object, ? super Object> filterValueMapping;
	private TableFilter filter;
	private CellExistenceTester cellExistenceTester;
	
	/**
	 * Create a new {@link ColumnFilterHolder}.
	 */
	public ColumnFilterHolder(String filterPosition, TableFilter filter,
			Mapping<? super Object, ? super Object> filterValueMapping,
			CellExistenceTester cellExistenceTester) {
		this.filterPosition = filterPosition;
		this.filter = filter;
		this.filterValueMapping = filterValueMapping;
		this.cellExistenceTester = cellExistenceTester;
	}

	/**
	 * @return column name, or global filter id, {@link #getFilter()} belongs to.
	 */
	public String getFilterPosition() {
		return filterPosition;
	}

	/**
	 * @return {@link TableFilter} of {@link #getFilterPosition()}.
	 */
	public TableFilter getFilter() {
		return filter;
	}

	/**
	 * @return {@link Mapping Cell value mapping} of {@link #getFilterPosition()}.
	 */
	public Mapping<? super Object, ? super Object> getFilterValueMapping() {
		return filterValueMapping;
	}

	/**
	 * @return {@link CellExistenceTester} of {@link #getFilterPosition()}.
	 */
	public CellExistenceTester getCellExistenceTester() {
		return cellExistenceTester;
	}
}
