/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import java.util.IdentityHashMap;
import java.util.Map;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link ModelBinding} of a live {@link TLModel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ApplicationModelBinding extends AbstractModelBinding {

	final KnowledgeBase _kb;

	private final Map<Expr, QueryExecutor> _compiledExprs = new IdentityHashMap<>();

	/**
	 * Creates a {@link ApplicationModelBinding}.
	 */
	public ApplicationModelBinding(KnowledgeBase kb, TLModel model) {
		super(model);
		_kb = kb;
	}

	@Override
	public Object eval(Expr expr, Object... args) {
		return compile(expr).execute(args);
	}

	private QueryExecutor compile(Expr expr) {
		QueryExecutor result = _compiledExprs.get(expr);
		if (result == null) {
			result = QueryExecutor.compile(_kb, _model, expr);
			_compiledExprs.put(expr, result);
		}
		return result;
	}

	@Override
	protected TLObject createObject(TLClass type) {
		return ModelService.getInstance().getFactory().createObject(type, null, null);
	}

}