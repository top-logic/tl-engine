/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Visitor for {@link Function}s.
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type for the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FunctionVisitor<R, A> {

	/**
	 * Visit case for {@link CountFunction}.
	 */
	R visitCount(CountFunction fun, A arg);

	/**
	 * Visit case for {@link SumFunction}.
	 */
	R visitSum(SumFunction fun, A arg);

	/**
	 * Visit case for {@link MinFunction}.
	 */
	R visitMin(MinFunction fun, A arg);

	/**
	 * Visit case for {@link MaxFunction}.
	 */
	R visitMax(MaxFunction fun, A arg);
	
}
