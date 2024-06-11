/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.sql.SQLModifyColumn.ModificationAspect;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLQuery.SetParameter;
import com.top_logic.basic.db.sql.SQLQuery.SimpleParameter;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBType;

/**
 * Factory for {@link SQLExpression}s.
 * 
 * <p>
 * SQL statements created with this factory can be compiled to executable statements using
 * {@link SQLQuery#toSql(com.top_logic.basic.sql.DBHelper)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLFactory {
	
	/** Constant to use as table alias when when no table alias is used. */
	public static final String NO_TABLE_ALIAS = null;

	/** Constant to use when creating a column which is not null. */
	public static final boolean NOT_NULL = true;

	/** Constant to use when no parameters are given. */
	public static final List<Parameter> NO_PARAMETERS = Collections.<Parameter> emptyList();

	/** Constant to use when no order is necessary. */
	public static final List<SQLOrder> NO_ORDERS = Collections.<SQLOrder> emptyList();

	/**
	 * Create a {@link Parameter} for an {@link SQLParameter}.
	 * 
	 * @param type
	 *        {@link DBType} of the value later filled into the corresponding {@link SQLParameter}.
	 * @param name
	 *        Name of the parameter.
	 */
	public static Parameter parameterDef(DBType type, String name) {
		return new SimpleParameter(type, name);
	}

	/**
	 * Create a {@link Parameter} for an {@link SQLSetParameter}.
	 * 
	 * @param name
	 *        Name of the parameter.
	 * @param types
	 *        {@link DBType}s of the values later filled into the corresponding
	 *        {@link SQLSetParameter}.
	 */
	public static Parameter setParameterDef(String name, DBType... types) {
		return new SetParameter(types, name);
	}

	/**
	 * Creates a SQL query without parameters from the given statement.
	 * 
	 * @param statement
	 *        The {@link SQLStatement} to execute.
	 * 
	 * @see #query(List, SQLStatement)
	 * @see SQLQuery#toSql(com.top_logic.basic.sql.DBHelper)
	 */
	public static <S extends SQLStatement> SQLQuery<S> query(S statement) {
		return query(NO_PARAMETERS, statement);
	}
	
	/**
	 * Creates a SQL query executing the given statement.
	 *
	 * @param parameters
	 *        Parameters of the query, see
	 *        {@link CompiledStatement#executeQuery(java.sql.Connection, Object...)}.
	 * @param statement
	 *        The {@link SQLStatement} to execute.
	 * @see SQLQuery#toSql(com.top_logic.basic.sql.DBHelper)
	 */
	public static <S extends SQLStatement> SQLQuery<S> query(List<Parameter> parameters, S statement) {
		return new SQLQuery<>(parameters, statement);
	}

	/**
	 * Representation of column "*" of the table with the given table alias.
	 */
	public static SQLColumnReference allColumns(String tableAlias) {
		return new SQLColumnReference(tableAlias);
	}

	/**
	 * Representation of column <code>columnName</code> if the query only selects from a single
	 * table.
	 * 
	 * @see #table(String)
	 */
	public static SQLColumnReference column(String columnName) {
		return column(NO_TABLE_ALIAS, columnName);
	}

	/**
	 * Representation of column <code>columnName</code> of the table with the given table alias.
	 */
	public static SQLColumnReference column(String tableAlias, String columnName) {
		return column(tableAlias, columnName, !NOT_NULL);
	}

	/**
	 * Representation of column <code>columnName</code> of the table with the given table alias,
	 * with additional info that the column is not <code>null</code>.
	 */
	public static SQLColumnReference notNullColumn(String tableAlias, String columnName) {
		return column(tableAlias, columnName, NOT_NULL);
	}

	/**
	 * Representation of column <code>columnName</code> of the table with the given table alias,
	 * with additional info whether the column is not <code>null</code>.
	 */
	public static SQLColumnReference column(String tableAlias, String columnName, boolean notNull) {
		return new SQLColumnReference(tableAlias, columnName, notNull);
	}

	/**
	 * Creates a SQL tuple expression.
	 * 
	 * @param expressions
	 *        The contents of the tuple.
	 */
	public static SQLTuple tuple(SQLExpression... expressions) {
		return tuple(Arrays.asList(expressions));
	}

	/**
	 * Creates a SQL tuple expression.
	 * 
	 * @param expressions
	 *        The contents of the tuple.
	 */
	public static SQLTuple tuple(List<SQLExpression> expressions) {
		return new SQLTuple(expressions);
	}

	public static SQLJoin innerJoin(SQLTableReference leftTable, SQLTableReference rightTable) {
		return join(true, leftTable, rightTable, SQLBoolean.TRUE);
	}
	
	public static SQLJoin innerJoin(SQLTableReference leftTable, SQLTableReference rightTable, SQLExpression condition) {
		return join(true, leftTable, rightTable, condition);
	}

	public static SQLJoin leftJoin(SQLTableReference leftTable, SQLTableReference rightTable, SQLExpression condition) {
		return join(false, leftTable, rightTable, condition);
	}

	public static SQLJoin join(boolean inner, SQLTableReference leftTable, SQLTableReference rightTable, SQLExpression condition) {
		return new SQLJoin(inner, leftTable, rightTable, condition);
	}

	public static SQLSelect select(List<SQLColumnDefinition> columns, SQLTableReference from) {
		return select(columns, from, SQLBoolean.TRUE);
	}

	public static SQLSelect select(List<SQLColumnDefinition> columns, SQLTableReference from, SQLExpression where) {
		return select(columns, from, where, noOrder());
	}

	public static SQLSelect selectDistinct(List<SQLColumnDefinition> columns, SQLTableReference from) {
		return selectDistinct(columns, from, SQLBoolean.TRUE);
	}

	public static SQLSelect selectDistinct(List<SQLColumnDefinition> columns, SQLTableReference from,
			SQLExpression where) {
		return selectDistinct(columns, from, where, noOrder());
	}

	public static SQLSelect selectDistinct(List<SQLColumnDefinition> columns, SQLTableReference from,
			SQLExpression where, List<SQLOrder> orderBy) {
		return select(true, columns, from, where, orderBy);
	}

	public static SQLSelect select(List<SQLColumnDefinition> columns, SQLTableReference from, SQLExpression where,
			List<SQLOrder> orderBy) {
		return select(false, columns, from, where, orderBy);
	}

	public static SQLSelect select(boolean distinct, List<SQLColumnDefinition> columns, SQLTableReference from) {
		return select(distinct, columns, from, SQLBoolean.TRUE);
	}

	public static SQLSelect select(boolean distinct, List<SQLColumnDefinition> columns, SQLTableReference from,
			SQLExpression where) {
		return select(distinct, columns, from, where, noOrder());
	}

	public static SQLSelect select(boolean distinct, List<SQLColumnDefinition> columns, SQLTableReference from, SQLExpression where, List<SQLOrder> orderBy) {
		return select(distinct, columns, from, where, orderBy, SQLLimit.NO_LIMIT);
	}

	public static SQLSelect select(boolean distinct, List<SQLColumnDefinition> columns, SQLTableReference from,
			SQLExpression where, List<SQLOrder> orderBy, SQLLimit limit) {
		return new SQLSelect(distinct, columns, from, where, orderBy, limit);
	}
	
	public static SQLSubQuery subQuery(SQLSelectionStatement select, String tableAlias) {
		return new SQLSubQuery(select, tableAlias);
	}

	/**
	 * Creates an {@link SQLExpression} that deletes from the given table all rows matching the
	 * given {@link SQLExpression}.
	 */
	public static SQLDelete delete(SQLTable table, SQLExpression condition) {
		return new SQLDelete(table, condition);
	}

	/**
	 * Creates a {@link SQLStatement} that adds an index to the given table.
	 * 
	 * @param table
	 *        The table to modify.
	 * @param indexName
	 *        The name of the new index.
	 * @param unique
	 *        Whether the new index should be unique.
	 * @param columnNames
	 *        Names of the columns in the new index in the given order (<code>null</code> values are
	 *        skipped).
	 */
	public static SQLAddIndex addIndex(SQLTable table, String indexName, boolean unique, String... columnNames) {
		SQLAddIndex addIndex = new SQLAddIndex(table, indexName);
		addIndex.setUnique(unique);
		addNonNull(addIndex.getIndexColumns(), columnNames);
		return addIndex;
	}

	@SafeVarargs
	private static <T> void addNonNull(List<T> collectionColumns, T... elements) {
		for (T element : elements) {
			if (element == null) {
				continue;
			}
			collectionColumns.add(element);
		}
	}

	/**
	 * Creates a {@link SQLStatement} that drops an index from the given table.
	 * 
	 * @param table
	 *        The table to modify.
	 * @param indexName
	 *        The name of the index to drop.
	 */
	public static SQLDropIndex dropIndex(SQLTable table, String indexName) {
		return new SQLDropIndex(table, indexName);
	}

	/**
	 * Creates a {@link SQLTableModification} that adds a new column to the given table.
	 * 
	 * @param columnName
	 *        Name of the new column.
	 * @param type
	 *        The database type of the new column.
	 */
	public static SQLAddColumn addColumn(SQLTable table, String columnName, DBType type, boolean mandatory,
			boolean binary, long size,
			int precision, Object defaultValue) {
		return SQLFactory.addColumn(table, columnName, type)
			.setMandatory(mandatory)
			.setBinary(binary)
			.setSize(size)
			.setPrecision(precision)
			.setDefaultValue(defaultValue);
	}

	/**
	 * Creates a {@link SQLTableModification} that adds a new column to the given table.
	 * 
	 * @param column
	 *        Name of the new column.
	 */
	public static SQLAddColumn addColumn(SQLTable table, DBColumn column, Object defaultValue) {
		return SQLFactory.addColumn(table, column.getDBName(), column.getType(), column.isMandatory(),
			column.isBinary(),
			column.getSize(), column.getPrecision(), defaultValue);
	}

	/**
	 * Creates a {@link SQLTableModification} that adds a new column to the given table.
	 * 
	 * @param columnName
	 *        Name of the new column.
	 * @param type
	 *        The database type of the new column.
	 */
	public static SQLAddColumn addColumn(SQLTable table, String columnName, DBType type) {
		return new SQLAddColumn(table, columnName, type);
	}

	/**
	 * Creates a {@link SQLTableModification} that modifies the name of a column in the given table.
	 * 
	 * @param columnName
	 *        Name of the column to modify.
	 * @param newName
	 *        The new database name of the column.
	 */
	public static SQLModifyColumn modifyColumnName(SQLTable table, String columnName, DBType type, String newName) {
		return new SQLModifyColumn(table, columnName, ModificationAspect.NAME, type).setNewName(newName);
	}

	/**
	 * Creates a {@link SQLTableModification} that modifies the type of a column in the given table.
	 * 
	 * @param columnName
	 *        Name of the column to modify.
	 * @param type
	 *        The new database type of the column.
	 */
	public static SQLModifyColumn modifyColumnType(SQLTable table, String columnName, DBType type) {
		return new SQLModifyColumn(table, columnName, ModificationAspect.TYPE, type);
	}

	/**
	 * Creates a {@link SQLTableModification} that modifies the mandatory state of a column in the
	 * given table.
	 * 
	 * @apiNote Whereas the meaning of the method is to modify the mandatory state of the column,
	 *          some databases need the whole column definition. In this case the complete column
	 *          description is applied.
	 * 
	 * @param columnName
	 *        Name of the column to modify.
	 * @param type
	 *        The new database type of the new.
	 * @param mandatory
	 *        The new mandatory state of the column.
	 */
	public static SQLModifyColumn modifyColumnMandatory(SQLTable table, String columnName, DBType type,
			boolean mandatory) {
		return new SQLModifyColumn(table, columnName, ModificationAspect.MANDATORY, type).setMandatory(mandatory);
	}

	/**
	 * Creates a {@link SQLTableModification} that drops a column in the given table.
	 * 
	 * @param columnName
	 *        Name of the column to drop.
	 */
	public static SQLDropColumn dropColumn(SQLTable table, String columnName) {
		return new SQLDropColumn(table, columnName);
	}

	public static SQLInsert insert(SQLTable table) {
		return insert(table, new ArrayList<>(), new ArrayList<>());
	}
	
	public static SQLInsert insert(SQLTable table, List<String> columnNames, List<? extends SQLExpression> values) {
		return new SQLInsert(table, columnNames, values);
	}
	
	/**
	 * Creates a bulk INSERT statement that inserts the result of another selection.
	 * 
	 * @param table
	 *        The table to insert into.
	 * @param columnNames
	 *        The column names to fill. Must match the number of column names specified in the given
	 *        select statement.
	 * @param select
	 *        The selection of rows to insert.
	 */
	public static SQLInsertSelect insert(SQLTable table, List<String> columnNames, SQLSelect select) {
		return new SQLInsertSelect(table, columnNames, select);
	}

	public static SQLUpdate update(SQLTable table, SQLExpression where, List<String> columns,
			List<SQLExpression> values) {
		return new SQLUpdate(table, where, columns, values);
	}

	public static SQLUnion union(List<SQLSelect> selects) {
		return union(true, selects);
	}
	
	public static SQLUnion union(boolean distinct, List<SQLSelect> selects) {
		return union(distinct, selects, new ArrayList<>()); // Note: SQLPart expressions are
															// mutable.
	}
	
	public static SQLUnion union(List<SQLSelect> selects, List<SQLOrder> orderBy) {
		return union(true, selects, orderBy);
	}
	
	public static SQLUnion union(boolean distinct, List<SQLSelect> selects, List<SQLOrder> orderBy) {
		return new SQLUnion(distinct, selects, orderBy);
	}

	public static SQLExpression binaryExpression(SQLOp op, SQLExpression leftExpr, SQLExpression rightExpr) {
		switch (op) {
		case add:
			return add(leftExpr, rightExpr);
		case sub:
			return sub(leftExpr, rightExpr);
		case mul:
			return mul(leftExpr, rightExpr);
		case div:
			return div(leftExpr, rightExpr);
		case eq: {
			// Note: The logic equality is expanded by the SQLFactory to multiple compare
			// operations. Therefore the native SQLOp#eq must represent the native SQL compare
			// logic. This method is used from SQLCopy to copy an expression.
			return eqSQL(leftExpr, rightExpr);
		}
		case and: {
			return and(leftExpr, rightExpr);
		}
		case or: {
			return or(leftExpr, rightExpr);
		}
		case ge: {
			return ge(leftExpr, rightExpr);
		}
		case gt: {
			return gt(leftExpr, rightExpr);
		}
		case le: {
			return le(leftExpr, rightExpr);
		}
		case lt: {
			return lt(leftExpr, rightExpr);
		}
		default: {
			return new SQLBinaryExpression(op, leftExpr, rightExpr);
		}
		}
	}

	/**
	 * Addition of two expressions.
	 */
	public static SQLExpression add(SQLExpression leftExpr, SQLExpression rightExpr) {
		return new SQLBinaryExpression(SQLOp.add, leftExpr, rightExpr);
	}

	/**
	 * Substraction of two expressions.
	 */
	public static SQLExpression sub(SQLExpression leftExpr, SQLExpression rightExpr) {
		return new SQLBinaryExpression(SQLOp.sub, leftExpr, rightExpr);
	}

	/**
	 * Multiplication of two expressions.
	 */
	public static SQLExpression mul(SQLExpression leftExpr, SQLExpression rightExpr) {
		return new SQLBinaryExpression(SQLOp.mul, leftExpr, rightExpr);
	}

	/**
	 * Division of two expressions.
	 */
	public static SQLExpression div(SQLExpression leftExpr, SQLExpression rightExpr) {
		return new SQLBinaryExpression(SQLOp.div, leftExpr, rightExpr);
	}

	/**
	 * Creates a {@link SQLCast} expression.
	 * 
	 * @param expr
	 *        The expression to cast.
	 * @param dbType
	 *        The datatype kind to cast to.
	 * @param size
	 *        The size of the datatype.
	 * @param precision
	 *        the precision of the datatype.
	 * @param binary
	 *        The binary modifier of the datatype.
	 * @return The created {@link SQLCast} expression.
	 */
	public static SQLExpression cast(SQLExpression expr, DBType dbType, long size, int precision, boolean binary) {
		return new SQLCast(expr, dbType, size, precision, binary);
	}

	/**
	 * Computes the minimum of the given expression in the whole result.
	 */
	public static SQLExpression min(SQLExpression expr) {
		return function(SQLFun.min, expr);
	}

	/**
	 * Computes the maximum of the given expression in the whole result.
	 */
	public static SQLExpression max(SQLExpression expr) {
		return function(SQLFun.max, expr);
	}

	public static SQLExpression function(SQLFun fun, SQLExpression... arguments) {
		return function(fun, Arrays.asList(arguments));
	}
	
	public static SQLExpression function(SQLFun fun, List<SQLExpression> arguments) {
		return new SQLFunction(fun, arguments);
	}
	
	public static SQLExpression isNull(SQLExpression expr) {
		if (expr instanceof SQLColumnReference && ((SQLColumnReference) expr).isNotNull()) {
			return SQLBoolean.FALSE;
		}
		if (expr instanceof SQLLiteral) {
			Object literalValue = ((SQLLiteral) expr).getValue();
			return literalValue == null ? SQLBoolean.TRUE : SQLBoolean.FALSE;
		}
		if (expr instanceof SQLParameter && !((SQLParameter) expr).isPotentiallyNull()) {
			// parameter will not be filled with a null value (#9479).
			return SQLBoolean.FALSE;
		}
		return new SQLIsNull(expr);
	}
	
	/**
	 * Creates a {@link SQLSetLiteral} with the given values.
	 * 
	 * @param values
	 *        The literal values in the set
	 * @param types
	 *        The types of the values, e.g. when the values are tuples containing a {@link String},
	 *        {@link Long} and {@link TLID} the types would be {@link DBType#STRING},
	 *        {@link DBType#LONG}, {@link DBType#ID}. When the values are non Tuple values, a 1-slot
	 *        array with correct type must be given.
	 */
	public static SQLSetLiteral setLiteral(Collection<? extends Object> values, DBType... types) {
		return new SQLSetLiteral(values, types);
	}

	/**
	 * Creates a {@link SQLExpression} that evaluates to <code>true</code>, when the evaluated
	 * <code>expr</code> is contained in the given values.
	 * 
	 * @param expr
	 *        The {@link SQLExpression} to test.
	 * @param values
	 *        The created expression tests whether the test expression is contained in that values.
	 * @param types
	 *        The types of the values, e.g. when the values are tuples containing a {@link String},
	 *        {@link Long} and {@link TLID} the types would be {@link DBType#STRING},
	 *        {@link DBType#LONG}, {@link DBType#ID}. When the values are non Tuple values, a 1-slot
	 *        array with correct type must be given.
	 */
	public static SQLExpression inSet(SQLExpression expr, Collection<? extends Object> values, DBType... types) {
		if (values.isEmpty()) {
			return SQLBoolean.FALSE;
		}
		return inSet(expr, setLiteral(values, types));
	}

	public static SQLExpression inSet(SQLExpression expr, SQLExpression values) {
		return new SQLInSet(expr, values);
	}
	
	/**
	 * Creates an in-set expression with the test set being a sub-select.
	 */
	public static SQLExpression inSetSelect(SQLExpression expr, SQLSelect select) {
		return new SQLInSetSelect(expr, select);
	}

	public static SQLExpression ge(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr instanceof SQLLiteral) {
			if (rightExpr instanceof SQLLiteral) {
				Comparable leftValue = (Comparable) ((SQLLiteral) leftExpr).getValue();
				Comparable rightValue = (Comparable) ((SQLLiteral) rightExpr).getValue();
				
				return SQLBoolean.valueOf(leftValue.compareTo(rightValue) >= 0);
			}
			else if (isLong(leftExpr)) {
				long value = getLong(leftExpr);
				if (value == Long.MAX_VALUE) {
					return SQLBoolean.TRUE;
				}
			}
		}
		else if (isLong(rightExpr)) {
			long value = getLong(rightExpr);
			if (value == Long.MIN_VALUE) {
				return SQLBoolean.TRUE;
			}
		}
		return new SQLBinaryExpression(SQLOp.ge, leftExpr, rightExpr);
	}
	
	public static SQLExpression le(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr instanceof SQLLiteral) {
			if (rightExpr instanceof SQLLiteral) {
				Comparable leftValue = (Comparable) ((SQLLiteral) leftExpr).getValue();
				Comparable rightValue = (Comparable) ((SQLLiteral) rightExpr).getValue();
				
				return SQLBoolean.valueOf(leftValue.compareTo(rightValue) <= 0);
			}
			else if (isLong(leftExpr)) {
				long value = getLong(leftExpr);
				if (value == Long.MIN_VALUE) {
					return SQLBoolean.TRUE;
				}
			}
		}
		else if (isLong(rightExpr)) {
			long value = getLong(rightExpr);
			if (value == Long.MAX_VALUE) {
				return SQLBoolean.TRUE;
			}
		}
		return new SQLBinaryExpression(SQLOp.le, leftExpr, rightExpr);
	}
	
	public static SQLExpression gt(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr instanceof SQLLiteral) {
			if (rightExpr instanceof SQLLiteral) {
				Comparable leftValue = (Comparable) ((SQLLiteral) leftExpr).getValue();
				Comparable rightValue = (Comparable) ((SQLLiteral) rightExpr).getValue();
				
				return SQLBoolean.valueOf(leftValue.compareTo(rightValue) > 0);
			}
			else if (isLong(leftExpr)) {
				long value = getLong(leftExpr);
				if (value == Long.MIN_VALUE) {
					return SQLBoolean.FALSE;
				}
			}
		}
		else if (isLong(rightExpr)) {
			long value = getLong(rightExpr);
			if (value == Long.MAX_VALUE) {
				return SQLBoolean.FALSE;
			}
		}
		return new SQLBinaryExpression(SQLOp.gt, leftExpr, rightExpr);
	}
	
	public static SQLExpression lt(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr instanceof SQLLiteral) {
			if (rightExpr instanceof SQLLiteral) {
				Comparable leftValue = (Comparable) ((SQLLiteral) leftExpr).getValue();
				Comparable rightValue = (Comparable) ((SQLLiteral) rightExpr).getValue();
				
				return SQLBoolean.valueOf(leftValue.compareTo(rightValue) < 0);
			}
			else if (isLong(leftExpr)) {
				long value = getLong(leftExpr);
				if (value == Long.MAX_VALUE) {
					return SQLBoolean.FALSE;
				}
			}
		}
		else if (isLong(rightExpr)) {
			long value = getLong(rightExpr);
			if (value == Long.MIN_VALUE) {
				return SQLBoolean.FALSE;
			}
		}
		return new SQLBinaryExpression(SQLOp.lt, leftExpr, rightExpr);
	}
	
	private static long getLong(SQLExpression expr) {
		return (Long) ((SQLLiteral) expr).getValue();
	}

	private static boolean isLong(SQLExpression expr) {
		if (expr instanceof SQLLiteral) {
			return ((SQLLiteral) expr).getJdbcType() == DBType.LONG;
		}
		return false;
	}

	/**
	 * Creates an {@link SQLExpression} that resolves to true iff the left expression evaluates to
	 * the same value as the right expression.
	 * 
	 * <p>
	 * In contrast to {@link #eqSQL(SQLExpression, SQLExpression)} the result is also true when both
	 * expressions represents columns and contain both <code>null</code>.
	 * </p>
	 * 
	 * @see #eqSQL(SQLExpression, SQLExpression) for SQL null Logic
	 */
	public static SQLExpression eq(SQLExpression leftExpr, SQLExpression rightExpr) {
		return eq(leftExpr, rightExpr, false);
	}

	private static SQLExpression eq(SQLExpression leftExpr, SQLExpression rightExpr, boolean sqlNullLogic) {
		if (leftExpr instanceof SQLLiteral) {
			if (rightExpr instanceof SQLLiteral) {
				Object leftValue = ((SQLLiteral) leftExpr).getValue();
				Object rightValue = ((SQLLiteral) rightExpr).getValue();
				
				return SQLBoolean.valueOf(CollectionUtil.equals(leftValue, rightValue));
			} else if (rightExpr instanceof SQLColumnReference) {
				Object leftValue = ((SQLLiteral) leftExpr).getValue();
				if (leftValue == null) {
					return isNull(rightExpr);
				}
			}
		} else {
			if (leftExpr instanceof SQLColumnReference) {
				if (rightExpr instanceof SQLLiteral) {
					Object rightValue = ((SQLLiteral) rightExpr).getValue();
					if (rightValue == null) {
						return isNull(leftExpr);
					}
				}
			}
		}

		if (leftExpr instanceof SQLParameter && rightExpr instanceof SQLParameter) {
			if (((SQLParameter) leftExpr).getName().equals(((SQLParameter) rightExpr).getName())) {
				return SQLBoolean.TRUE;
			}
		}

		if (!sqlNullLogic) {
			if (leftExpr instanceof SQLColumnReference || rightExpr instanceof SQLColumnReference) {
				return logicEquality(leftExpr, rightExpr);
			}
		}

		return new SQLBinaryExpression(SQLOp.eq, leftExpr, rightExpr);
	}

	private static SQLExpression logicEquality(SQLExpression leftExpr, SQLExpression rightExpr) {
		// a = b iff (a==b and !a isNull and !b isNull) or (a isNull and b isNull)
		/* It seems that (!a isNull and !b isNull) is not necessary as if a is null and b is not
		 * null then !a==b. This is not correct. If a is null and b is not null then the SQL result
		 * of a==b is not false, but null. Therefore if a=b is negated, then not a==b (for a is null
		 * and b is not null) is also null which evaluates to false. */
		return ensureLogicalEquality(leftExpr, rightExpr, new SQLBinaryExpression(SQLOp.eq, leftExpr, rightExpr));
	}

	/**
	 * Creates an expression that is <code>true</code> iff either <code>left</code> and
	 * <code>right</code> are both not <code>null</code> and <code>equality</code> is
	 * <code>true</code> or <code>left</code> and <code>right</code> are both <code>null</code>.
	 */
	@FrameworkInternal
	public static SQLExpression ensureLogicalEquality(SQLExpression left, SQLExpression right,
			SQLExpression equality) {
		SQLExpression bothNull = and(isNull(copy(left)), isNull(copy(right)));
		SQLExpression bothNotNull = and(not(isNull(copy(left))), not(isNull(copy(right))));
		return or(and(equality, bothNotNull), bothNull);
	}

	/**
	 * Creates an {@link SQLExpression} that resolves to true iff the left expression evaluates to
	 * the same value as the right expression.
	 * 
	 * <p>
	 * In contrast to {@link #eq(SQLExpression, SQLExpression)} the comparison of two columns is
	 * done as in SQL, i.e. <code>null != null</code>.
	 * </p>
	 * 
	 * @see #eq(SQLExpression, SQLExpression) for Java null-Logic
	 */
	public static SQLExpression eqSQL(SQLExpression leftExpr, SQLExpression rightExpr) {
		return eq(leftExpr, rightExpr, true);
	}

	public static SQLExpression and(SQLExpression... exprs) {
		if (exprs == null || exprs.length == 0) {
			return SQLBoolean.TRUE;
		}
		
		int n = exprs.length - 1;
		SQLExpression result = exprs[n--];
		while (n >= 0) {
			result = and(exprs[n--], result);
		}
		return result;
	}
	
	public static SQLExpression and(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr == SQLBoolean.TRUE) {
			return rightExpr;
		}
		else if (rightExpr == SQLBoolean.TRUE) {
			return leftExpr;
		}
		else if (leftExpr == SQLBoolean.FALSE) {
			return SQLBoolean.FALSE;
		}
		else if (rightExpr == SQLBoolean.FALSE) {
			return SQLBoolean.FALSE;
		}
		else {
			return new SQLBinaryExpression(SQLOp.and, leftExpr, rightExpr);
		}
	}

	public static SQLExpression or(SQLExpression... exprs) {
		if (exprs == null || exprs.length == 0) {
			return SQLBoolean.FALSE;
		}
		
		int n = exprs.length - 1;
		SQLExpression result = exprs[n--];
		while (n >= 0) {
			result = or(exprs[n--], result);
		}
		return result;
	}
	
	public static SQLExpression or(SQLExpression leftExpr, SQLExpression rightExpr) {
		if (leftExpr == SQLBoolean.TRUE) {
			return SQLBoolean.TRUE;
		}
		else if (rightExpr == SQLBoolean.TRUE) {
			return SQLBoolean.TRUE;
		}
		else if (leftExpr == SQLBoolean.FALSE) {
			return rightExpr;
		}
		else if (rightExpr == SQLBoolean.FALSE) {
			return leftExpr;
		}
		else {
			return new SQLBinaryExpression(SQLOp.or, leftExpr, rightExpr);
		}
	}
	
	public static SQLExpression not(SQLExpression expr) {
		if (expr == SQLBoolean.TRUE) {
			return SQLBoolean.FALSE;
		}
		else if (expr == SQLBoolean.FALSE) {
			return SQLBoolean.TRUE;
		}
		else if (expr instanceof SQLNot) {
			return ((SQLNot) expr).getExpr();
		}
		else if (expr instanceof SQLBinaryExpression) {
			SQLBinaryExpression sqlBinaryExpression = (SQLBinaryExpression) expr;
			switch (sqlBinaryExpression.getOp()) {
				case and:
					return or(not(sqlBinaryExpression.getLeftExpr()), not(sqlBinaryExpression.getRightExpr()));
				case or:
					return and(not(sqlBinaryExpression.getLeftExpr()), not(sqlBinaryExpression.getRightExpr()));
				default:
					return new SQLNot(expr);
			}
		} else {
			return new SQLNot(expr);
		}
	}
	
	public static SQLOrder order(boolean descending, CollationHint collationHint, SQLExpression expr) {
		return new SQLOrder(descending, collationHint, expr);
	}
	
	public static SQLOrder order(boolean descending, SQLExpression expr) {
		return order(descending, CollationHint.NONE, expr);
	}

	/**
	 * An {@link SQLLiteral} representing <code>null</code> if the given {@link DBType}.
	 */
	public static SQLLiteral literalNull(DBType sqlType) {
		return literal(sqlType, null);
	}
	
	/**
	 * An {@link SQLLiteral} representing the given string.
	 */
	public static SQLLiteral literalString(String value) {
		return literal(DBType.STRING, value);
	}

	/**
	 * An {@link SQLLiteral} representing the given identifier.
	 */
	public static SQLLiteral literalID(TLID value) {
		return literal(DBType.ID, value);
	}

	/**
	 * An {@link SQLLiteral} representing the given long.
	 */
	public static SQLLiteral literalLong(long value) {
		return literal(DBType.LONG, (Object) value);
	}
	
	/**
	 * An {@link SQLLiteral} representing the given int.
	 */
	public static SQLLiteral literalInteger(int value) {
		return literal(DBType.INT, (Object) value);
	}

	/**
	 * An {@link SQLLiteral} representing the given double.
	 */
	public static SQLLiteral literalDouble(double value) {
		return literal(DBType.DOUBLE, (Object) value);
	}
	
	/**
	 * An {@link SQLLiteral} representing SQL date aspect of the given {@link Date}.
	 * 
	 * @see DBType#DATE
	 */
	public static SQLLiteral literalDate(Date value) {
		return literal(DBType.DATE, value);
	}

	/**
	 * An {@link SQLLiteral} representing SQL time aspect of the given {@link Date}.
	 * 
	 * @see DBType#TIME
	 */
	public static SQLLiteral literalTime(Date value) {
		return literal(DBType.TIME, value);
	}

	/**
	 * An {@link SQLLiteral} representing SQL timestamp aspect of the given {@link Date}.
	 * 
	 * @see DBType#DATETIME
	 */
	public static SQLLiteral literalTimestamp(Date value) {
		return literal(DBType.DATETIME, value);
	}
	
	/**
	 * A {@link SQLLiteral} column value representing {@link Boolean#TRUE}.
	 * 
	 * <p>
	 * Note: This value <b>must not</b> be used where a conditional expression is expected (like
	 * WHERE and ON clauses).
	 * </p>
	 * 
	 * @see #literalTrueLogical()
	 */
	public static SQLLiteral literalTrueValue() {
		return literalBooleanValue(true);
	}

	/**
	 * A {@link SQLLiteral} column value representing {@link Boolean#FALSE}.
	 * 
	 * <p>
	 * Note: This value <b>must not</b> be used where a conditional expression is expected (like
	 * WHERE and ON clauses).
	 * </p>
	 * 
	 * @see #literalFalseLogical()
	 */
	public static SQLLiteral literalFalseValue() {
		return literalBooleanValue(false);
	}

	/**
	 * A {@link SQLLiteral} column value representing the given {@link Boolean}.
	 * 
	 * <p>
	 * Note: This value <b>must not</b> be used where a conditional expression is expected (like
	 * WHERE and ON clauses).
	 * </p>
	 * 
	 * @see #literalBooleanLogical(Boolean)
	 */
	public static SQLLiteral literalBooleanValue(Boolean value) {
		return literal(DBType.BOOLEAN, value);
	}

	/**
	 * Logical {@link SQLBoolean} value representing {@link Boolean#TRUE}.
	 * 
	 * <p>
	 * Note: This value must only be used where a conditional expression is expected (like WHERE and
	 * ON clauses).
	 * </p>
	 * 
	 * @see #literalTrueValue()
	 */
	public static SQLBoolean literalTrueLogical() {
		return literalBooleanLogical(true);
	}

	/**
	 * Logical {@link SQLBoolean} value representing {@link Boolean#FALSE}.
	 * 
	 * <p>
	 * Note: This value must only be used where a conditional expression is expected (like WHERE and
	 * ON clauses).
	 * </p>
	 * 
	 * @see #literalFalseValue()
	 */
	public static SQLBoolean literalFalseLogical() {
		return literalBooleanLogical(false);
	}

	/**
	 * Logical {@link SQLBoolean} value representing the given Java {@link Boolean}.
	 * 
	 * <p>
	 * Note: This value must only be used where a conditional expression is expected (like WHERE and
	 * ON clauses).
	 * </p>
	 * 
	 * @see #literalBooleanValue(Boolean)
	 */
	public static SQLBoolean literalBooleanLogical(Boolean value) {
		return value.booleanValue() ? SQLBoolean.TRUE : SQLBoolean.FALSE;
	}

	/**
	 * An {@link SQLLiteral} representing the given character.
	 */
	public static SQLLiteral literalCharacter(Character value) {
		return literal(DBType.CHAR, value);
	}

	/**
	 * An {@link SQLLiteral} representing the given byte.
	 */
	public static SQLLiteral literalByte(Byte value) {
		return literal(DBType.BYTE, value);
	}

	/**
	 * An {@link SQLLiteral} representing the given short.
	 */
	public static SQLLiteral literalShort(Short value) {
		return literal(DBType.SHORT, value);
	}

	/**
	 * Returns an {@link SQLLiteral} representing the given object.
	 * 
	 * @param jdbcType
	 *        the SQL type of the given value.
	 * @param value
	 *        the represented object.
	 */
	public static SQLLiteral literal(DBType jdbcType, Object value) {
		if (jdbcType == DBType.BOOLEAN && value != null) {
			return ((Boolean) value).booleanValue() ? SQLLiteral.TRUE : SQLLiteral.FALSE;
		}
		return new SQLLiteral(jdbcType, value);
	}

	/**
	 * Creates a parameter with the identity conversion.
	 * 
	 * @see SQLFactory#parameter(DBType, Conversion, String)
	 */
	public static SQLParameter parameter(DBType type, String name) {
		return parameter(type, Conversion.IDENTITY, name);
	}

	/**
	 * Creates a new SQL parameter that must not be filled with <code>null</code> values.
	 * 
	 * @param jdbcType
	 *        The database type of the argument of the returned parameter.
	 * @param conversion
	 *        A {@link Conversion conversion} to convert the later argument to the actual value for
	 *        the database.
	 * @param name
	 *        The name of the parameter to identify it.
	 */
	public static SQLParameter parameter(DBType jdbcType, Conversion conversion, String name) {
		return new SQLParameter(jdbcType, conversion, name);
	}

	/**
	 * Creates an {@link SQLParameter} that can be filled with values or <code>null</code>.
	 * 
	 * @see SQLFactory#parameter(DBType, String)
	 */
	public static SQLParameter nullParameter(DBType type, String name) {
		return nullParameter(type, Conversion.IDENTITY, name);
	}

	/**
	 * Creates an {@link SQLParameter} that can be filled with values or <code>null</code>.
	 * 
	 * @see SQLFactory#parameter(DBType, Conversion, String)
	 */
	public static SQLParameter nullParameter(DBType jdbcType, Conversion conversion, String name) {
		return new PotentiallyNullSQLParameter(jdbcType, conversion, name);
	}

	/**
	 * Creates a {@link SQLSetParameter} which {@link Conversion#IDENTITY}.
	 * 
	 * @see #setParameter(Conversion, String, DBType...)
	 */
	public static SQLSetParameter setParameter(String name, DBType... types) {
		return setParameter(Conversion.IDENTITY, name, types);
	}

	/**
	 * Creates a {@link SQLSetParameter} which is later filled with literal values.
	 * 
	 * @param conversion
	 *        See {@link SQLSetParameter#getConversion()}.
	 * @param name
	 *        The name for the parameter.
	 * @param types
	 *        The types of the entries later filled into the {@link SQLParameter}, e.g. when the
	 *        entries are a tuple containing a {@link String}, {@link Long} and {@link TLID} the
	 *        types would be {@link DBType#STRING}, {@link DBType#LONG}, {@link DBType#ID}. When the
	 *        set is filled with non Tuple values, a 1-slot array with correct type must be given.
	 */
	public static SQLSetParameter setParameter(Conversion conversion, String name, DBType... types) {
		return new SQLSetParameter(types, conversion, name);
	}

	/**
	 * Access to the built-in dummy table.
	 */
	public static SQLTable dual() {
		return table(SQLTable.NO_TABLE);
	}

	/**
	 * Reference to the table with the given name, it is the only table referenced in the query.
	 * 
	 * @see #table(String, String)
	 */
	public static SQLTable table(String tableName) {
		return table(tableName, NO_TABLE_ALIAS);
	}

	public static SQLTable table(String tableName, String tableAlias) {
		return new SQLTable(tableName, tableAlias);
	}
	
	/**
	 * Select column definition referencing the column with the given name of the only table being
	 * accessed.
	 * 
	 * @see #column(String)
	 */
	public static SQLColumnDefinition columnDef(String columnName) {
		return columnDef(column(columnName));
	}

	/**
	 * Select column definition referencing the column with the given name from the table with the
	 * given alias.
	 * 
	 * <p>
	 * The defined column is a accessible through the given alias name.
	 * </p>
	 * 
	 * @see #column(String, String)
	 */
	public static SQLColumnDefinition columnDef(String columnName, String tableAlias, String aliasName) {
		return columnDef(column(tableAlias, columnName), aliasName);
	}

	public static SQLColumnDefinition columnDef(SQLExpression expr) {
		return columnDef(expr, null);
	}

	public static SQLColumnDefinition columnDef(SQLExpression expr, String aliasName) {
		return new SQLColumnDefinition(expr, aliasName);
	}

	/**
	 * Limits the database result to the first row in the result.
	 * 
	 * @see #limit(SQLExpression, SQLExpression)
	 * @see SQLLimit#FIRST_ROW
	 */
	public static SQLLimit limitFirstRow() {
		SQLLiteral startRow = literalInteger(SQLLimit.FIRST_ROW);
		SQLLiteral stopRow = literalInteger(SQLLimit.FIRST_ROW + 1);
		return limit(startRow, stopRow);
	}

	public static SQLLimit limit(SQLExpression startRow, SQLExpression stopRow) {
		return new SQLLimit(startRow, stopRow);
	}

	/**
	 * Creates an SQL case expression
	 * 
	 * @param conditionExpr
	 *        Boolean expression that decides which expression will be evaluated
	 * @param thenExpr
	 *        Expression to evaluate, when condition evaluates to true.
	 * @param elseExpr
	 *        Expression to evaluate, when condition evaluates to false.
	 */
	public static SQLExpression sqlCase(SQLExpression conditionExpr, SQLExpression thenExpr, SQLExpression elseExpr) {
		if (conditionExpr == SQLBoolean.TRUE) {
			return thenExpr;
		}
		if (conditionExpr == SQLBoolean.FALSE) {
			return elseExpr;
		}
		return new SQLCase(conditionExpr, thenExpr, elseExpr);
	}

	/**
	 * List of {@link SQLColumnDefinition}s.
	 */
	public static List<SQLColumnDefinition> columns(SQLColumnDefinition... columns) {
		return Arrays.asList(columns);
	}

	/**
	 * List of column names, see e.g. {@link #insert(SQLTable, List, List)}.
	 */
	public static List<String> columnNames(String... columns) {
		return Arrays.asList(columns);
	}

	/**
	 * List of {@link SQLExpression}s.
	 */
	public static List<SQLExpression> expressions(SQLExpression... expressions) {
		return Arrays.asList(expressions);
	}

	/**
	 * List of query {@link Parameter}s.
	 */
	public static List<Parameter> parameters(Parameter... parameters) {
		return Arrays.asList(parameters);
	}

	/**
	 * List of {@link SQLOrder}s.
	 */
	public static List<SQLOrder> orders(SQLOrder... parameters) {
		return Arrays.asList(parameters);
	}

	/**
	 * Empty {@link SQLOrder} list.
	 */
	public static List<SQLOrder> noOrder() {
		return Collections.<SQLOrder> emptyList();
	}

	/**
	 * List of {@link SQLSelect}s.
	 */
	public static List<SQLSelect> selects(SQLSelect... selects) {
		return Arrays.asList(selects);
	}

	/**
	 * Creates a deep clone of the given {@link SQLPart}.
	 */
	public static <T extends SQLPart> T copy(T part) {
		return SQLCopy.copy(part);
	}

}
