/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * Base class with utility methods for descending {@link Visitor}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDescendingVisitor<R, A> implements Visitor<R, A> {

	/**
	 * Descends generically into all parts of a {@link SearchExpression} model element.
	 * 
	 * @param expr
	 *        The {@link SearchExpression} being visited.
	 * @param arg
	 *        The current argument to the visit.
	 * @param parts
	 *        All {@link SearchExpression} parts of the currently visited expression. These parts
	 *        are visited.
	 * @return The results returned from descending into the parts.
	 */
	protected List<R> descendParts(SearchExpression expr, A arg, SearchExpression... parts) {
		return descendParts(newResult(parts), expr, arg, parts);
	}

	/**
	 * Descends into all given parts and appends the results to the given list.
	 *
	 * @param result
	 *        The result to build.
	 * @param expr
	 *        The {@link SearchExpression} being visited.
	 * @param arg
	 *        The current argument to the visit.
	 * @param parts
	 *        All {@link SearchExpression} parts of the currently visited expression. These parts
	 *        are visited.
	 * @return The given result list.
	 */
	protected final List<R> descendParts(List<R> result, SearchExpression expr, A arg, SearchExpression... parts) {
		for (int n = 0; n < parts.length; n++) {
			R descendResult = descendPart(expr, arg, parts[n]);
			result.add(descendResult);
		}
		return result;
	}

	/**
	 * Allocates the result array for
	 * {@link #descendParts(SearchExpression, Object, SearchExpression...)}.
	 */
	protected final List<R> newResult(SearchExpression... parts) {
		return newResult(parts.length);
	}

	/**
	 * Allocates the result array for
	 * {@link #descendParts(SearchExpression, Object, SearchExpression...)}.
	 */
	protected final List<R> newResult(int length) {
		return new ArrayList<>(length);
	}

	/**
	 * Descends into a single part of a {@link SearchExpression} model element.
	 * 
	 * @param expr
	 *        The {@link SearchExpression} being visited.
	 * @param arg
	 *        The current argument to the visit.
	 * @param part
	 *        The {@link SearchExpression} part of the currently visited expression that is being
	 *        descended.
	 * @return The result returned from descending into the actual part.
	 */
	protected R descendPart(SearchExpression expr, A arg, SearchExpression part) {
		return part == null ? null : part.visit(this, arg);
	}

}
