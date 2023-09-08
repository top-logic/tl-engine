/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link UnaryOperation} representing the {@link Boolean} not operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Not extends UnaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link Not}.
	 * 
	 * @param arg
	 *        See {@link #getArgument()}.
	 */
	Not(SearchExpression arg) {
		super(arg);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitNot(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		boolean value = isTrue(getArgument().evalWith(definitions, args));
		return Boolean.valueOf(!value);
	}

}