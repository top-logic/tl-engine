/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.Closeable;
import java.io.IOException;

import com.top_logic.basic.sql.DBHelper;

/**
 * Helper class for {@link SQLDumper} that can be subclassed for the various SQL dialects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractInsertWriter implements InsertWriter {

	/** The used SQL dialect. */
	protected final DBHelper _sqlDialect;

	/** The output to append content to. */
	protected Appendable _out;

	/**
	 * Creates a new {@link AbstractInsertWriter}.
	 */
	public AbstractInsertWriter(DBHelper sqlDialect, Appendable out) {
		_sqlDialect = sqlDialect;
		_out = out;
	}
	
	/**
	 * Appends a new line.
	 */
	protected void newLine() throws IOException {
		newLine(_out);
	}

	/**
	 * Appends a new line to given {@link Appendable}.
	 */
	protected void newLine(Appendable out) throws IOException {
		out.append('\n');
	}

	/**
	 * Appends a line comment to the SQL script:
	 * 
	 * <pre>
	 * -- Some comment...
	 * </pre>
	 */
	protected final void appendLineComment(String comment) throws IOException {
		_out.append("-- ");
		appendLine(comment);
	}

	/**
	 * Appends the given line and calls {@link #newLine()}.
	 */
	protected final void appendLine(String line) throws IOException {
		_out.append(line);
		newLine();
	}

	@Override
	public void close() throws IOException {
		if (_out instanceof Closeable) {
			((Closeable) _out).close();
		}
	}
	
	
}

