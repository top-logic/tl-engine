/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link BinaryOperation} implementing an arithmetic operation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ArithmeticExpr extends BinaryOperation implements WithFlatMapSemantics<Object> {

	private Op _op;

	/**
	 * The arithmetic operator.
	 */
	public enum Op {
		/**
		 * Addition
		 */
		ADD,

		/**
		 * Subtraction
		 */
		SUB,

		/**
		 * Multiplication
		 */
		MUL,

		/**
		 * Division
		 */
		DIV,

		/**
		 * Modulo operation
		 */
		MOD;
	}

	/**
	 * Creates a {@link ArithmeticExpr}.
	 */
	ArithmeticExpr(Op op, SearchExpression left, SearchExpression right) {
		super(left, right);
		_op = op;
	}

	/**
	 * The arithmetic operation.
	 */
	public Op getOp() {
		return _op;
	}

	/**
	 * @see #getOp()
	 */
	public void setOp(Op op) {
		_op = op;
	}

	@Override
	protected Object internalEval(EvalContext definitions, Args args) {
		return compute(getLeft().evalWith(definitions, args), getRight().evalWith(definitions, args));
	}

	/**
	 * Actually computes the result for concrete values.
	 */
	public final Object compute(Object leftResult, Object rightResult) {
		return evalPotentialFlatMap(null, leftResult, rightResult);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object leftResult, Object rightResult) {
		if (_op == Op.ADD && (leftResult instanceof CharSequence || rightResult instanceof CharSequence)) {
			return addString(leftResult, rightResult);
		}

		if (leftResult == null) {
			return null;
		} else if (rightResult == null) {
			return null;
		}

		double left = asDouble(leftResult);
		double right = asDouble(rightResult);
		switch (_op) {
			case ADD:
				return left + right;
			case SUB:
				return left - right;
			case MUL:
				return left * right;
			case DIV:
				return left / right;
			case MOD:
				return left % right;
		}

		throw new UnreachableAssertion("No such operation: " + _op);
	}

	private Object addString(Object left, Object right) {
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return ToString.toString(left) + ToString.toString(right);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitArithmetic(this, arg);
	}


}
