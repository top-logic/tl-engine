/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Expression that is set-valued.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SetExpression extends QueryPart {

	/**
	 * Delegates to {@link #visitSetExpr(SetExpressionVisitor, Object)}
	 * 
	 * @see QueryPart#visitQuery(QueryVisitor, Object)
	 */
	@Override
	public final <RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ,A> RQ visitQuery(QueryVisitor<RQ, RE, RS, RF, RO, A> v, A arg) {
		return visitSetExpr(v, arg);
	}

	/**
	 * Visits this SetExpression using the given {@link SetExpressionVisitor}
	 * 
	 * @param v
	 *        the visitor of this {@link SetExpression}. Used for callback.
	 * @param arg
	 *        the argument of the visit. Used for callback.
	 * 
	 * @return the return value of the visitor when executing callback method
	 */
	public abstract <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg);

}
