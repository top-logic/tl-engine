/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Visitor of {@link Order} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OrderVisitor<R,A> {

	/**
	 * Visits {@link OrderSpec}s.
	 */
	R visitOrderSpec(OrderSpec expr, A arg);
	
	/**
	 * Visits {@link OrderTuple}s.
	 */
	R visitOrderTuple(OrderTuple expr, A arg);

}
