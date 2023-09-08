/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.Collection;
import java.util.HashMap;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.DecisionTree;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * {@link DecisionTree} that tests an {@link ItemFunction} result to be in a set of literal values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class Switch<T> extends DecisionTree<T> {
	private final ItemFunction _expr;

	private final HashMap<Object, DecisionTree<T>> _decisionForValue;

	public Switch(ItemFunction expr, HashMap<Object, DecisionTree<T>> decisionForValue) {
		_expr = expr;
		_decisionForValue = decisionForValue;
	}

	@Override
	public void addMatches(KnowledgeItem item, Collection<T> result) {
		Object value = _expr.apply(item);
		DecisionTree<T> dispatch = _decisionForValue.get(value);
		if (dispatch != null) {
			dispatch.addMatches(item, result);
		}
	}

	@Override
	public T getFirstMatch(KnowledgeItem item) {
		Object value = _expr.apply(item);
		DecisionTree<T> dispatch = _decisionForValue.get(value);
		if (dispatch != null) {
			return dispatch.getFirstMatch(item);
		} else {
			return null;
		}
	}

}