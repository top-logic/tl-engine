/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing the union of two sets.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Union extends BinaryOperation {

	/**
	 * Creates a {@link Union}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	Union(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitUnion(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Collection<?> result1 = asCollection(getLeft().evalWith(definitions, args));
		Collection<?> result2 = asCollection(getRight().evalWith(definitions, args));
		return CollectionUtil.union(result1, result2);
	}
}