/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ModelProvider} that can be parameterized with a TL-Script expression.
 * 
 * @see Config#getExpr()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ModelProviderByExpression extends AbstractConfiguredInstance<ModelProviderByExpression.Config<?>>
		implements ModelProvider {

	private QueryExecutor _provider;

	/**
	 * Configuration options for {@link ModelProviderByExpression}.
	 */
	@TagName("script")
	public interface Config<I extends ModelProviderByExpression>
			extends PolymorphicConfiguration<ModelProviderByExpression> {
		/**
		 * The expression to evaluate.
		 * 
		 * <p>
		 * The resulting value is used as (initial) component model of the component using this
		 * provider.
		 * </p>
		 */
		@ItemDefault(Expr.Null.class)
		Expr getExpr();
	}

	/**
	 * Creates a {@link ModelProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModelProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_provider = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return _provider.execute();
	}

}
