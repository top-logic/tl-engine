/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * Describes a limitation of SQL results, e.g. a {@link SQLLimit} with {@link #getStartRow()}
 * <code>10</code> and {@link #getStopRow()} <code>15</code> limits the result to <code>5</code>
 * elements.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLLimit extends AbstractSQLPart {

	/**
	 * Constant to indicate that the first row of the database result should occur in the final
	 * result.
	 * 
	 * @see SQLFactory#limitFirstRow()
	 */
	public static final int FIRST_ROW = 0;

	/**
	 * Constant to indicate that the last row of the database result should occur in the final
	 * result
	 */
	public static final int LAST_ROW = -1;

	/**
	 * Constant to indicate that the whole result is needed.
	 */
	public static final SQLLimit NO_LIMIT = null;

	private SQLExpression _startRow;

	private SQLExpression _stopRow;

	SQLLimit(SQLExpression startRow, SQLExpression stopRow) {
		assert availableType(startRow) : "Can not use " + startRow + " as row.";
		assert availableType(stopRow) : "Can not use " + stopRow + " as row.";
		_startRow = startRow;
		_stopRow = stopRow;
	}

	private static boolean availableType(SQLExpression firstRow) {
		boolean isParameter = firstRow instanceof SQLParameter;
		boolean isLiteral = firstRow instanceof SQLLiteral && ((SQLLiteral) firstRow).getJdbcType() == DBType.INT;
		return isParameter || isLiteral;
	}

	/**
	 * the index of the first row to occur in the result, e.g. if <code>0</code> the first
	 *         row occur in the result, if <code>5</code> the sixth row is the first occurring in
	 *         the result.
	 */
	public SQLExpression getStartRow() {
		return _startRow;
	}

	/** @see #getStartRow() */
	public void setStartRow(SQLExpression startRow) {
		assert availableType(startRow) : "Can not use " + startRow + " as row.";
		_startRow = startRow;
	}

	/**
	 * the index of the first row which do not occur in the result.
	 */
	public SQLExpression getStopRow() {
		return _stopRow;
	}

	/** @see #getStopRow() */
	public void setStopRow(SQLExpression stopRow) {
		assert availableType(stopRow) : "Can not use " + stopRow + " as row.";
		_stopRow = stopRow;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLLimit(this, arg);
	}

}

