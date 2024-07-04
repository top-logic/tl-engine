/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.structure.embedd.URLProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * 
 */
public class UrlProviderByExpression extends AbstractConfiguredInstance<UrlProviderByExpression.Config<?>>
		implements URLProvider {

	/**
	 * Configuration options for {@link UrlProviderByExpression}.
	 */
	public interface Config<I extends UrlProviderByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * The component channel to take the argument for the URL creation from.
		 */
		ModelSpec getModel();

		/**
		 * Function constructing the URL to open.
		 * 
		 * <p>
		 * The function receives either the value of the component channel, or the context model if
		 * no other model was specified as argument.
		 * </p>
		 */
		@Mandatory
		Expr getUrl();

	}

	private QueryExecutor _url;

	private ChannelLinking _target;

	/**
	 * Creates a {@link UrlProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UrlProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);
		_url = QueryExecutor.compile(config.getUrl());
		_target = context.getInstance(config.getModel());
	}

	@Override
	public String getUrl(DisplayContext context, LayoutComponent component, Object model) {
		Object arg = _target == null ? model : _target.eval(component);

		return (String) _url.execute(arg);
	}

}
