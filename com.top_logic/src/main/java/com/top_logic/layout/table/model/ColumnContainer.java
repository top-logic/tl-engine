/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Handling of collections of columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnContainer<C extends ColumnConfiguration> {

	/**
	 * Get the {@link ColumnConfiguration} for the column with the given name and ensure that the
	 * result can be modified.
	 */
	C declareColumn(String columnName);

	/**
	 * Removes the explicit configuration for the column with the given name.
	 * 
	 * @param columnName
	 *        The column to remove.
	 * @return The removed {@link ColumnConfiguration}, or <code>null</code>, if the requested
	 *         column was not explicitly configured.
	 * 
	 * @see #declareColumn(String)
	 */
	C removeColumn(String columnName);

	/**
	 * The {@link ColumnConfiguration} that was defined through {@link #declareColumn(String)} with
	 * the given name.
	 * 
	 * @param aName
	 *        The name of the requested {@link ColumnConfiguration}.
	 * @return The {@link ColumnConfiguration} defined with {@link #declareColumn(String)} or
	 *         <code>null</code>, if no such column exists.
	 * 
	 * @see #declareColumn(String)
	 * @see #getDefaultColumn()
	 * @see #getCol(String)
	 */
	C getDeclaredColumn(String aName);

	/**
	 * The configuration that applies to all column that are not explicitly declared.
	 * 
	 * @see #getDeclaredColumn(String)
	 */
	C getDefaultColumn();

	/**
	 * A read-only version of the column with the given name.
	 * 
	 * @param aName
	 *        The column name.
	 * @return The configuration for the requested column.
	 */
	C getCol(String aName);

	/**
	 * All explicitly declared columns.
	 * 
	 * @see #declareColumn(String)
	 */
	Collection<? extends C> getDeclaredColumns();

	/**
	 * All columns indexed by their name.
	 */
	Map<String, ColumnConfiguration> createColumnIndex();

	/**
	 * All elementary columns (that are not column groups).
	 */
	List<ColumnConfiguration> getElementaryColumns();

	/**
	 * All names of {@link #getElementaryColumns()}
	 */
	List<String> getElementaryColumnNames();

	/**
	 * Registration of the given column/name combination.
	 */
	void addColumn(C columnConfiguration);

}
