/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBHelper;

/**
 * Helper class to create sources of SQL statements.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class SimpleSQLBuffer implements Appendable {

	/**
	 * Abstraction of the database used
	 */
	protected final DBHelper sqlDialect;

	/**
	 * The SQL source buffer.
	 */
	protected final StringBuilder buffer = new StringBuilder();

	public SimpleSQLBuffer(DBHelper sqlDialect) {
		this.sqlDialect = sqlDialect;
	}

	@Override
	public Appendable append(CharSequence csq) {
		return buffer.append(csq);
	}

	@Override
	public Appendable append(char c) {
		return buffer.append(c);
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) {
		return buffer.append(csq, start, end);
	}

	/**
	 * Returns the context of the currently visited SQL part, i.e. the "parent" {@link SQLPart}.
	 */
	abstract SQLPart context();

	/**
	 * Sets the context of the currently visited SQL part, i.e. the "parent" {@link SQLPart}.
	 */
	abstract void setContext(SQLPart context);

}

