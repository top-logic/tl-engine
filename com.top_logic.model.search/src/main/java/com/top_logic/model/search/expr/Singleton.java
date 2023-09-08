/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link UnaryOperation} promoting a value to a singleton set with this value as single element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Singleton extends UnaryOperation {

	/**
	 * Creates a {@link Singleton}.
	 * 
	 * @param arg
	 *        See {@link #getArgument()}.
	 */
	Singleton(SearchExpression arg) {
		super(arg);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitSingleton(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object value = getArgument().evalWith(definitions, args);
		return compute(value);
	}

	/**
	 * Computes result based on concrete values.
	 */
	public static Object compute(Object value) {
		return CollectionUtilShared.singletonOrEmptyList(value);
	}

}