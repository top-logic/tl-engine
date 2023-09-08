/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.CollationHint;

/**
 * {@link SQLPart} specifying a order.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLOrder extends AbstractSQLPart {

	private boolean descending;

	private SQLExpression expr;

	private CollationHint collationHint;

	SQLOrder(boolean descending, CollationHint collationHint, SQLExpression expr) {
		this.descending = descending;
		this.collationHint = collationHint;
		this.expr = expr;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLOrder(this, arg);
	}

	/**
	 * Whether the order should be descending or ascending
	 */
	public boolean isDescending() {
		return descending;
	}

	/** @see #isDescending() */
	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	/**
	 * A hint for the comparison of the expression.
	 */
	public CollationHint getCollationHint() {
		return collationHint;
	}
	
	/** @see #getCollationHint() */
	public void setCollationHint(CollationHint collationHint) {
		this.collationHint = collationHint;
	}

	/**
	 * The expression which must be ordered by this {@link SQLOrder}.
	 */
	public SQLExpression getExpr() {
		return expr;
	}

	/** @see #getExpr() */
	public void setExpr(SQLExpression expr) {
		this.expr = expr;
	}

}
