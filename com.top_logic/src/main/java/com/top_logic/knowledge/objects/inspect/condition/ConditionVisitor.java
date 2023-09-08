/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;


/**
 * Visitor interface for the {@link Condition} hierarchy.
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type of the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConditionVisitor<R, A> {

	/**
	 * Visit case for {@link True}.
	 */
	R visitTrue(True condition, A arg);

	/**
	 * Visit case for {@link False}.
	 */
	R visitFalse(False condition, A arg);

	/**
	 * Visit case for {@link Equals}.
	 */
	R visitEquals(Equals condition, A arg);

	/**
	 * Visit case for {@link And}.
	 */
	R visitAnd(And condition, A arg);

	/**
	 * Visit case for {@link Or}.
	 */
	R visitOr(Or condition, A arg);

}