/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link AbstractQueryVisitor} which implements Methods by delegating to new methods for common
 * super classes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultAbstractQueryVisitor<R, A> implements AbstractQueryVisitor<R, A> {

	@Override
	public R visitHistoryQuery(HistoryQuery expr, A arg) {
		return visitAbstractQuery(expr, arg);
	}

	@Override
	public R visitRevisionQuery(RevisionQuery<?> expr, A arg) {
		return visitAbstractQuery(expr, arg);
	}

	@Override
	public R visitParameterDeclaration(ParameterDeclaration expr, A arg) {
		return visitAbstractQueryPart(expr, arg);
	}

	/**
	 * Method to handle all subclasses of {@link AbstractQuery}.
	 * 
	 * @param expr
	 *        the expression to process
	 * @param arg
	 *        the argument of the specialised visit.
	 * 
	 * @return the return value for the specialised method. This implementation delegates to
	 *         {@link #visitAbstractQueryPart(AbstractQueryPart, Object)}
	 */
	protected R visitAbstractQuery(AbstractQuery<?> expr, A arg) {
		return visitAbstractQueryPart(expr, arg);
	}

	/**
	 * Method to handle all subclasses of {@link AbstractQueryPart}.
	 * 
	 * @param expr
	 *        the expression to process
	 * @param arg
	 *        the argument of the specialised visit.
	 * 
	 * @return the return value for the specialised method. This implementation returns
	 *         <code>null</code>.
	 */
	protected R visitAbstractQueryPart(AbstractQueryPart expr, A arg) {
		return null;
	}

}
