/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLExpression} representing a literal value.
 * 
 * @see SQLSetLiteral
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLLiteral extends AbstractSQLExpression {

	/** Literal represents {@link Boolean#TRUE} */
	static final SQLLiteral TRUE = new SQLLiteral(DBType.BOOLEAN, Boolean.TRUE);

	/** Literal represents {@link Boolean#FALSE} */
	static final SQLLiteral FALSE = new SQLLiteral(DBType.BOOLEAN, Boolean.FALSE);

	private final Object value;

	private final DBType jdbcType;

	/**
	 * Creates a new {@link SQLLiteral}.
	 * 
	 * @param jdbcType
	 *        see {@link #getJdbcType()}
	 * @param value
	 *        see {@link #getValue()}
	 */
	public SQLLiteral(DBType jdbcType, Object value) {
		this.jdbcType = jdbcType;
		this.value = value;
	}
	
	/**
	 * The SQL type of this literal.
	 */
	public DBType getJdbcType() {
		return jdbcType;
	}

	/**
	 * The represented value.
	 */
	public Object getValue() {
		return this.value;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLLiteral(this, arg);
	}

}
