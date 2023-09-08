/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

/**
 * Rewriter for a single table row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowTransformer {

	/**
	 * Transforms the given row.
	 * 
	 * @param row
	 *        The table row to transform.
	 * @param out
	 *        The output to write the transformed result to. Not writing the passed row drops the
	 *        row. A row can be split into multiple rows by creating new rows from the given one and
	 *        passing these to the output.
	 */
	void transform(RowValue row, RowWriter out);

}
