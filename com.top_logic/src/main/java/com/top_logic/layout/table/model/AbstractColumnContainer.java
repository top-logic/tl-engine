/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.col.AbstractFreezable;
import com.top_logic.layout.table.model.ColumnBase.PropertyCopier;

/**
 * Implementation of contracts defined in {@link ColumnContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractColumnContainer<C extends ColumnConfiguration> extends AbstractFreezable implements
		ColumnContainer<C> {

	@Override
	public final C declareColumn(String columnName) {
		C column = getDeclaredColumn(columnName);
		if (column == null) {
			column = createColumn(columnName);
			addColumn(column);
		}
		return column;
	}

	@Override
	public final C getCol(String aName) {
		C theResult = getDeclaredColumn(aName);
		if (theResult == null) {
			theResult = getDefaultColumn();
		}
		return theResult;
	}

	/**
	 * Creates a newly declared column.
	 * 
	 * @param columnName
	 *        The name for the new column.
	 */
	protected abstract C createColumn(String columnName);

	@Override
	public abstract void addColumn(C columnConfiguration);

	/**
	 * Applies all column configurations to the given target container.
	 */
	public void copyTo(ColumnContainer<?> columns) {
		copyDefaultColumnTo(columns);
		copyDefinedColumnsTo(columns);
	}

	private void copyDefaultColumnTo(ColumnContainer<?> columnsCopy) {
		ColumnConfiguration targetDefaults = columnsCopy.getDefaultColumn();
		C sourceDefaults = this.getDefaultColumn();
		if (sourceDefaults != null && targetDefaults != null) {
			targetDefaults.updateFrom(PropertyCopier.INSTANCE, sourceDefaults.getSettings());
		}
	}

	private void copyDefinedColumnsTo(ColumnContainer<?> columnsCopy) {
		for (C copySourceColumn : getDeclaredColumns()) {
			ColumnConfiguration columnCopy = columnsCopy.declareColumn(copySourceColumn.getName());
			copySourceColumn.copyTo(columnCopy);
		}
	}
}
