/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link BinaryOperation} testing, whether {@link #getLeft()} and {@link #getRight()} have a
 * non-empty intersection.
 * 
 * <p>
 * Both operands {@link #getLeft()} and {@link #getRight()} are expected to be of collection type.
 * The result is {@link Boolean}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContainsSome extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link ContainsSome}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	ContainsSome(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitContainsSome(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Collection<?> set = asCollection(getLeft().evalWith(definitions, args));
		Collection<?> test = asCollection(getRight().evalWith(definitions, args));
		for (Object element : test) {
			if (set.contains(element)) {
				return true;
			}
		}
		return false;
	}

}