/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.model.ModelService;

/**
 * {@link QueryExecutor} that lazily compiles its expression.
 * 
 * <p>
 * Used in situations, where an {@link QueryExecutor} is required during construction but the
 * application is not (yet) started.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DeferredQueryExecutor extends QueryExecutor {

	private final Expr _expr;

	private QueryExecutor _executor;

	/**
	 * Creates a {@link DeferredQueryExecutor}.
	 */
	public DeferredQueryExecutor(Expr expr) {
		_expr = expr;
	}

	QueryExecutor executor() {
		if (_executor == null) {
			_executor = QueryExecutor.compile(getKnowledgeBase(), getTLModel(), _expr);
		}
		return _executor;
	}

	@Override
	protected KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	protected TLModel getTLModel() {
		return ModelService.getApplicationModel();
	}

	@Override
	public SearchExpression getSearch() {
		return executor().getSearch();
	}

	@Override
	public Object executeWith(EvalContext definitions, Args args) {
		return executor().executeWith(definitions, args);
	}

}
