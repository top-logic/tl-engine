/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.ConditionEvaluator;
import com.top_logic.knowledge.objects.inspect.DecisionTree;
import com.top_logic.knowledge.objects.inspect.build.DecisionTreeBuilder;

/**
 * Construction algorithm for {@link ConditionEvaluator}s from {@link Condition}s.
 * 
 * @see #build(Condition)
 * @see DirectConditionEvaluator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConditionEvaluatorBuilder {

	private static final NamedConstant MATCHES = new NamedConstant("matches");

	/**
	 * Transforms the given {@link Condition} into an efficient evaluation algorithm.
	 * 
	 * @param condition
	 *        The {@link Condition} to transform.
	 * @return The {@link ConditionEvaluator} algorithm.
	 */
	public static ConditionEvaluator build(Condition condition) {
		DecisionTreeBuilder<NamedConstant> builder = new DecisionTreeBuilder<>();
		builder.add(condition, MATCHES);
		final DecisionTree<NamedConstant> decisionTree = builder.build();
		return new ConditionEvaluator() {
			@Override
			public boolean eval(KnowledgeItem item) {
				return decisionTree.getFirstMatch(item) != null;
			}
		};
	}

}
