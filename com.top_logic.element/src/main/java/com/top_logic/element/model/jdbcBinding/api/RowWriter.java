/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding.api;

import com.top_logic.model.TLObject;

/**
 * Algorithm for exporting values from a {@link TLObject} to a database row.
 * 
 * @see RowReader
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowWriter {

	/**
	 * Exports values from the given {@link TLObject} to the given row.
	 *
	 * @param row
	 *        The row to update.
	 * @param model
	 *        The object to export.
	 */
	void write(ExportRow row, TLObject model);

}
