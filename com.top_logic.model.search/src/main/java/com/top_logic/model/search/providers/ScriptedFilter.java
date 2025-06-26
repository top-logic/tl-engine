/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link Filter} that can be configured dynamically using {@link Expr TL-Script}.
 * 
 * @see FilterByExpression
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ScriptedFilter extends AbstractConfiguredInstance<ScriptedFilter.Config<?>> implements Filter<Object> {

	private QueryExecutor _function;

	/**
	 * Configuration options for {@link ScriptedFilter}.
	 */
	@TagName("filter-by-expression")
	public interface Config<I extends ScriptedFilter> extends PolymorphicConfiguration<I> {

		/**
		 * The function deciding whether the argument is accepted.
		 */
		Expr getPredicate();

	}

	/**
	 * Creates a {@link ScriptedFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptedFilter(InstantiationContext context, Config<?> config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getPredicate());
	}

	@Override
	public boolean accept(Object value) {
		return SearchExpression.asBoolean(_function.execute(value));
	}

}
