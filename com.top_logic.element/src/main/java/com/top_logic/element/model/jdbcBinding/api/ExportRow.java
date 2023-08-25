/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api;

/**
 * Write interface to a table row being exported.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see ImportRow
 */
public interface ExportRow {

	/**
	 * Updates the value of the column with the given name in the row currently being exported.
	 *
	 * @param column
	 *        Name of the column to write.
	 * @param value
	 *        Value to be written to the database.
	 */
	void setValue(String column, Object value);

}
