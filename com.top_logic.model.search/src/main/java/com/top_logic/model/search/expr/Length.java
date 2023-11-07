/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} computing the length of a {@link String}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Length extends UnaryOperation {

	/**
	 * Creates a {@link Length}.
	 */
	public Length(SearchExpression string) {
		super(string);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return compute(getArgument().evalWith(definitions, args));
	}

	/**
	 * Performs the computation on concrete arguments.
	 */
	public final Object compute(Object value) {
		if (value == null) {
			// Identifiy null and the emtpy string.
			return toNumber(0);
		}
		if (!(value instanceof CharSequence)) {
			// Cannot compute length.
			return null;
		}
		return toNumber(((CharSequence) value).length());
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitLength(this, arg);
	}

}
