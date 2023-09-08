/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.function.Function;

import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.selector.SearchAndReportConfig;
import com.top_logic.model.search.ui.selector.SearchType;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Editor to create {@link SearchExpression}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface SearchExpressionEditor {

	/**
	 * Creates the {@link SearchExpression} and executes it using the given algorithm.
	 * 
	 * @param algorithm
	 *        Function that executes the created {@link SearchExpression}. The
	 *        {@link SearchExpression} may be <code>null</code>.
	 * @return Result of the search: When creating {@link SearchExpression} failed, the
	 *         {@link HandlerResult} holds the errors. Otherwise it is the result of the algorithm.
	 */
	HandlerResult search(Function<SearchExpression, HandlerResult> algorithm);

	/**
	 * Normal, if its a GUI Editor, otherwise expert.
	 */
	SearchType getType();
	
	/**
	 * Sets the editors model with the given search expression.
	 */
	void setFormModel(SearchExpressionImpl expressionWrapper, SearchAndReportConfig searchConfig);

	/**
	 * {@link SearchExpressionImpl} loaded in this editor.
	 */
	SearchExpressionImpl getLoadedPersistentSearchExpression();

	/**
	 * @see #getLoadedPersistentSearchExpression()
	 */
	void setLoadedPersistentSearchExpression(SearchExpressionImpl expressionWrapper);

}
