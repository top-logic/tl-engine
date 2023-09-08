/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.top_logic.basic.db.model.DBTable;

/**
 * Consumer of SQL import statement values.
 * 
 * @see #appendInsert(DBTable, List)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InsertWriter extends Closeable {

	/**
	 * Appends content at the top of the whole dump.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	default void beginDump() throws IOException {
		// Ignore.
	}

	/**
	 * Appends content at the end of the whole dump.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	default void endDump() throws IOException {
		// Ignore.
	}

	/**
	 * Appends an insert for the given values in the given table.
	 * 
	 * @param table
	 *        The table to insert to
	 * @param values
	 *        The values for the table. Each value must contain exactly one entry for each column in
	 *        column order. The values must not contain <code>null</code> values.
	 */
	void appendInsert(DBTable table, List<Object[]> values) throws IOException;

	@Override
	default void close() throws IOException {
		// Ignore.
	}

}
