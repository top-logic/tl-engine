/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;
import java.util.Map;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} computing the size of a {@link Collection}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Size extends UnaryOperation {


	/**
	 * Creates a {@link Size}.
	 */
	public Size(SearchExpression list) {
		super(list);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return compute(getArgument().evalWith(definitions, args));
	}

	/**
	 * Performs the computation on concrete arguments.
	 */
	public final Object compute(Object list) {
		if (list == null) {
			// Identify null and the empty list.
			return 0;
		} else if (list instanceof Collection<?>) {
			return ((Collection<?>) list).size();
		} else if (list instanceof Map<?, ?>) {
			return ((Map<?, ?>) list).size();
		} else {
			// Identify an object and the singleton list containing only this object.
			return 1;
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitSize(this, arg);
	}

}
