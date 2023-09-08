/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * Tester to determine, whether a table cell is defined (e.g. an accessor can deliver a valid value
 * for the table cell), or not.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface CellExistenceTester {

	/**
	 * true, if the table cell, specified by row object and column name, is existent in
	 *         terms of retrievable cell value, false otherwise.
	 */
	boolean isCellExistent(Object rowObject, String columnName);
}
