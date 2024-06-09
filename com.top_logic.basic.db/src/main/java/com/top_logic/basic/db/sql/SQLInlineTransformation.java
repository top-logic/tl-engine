/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;
import java.util.ListIterator;


/**
 * Base class for inline {@link SQLPart} transformations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SQLInlineTransformation<A> extends SQLTransformation<A> {

	@Override
	protected <S extends SQLStatement> SQLPart composeSQLQuery(SQLQuery<S> sql, S statement, A arg) {
		sql.setStatement(statement);
		return sql;
	}

	@Override
	protected SQLPart composeSQLSelect(SQLSelect sql, SQLTableReference tableReference,
			List<SQLColumnDefinition> columns, SQLExpression where, List<SQLOrder> orderBy, SQLLimit limit, A arg) {
		sql.setTableReference(tableReference);
		sql.setColumns(columns);
		sql.setWhere(where);
		sql.setOrderBy(orderBy);
		sql.setLimit(limit);
		return sql;
	}

	@Override
	protected SQLPart composeSQLInsert(SQLInsert sql, SQLTable table, List<SQLExpression> values, A arg) {
		sql.setTable(table);
		sql.setValues(values);
		return sql;
	}

	@Override
	protected SQLPart composeSQLInsertSelect(SQLInsertSelect sql, SQLTable table, SQLSelect select, A arg) {
		sql.setTable(table);
		sql.setSelect(select);
		return sql;
	}

	@Override
	protected SQLPart composeSQLDelete(SQLDelete sql, SQLTable table, SQLExpression condition, A arg) {
		sql.setTable(table);
		sql.setCondition(condition);
		return sql;
	}

	@Override
	protected SQLPart composeSQLUpdate(SQLUpdate sql, SQLTable table, List<SQLExpression> values, SQLExpression where,
			A arg) {
		sql.setTable(table);
		sql.setValues(values);
		sql.setWhere(where);
		return sql;
	}

	@Override
	protected SQLPart composeSQLUnion(SQLUnion sql, List<SQLSelect> selects, List<SQLOrder> orderBy, A arg) {
		sql.setSelects(selects);
		sql.setOrderBy(orderBy);
		return sql;
	}

	@Override
	protected SQLPart composeSQLColumnDefinition(SQLColumnDefinition sql, SQLExpression expr, A arg) {
		sql.setExpr(expr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLOrder(SQLOrder sql, SQLExpression expr, A arg) {
		sql.setExpr(expr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLBinaryExpression(SQLBinaryExpression sql, SQLExpression leftExpr,
			SQLExpression rightExpr, A arg) {
		sql.setLeftExpr(leftExpr);
		sql.setRightExpr(rightExpr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLCast(SQLCast sql, SQLExpression expr, A arg) {
		sql.setExpr(expr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLFunction(SQLFunction sql, List<SQLExpression> arguments, A arg) {
		sql.setArguments(arguments);
		return sql;
	}

	@Override
	protected SQLPart composeSQLNot(SQLNot sql, SQLExpression expr, A arg) {
		sql.setExpr(expr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLIsNull(SQLIsNull sql, SQLExpression expr, A arg) {
		sql.setExpr(expr);
		return sql;
	}

	@Override
	protected SQLPart composeSQLInSet(SQLInSet sql, SQLExpression expr, SQLExpression values, A arg) {
		sql.setExpr(expr);
		sql.setValues(values);
		return sql;
	}

	@Override
	protected SQLPart composeSQLTuple(SQLTuple sql, List<SQLExpression> expressions, A arg) {
		sql.setExpressions(expressions);
		return sql;
	}

	@Override
	protected SQLPart composeSQLJoin(SQLJoin sql, SQLExpression condition, SQLTableReference leftTable,
			SQLTableReference rightTable, A arg) {
		sql.setCondition(condition);
		sql.setLeftTable(leftTable);
		sql.setRightTable(rightTable);
		return sql;
	}

	@Override
	protected SQLPart composeSQLSubQuery(SQLSubQuery sql, SQLSelectionStatement select, A arg) {
		sql.setSelect(select);
		return sql;
	}

	@Override
	protected SQLPart composeSQLLimit(SQLLimit sql, SQLExpression startRow, SQLExpression stopRow, A arg) {
		sql.setStartRow(startRow);
		sql.setStopRow(stopRow);
		return sql;
	}

	@Override
	protected SQLPart composeSQLCase(SQLCase sql, SQLExpression condition, SQLExpression thenExpr,
			SQLExpression elseExpr, A arg) {
		sql.setCondition(condition);
		sql.setThen(thenExpr);
		sql.setElse(elseExpr);
		return sql;
	}

	@Override
	protected <P extends SQLPart> List<P> transformList(List<P> parts, A arg) {
		if (parts != null && !parts.isEmpty()) {
			for (ListIterator<P> it = parts.listIterator(); it.hasNext();) {
				it.set(transform(it.next(), arg));
			}
		}
		return parts;
	}

}
