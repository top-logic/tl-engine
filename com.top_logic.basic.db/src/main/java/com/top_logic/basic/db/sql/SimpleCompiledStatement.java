/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;

/**
 * {@link PrepStmtBasedCompiledStatement} creating {@link PreparedStatement} without parameters.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCompiledStatement extends PrepStmtBasedCompiledStatement {

	private final String _source;

	/**
	 * Creates a new {@link SimpleCompiledStatement}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 * @param source
	 *        the actual statement.
	 */
	public SimpleCompiledStatement(DBHelper sqlDialect, String source) {
		super(sqlDialect);
		_source = source;
	}

	@Override
	public String toString(Object[] arguments) {
		return _source;
	}

	@Override
	protected void setArguments(PreparedStatement statement, Object[] arguments) throws SQLException {
		// no arguments
	}

}

