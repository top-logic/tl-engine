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
 * {@link BinaryOperation} testing, whether {@link #getRight()} is an element of the collection
 * {@link #getLeft()}.
 * 
 * <p>
 * The operand {@link #getLeft()} is expected to be of collection type. The operand
 * {@link #getRight()} is of the collection contents type. The result is {@link Boolean}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContainsElement extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link ContainsElement}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	ContainsElement(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitContainsElement(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Collection<?> set = asCollection(getLeft().evalWith(definitions, args));
		Object element = getRight().evalWith(definitions, args);
		return set.contains(element);
	}

}