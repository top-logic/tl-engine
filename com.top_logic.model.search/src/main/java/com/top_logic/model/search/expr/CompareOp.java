/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing a comparison operation between {@link #getLeft()} and
 * {@link #getRight()}.
 * 
 * @see CompareKind
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompareOp extends BinaryOperation implements BooleanExpression {

	private CompareKind _kind;

	/**
	 * Creates a {@link CompareOp}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	CompareOp(CompareKind kind, SearchExpression left, SearchExpression right) {
		super(left, right);
		_kind = kind;
	}

	/**
	 * The compare operation to perform.
	 */
	public CompareKind getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public void setKind(CompareKind kind) {
		_kind = kind;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitCompareOp(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object left = getLeft().evalWith(definitions, args);
		Object right = getRight().evalWith(definitions, args);
		return compute(left, right);
	}

	/**
	 * Actually computes the comparison based on the given concrete values.
	 */
	public final Boolean compute(Object left, Object right) {
		if ((left == null) || (right == null)) {
			/* Null is not a valid value in comparisons: If one of the values is null, the result is
			 * not result, but null itself. */
			return null;
		}
		int comparison = Compare.Cmp.NULL_SMALLEST.compare(left, right);

		switch (_kind) {
			case GE:
				return comparison >= 0;
			case GT:
				return comparison > 0;
			case LE:
				return comparison <= 0;
			case LT:
				return comparison < 0;
		}

		throw new UnsupportedOperationException("No such comparison: " + _kind);
	}

	@Override
	public Object getId() {
		return getKind();
	}

}
