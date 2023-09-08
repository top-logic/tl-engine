/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Representation of SQL-Boolean expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLBoolean extends AbstractSQLExpression {

	/**
	 * Singleton instance for <code>true</code>.
	 */
	public static final SQLBoolean TRUE = new SQLBoolean(Boolean.TRUE);

	/**
	 * Singleton instance for <code>false</code>.
	 */
	public static final SQLBoolean FALSE = new SQLBoolean(Boolean.FALSE);
	
	private final Boolean value;

	private SQLBoolean(Boolean value) {
		this.value = value;
	}
	
	/**
	 * The <tt>boolean</tt> represented by this {@link SQLBoolean}.
	 */
	public Boolean getValue() {
		return value;
	}
	
	/**
	 * Returns an {@link SQLBoolean} instance representing the specified <tt>boolean</tt> value. If
	 * the specified <tt>boolean</tt> value is <tt>true</tt>, this method returns
	 * {@link SQLBoolean#TRUE}; if it is <tt>false</tt>, this method returns
	 * {@link SQLBoolean#FALSE}.
	 * 
	 * @param value
	 *        a boolean value.
	 * @return an {@link SQLBoolean} instance representing <tt>b</tt>.
	 */
	public static SQLExpression valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLBoolean(this, arg);
	}

}
