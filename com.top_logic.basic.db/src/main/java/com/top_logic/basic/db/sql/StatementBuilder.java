/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.util.Map;

import com.top_logic.basic.sql.DBHelper;

/**
 * Builder of {@link CompiledStatement}s from {@link SQLExpression}s that creates
 * {@link PreparedStatement}
 * 
 * @see DefaultCompiledStatementImpl
 * @see DirectStatementBuilder creator of compiled statement which do not create prepared statement
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class StatementBuilder extends AbstractStatementBuilder<SQLBuffer> {

	private static final StatementBuilder INSTANCE = new StatementBuilder();

	StatementBuilder() {
		// nothing to do here
	}
	
	@Override
	protected Integer getLimitRow(SQLLimit limit, SQLExpression rowExpr, SQLBuffer buffer) {
		if (rowExpr instanceof SQLLiteral) {
			return (Integer) ((SQLLiteral) rowExpr).getValue();
		}
		throw new IllegalArgumentException("Unable to get row number for sql expression: " + rowExpr);
	}

	@Override
	public Void visitSQLParameter(SQLParameter sql, SQLBuffer buffer) {
		buffer.appendParameter(sql.getJdbcType(), sql.getConversion(), sql.getName());
		return none;
	}
	
	@Override
	public Void visitSQLLiteral(SQLLiteral sql, SQLBuffer buffer) {
		buffer.appendConstant(sql.getJdbcType(), sql.getValue());
		return none;
	}

	@Override
	public Void visitSQLSetParameter(SQLSetParameter sql, SQLBuffer arg) {
		throw new UnsupportedOperationException("Unable to handle set parameter");
	}

	static CompiledStatement compileStatement(DBHelper sqlDialect, SQLPart expr,
			Map<String, Integer> argumentIndexByName) {
		SQLBuffer buffer = new SQLBuffer(sqlDialect, argumentIndexByName);
		expr = AbstractStatementBuilder.postProcessSQL(sqlDialect, expr, SetParameterResolver.RESOLVE_ERROR);
		expr.visit(INSTANCE, buffer);
		return buffer.createStatement();
	}
	
}
