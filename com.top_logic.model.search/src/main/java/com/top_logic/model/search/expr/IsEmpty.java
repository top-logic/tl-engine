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
 * {@link UnaryOperation} deciding whether a value is considered empty.
 * 
 * <p>
 * The {@link #getArgument()} is considered to be of any type. The result is {@link Boolean}. A
 * value is considered empty, if it is <code>null</code>, the empty collection, or the empty string.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsEmpty extends UnaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link IsEmpty}.
	 * 
	 * @param arg
	 *        See {@link #getArgument()}.
	 */
	IsEmpty(SearchExpression arg) {
		super(arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object value = getArgument().evalWith(definitions, args);
		return compute(value);
	}

	/**
	 * Computes the result based on concrete values.
	 */
	public final Object compute(Object value) {
		return isNull(value) || isEmptyCollection(value) || isEmptyString(value);
	}

	private static boolean isNull(Object value) {
		return value == null;
	}

	private static boolean isEmptyCollection(Object value) {
		return (value instanceof Collection<?>) && ((Collection<?>) value).isEmpty();
	}

	private static boolean isEmptyString(Object value) {
		return (value instanceof String) && ((String) value).isEmpty();
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitIsEmpty(this, arg);
	}

}