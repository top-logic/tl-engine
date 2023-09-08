/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import java.util.Collection;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.DecisionTree;

/**
 * {@link DecisionTree} that unconditionally delivers a list of results.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class Single<T> extends DecisionTree<T> {

	private final T _value;

	public Single(T value) {
		_value = value;
	}

	@Override
	public void addMatches(KnowledgeItem item, Collection<T> result) {
		result.add(_value);
	}

	@Override
	public T getFirstMatch(KnowledgeItem item) {
		return _value;
	}
}