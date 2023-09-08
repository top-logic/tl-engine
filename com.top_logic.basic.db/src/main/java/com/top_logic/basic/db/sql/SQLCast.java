/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLExpression} converting between datatypes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLCast extends AbstractSQLExpression {

	private SQLExpression _expr;

	private DBType _dbType;

	private long _size;

	private int _precision;

	private boolean _binary;

	/**
	 * @see SQLFactory#cast(SQLExpression, DBType, long, int, boolean)
	 */
	SQLCast(SQLExpression expr, DBType dbType, long size, int precision, boolean binary) {
		_expr = expr;
		_dbType = dbType;
		_size = size;
		_precision = precision;
		_binary = binary;
	}

	/**
	 * The Expression to cast.
	 */
	public SQLExpression getExpr() {
		return _expr;
	}

	/**
	 * @see #getExpr()
	 */
	public void setExpr(SQLExpression expr) {
		_expr = expr;
	}

	/**
	 * The {@link DBType} to cast to.
	 */
	public DBType getDbType() {
		return _dbType;
	}

	/**
	 * @see #getDbType()
	 */
	public void setDbType(DBType dbType) {
		_dbType = dbType;
	}

	/**
	 * The size of the target {@link #getDbType()}.
	 */
	public long getSize() {
		return _size;
	}

	/**
	 * @see #getSize()
	 */
	public void setSize(long size) {
		_size = size;
	}

	/**
	 * The precision modifier of the {@link #getDbType()}.
	 */
	public int getPrecision() {
		return _precision;
	}

	/**
	 * @see #getPrecision()
	 */
	public void setPrecision(int precision) {
		_precision = precision;
	}

	/**
	 * The binary modifier of the {@link #getDbType()}.
	 */
	public boolean getBinary() {
		return _binary;
	}

	/**
	 * @see #getBinary()
	 */
	public void setBinary(boolean binary) {
		_binary = binary;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLCast(this, arg);
	}

}
