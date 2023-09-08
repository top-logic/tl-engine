/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Adaptor implementation of {@link SQLVisitor} ignoring the argument of the
 * visit and returning <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSQLVisitor<R, A> implements SQLVisitor<R, A> {

	@Override
	public <S extends SQLStatement> R visitSQLQuery(SQLQuery<S> sql, A arg) {
		return visitSQLPart(sql, arg);
	}
	
	@Override
	public R visitSQLBinaryExpression(SQLBinaryExpression sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLCast(SQLCast sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLFunction(SQLFunction sql, A arg) {
		return visitSQLExpression(sql, arg);
	}
	
	@Override
	public R visitSQLBoolean(SQLBoolean sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLColumnReference(SQLColumnReference sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLIsNull(SQLIsNull sql, A arg) {
		return visitSQLExpression(sql, arg);
	}
	
	@Override
	public R visitSQLInSet(SQLInSet sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLTuple(SQLTuple sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLJoin(SQLJoin sql, A arg) {
		return visitSQLTableReference(sql, arg);
	}
	
	@Override
	public R visitSQLNot(SQLNot sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLOrder(SQLOrder sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	@Override
	public R visitSQLLimit(SQLLimit sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	@Override
	public R visitSQLSelect(SQLSelect sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLInsert(SQLInsert sql, A arg) {
		return visitSQLStatement(sql, arg);
	}
	
	@Override
	public R visitSQLInsertSelect(SQLInsertSelect sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLDelete(SQLDelete sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLUpdate(SQLUpdate sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLColumnDefinition(SQLColumnDefinition sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	@Override
	public R visitSQLLiteral(SQLLiteral sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLTable(SQLTable sql, A arg) {
		return visitSQLTableReference(sql, arg);
	}

	@Override
	public R visitSQLSubQuery(SQLSubQuery sql, A arg) {
		return visitSQLTableReference(sql, arg);
	}

	@Override
	public R visitSQLUnion(SQLUnion sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	@Override
	public R visitSQLParameter(SQLParameter sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLSetLiteral(SQLSetLiteral sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLSetParameter(SQLSetParameter sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLCase(SQLCase sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	@Override
	public R visitSQLAlterTable(SQLAlterTable sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLAddIndex(SQLAddIndex sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLDropIndex(SQLDropIndex sql, A arg) {
		return visitSQLStatement(sql, arg);
	}

	@Override
	public R visitSQLAddColumn(SQLAddColumn sql, A arg) {
		return visitSQLTableModification(sql, arg);
	}

	@Override
	public R visitSQLModifyColumn(SQLModifyColumn sql, A arg) {
		return visitSQLTableModification(sql, arg);
	}

	@Override
	public R visitSQLDropColumn(SQLDropColumn sql, A arg) {
		return visitSQLTableModification(sql, arg);
	}

	/**
	 * Common visit case for {@link SQLTableModification}.
	 */
	protected R visitSQLTableModification(SQLTableModification sql, A arg) {
		return visitSQLExpression(sql, arg);
	}

	/**
	 * Common visit case for {@link SQLTableReference}.
	 */
	protected R visitSQLTableReference(SQLTableReference sql, A arg) {
		return visitSQLPart(sql, arg);
	}
	
	/**
	 * Common visit case for {@link SQLStatement}.
	 */
	protected R visitSQLStatement(SQLStatement sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	/**
	 * Common visit case for {@link SQLExpression}.
	 */
	protected R visitSQLExpression(SQLExpression sql, A arg) {
		return visitSQLPart(sql, arg);
	}

	/**
	 * Common visit case for {@link SQLPart}.
	 */
	protected R visitSQLPart(SQLPart sql, A arg) {
		return null;
	}

}
