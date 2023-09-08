/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ModelBinding} operating on a transient {@link TLModel} creating transient
 * {@link TLObject}s.
 * 
 * @see ApplicationModelBinding Algorithm for creating persistent objects in the current
 *      application.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransientModelBinding extends AbstractModelBinding {

	/**
	 * Creates a {@link TransientModelBinding}.
	 *
	 * @param model
	 *        See {@link #getModel()}
	 */
	public TransientModelBinding(TLModel model) {
		super(model);
	}

	@Override
	public Object eval(Expr expr, Object... args) {
		return QueryExecutor
			.interpret(null, getModel(), SearchBuilder.toSearchExpression(getModel(), expr)).execute(args);
	}

	@Override
	protected TLObject createObject(TLClass type) {
		return TransientModelFactory.createTransientObject(type);
	}

}
