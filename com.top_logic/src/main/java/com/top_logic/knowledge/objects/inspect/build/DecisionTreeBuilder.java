/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.knowledge.objects.inspect.DecisionTree;
import com.top_logic.knowledge.objects.inspect.condition.Condition;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * Builder for a {@link DecisionTree}.
 * 
 * <p>
 * A {@link DecisionTreeBuilder} is the root node of a decision tree being built.
 * </p>
 * 
 * @see #when(ItemFunction, Object)
 * @see #add(Object)
 * @see #add(Condition, Object)
 * @see #build()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DecisionTreeBuilder<T> extends DecisionTreeNode<T> {

	private final Map<ItemFunction, Map<Object, DecisionTreeBuilder<T>>> _switches =
		new HashMap<>();

	private final List<T> _values = new ArrayList<>();

	/**
	 * Creates a {@link DecisionTreeBuilder}.
	 */
	public DecisionTreeBuilder() {
		super();
	}

	@Override
	public DecisionTreeNode<T> add(T value) {
		_values.add(value);
		return this;
	}

	@Override
	public DecisionTreeNode<T> when(ItemFunction test, Object expected) {
		Map<Object, DecisionTreeBuilder<T>> switchBlock = _switches.get(test);
		if (switchBlock == null) {
			switchBlock = new HashMap<>();
			_switches.put(test, switchBlock);
		}

		DecisionTreeBuilder<T> continuation = switchBlock.get(expected);
		if (continuation == null) {
			continuation = new DecisionTreeBuilder<>();
			switchBlock.put(expected, continuation);
		}

		return continuation;
	}

	/**
	 * Builds the final {@link DecisionTree} to be used for decisions.
	 */
	public DecisionTree<T> build() {
		List<DecisionTree<T>> result = new ArrayList<>();

		if (!_values.isEmpty()) {
			if (_values.size() == 1) {
				result.add(new Single<>(_values.get(0)));
			} else {
				result.add(new All<>(_values));
			}
		}

		for (Entry<ItemFunction,  Map<Object, DecisionTreeBuilder<T>>> switchBlock : _switches.entrySet()) {
			final ItemFunction expr = switchBlock.getKey();

			final HashMap<Object, DecisionTree<T>> decisionForValue = new HashMap<>();
			for (Entry<Object, DecisionTreeBuilder<T>> caseStatement : switchBlock.getValue().entrySet()) {
				Object value = caseStatement.getKey();

				decisionForValue.put(value, caseStatement.getValue().build());
			}

			result.add(new Switch<>(expr, decisionForValue));
		}

		int decisionCnt = result.size();
		switch (decisionCnt) {
			case 0:
				return new None<>();
			case 1:
				return result.get(0);
			default:
				return new Some<>(result);
		}
	}
}