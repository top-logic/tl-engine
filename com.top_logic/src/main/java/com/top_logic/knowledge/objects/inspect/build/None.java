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
 * The {@link DecisionTree} that never delivers any results.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class None<T> extends DecisionTree<T> {
	public None() {
		super();
	}

	@Override
	public void addMatches(KnowledgeItem item, Collection<T> result) {
		// Ignore.
	}

	@Override
	public T getFirstMatch(KnowledgeItem item) {
		return null;
	}
}