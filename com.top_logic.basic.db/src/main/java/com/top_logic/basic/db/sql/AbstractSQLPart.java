/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.MySQLHelper;

/**
 * Common base class for all {@link SQLPart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSQLPart implements SQLPart {

	private static final DBHelper TO_STRING_DIALECT = DBHelper.createDefaultInstance(MySQLHelper.class);

	@Override
	public String toString() {
		Map<String, Integer> args = Collections.emptyMap();
		SQLBuffer sqlBuffer = new SQLBuffer(TO_STRING_DIALECT, args);
		this.visit(new StatementBuilder() {

			@Override
			public Void visitSQLLiteral(SQLLiteral sql, SQLBuffer buffer) {
				buffer.sqlDialect.appendValue(buffer.buffer, sql.getValue());
				return none;
			}

			@Override
			public Void visitSQLParameter(SQLParameter sql, SQLBuffer buffer) {
				return toStringParameter(sql, buffer);
			}

			@Override
			public Void visitSQLSetParameter(SQLSetParameter sql, SQLBuffer buffer) {
				return toStringParameter(sql, buffer);
			}

			private Void toStringParameter(AbstractSQLParameter sql, SQLBuffer buffer) {
				buffer.append(":");
				buffer.append(sql.getName());
				return null;
			}

		}, sqlBuffer);
		CompiledStatement sql = sqlBuffer.createStatement();
		return sql.toString();
	}

}

