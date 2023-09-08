/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.objects.inspect.DecisionTree;
import com.top_logic.knowledge.objects.inspect.condition.And;
import com.top_logic.knowledge.objects.inspect.condition.Condition;
import com.top_logic.knowledge.objects.inspect.condition.ConditionVisitor;
import com.top_logic.knowledge.objects.inspect.condition.Equals;
import com.top_logic.knowledge.objects.inspect.condition.False;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;
import com.top_logic.knowledge.objects.inspect.condition.Or;
import com.top_logic.knowledge.objects.inspect.condition.True;


/**
 * Base class for a single node in a {@link DecisionTree} being built.
 * 
 * @see DecisionTreeBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DecisionTreeNode<T> implements ConditionVisitor<DecisionTreeNode<T>, Void> {

	/**
	 * Adds a test to this node.
	 * 
	 * @param test
	 *        The test function being executed when this node is reached during decision.
	 * @param expected
	 *        The expected result that must be returned from the test function to continue to the
	 *        resulting decision tree node.
	 * @return The decision tree node that is reached, if the given test function returns the given
	 *         expected value.
	 */
	public abstract DecisionTreeNode<T> when(ItemFunction test, Object expected);

	/**
	 * Adds a value that is returned from the decision process whenever this node in the decision
	 * tree is reached.
	 * 
	 * @param value
	 *        The value to return at this stage of decision.
	 * @return This node for call chaining.
	 */
	public abstract DecisionTreeNode<T> add(T value);

	/**
	 * Build a path starting at this {@link DecisionTreeNode} corresponding to the given condition
	 * and assign the given value to the resulting node.
	 * 
	 * @param condition
	 *        The condition to transform to a path in the final {@link DecisionTree}.
	 * @param value
	 *        The value to return, if the given condition holds.
	 * @return The {@link DecisionTreeNode} that is reached, if the given condition holds.
	 */
	public final DecisionTreeNode<T> add(Condition condition, T value) {
		return condition.visit(this, null).add(value);
	}

	@Override
	public DecisionTreeNode<T> visitTrue(True condition, Void arg) {
		return this;
	}

	@Override
	public DecisionTreeNode<T> visitFalse(False condition, Void arg) {
		return new IgnoreAll<>();
	}

	@Override
	public final DecisionTreeNode<T> visitEquals(Equals condition, Void arg) {
		return when(condition.test(), condition.expected());
	}

	@Override
	public DecisionTreeNode<T> visitAnd(And condition, Void arg) {
		DecisionTreeNode<T> target = this;
		for (Condition part : condition.conditions()) {
			target = part.visit(target, arg);
		}
		return target;
	}

	@Override
	public DecisionTreeNode<T> visitOr(Or condition, Void arg) {
		List<DecisionTreeNode<T>> results = new ArrayList<>();
		for (Condition part : condition.conditions()) {
			DecisionTreeNode<T> resultPart = part.visit(this, arg);
			results.add(resultPart);
		}
		return new ParallelContinuation<>(results);
	}

}
