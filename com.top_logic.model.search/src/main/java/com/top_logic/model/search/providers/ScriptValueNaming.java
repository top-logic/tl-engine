/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ModelNamingScheme} retrieving a value by executing a TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptValueNaming extends ModelNamingScheme<Object, Object, ScriptValueNaming.Name> {

	/**
	 * Configuration options for {@link ScriptValueNaming}.
	 */
	public interface Name extends ModelName {
		/**
		 * The expression to evaluate to retrieve the value.
		 */
		Expr getExpr();
	}

	/**
	 * Creates a {@link ScriptValueNaming}.
	 */
	public ScriptValueNaming() {
		super(Object.class, Name.class, Object.class);
	}

	@Override
	public Object locateModel(ActionContext context, Object valueContext, Name name) {
		return QueryExecutor.compile(name.getExpr()).execute();
	}

	@Override
	protected Maybe<Name> buildName(Object valueContext, Object model) {
		return Maybe.none();
	}
}
