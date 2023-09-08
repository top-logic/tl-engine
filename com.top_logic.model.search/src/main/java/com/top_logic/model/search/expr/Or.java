/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link BinaryOperation} representing the {@link Boolean} or operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Or extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link Or}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	Or(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitOr(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object left = evalLeft(args, definitions);
		if (isTrue(left)) {
			// Note: in a or combination, the result is not converted to a boolean. This allows
			// combining arbitrary values with a boolean or combinator and retrieving the first
			// value that is considered equivalent to true. An expression $x || "unknown" results in
			// the value of x whenever this value is not null, false or empty. Otherwise the value
			// "unknown" is returned.
			return left;
		}
		return evalRight(args, definitions);
	}

	private Object evalLeft(Args args, EvalContext definitions) {
		return getLeft().evalWith(definitions, args);
	}

	private Object evalRight(Args args, EvalContext definitions) {
		return getRight().evalWith(definitions, args);
	}

}