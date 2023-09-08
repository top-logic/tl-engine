/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link UnaryOperation} for flattening a collection of collections.
 * 
 * <p>
 * By flattening a collection of collections, a new collection is built that contains each element
 * of all collections in the argument collection only once.
 * </p>
 * 
 * <p>
 * The {@link #getArgument()} is expected to be a collection of collections. The result is a
 * collection with the element type of the element collections of the argument.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Flatten extends UnaryOperation {

	/**
	 * Creates a {@link Flatten}.
	 * 
	 * @param arg
	 *        See {@link #getArgument()}.
	 */
	Flatten(SearchExpression arg) {
		super(arg);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitFlatten(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		List<Object> result = new ArrayList<>();
		Collection<?> sets = asCollection(getArgument().evalWith(definitions, args));
		for (Object element : sets) {
			result.addAll(asCollection(element));
		}
		return result;
	}

}