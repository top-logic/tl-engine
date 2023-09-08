/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.build;

import com.top_logic.knowledge.objects.inspect.condition.And;
import com.top_logic.knowledge.objects.inspect.condition.False;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;
import com.top_logic.knowledge.objects.inspect.condition.Or;
import com.top_logic.knowledge.objects.inspect.condition.True;

/**
 * {@link DecisionTreeNode} that drops all updates.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class IgnoreAll<T> extends DecisionTreeNode<T> {

	@Override
	public DecisionTreeNode<T> add(T value) {
		// Ignore.
		return this;
	}

	@Override
	public DecisionTreeNode<T> visitTrue(True condition, Void arg) {
		return this;
	}

	@Override
	public DecisionTreeNode<T> visitFalse(False condition, Void arg) {
		return this;
	}

	@Override
	public DecisionTreeNode<T> when(ItemFunction test, Object expected) {
		return this;
	}

	@Override
	public DecisionTreeNode<T> visitAnd(And condition, Void arg) {
		return this;
	}

	@Override
	public DecisionTreeNode<T> visitOr(Or condition, Void arg) {
		return this;
	}

}