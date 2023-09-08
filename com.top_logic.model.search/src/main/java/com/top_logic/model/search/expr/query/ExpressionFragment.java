/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.model.ModelService;

/**
 * {@link HTMLFragment} rendering a {@link SearchExpression} producing HTML output as
 * side-effect.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ExpressionFragment implements HTMLFragment {
	private final SearchExpression _expr;

	/**
	 * Creates a {@link ExpressionFragment}.
	 *
	 * @param expr
	 *        See {@link #getExpr()}.
	 */
	public ExpressionFragment(SearchExpression expr) {
		_expr = expr;
	}

	/**
	 * The rendering expression creating output as side-effect.
	 */
	public SearchExpression getExpr() {
		return _expr;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		KnowledgeBase defaultKnowledgeBase = PersistencyLayer.getKnowledgeBase();
		TLModel defaultTLModel = ModelService.getApplicationModel();
		QueryExecutor executor = interpret(defaultKnowledgeBase, defaultTLModel, _expr);
		executor.executeWith(context, out, Args.none());
	}
}