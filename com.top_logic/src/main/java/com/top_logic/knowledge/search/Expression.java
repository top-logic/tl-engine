/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Common super class of all expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Expression extends QueryPart {
	
	/**
	 * Applies the given {@link ExpressionVisitor} to this expression.
	 * 
	 * @param v
	 *        The visitor.
	 * @param arg
	 *        The argument passed to the visit methods.
	 * @return The result computed by the visitor.
	 */
	public abstract <R,A> R visit(ExpressionVisitor<R,A> v, A arg);
	
	@Override
	public final <RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ,A> RQ visitQuery(QueryVisitor<RQ,RE,RS,RF,RO,A> v, A arg) {
		return this.visit(v, arg);
	}
	
}

