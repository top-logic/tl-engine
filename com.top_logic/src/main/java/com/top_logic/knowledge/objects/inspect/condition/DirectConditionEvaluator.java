/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Evaluator algorithm for {@link Condition}s.
 * 
 * <p>
 * Note: This evaluation algorithm only defines the semantics of conditions. For efficient mass
 * evaluation, {@link Condition}s must be transformed using a {@link ConditionEvaluatorBuilder}.
 * </p>
 * 
 * @see ConditionEvaluatorBuilder#build(Condition)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectConditionEvaluator implements ConditionVisitor<Boolean, KnowledgeItem> {

	/**
	 * Singleton {@link DirectConditionEvaluator} instance.
	 */
	private static final DirectConditionEvaluator INSTANCE = new DirectConditionEvaluator();

	private DirectConditionEvaluator() {
		// Singleton constructor.
	}

	/**
	 * Evaluates the given {@link Condition} on the given {@link KnowledgeItem}.
	 * 
	 * @param condition
	 *        The condition to evaluate.
	 * @param item
	 *        The {@link KnowledgeItem} to pass to all {@link ItemFunction}s part of the given
	 *        condition.
	 * @return Whether the given {@link Condition} holds for the given {@link KnowledgeItem}.
	 */
	public static boolean eval(Condition condition, KnowledgeItem item) {
		return condition.visit(INSTANCE, item).booleanValue();
	}

	@Override
	public Boolean visitTrue(True condition, KnowledgeItem arg) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitFalse(False condition, KnowledgeItem arg) {
		return Boolean.FALSE;
	}

	@Override
	public Boolean visitEquals(Equals condition, KnowledgeItem arg) {
		return Boolean.valueOf(condition.test().apply(arg) == condition.expected());
	}

	@Override
	public Boolean visitAnd(And condition, KnowledgeItem arg) {
		Condition[] conditions = condition.conditions();
		for (int n = 0, cnt = conditions.length; n < cnt; n++) {
			if (!conditions[n].visit(this, arg)) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitOr(Or condition, KnowledgeItem arg) {
		Condition[] conditions = condition.conditions();
		for (int n = 0, cnt = conditions.length; n < cnt; n++) {
			if (conditions[n].visit(this, arg)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}