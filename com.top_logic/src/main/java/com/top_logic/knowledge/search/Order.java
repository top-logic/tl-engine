/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Base class for {@link QueryPart}s that define the result order.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Order extends QueryPart {

	/**
	 * Visits this {@link Order} using the given {@link OrderVisitor}.
	 * 
	 * @param v
	 *        The visitor that visits this order.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	public abstract <R,A> R visitOrder(OrderVisitor<R,A> v, A arg);
	
	@Override
	public final <RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ,A> RQ visitQuery(QueryVisitor<RQ,RE,RS,RF,RO,A> v, A arg) {
		return visitOrder(v, arg);
	}
	
}
