/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * The default {@link QueryExecutor} implementation that contains a pre-compiled query.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DirectQueryExecutor extends QueryExecutor {

	private final KnowledgeBase _knowledgeBase;

	private final TLModel _tlModel;

	private final SearchExpression _search;

	/**
	 * Creates a {@link DirectQueryExecutor}.
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} for which the {@link SearchExpression} was compiled. Is
	 *        allowed to be null, if the {@link SearchExpression} won't use the
	 *        {@link KnowledgeBase}.
	 * @param tlModel
	 *        The {@link TLModel} for which the {@link SearchExpression} was compiled.
	 */
	public DirectQueryExecutor(KnowledgeBase knowledgeBase, TLModel tlModel, SearchExpression expr) {
		_knowledgeBase = knowledgeBase;
		_tlModel = tlModel;
		_search = expr;
	}

	@Override
	protected KnowledgeBase getKnowledgeBase() {
		return _knowledgeBase;
	}

	@Override
	protected TLModel getTLModel() {
		return _tlModel;
	}

	@Override
	public SearchExpression getSearch() {
		return _search;
	}

	/**
	 * Executes the expression with the given arguments.
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	@Override
	public Object executeWith(EvalContext definitions, Args args) {
		return getSearch().evalWith(definitions, args);
	}

}
