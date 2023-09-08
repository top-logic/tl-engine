/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;


import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.build.DecisionTreeBuilder;

/**
 * A condition (boolean function) that can be evaluated on a {@link KnowledgeItem}.
 * 
 * @see DirectConditionEvaluator
 * @see DecisionTreeBuilder#add(Condition, Object)
 * @see Conditions
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Condition {

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	@Override
	public abstract String toString();

	/**
	 * Visit method for the {@link Condition} hierarchy.
	 * 
	 * @param <R>
	 *        The result type of the visit.
	 * @param <A>
	 *        The argument type for the visit.
	 * 
	 * @param v
	 *        The {@link ConditionVisitor} to use.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	public abstract <R, A> R visit(ConditionVisitor<R, A> v, A arg);

}