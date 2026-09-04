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
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}. An import of external data normally passes
	 *        <code>false</code>.
	 */
	public TransientModelBinding(TLModel model, boolean usesSecurity) {
		super(model, usesSecurity);
	}

	/**
	 * @implNote Like {@link ApplicationModelBinding#eval(Expr, Object...)}, the result is an
	 *           intermediate value of the import and is therefore not filtered for the importing
	 *           user's read rights.
	 */
	@Override
	public Object eval(Expr expr, Object... args) {
		QueryExecutor executor =
			QueryExecutor.interpret(null, getModel(), SearchBuilder.toSearchExpression(getModel(), expr));
		if (!usesSecurity()) {
			// Definer's rights: the expression itself is evaluated without a check.
			executor.disableSecurity();
		}
		// Independent of that decision, the result of an import step is an intermediate value of the
		// import and must never be filtered for read access - only a value handed to a user is. With
		// the security switched off, the executor would not filter anyway; the unconditional call
		// keeps the invariant independent of the setting.
		return executor.executeIntermediate(args);
	}

	@Override
	protected TLObject createObject(TLClass type) {
		return TransientModelFactory.createTransientObject(type);
	}

}
