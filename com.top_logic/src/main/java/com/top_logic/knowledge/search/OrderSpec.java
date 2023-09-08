/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Expression} that specifies (part of) the sort order of a {@link RevisionQuery} result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrderSpec extends Order {

	/**
	 * @see #isDescending()
	 */
	private final boolean descending;
	
	/**
	 * @see #getOrderExpr()
	 */
	private Expression orderExpr;

	/**
	 * Creates a {@link OrderSpec}.
	 * 
	 * @param orderExpr
	 *        The expression that is compared during sorting.
	 * @param descending
	 *        Whether the order should be ascending (<code>false</code>) or
	 *        descending (<code>true</code>).
	 */
	OrderSpec(Expression orderExpr, boolean descending) {
		this.orderExpr = orderExpr;
		this.descending = descending;
	}

	/**
	 * Whether the result should be ordered in descending or ascending order.
	 * 
	 * @return <code>true</code> for descending order and <code>false</code> for
	 *         ascending order.
	 */
	public boolean isDescending() {
		return descending;
	}

	/**
	 * The expression whose evaluation produces the values that determine the
	 * ordering.
	 */
	public Expression getOrderExpr() {
		return orderExpr;
	}

	/**
	 * Sets value of {@link #getOrderExpr()}.
	 */
	public void setOrderExpr(Expression orderExpr) {
		this.orderExpr = orderExpr;
	}

	@Override
	public <R,A> R visitOrder(OrderVisitor<R,A> v, A arg) {
		return v.visitOrderSpec(this, arg);
	}

}
