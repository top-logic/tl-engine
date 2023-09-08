/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.model.search.expr.interpreter.SearchExpressionPart;

/**
 * Mix-in interface for {@link SearchExpressionPart}s that implement implicit flat-map-semantics.
 * 
 * @param <P>
 *        A parameter type for the evaluation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithFlatMapSemantics<P> extends SearchExpressionPart {

	/**
	 * Evaluates this expression on the base value.
	 * 
	 * <p>
	 * If base is a {@link Collection}, {@link #evalDirect(EvalContext, Object, Object) evaluate}
	 * the expression on all entries an return the collection of the results.
	 * </p>
	 *
	 * @param definitions
	 *        See {@link SearchExpression#eval(EvalContext)}.
	 * @param base
	 *        The base value to evaluate the expression on.
	 * @param param
	 *        A parameter as defined by the context.
	 * @return The evaluation result.
	 */
	default Object evalPotentialFlatMap(EvalContext definitions, Object base, P param) {
		if (base instanceof Collection<?>) {
			return evalFlatMap(definitions, (Collection<?>) base, param);
		} else {
			return evalDirect(definitions, base, param);
		}
	}

	/**
	 * Evaluates {@link #evalDirect(EvalContext, Object, Object)} on each entry of the base
	 * collection value.
	 */
	default Object evalFlatMap(EvalContext definitions, Collection<?> base, P param) {
		Collection<Object> result = new ArrayList<>();
		for (Object entry : base) {
			Object entryResult = evalDirect(definitions, entry, param);
			if (entryResult == null) {
				continue;
			}
			if (entryResult instanceof Collection<?>) {
				result.addAll((Collection<?>) entryResult);
			} else {
				result.add(entryResult);
			}
		}
		return result;
	}

	/**
	 * Evaluates this expression on the given singleton value.
	 */
	Object evalDirect(EvalContext definitions, Object singletonValue, P param);

}
