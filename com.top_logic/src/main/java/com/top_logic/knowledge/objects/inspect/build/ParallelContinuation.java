/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * {@link DecisionTreeNode} that manipulates multiple paths of a {@link DecisionTreeBuilder} in
 * parallel.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ParallelContinuation<T> extends DecisionTreeNode<T> {

	private final List<DecisionTreeNode<T>> _ends;

	public ParallelContinuation(List<DecisionTreeNode<T>> ends) {
		_ends = ends;
	}

	@Override
	public DecisionTreeNode<T> add(T value) {
		for (DecisionTreeNode<T> end : _ends) {
			end.add(value);
		}
		return this;
	}

	@Override
	public DecisionTreeNode<T> when(ItemFunction test, Object expected) {
		List<DecisionTreeNode<T>> results = new ArrayList<>();
		for (DecisionTreeNode<T> end : _ends) {
			DecisionTreeNode<T> resultPart = end.when(test, expected);
			results.add(resultPart);
		}
		return new ParallelContinuation<>(results);
	}

}
