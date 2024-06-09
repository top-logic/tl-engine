/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.io.IOError;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MySQLHelper;

/**
 * {@link SQLVisitor} to create a {@link CompiledStatement} from  a given {@link SQLPart}.
 * 
 * @see SQLQuery#toSql(DBHelper, SQLPart, Map)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractStatementBuilder<E extends SimpleSQLBuffer> implements SQLVisitor<Void, E> {

	private static final String FROM = " FROM ";

	/**
	 * {@link SQLVisitor} that checks whether for the visited {@link SQLPart} a SQL source can be
	 * build from which a {@link PreparedStatement} can be build (so arguments can be filled later)
	 * or whether it is impossible so the arguments must be integrated into the source string.
	 * 
	 * <p>
	 * Result of the visit: <code>true</code> iff it is <i>not</i> possible to create a SQL for a
	 * {@link PreparedStatement}.
	 * </p>
	 */
	private static SQLVisitor<Boolean, DBHelper> BUILDER_DETERMINATION = new SQLVisitor<>() {
		
		private final Boolean noPrepStatement = Boolean.TRUE;

		private final Boolean mayUsePrepStatement = Boolean.FALSE;

		@Override
		public <S extends SQLStatement> Boolean visitSQLQuery(SQLQuery<S> sql, DBHelper arg) {
			return sql.getStatement().visit(this, arg);
		}

		@Override
		public Boolean visitSQLSelect(SQLSelect sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getColumns(), arg, result);
			result = descend(sql.getLimit(), arg, result);
			result = descend(sql.getTableReference(), arg, result);
			result = descend(sql.getWhere(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLDelete(SQLDelete sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getTable(), arg, result);
			result = descend(sql.getCondition(), arg, result);
			return result;
		}

		private Boolean descend(SQLPart subSQL, DBHelper arg, Boolean result) {
			if (subSQL == null) {
				return result;
			}

			// Stop descending early.
			if (result.booleanValue() == noPrepStatement.booleanValue()) {
				return noPrepStatement;
			}
			return subSQL.visit(this, arg);
		}
		
		private Boolean descend(Collection<? extends SQLPart> subSQLs, DBHelper arg, Boolean result) {
			for (SQLPart subSQL : subSQLs) {
				result = descend(subSQL, arg, result);
			}
			return result;
		}

		@Override
		public Boolean visitSQLJoin(SQLJoin sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getLeftTable(), arg, result);
			result = descend(sql.getRightTable(), arg, result);
			result = descend(sql.getCondition(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLSubQuery(SQLSubQuery sql, DBHelper arg) {
			return sql.getSelect().visit(this, arg);
		}

		@Override
		public Boolean visitSQLLimit(SQLLimit sql, DBHelper arg) {
			if (sql.getStartRow() instanceof SQLParameter) {
				return noPrepStatement;
			}
			if (sql.getStopRow() instanceof SQLParameter) {
				return noPrepStatement;
			}
			return mayUsePrepStatement;
		}
		
		@Override
		public Boolean visitSQLInSet(SQLInSet sql, DBHelper arg) {
			if (sql.getValues() instanceof SQLSetLiteral) {
				// Set Literals are injected directly
				return mayUsePrepStatement;
			}
			return noPrepStatement;
		}
		
		@Override
		public Boolean visitSQLTuple(SQLTuple sql, DBHelper arg) {
			return descend(sql.getExpressions(), arg, mayUsePrepStatement);
		}

		@Override
		public Boolean visitSQLBinaryExpression(SQLBinaryExpression sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getLeftExpr(), arg, result);
			result = descend(sql.getRightExpr(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLCast(SQLCast sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getExpr(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLNot(SQLNot sql, DBHelper arg) {
			return descend(sql.getExpr(), arg, mayUsePrepStatement);
		}

		@Override
		public Boolean visitSQLCase(SQLCase sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getCondition(), arg, result);
			result = descend(sql.getThen(), arg, result);
			result = descend(sql.getElse(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLSetParameter(SQLSetParameter sql, DBHelper arg) {
			return noPrepStatement;
		}
		
		@Override
		public Boolean visitSQLInsert(SQLInsert sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getTable(), arg, result);
			result = descend(sql.getValues(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLInsertSelect(SQLInsertSelect sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getTable(), arg, result);
			result = descend(sql.getSelect(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLUpdate(SQLUpdate sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getTable(), arg, result);
			result = descend(sql.getValues(), arg, result);
			result = descend(sql.getWhere(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLUnion(SQLUnion sql, DBHelper arg) {
			Boolean result = mayUsePrepStatement;
			result = descend(sql.getSelects(), arg, result);
			result = descend(sql.getOrderBy(), arg, result);
			return result;
		}

		@Override
		public Boolean visitSQLColumnReference(SQLColumnReference sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLColumnDefinition(SQLColumnDefinition sql, DBHelper arg) {
			SQLExpression expr = sql.getExpr();
			if (!arg.allowParameterColumn()) {
				if (expr.visit(IsParameter.INSTANCE, none)) {
					return noPrepStatement;
				}
			}
			return descend(expr, arg, mayUsePrepStatement);
		}

		@Override
		public Boolean visitSQLOrder(SQLOrder sql, DBHelper arg) {
			return descend(sql.getExpr(), arg, mayUsePrepStatement);
		}

		@Override
		public Boolean visitSQLParameter(SQLParameter sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLLiteral(SQLLiteral sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLBoolean(SQLBoolean sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLFunction(SQLFunction sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLIsNull(SQLIsNull sql, DBHelper arg) {
			return descend(sql.getExpr(), arg, mayUsePrepStatement);
		}

		@Override
		public Boolean visitSQLTable(SQLTable sql, DBHelper arg) {
			return mayUsePrepStatement;
		}

		@Override
		public Boolean visitSQLSetLiteral(SQLSetLiteral sql, DBHelper arg) {
			return mayUsePrepStatement;
		}
		
		@Override
		public Boolean visitSQLAddColumn(SQLAddColumn sql, DBHelper arg) {
			return noPrepStatement;
		}

		@Override
		public Boolean visitSQLModifyColumn(SQLModifyColumn sql, DBHelper arg) {
			return noPrepStatement;
		}

		@Override
		public Boolean visitSQLDropColumn(SQLDropColumn sql, DBHelper arg) {
			return noPrepStatement;
		}

		@Override
		public Boolean visitSQLAddIndex(SQLAddIndex sql, DBHelper arg) {
			return noPrepStatement;
		}

		@Override
		public Boolean visitSQLDropIndex(SQLDropIndex sql, DBHelper arg) {
			return noPrepStatement;
		}

	};

	/** Constant to use for {@link Void} */
	protected static final Void none = null;

	static SQLPart postProcessSQL(DBHelper sqlDialect, SQLPart expr, SetParameterResolver resolver) {
		/* should be routed via DBHelper which is currently not possible, because SQLPart lies in
		 * com.top_logic.basic.db and DBHelper lies in com.top_logic.basic. */
		if (!(sqlDialect instanceof MSSQLHelper)) {
			return expr;
		}
		return MSSQLTupleRewriter.removeTuple(expr, resolver);
	}

	/**
	 * @param sqlDialect
	 *        The SQL dialect used to create the statement.
	 * @return whether no SQL String can be derived from the given {@link SQLPart} that can be used
	 *         as source of an {@link PreparedStatement} (i.e. the actual arguments can be filled
	 *         later)
	 */
	static boolean checkPreparedStatement(SQLPart expr, DBHelper sqlDialect) {
		return expr.visit(BUILDER_DETERMINATION, sqlDialect);
	}

	@Override
	public <S extends SQLStatement> Void visitSQLQuery(SQLQuery<S> sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		// No representative in the SQL source code.
		Void result = sql.getStatement().visit(this, buffer);
		buffer.setContext(oldContext);
		return result;
	}

	/**
	 * Calls {@link SimpleSQLBuffer#setContext(SQLPart)} on the given {@link SimpleSQLBuffer} with
	 * the new context.
	 * 
	 * @return the former context.
	 */
	protected SQLPart setContext(SQLPart newContext, E buffer) {
		SQLPart oldContext = buffer.context();
		buffer.setContext(newContext);
		return oldContext;
	}
	
	/**
	 * Resets the {@link SimpleSQLBuffer#context()} to the given {@link SQLPart}
	 * 
	 * @return {@link #none}
	 */
	protected Void resetContext(SQLPart oldContext, E buffer) {
		buffer.setContext(oldContext);
		return none;
	}

	@Override
	public Void visitSQLOrder(SQLOrder sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		
		CollationHint collationHint = sql.getCollationHint();
		if (collationHint == CollationHint.NONE) {
			sql.getExpr().visit(this, buffer);
		} else {
			// Create inner expression.
			String sqlExpr = toString(buffer, sql.getExpr());
			
			// Wrap inner expression with collation markup.
			buffer.sqlDialect.appendCollatedExpression(buffer, sqlExpr, collationHint);
		}
		
		buffer.append(' ');
		if (sql.isDescending()) {
			buffer.append("DESC");
		} else {
			buffer.append("ASC");
		}
		return resetContext(oldContext, buffer);
	}

	private String toString(E buffer, SQLExpression expr) {
		StringBuilder sqlBuffer = buffer.buffer;
		int start = sqlBuffer.length();
		expr.visit(this, buffer);
		String sqlExpr = sqlBuffer.substring(start, sqlBuffer.length());
		sqlBuffer.setLength(start);
		return sqlExpr;
	}

	@Override
	public Void visitSQLNot(SQLNot sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append("NOT ");
		buffer.append('(');
		sql.getExpr().visit(this, buffer);
		buffer.append(')');
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLIsNull(SQLIsNull sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append('(');
		sql.getExpr().visit(this, buffer);
		buffer.append(')');
		buffer.append(" IS NULL");
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLSetLiteral(SQLSetLiteral sql, E buffer) {
		Iterable<?> values = sql.getValues();
		visitIterable(values, buffer);
		return none;
	}

	void visitIterable(Iterable<?> values, E buffer) {
		boolean first = true;
		for (Object value : values) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			visitObject(value, buffer);
		}
	}

	void visitObject(Object value, E buffer) {
		if (value instanceof SQLPart) {
			((SQLPart) value).visit(this, buffer);
		} else {
			buffer.sqlDialect.appendValue(buffer, value);
		}
	}

	@Override
	public Void visitSQLBoolean(SQLBoolean sql, E buffer) {
		buffer.append('0');
		buffer.append('=');
		buffer.append(sql.getValue() ? '0' : '1');
		return none;
	}

	@Override
	public Void visitSQLUnion(SQLUnion sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		List<SQLSelect> selects = sql.getSelects();
		for (int n = 0, cnt = selects.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(' ');
				buffer.append("UNION");
				
				if (sql.isDistinct()) {
					// this is default
				} else {
					buffer.append(' ');
					buffer.append("ALL");
				}
				buffer.append(' ');
			}
			
			SQLSelect select = selects.get(n);
			buffer.append('(');
			select.visit(this, buffer);
			buffer.append(')');
		}
		
		appendOrders(sql.getOrderBy(), buffer);

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLInsert(SQLInsert sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		appendInsert(sql, buffer);
		appendValues(sql, buffer);
		return resetContext(oldContext, buffer);
	}

	private void appendValues(SQLInsert sql, E buffer) {
		buffer.append("VALUES");
		buffer.append(' ');
		buffer.append('(');
	
		appendSeparated(sql.getValues(), buffer, ", ");
		
		buffer.append(')');
	}

	@Override
	public Void visitSQLInsertSelect(SQLInsertSelect sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		appendInsert(sql, buffer);
		sql.getSelect().visit(this, buffer);
		return resetContext(oldContext, buffer);
	}

	private void appendInsert(AbstractSQLInsert sql, E buffer) {
		buffer.append("INSERT");
		buffer.append(' ');
		buffer.append("INTO");
		buffer.append(' ');
		// Note: Do not descend, because in an INSERT, no table alias must be created.
		buffer.append(buffer.sqlDialect.tableRef(sql.getTable().getTableName()));
		buffer.append(' ');
		buffer.append('(');
		List<String> columnNames = sql.getColumnNames();
		for (int n = 0, cnt = columnNames.size(); n < cnt; n++) {
			String columnName = columnNames.get(n);
	
			if (n > 0) {
				buffer.append(',');
			}
			buffer.append(buffer.sqlDialect.columnRef(columnName));
		}
		buffer.append(')');
		buffer.append(' ');
	}

	@Override
	public Void visitSQLDelete(SQLDelete sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append("DELETE");
		buffer.append(FROM);
		// Note: Do not descend, because in an DELETE, no table alias must be created.
		buffer.append(buffer.sqlDialect.tableRef(sql.getTable().getTableName()));
		SQLExpression where = sql.getCondition();
		if (where != SQLBoolean.TRUE) {
			buffer.append(' ');
			buffer.append("WHERE");
			buffer.append(' ');
			where.visit(this, buffer);
		}

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLAddIndex(SQLAddIndex sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append("CREATE ");
		if (sql.isUnique()) {
			buffer.append("UNIQUE ");
		}
		buffer.append("INDEX ");
		buffer.append(buffer.sqlDialect.columnRef(sql.getIndexName()));
		buffer.append(" ON ");
		buffer.append(buffer.sqlDialect.tableRef(sql.getTable().getTableName()));
		buffer.append('(');
		for (int n = 0, cnt = sql.getIndexColumns().size(); n < cnt; n++) {
			String column = sql.getIndexColumns().get(n);
			if (n > 0) {
				buffer.append(',');
			}
			buffer.append(buffer.sqlDialect.columnRef(column));
		}
		buffer.append(')');
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLDropIndex(SQLDropIndex sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		try {
			buffer.sqlDialect.appendDropIndex(buffer, sql.getIndexName(), sql.getTable().getTableName());
		} catch (IOException ex) {
			// SimpleSQLBuffer does not throw IOException.
			throw new IOError(ex);
		}
		return resetContext(oldContext, buffer);
	}

	/**
	 * Appends "ALTER TABLE ..." statement prefix to the builder.
	 */
	protected void appendAlterTable(E buffer, SQLAlterTable sql) {
		buffer.append("ALTER TABLE ");
		buffer.append(buffer.sqlDialect.tableRef(sql.getTable().getTableName()));
		buffer.append(StringServices.BLANK_CHAR);
	}

	@Override
	public Void visitSQLAddColumn(SQLAddColumn sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);

		appendAlterTable(buffer, sql);

		buffer.append("ADD ");
		buffer.append(buffer.sqlDialect.columnRef(sql.getColumnName()));
		buffer.append(StringServices.BLANK_CHAR);

		DBType type = sql.getType();
		long size = sql.getSize();
		int prec = sql.getPrecision();
		boolean mandatory = sql.isMandatory();
		boolean binary = sql.isBinary();
		Object defaultValue = sql.getDefaultValue();
		buffer.sqlDialect.appendDBType(buffer, type, sql.getColumnName(), size, prec, mandatory, binary, defaultValue);

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLModifyColumn(SQLModifyColumn sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		DBType type = sql.getType();
		long size = sql.getSize();
		int prec = sql.getPrecision();
		boolean mandatory = sql.isMandatory();
		boolean binary = sql.isBinary();
		Object defaultValue = sql.getDefaultValue();
		String tableName = sql.getTable().getTableName();
		try {
			switch (sql.getModificationAspect()) {
				case NAME:
					buffer.sqlDialect.appendChangeColumnName(buffer, tableName, type, sql.getColumnName(), sql.getNewName(),
						size, prec, mandatory, binary, defaultValue);
					break;
				case TYPE:
					buffer.sqlDialect.appendChangeColumnType(buffer, tableName, type, sql.getColumnName(), sql.getNewName(),
						size, prec, mandatory, binary, defaultValue);
					break;
				case MANDATORY:
					buffer.sqlDialect.appendChangeMandatory(buffer, tableName, type, sql.getColumnName(), sql.getNewName(),
						size, prec, mandatory, binary, defaultValue);
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLDropColumn(SQLDropColumn sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		appendAlterTable(buffer, sql);
		buffer.append("DROP COLUMN ");
		buffer.append(buffer.sqlDialect.columnRef(sql.getColumnName()));

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLUpdate(SQLUpdate sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append("UPDATE");
		buffer.append(' ');
		// Note: Do not descend, because in an UPDATE, no table alias must be created.
		buffer.append(buffer.sqlDialect.tableRef(sql.getTable().getTableName()));
	
		buffer.append(' ');
		buffer.append("SET");
		buffer.append(' ');
		List<String> columnNames = sql.getColumns();
		List<SQLExpression> values = sql.getValues();
		for (int n = 0, cnt = columnNames.size(); n < cnt; n++) {
			String columnName = columnNames.get(n);
			SQLExpression value = values.get(n);
	
			if (n > 0) {
				buffer.append(',');
			}
			buffer.append(buffer.sqlDialect.columnRef(columnName));
			buffer.append('=');
			value.visit(this, buffer);
		}
	
		SQLExpression where = sql.getWhere();
		if (where != SQLBoolean.TRUE) {
			buffer.append(' ');
			buffer.append("WHERE");
			buffer.append(' ');
			where.visit(this, buffer);
		}
	
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLJoin(SQLJoin sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		SQLTableReference leftTable = sql.getLeftTable();
		SQLTableReference rightTable = sql.getRightTable();
		SQLExpression condition = sql.getCondition();
		boolean hasCondition = condition != SQLBoolean.TRUE;
	
		if (sql.isInner() || (! hasCondition)) {
			boolean bracketsRequired = !(rightTable instanceof SQLTable || rightTable instanceof SQLSubQuery);
			leftTable.visit(this, buffer);
			if (hasCondition) {
				buffer.append(" INNER JOIN ");
				if (bracketsRequired) {
					buffer.append("(");
				}
				rightTable.visit(this, buffer);
				if (bracketsRequired) {
					buffer.append(")");
				}
				buffer.append(" ON ");
				condition.visit(this, buffer);
			} else {
				buffer.append(" CROSS JOIN ");
				if (bracketsRequired) {
					buffer.append("(");
				}
				rightTable.visit(this, buffer);
				if (bracketsRequired) {
					buffer.append(")");
				}
			}
		} else {
			sql.getLeftTable().visit(this, buffer);
			buffer.append(" LEFT JOIN ");
			sql.getRightTable().visit(this, buffer);
			buffer.append(" ON ");
			condition.visit(this, buffer);
		}
		return resetContext(oldContext, buffer);
	}

	/**
	 * Visits all parts in the natural order separated by the given separator
	 * 
	 * @param separator
	 *        to append to the given buffer between the visit of the single parts.
	 */
	protected void appendSeparated(List<? extends SQLPart> parts, E buffer, String separator) {
		for (int n = 0, cnt = parts.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(separator);
			}
			parts.get(n).visit(this, buffer);
		}
	}

	@Override
	public Void visitSQLSelect(SQLSelect sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		SQLLimit limit = sql.getLimit();
		boolean hasLimit = limit != SQLLimit.NO_LIMIT;

		if (hasLimit) {
			limitStart(limit, buffer);
		}
		buffer.append("SELECT");
		if (hasLimit) {
			limitColumns(limit, buffer);
		}
		buffer.append(' ');
		if (sql.isDistinct()) {
			buffer.append("DISTINCT");
			buffer.append(' ');
		}
		appendSeparated(sql.getColumns(), buffer, ", ");
		buffer.append(FROM);
		sql.getTableReference().visit(this, buffer);
		if (sql.getNoBlockHint()) {
			buffer.append(buffer.sqlDialect.selectNoBlockHint());
		}

		if (sql.isForUpdate()) {
			buffer.sqlDialect.forUpdate1();
		}
		SQLExpression where = sql.getWhere();
		if (where != SQLBoolean.TRUE) {
			buffer.append(' ');
			buffer.append("WHERE");
			buffer.append(' ');
			where.visit(this, buffer);
		}
		
		appendOrders(sql.getOrderBy(), buffer);
		
		if (hasLimit) {
			limitLast(limit, buffer);
		}

		if (sql.isForUpdate()) {
			buffer.sqlDialect.forUpdate2();
		}

		return resetContext(oldContext, buffer);
	}

	/** 
	 * Append the list of orders to the result.
	 */
	protected void appendOrders(List<? extends SQLOrder> orderBy, E buffer) {
		if (orderBy.size() > 0) {
			buffer.append(' ');
			buffer.append("ORDER BY");
			buffer.append(' ');
			appendSeparated(orderBy, buffer, ", ");
		}
	}

	/**
	 * Append limitation before the statement keyword of the query
	 * 
	 * @param limit
	 *        the visited {@link SQLLimit}
	 * @param buffer
	 *        the visit argument
	 * 
	 * @see DBHelper#limitStart(StringBuilder, int)
	 * @see #limitColumns(SQLLimit, SimpleSQLBuffer)
	 * @see #limitLast(SQLLimit, SimpleSQLBuffer)
	 */
	protected void limitStart(SQLLimit limit, E buffer) {
		Integer startRow = getStartRow(limit, buffer);
		Integer stopRow = getStopRow(limit, buffer);
		buffer.sqlDialect.limitStart(buffer.buffer, startRow, stopRow);
	}
	
	/**
	 * Append limitation after the statement keyword of the query
	 * 
	 * @param limit
	 *        the visited {@link SQLLimit}
	 * @param buffer
	 *        the visit argument
	 * 
	 * @see DBHelper#limitStart(StringBuilder, int)
	 * @see #limitColumns(SQLLimit, SimpleSQLBuffer)
	 * @see #limitLast(SQLLimit, SimpleSQLBuffer)
	 */
	protected void limitColumns(SQLLimit limit, E buffer) {
		Integer startRow = getStartRow(limit, buffer);
		Integer stopRow = getStopRow(limit, buffer);
		buffer.sqlDialect.limitColumns(buffer.buffer, startRow, stopRow);
	}

	/**
	 * append limitation at the end of the query
	 * 
	 * @param limit
	 *        the visited {@link SQLLimit}
	 * @param buffer
	 *        the visit argument
	 * 
	 * @see DBHelper#limitLast(StringBuilder, int)
	 * @see #limitStart(SQLLimit, SimpleSQLBuffer)
	 * @see #limitColumns(SQLLimit, SimpleSQLBuffer)
	 */
	protected void limitLast(SQLLimit limit, E buffer) {
		Integer startRow = getStartRow(limit, buffer);
		Integer stopRow = getStopRow(limit, buffer);
		buffer.sqlDialect.limitLast(buffer.buffer, startRow, stopRow);
	}

	private Integer getStartRow(SQLLimit limit, E buffer) {
		return getLimitRow(limit, limit.getStartRow(), buffer);
	}

	private Integer getStopRow(SQLLimit limit, E buffer) {
		return getLimitRow(limit, limit.getStopRow(), buffer);
	}

	/**
	 * Resolves the row value for the given expression.
	 * 
	 * @param limit
	 *        the limit currently processed
	 * @param rowExpr
	 *        the expression to resolve number for, either {@link SQLLimit#getStartRow() start row}
	 *        or {@link SQLLimit#getStopRow() stop row} of the given {@link SQLLimit}.
	 * @param buffer
	 *        the visit argument
	 */
	protected abstract Integer getLimitRow(SQLLimit limit, SQLExpression rowExpr, E buffer);
	
	@Override
	public final Void visitSQLLimit(SQLLimit sql, E buffer) {
		throw new RuntimeException(
			"SQLLimits must not be visited directly as the place in the resulting SQL depends on the SQL dialect");
	}

	@Override
	public Void visitSQLTable(SQLTable sql, E buffer) {
		String tableName = sql.getTableName();
		if (tableName.equals(SQLTable.NO_TABLE)) {
			// Drop the " FROM " keyword from the context, since it is written by the SQL dialect.
			buffer.buffer.setLength(buffer.buffer.length() - FROM.length());
			buffer.append(buffer.sqlDialect.fromNoTable());
		} else {
			buffer.append(buffer.sqlDialect.tableRef(tableName));
		}
		if (sql.getTableAlias() != null) {
			buffer.append(' ');
			buffer.append(buffer.sqlDialect.tableRef(sql.getTableAlias()));
		}
		return none;
	}

	@Override
	public Void visitSQLSubQuery(SQLSubQuery sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append('(');
		sql.getSelect().visit(this, buffer);
		buffer.append(')');
		buffer.append(' ');
		buffer.append(buffer.sqlDialect.tableRef(sql.getTableAlias()));
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLColumnDefinition(SQLColumnDefinition sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		sql.getExpr().visit(this, buffer);
		if (sql.getAliasName() != null) {
			buffer.append(' ');
			buffer.append("AS");
			buffer.append(' ');
			buffer.append(buffer.sqlDialect.columnRef(sql.getAliasName()));
		}
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLColumnReference(SQLColumnReference sql, E buffer) {
		String tableAlias = sql.getTableAlias();
		if (tableAlias != null) {
			buffer.append(buffer.sqlDialect.tableRef(tableAlias));
			buffer.append('.');
		}
		String columnName = sql.getColumnName();
		if (columnName == null) {
			buffer.append("*");
		} else {
			buffer.append(buffer.sqlDialect.columnRef(columnName));
		}
		return none;
	}

	@Override
	public Void visitSQLBinaryExpression(SQLBinaryExpression sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		SQLExpression left = sql.getLeftExpr();
		SQLExpression right = sql.getRightExpr();
		
		SQLOp operation = sql.getOp();
		boolean needLeftParenthesis =
			(left instanceof SQLBinaryExpression) && (((SQLBinaryExpression) left).getOp() != operation);
		boolean needRightParenthesis =
			(right instanceof SQLBinaryExpression) && (((SQLBinaryExpression) right).getOp() != operation);

		if (needLeftParenthesis) buffer.append('(');
		left.visit(this, buffer);
		if (needLeftParenthesis) buffer.append(')');
		buffer.append(' ');

		switch (operation) {
		case add:
			buffer.append("+");
			break;
		case sub:
			buffer.append("-");
			break;
		case mul:
			buffer.append("*");
			break;
		case div:
			buffer.append("/");
			break;
		case and:
			buffer.append("AND");
			break;
		case or:
			buffer.append("OR");
			break;
		case eq:
			buffer.append("=");
			break;
		case ge:
			buffer.append(">=");
			break;
		case gt:
			buffer.append(">");
			break;
		case le:
			buffer.append("<=");
			break;
		case lt:
			buffer.append("<");
			break;
		}
		buffer.append(' ');
		if (needRightParenthesis) buffer.append('(');
		right.visit(this, buffer);
		if (needRightParenthesis) buffer.append(')');

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLCast(SQLCast sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);

		String exprSql = toString(buffer, sql.getExpr());

		buffer.sqlDialect.appendCastExpression(buffer,
			exprSql, sql.getDbType(), sql.getSize(), sql.getPrecision(), sql.getBinary());

		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLFunction(SQLFunction sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		SQLFun fun = sql.getFun();
		switch (fun) {
		case isTrue: {
			SQLExpression testExpr = sql.getArguments().get(0);
				// TODO: Dispatch to sqlDialect.
				if (buffer.sqlDialect instanceof MySQLHelper) {
				testExpr.visit(this, buffer);
				buffer.append(' ');
				buffer.append("IS TRUE");
			} else {
				buffer.append("CASE WHEN");
				buffer.append(' ');
				testExpr.visit(this, buffer);
				buffer.append(' ');
				buffer.append("THEN 1 ELSE 0 END");
			}
			break;
		}

		case greatest:
		case least:
		case min:
		case max: 
		{
			buffer.append(fun.name());
			buffer.append('(');
				appendSeparated(sql.getArguments(), buffer, ", ");
			buffer.append(')');
		}
		}
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLInSet(SQLInSet sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		sql.getExpr().visit(this, buffer);
		buffer.append(" IN ");
		buffer.append('(');
		sql.getValues().visit(this, buffer);
		buffer.append(')');
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLTuple(SQLTuple sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append('(');
		appendSeparated(sql.getExpressions(), buffer, ",");
		buffer.append(')');
		return resetContext(oldContext, buffer);
	}

	@Override
	public Void visitSQLCase(SQLCase sql, E buffer) {
		SQLPart oldContext = setContext(sql, buffer);
		buffer.append("CASE WHEN (");
		sql.getCondition().visit(this, buffer);
		buffer.append(") THEN (");
		sql.getThen().visit(this, buffer);
		buffer.append(") ELSE (");
		sql.getElse().visit(this, buffer);
		buffer.append(") END");
		return resetContext(oldContext, buffer);
	}

}
