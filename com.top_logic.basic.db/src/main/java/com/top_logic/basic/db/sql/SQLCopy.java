/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ArrayUtil;

/**
 * Visitor that copies an {@link SQLExpression}.
 * 
 * @see SQLFactory#copy(SQLPart)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLCopy implements SQLVisitor<SQLPart, Void> {

	/**
	 * Singleton {@link SQLCopy} instance.
	 */
	private static final SQLCopy INSTANCE = new SQLCopy();
	
	/**
	 * Allow subclasses of SQLCopy.
	 */
	protected SQLCopy() {
		// Singleton constructor.
	}

	/**
	 * Creates a copy of the given expression.
	 * 
	 * @param <T>
	 *        The top-level type of the expression.
	 * @param expr
	 *        The expression to copy.
	 * @return A copy of the given expression.
	 * 
	 * @see SQLFactory#copy(SQLPart) Public API.
	 */
	static <T extends SQLPart> T copy(T expr) {
		return INSTANCE.copy(expr, none);
	}
	
	@Override
	public <S extends SQLStatement> SQLPart visitSQLQuery(SQLQuery<S> sql, Void arg) {
		return query(new ArrayList<>(sql.getParameters()), copy(sql.getStatement(), arg));
	}
	
	@Override
	public SQLPart visitSQLBinaryExpression(SQLBinaryExpression sql, Void arg) {
		SQLExpression leftExpr = sql.getLeftExpr();
		SQLExpression rightExpr = sql.getRightExpr();
		return binaryExpression(sql.getOp(), copy(leftExpr, arg), copy(rightExpr, arg));
	}

	@Override
	public SQLPart visitSQLCast(SQLCast sql, Void arg) {
		SQLExpression expr = sql.getExpr();
		return cast(copy(expr, arg), sql.getDbType(), sql.getSize(), sql.getPrecision(), sql.getBinary());
	}

	@Override
	public SQLPart visitSQLBoolean(SQLBoolean sql, Void arg) {
		// Booleans are a singletons.
		return sql;
	}

	@Override
	public SQLPart visitSQLColumnDefinition(SQLColumnDefinition sql, Void arg) {
		return columnDef(copy(sql.getExpr(), arg), sql.getAliasName());
	}

	@Override
	public SQLPart visitSQLColumnReference(SQLColumnReference sql, Void arg) {
		return column(sql.getTableAlias(), sql.getColumnName(), sql.isNotNull());
	}

	@Override
	public SQLPart visitSQLFunction(SQLFunction sql, Void arg) {
		return function(sql.getFun(), copy(sql.getArguments(), arg));
	}

	@Override
	public SQLPart visitSQLIsNull(SQLIsNull sql, Void arg) {
		return isNull(copy(sql.getExpr(), arg));
	}

	@Override
	public SQLPart visitSQLInSet(SQLInSet sql, Void arg) {
		return inSet(copy(sql.getExpr(), arg), copy(sql.getValues(), arg));
	}

	@Override
	public SQLPart visitSQLTuple(SQLTuple sql, Void arg) {
		return tuple(copy(sql.getExpressions(), arg));
	}
	
	@Override
	public SQLPart visitSQLJoin(SQLJoin sql, Void arg) {
		return join(sql.isInner(), copy(sql.getLeftTable(), arg), copy(sql.getRightTable(), arg), copy(sql.getCondition(), arg));
	}

	@Override
	public SQLPart visitSQLNot(SQLNot sql, Void arg) {
		return not(copy(sql.getExpr(), arg));
	}

	@Override
	public SQLPart visitSQLOrder(SQLOrder sql, Void arg) {
		return order(sql.isDescending(), sql.getCollationHint(), copy(sql.getExpr(), arg));
	}

	@Override
	public SQLPart visitSQLLimit(SQLLimit sql, Void arg) {
		return limit(sql.getStartRow(), sql.getStopRow());
	}

	@Override
	public SQLPart visitSQLSelect(SQLSelect sql, Void arg) {
		List<SQLOrder> orderBy = copy(sql.getOrderBy(), arg);
		SQLLimit originalLimit = sql.getLimit();
		SQLLimit limit;
		if (originalLimit == SQLLimit.NO_LIMIT) {
			// no reason to copy constant
			limit = originalLimit;
		} else {
			limit = copy(originalLimit, arg);
		}
		List<SQLColumnDefinition> columns = copy(sql.getColumns(), arg);
		SQLTableReference from = copy(sql.getTableReference(), arg);
		SQLExpression where = copy(sql.getWhere(), arg);
		SQLSelect select = select(sql.isDistinct(), columns, from, where, orderBy, limit);
		select.setNoBlockHint(sql.getNoBlockHint());
		select.setForUpdate(sql.isForUpdate());
		return select;
	}

	@Override
	public SQLPart visitSQLInsert(SQLInsert sql, Void arg) {
		return insert(copy(sql.getTable(), arg), copyList(sql.getColumnNames()), copy(sql.getValues(), arg));
	}
	
	@Override
	public SQLPart visitSQLInsertSelect(SQLInsertSelect sql, Void arg) {
		return insert(copy(sql.getTable(), arg), copyList(sql.getColumnNames()), copy(sql.getSelect(), arg));
	}

	@Override
	public SQLPart visitSQLDelete(SQLDelete sql, Void arg) {
		return delete(copy(sql.getTable(), arg), copy(sql.getCondition(), arg));
	}

	@Override
	public SQLPart visitSQLUpdate(SQLUpdate sql, Void arg) {
		return update(copy(sql.getTable(), arg), copy(sql.getWhere(), arg),
			new ArrayList<>(sql.getColumns()),
			copy(sql.getValues(), arg));
	}

	@Override
	public SQLPart visitSQLLiteral(SQLLiteral sql, Void arg) {
		return literal(sql.getJdbcType(), sql.getValue());
	}

	@Override
	public SQLPart visitSQLTable(SQLTable sql, Void arg) {
		return table(sql.getTableName(), sql.getTableAlias());
	}

	@Override
	public SQLPart visitSQLSubQuery(SQLSubQuery sql, Void arg) {
		return subQuery(copy(sql.getSelect(), arg), sql.getTableAlias());
	}

	@Override
	public SQLPart visitSQLUnion(SQLUnion sql, Void arg) {
		return union(sql.isDistinct(), copy(sql.getSelects(), arg), copy(sql.getOrderBy(), arg));
	}

	@Override
	public SQLPart visitSQLParameter(SQLParameter sql, Void arg) {
		if (sql.isPotentiallyNull()) {
			return nullParameter(sql.getJdbcType(), sql.getConversion(), sql.getName());
		}
		return parameter(sql.getJdbcType(), sql.getConversion(), sql.getName());
	}

	@SuppressWarnings("unchecked")
	private <T extends SQLPart> T copy(T leftExpr, Void arg) {
		return (T) leftExpr.visit(this, arg);
	}

	private <T> List<T> copyList(List<T> list) {
		return new ArrayList<>(list);
	}

	private <T extends SQLPart> List<T> copy(List<T> expressions, Void arg) {
		ArrayList<T> result = new ArrayList<>(expressions.size());
		for (T expr : expressions) {
			result.add(copy(expr, arg));
		}
		return result;
	}

	@Override
	public SQLPart visitSQLSetLiteral(SQLSetLiteral sql, Void arg) {
		return setLiteral(sql.getValues(), sql.getTypes());
	}

	@Override
	public SQLPart visitSQLSetParameter(SQLSetParameter sql, Void arg) {
		return setParameter(sql.getConversion(), sql.getName(), sql.getTypes());
	}
	
	@Override
	public SQLPart visitSQLCase(SQLCase sql, Void arg) {
		return sqlCase(copy(sql.getCondition(), arg), copy(sql.getThen(), arg), copy(sql.getElse(), arg));
	}

	@Override
	public SQLPart visitSQLAlterTable(SQLAlterTable sql, Void arg) {
		return alterTable(copy(sql.getTable(), arg), copy(sql.getModification(), arg));
	}

	@Override
	public SQLPart visitSQLAddColumn(SQLAddColumn sql, Void arg) {
		return addColumn(sql.getColumnName(), sql.getType(), sql.isMandatory(), sql.isBinary(), sql.getSize(),
			sql.getPrecision(), sql.getDefaultValue());
	}

	@Override
	public SQLPart visitSQLModifyColumn(SQLModifyColumn sql, Void arg) {
		SQLModifyColumn result;
		switch (sql.getModificationAspect()) {
			case TYPE:
				result = modifyColumnType(sql.getColumnName(), sql.getType());
				result.setMandatory(sql.isMandatory());
				break;
			case MANDATORY:
				result = modifyColumnMandatory(sql.getColumnName(), sql.getType(), sql.isMandatory());
				break;
			default:
				throw new IllegalArgumentException();
		}
		return result.setBinary(sql.isBinary()).setSize(sql.getSize()).setPrecision(sql.getPrecision())
			.setDefaultValue(sql.getDefaultValue());
	}

	@Override
	public SQLPart visitSQLDropColumn(SQLDropColumn sql, Void arg) {
		return dropColumn(sql.getColumnName());
	}

	@Override
	public SQLPart visitSQLAddIndex(SQLAddIndex sql, Void arg) {
		return addIndex(sql.getTable(), sql.getIndexName(), sql.isUnique(),
			sql.getIndexColumns().toArray(ArrayUtil.EMPTY_STRING_ARRAY));
	}

	@Override
	public SQLPart visitSQLDropIndex(SQLDropIndex sql, Void arg) {
		return dropIndex(sql.getTable(), sql.getIndexName());
	}

}
