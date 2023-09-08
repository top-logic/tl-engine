/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;


/**
 * {@link BinaryOperation} computing the boolean and operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class And extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link And}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	And(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAnd(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		boolean leftResult = isTrue(getLeft().evalWith(definitions, args));
		if (!leftResult) {
			return Boolean.FALSE;
		}
		return evalRight(args, definitions);
	}

	private Boolean evalRight(Args args, EvalContext definitions) {
		return asBoolean(getRight().evalWith(definitions, args));
	}

}