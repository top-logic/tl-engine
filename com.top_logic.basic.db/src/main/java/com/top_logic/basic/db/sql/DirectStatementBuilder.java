/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;

/**
 * Creates {@link CompiledStatement} which do not use {@link PreparedStatement} to resolve answers.
 * For this the {@link SQLPart} is visited on demand and all arguments are inserted directly into
 * the SQL String.
 * 
 * @see DirectCompiledStatement
 * @see StatementBuilder creator of compiled statements that uses prepared statement
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class DirectStatementBuilder extends AbstractStatementBuilder<InternalSQLBuffer> {

	static final DirectStatementBuilder INSTANCE = new DirectStatementBuilder();

	private DirectStatementBuilder() {
		// singleton instance
	}

	@Override
	public Void visitSQLParameter(SQLParameter sql, InternalSQLBuffer buffer) {
		Object converted = buffer.getArgumentConverted(sql);
		visitObject(converted, buffer);
		return none;
	}

	@Override
	public Void visitSQLLiteral(SQLLiteral sql, InternalSQLBuffer buffer) {
		visitObject(sql.getValue(), buffer);
		return none;
	}

	@Override
	public Void visitSQLSetParameter(SQLSetParameter sql, InternalSQLBuffer buffer) {
		String paramName = sql.getName();
		if (buffer.hasArgument(paramName)) {
			Object converted = buffer.getArgumentConverted(sql);
			if (converted instanceof Iterable<?>) {
				visitIterable((Iterable<?>) converted, buffer);
			} else {
				visitObject(converted, buffer);
			}
		} else {
			// This may happen when generating the toString() representation of a statement (where
			// no parameters are actually given).
			buffer.append('?');
		}
		return none;
	}

	@Override
	protected Integer getLimitRow(SQLLimit limit, SQLExpression rowExpr, InternalSQLBuffer buffer) {
		if (rowExpr instanceof SQLParameter) {
			return (Integer) buffer.getArgument(((SQLParameter) rowExpr).getName());
		}
		if (rowExpr instanceof SQLLiteral) {
			return (Integer) ((SQLLiteral) rowExpr).getValue();
		}
		throw new IllegalArgumentException("Unable to get row number for sql expression: " + rowExpr);
	}

	/**
	 * Creates an {@link CompiledStatement} which creates the SQL string on demand from the given
	 * {@link SQLPart} by inserting the whole arguments directly into the SQL string.
	 * 
	 * @see SQLQuery#toSql(DBHelper, SQLPart, Map)
	 */
	static CompiledStatement compileStatement(DBHelper sqlDialect, final SQLPart sql,
			final Map<String, Integer> argumentIndexByName) {
		return new DirectCompiledStatement(sqlDialect) {

			@Override
			public String toString(Object[] arguments) {
				InternalSQLBuffer buffer = new InternalSQLBuffer(_sqlDialect, argumentIndexByName, arguments);
				SQLPart part = sql;
				part = AbstractStatementBuilder.postProcessSQL(_sqlDialect, part, buffer);
				part.visit(DirectStatementBuilder.INSTANCE, buffer);
				return buffer.buffer.toString();
			}
		};
	}

}

