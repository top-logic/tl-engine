/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api;

/**
 * Access to a table row being imported.
 * 
 * @see ExportRow
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImportRow {

	/**
	 * Reads the value of the column with the given name from the currently imported row.
	 *
	 * @param column
	 *        Column to read.
	 * @return Value read from the database.
	 */
	Object getValue(String column);

}
