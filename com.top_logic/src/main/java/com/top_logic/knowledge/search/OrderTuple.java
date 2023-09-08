/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

/**
 * Ordered list of other {@link OrderSpec}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrderTuple extends Order {

	private List<OrderSpec> orderSpecs;

	/**
	 * Creates a {@link OrderTuple}.
	 * 
	 * @param orderSpecs
	 *        The ordering expressions.
	 */
	OrderTuple(List<OrderSpec> orderSpecs) {
		this.orderSpecs = orderSpecs;
	}

	/**
	 * the orders of this tuple.
	 */
	public List<OrderSpec> getOrderSpecs() {
		return orderSpecs;
	}
	
	public void setOrderSpecs(List<OrderSpec> orderSpecs) {
		this.orderSpecs = orderSpecs;
	}
	
	@Override
	public <R,A> R visitOrder(OrderVisitor<R,A> v, A arg) {
		return v.visitOrderTuple(this, arg);
	}

}
