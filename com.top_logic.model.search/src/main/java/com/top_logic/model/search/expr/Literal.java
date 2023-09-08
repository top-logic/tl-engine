/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing a literal value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Literal extends SearchExpression {

	private Object _value;

	/**
	 * Creates a {@link Literal}.
	 * 
	 * @param value
	 *        See {@link #getValue()}.
	 */
	Literal(Object value) {
		_value = value;
	}

	/**
	 * The literal value object.
	 */
	public Object getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(Object value) {
		_value = value;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitLiteral(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return _value;
	}

}