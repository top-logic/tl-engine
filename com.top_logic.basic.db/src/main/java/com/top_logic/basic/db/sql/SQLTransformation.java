/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * Base class for {@link SQLPart} transformations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SQLTransformation<A> implements SQLVisitor<SQLPart, A> {

	@Override
	public <S extends SQLStatement> SQLPart visitSQLQuery(SQLQuery<S> sql, A arg) {
		S statement = transform(sql.getStatement(), arg);

		return composeSQLQuery(sql, statement, arg);
	}

	@Override
	public SQLPart visitSQLSelect(SQLSelect sql, A arg) {
		SQLTableReference tableReference = transform(sql.getTableReference(), arg);
		List<SQLColumnDefinition> columns = transformList(sql.getColumns(), arg);
		SQLExpression where = transform(sql.getWhere(), arg);
		List<SQLOrder> orderBy = transformList(sql.getOrderBy(), arg);
		SQLLimit currentLimit = sql.getLimit();
		SQLLimit limit;
		if (currentLimit == SQLLimit.NO_LIMIT) {
			limit = SQLLimit.NO_LIMIT;
		} else {
			limit = transform(currentLimit, arg);
		}

		return composeSQLSelect(sql, tableReference, columns, where, orderBy, limit, arg);
	}

	@Override
	public SQLPart visitSQLInsert(SQLInsert sql, A arg) {
		SQLTable table = transform(sql.getTable(), arg);
		List<SQLExpression> values = transformList(sql.getValues(), arg);

		return composeSQLInsert(sql, table, values, arg);
	}

	@Override
	public SQLPart visitSQLInsertSelect(SQLInsertSelect sql, A arg) {
		SQLTable table = transform(sql.getTable(), arg);
		SQLSelect select = transform(sql.getSelect(), arg);

		return composeSQLInsertSelect(sql, table, select, arg);
	}

	@Override
	public SQLPart visitSQLDelete(SQLDelete sql, A arg) {
		SQLTable table = transform(sql.getTable(), arg);
		SQLExpression where = transform(sql.getCondition(), arg);

		return composeSQLDelete(sql, table, where, arg);
	}

	@Override
	public SQLPart visitSQLAddIndex(SQLAddIndex sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLDropIndex(SQLDropIndex sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLAlterTable(SQLAlterTable sql, A arg) {
		SQLTable table = transform(sql.getTable(), arg);
		SQLTableModification where = transform(sql.getModification(), arg);

		return composeSQLAlterTable(sql, table, where, arg);
	}

	@Override
	public SQLPart visitSQLAddColumn(SQLAddColumn sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLModifyColumn(SQLModifyColumn sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLDropColumn(SQLDropColumn sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLUpdate(SQLUpdate sql, A arg) {
		SQLTable table = transform(sql.getTable(), arg);
		List<SQLExpression> values = transformList(sql.getValues(), arg);
		SQLExpression where = transform(sql.getWhere(), arg);

		return composeSQLUpdate(sql, table, values, where, arg);
	}

	@Override
	public SQLPart visitSQLUnion(SQLUnion sql, A arg) {
		List<SQLSelect> selects = transformList(sql.getSelects(), arg);
		List<SQLOrder> orderBy = transformList(sql.getOrderBy(), arg);

		return composeSQLUnion(sql, selects, orderBy, arg);
	}

	@Override
	public SQLPart visitSQLColumnReference(SQLColumnReference sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLColumnDefinition(SQLColumnDefinition sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);

		return composeSQLColumnDefinition(sql, expr, arg);
	}

	@Override
	public SQLPart visitSQLOrder(SQLOrder sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);

		return composeSQLOrder(sql, expr, arg);
	}

	@Override
	public SQLPart visitSQLParameter(SQLParameter sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLLiteral(SQLLiteral sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLBoolean(SQLBoolean sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLBinaryExpression(SQLBinaryExpression sql, A arg) {
		SQLExpression leftExpr = transform(sql.getLeftExpr(), arg);
		SQLExpression rightExpr = transform(sql.getRightExpr(), arg);

		return composeSQLBinaryExpression(sql, leftExpr, rightExpr, arg);
	}

	@Override
	public SQLPart visitSQLCast(SQLCast sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);

		return composeSQLCast(sql, expr, arg);
	}

	@Override
	public SQLPart visitSQLFunction(SQLFunction sql, A arg) {
		List<SQLExpression> arguments = transformList(sql.getArguments(), arg);

		return composeSQLFunction(sql, arguments, arg);
	}

	@Override
	public SQLPart visitSQLNot(SQLNot sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);

		return composeSQLNot(sql, expr, arg);
	}

	@Override
	public SQLPart visitSQLIsNull(SQLIsNull sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);

		return composeSQLIsNull(sql, expr, arg);
	}

	@Override
	public SQLPart visitSQLInSet(SQLInSet sql, A arg) {
		SQLExpression expr = transform(sql.getExpr(), arg);
		SQLExpression values = transform(sql.getValues(), arg);

		return composeSQLInSet(sql, expr, values, arg);
	}

	@Override
	public SQLPart visitSQLTuple(SQLTuple sql, A arg) {
		List<SQLExpression> expressions = transformList(sql.getExpressions(), arg);

		return composeSQLTuple(sql, expressions, arg);
	}

	@Override
	public SQLPart visitSQLTable(SQLTable sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLJoin(SQLJoin sql, A arg) {
		SQLExpression condition = transform(sql.getCondition(), arg);
		SQLTableReference leftTable = transform(sql.getLeftTable(), arg);
		SQLTableReference rightTable = transform(sql.getRightTable(), arg);

		return composeSQLJoin(sql, condition, leftTable, rightTable, arg);
	}

	@Override
	public SQLPart visitSQLSubQuery(SQLSubQuery sql, A arg) {
		SQLSelectionStatement select = transform(sql.getSelect(), arg);

		return composeSQLSubQuery(sql, select, arg);
	}

	@Override
	public SQLPart visitSQLLimit(SQLLimit sql, A arg) {
		SQLExpression startRow = transform(sql.getStartRow(), arg);
		SQLExpression stopRow = transform(sql.getStopRow(), arg);

		return composeSQLLimit(sql, startRow, stopRow, arg);
	}

	@Override
	public SQLPart visitSQLSetLiteral(SQLSetLiteral sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLSetParameter(SQLSetParameter sql, A arg) {
		return sql;
	}

	@Override
	public SQLPart visitSQLCase(SQLCase sql, A arg) {
		SQLExpression condition = transform(sql.getCondition(), arg);
		SQLExpression thenExpr = transform(sql.getThen(), arg);
		SQLExpression elseExpr = transform(sql.getElse(), arg);

		return composeSQLCase(sql, condition, thenExpr, elseExpr, arg);
	}

	/**
	 * Creates the {@link SQLQuery} transformation result.
	 * 
	 * @param sql
	 *        The original part.
	 * @param statement
	 *        the transformed {@link SQLQuery#getStatement()}.
	 * @param arg
	 *        The visit argument.
	 * @return The transformation result.
	 */
	protected abstract <S extends SQLStatement> SQLPart composeSQLQuery(SQLQuery<S> sql, S statement, A arg);

	/**
	 * Creates the {@link SQLSelect} transformation result.
	 * 
	 * @param sql
	 *        The original part.
	 * @param tableReference
	 *        the transformed {@link SQLSelect#getTableReference()}.
	 * @param columns
	 *        the transformed {@link SQLSelect#getColumns()}.
	 * @param where
	 *        the transformed {@link SQLSelect#getWhere()}.
	 * @param orderBy
	 *        the transformed {@link SQLSelect#getOrderBy()}.
	 * @param limit
	 *        the transformed {@link SQLSelect#getLimit()}.
	 * @param arg
	 *        The visit argument.
	 * @return The transformation result.
	 */
	protected abstract SQLPart composeSQLSelect(SQLSelect sql, SQLTableReference tableReference,
			List<SQLColumnDefinition> columns, SQLExpression where, List<SQLOrder> orderBy, SQLLimit limit, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLInsert(SQLInsert sql, SQLTable table, List<SQLExpression> values, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLInsertSelect(SQLInsertSelect sql, SQLTable table, SQLSelect select, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLDelete(SQLDelete sql, SQLTable table, SQLExpression condition, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLAlterTable(SQLAlterTable sql, SQLTable table, SQLTableModification modification, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLUpdate(SQLUpdate sql, SQLTable table, List<SQLExpression> values, SQLExpression where,
			A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLUnion(SQLUnion sql, List<SQLSelect> selects, List<SQLOrder> orderBy, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLColumnDefinition(SQLColumnDefinition sql, SQLExpression expr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLOrder(SQLOrder sql, SQLExpression expr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLBinaryExpression(SQLBinaryExpression sql, SQLExpression leftExpr,
			SQLExpression rightExpr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLCast(SQLCast sql, SQLExpression expr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLFunction(SQLFunction sql, List<SQLExpression> arguments, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLNot(SQLNot sql, SQLExpression expr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLIsNull(SQLIsNull sql, SQLExpression expr, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLInSet(SQLInSet sql, SQLExpression expr, SQLExpression values, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLTuple(SQLTuple sql, List<SQLExpression> expressions, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLJoin(SQLJoin sql, SQLExpression condition, SQLTableReference leftTable,
			SQLTableReference rightTable, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLSubQuery(SQLSubQuery sql, SQLSelectionStatement select, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLLimit(SQLLimit sql, SQLExpression startRow, SQLExpression stopRow, A arg);

	/** @see #composeSQLQuery(SQLQuery, SQLStatement, Object) */
	protected abstract SQLPart composeSQLCase(SQLCase sql, SQLExpression condition, SQLExpression thenExpr,
			SQLExpression elseExpr, A arg);

	/**
	 * Transforms a generic list of {@link SQLPart}s.
	 * 
	 * @param parts
	 *        The parts to transform.
	 * @param arg
	 *        The argument to the visit.
	 * @return The transformed list.
	 */
	protected abstract <P extends SQLPart> List<P> transformList(List<P> parts, A arg);

	/**
	 * Transforms a generic {@link SQLPart}.
	 * 
	 * @param part
	 *        The part to transform.
	 * @param arg
	 *        The argument to the visit.
	 * @return The transformed part.
	 */
	protected <P extends SQLPart> P transform(P part, A arg) {
		@SuppressWarnings("unchecked")
		P transformedPart = (P) part.visit(this, arg);
		return transformedPart;
	}

}
