/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.DecisionTree;

/**
 * {@link DecisionTree} that unconditionally delegates to some other {@link DecisionTree}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class Some<T> extends DecisionTree<T> {
	private final List<DecisionTree<T>> _decisions;

	public Some(List<DecisionTree<T>> decisions) {
		_decisions = decisions;
	}

	@Override
	public void addMatches(KnowledgeItem item, Collection<T> result) {
		for (DecisionTree<T> decision : _decisions) {
			decision.addMatches(item, result);
		}
	}

	@Override
	public T getFirstMatch(KnowledgeItem item) {
		for (DecisionTree<T> decision : _decisions) {
			T result = decision.getFirstMatch(item);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}