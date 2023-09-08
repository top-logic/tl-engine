/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.condition.Condition;
import com.top_logic.knowledge.objects.inspect.condition.ConditionEvaluatorBuilder;

/**
 * Evaluator for plain {@link Condition}s.
 * 
 * @see ConditionEvaluatorBuilder#build(Condition)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConditionEvaluator {

	/**
	 * The {@link ConditionEvaluator} that always returns <code>true</code>.
	 */
	ConditionEvaluator TRUE = new ConditionEvaluator() {
		@Override
		public boolean eval(KnowledgeItem item) {
			return true;
		}
	};

	/**
	 * The {@link ConditionEvaluator} that always returns <code>false</code>.
	 */
	ConditionEvaluator FALSE = new ConditionEvaluator() {
		@Override
		public boolean eval(KnowledgeItem item) {
			return false;
		}
	};

	/**
	 * Decides whether the {@link Condition} this {@link ConditionEvaluator} was build for holds for
	 * the given {@link KnowledgeItem}.
	 * 
	 * @param item
	 *        The {@link KnowledgeItem} to evaluate the condition on.
	 * @return Whether the condition holds.
	 */
	boolean eval(KnowledgeItem item);

}
