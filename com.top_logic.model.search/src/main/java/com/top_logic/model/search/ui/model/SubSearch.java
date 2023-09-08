/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * {@link SearchPart}s that represent a complete search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SubSearch extends SearchPart {

	/**
	 * Visits this {@link SubSearch} using the given {@link SubSearchVisitor}.
	 */
	<R, A> R visitSubSearch(SubSearchVisitor<R, A> v, A arg);

	/**
	 * Visitor interface for {@link SubSearch} parts.
	 * 
	 * @param <R>
	 *        The result type of the visit.
	 * @param <A>
	 *        The argument type for the visit.
	 */
	interface SubSearchVisitor<R, A> {

		/** Visit case for {@link Search} */
		R visit(Search search, A args);

		/**
		 * Visit case for {@link AbstractTypeSearch}
		 */
		R visit(AbstractTypeSearch search, A args);

		/**
		 * Visit case for {@link TupleSearch}
		 */
		R visit(TupleSearch search, A args);

		/**
		 * Visit case for {@link UnionSearch}
		 */
		R visit(UnionSearch search, A args);
	}

}
