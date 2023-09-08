/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * SELECT {@link #expr} AS {@link #aliasName} FROM.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLColumnDefinition extends AbstractSQLPart {

	/** Expression describing a Result column (e.g. MAX(AGE) ) */
	private SQLExpression expr;

	/** Optional Alias used for "AS" */
	private String aliasName;

	SQLColumnDefinition(SQLExpression expr, String aliasName) {
		this.expr = expr;
		this.aliasName = aliasName;
	}

	/**
	 * @see #expr
	 */
	public SQLExpression getExpr() {
		return expr;
	}

	/**
	 * @see #expr
	 */
	public void setExpr(SQLExpression expr) {
		this.expr = expr;
	}

	/**
	 * @see #aliasName
	 */
	public String getAliasName() {
		return aliasName;
	}

	/**
	 * @see #aliasName
	 */
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLColumnDefinition(this, arg);
	}
}
