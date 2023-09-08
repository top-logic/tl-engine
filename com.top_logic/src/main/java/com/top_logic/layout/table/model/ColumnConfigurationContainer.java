/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * {@link ColumnContainer} for {@link ColumnConfiguration}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumnConfigurationContainer extends ColumnContainerImpl<ColumnConfiguration> {

	/**
	 * Creates a {@link ColumnConfigurationContainer}.
	 * 
	 * @param defaultColumn
	 *        See {@link #getDefaultColumn()}.
	 */
	public ColumnConfigurationContainer(ColumnConfiguration defaultColumn) {
		super(defaultColumn);
	}

	@Override
	protected ColumnConfiguration createColumn(String columnName) {
		ColumnConfiguration defaultColumn = getDefaultColumn();
		if (defaultColumn == null) {
			return ColumnConfiguration.column(columnName);
		}
		return defaultColumn.copy(columnName);
	}

}
