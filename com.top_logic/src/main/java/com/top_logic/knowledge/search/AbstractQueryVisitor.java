/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Visitor for {@link AbstractQueryPart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AbstractQueryVisitor<R, A> {

	/**
	 * Visits {@link HistoryQuery}s.
	 */
	R visitHistoryQuery(HistoryQuery expr, A arg);

	/**
	 * Visits {@link RevisionQuery}s.
	 */
	R visitRevisionQuery(RevisionQuery<?> expr, A arg);

	/**
	 * Visits {@link ParameterDeclaration}s.
	 */
	R visitParameterDeclaration(ParameterDeclaration expr, A arg);

}

