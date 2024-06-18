/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
