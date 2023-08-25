/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.ddlimporter.api;

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
