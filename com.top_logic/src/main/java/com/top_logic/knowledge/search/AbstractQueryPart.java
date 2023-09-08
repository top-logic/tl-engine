/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link QueryPart} for things needed to create the actual query.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractQueryPart extends QueryPart {

	/**
	 * Applies the given {@link AbstractQueryVisitor} to this query part.
	 * 
	 * @param v
	 *        The visitor.
	 * @param arg
	 *        The argument passed to the visit methods.
	 * @return The result computed by the visitor.
	 */
	public abstract <R, A> R visit(AbstractQueryVisitor<R, A> v, A arg);

	/**
	 * Dispatches to the {@link AbstractQueryVisitor} aspect.
	 * 
	 * @see QueryPart#visitQuery(QueryVisitor, Object)
	 */
	@Override
	public final <RQ, RE extends RQ, RS extends RQ, RF extends RQ, RO extends RQ, A> RQ visitQuery(
			QueryVisitor<RQ, RE, RS, RF, RO, A> v, A arg) {
		return this.visit(v, arg);
	}

}

