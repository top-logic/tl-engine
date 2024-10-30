/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Performs all inner action on another model derived from the initial command result.
 */
@InApp
public class ActionsWithTransformedModel implements PostCreateAction {
	/**
	 * Configuration options for {@link ActionsWithTransformedModel}.
	 */
	@TagName(Config.TAG_NAME)
	@DisplayInherited(DisplayStrategy.APPEND)
	public interface Config extends PolymorphicConfiguration<ActionsWithTransformedModel>, WithPostCreateActions.Config {

		/**
		 * Short-cut tag name for configuring a {@link ConditionalActions} action.
		 */
		String TAG_NAME = "with-transformed-model";

		/**
		 * Additional arguments for the given {@link #getTransformation()}.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends DirectLinking>> getAditionalArguments();

		/**
		 * Function transforming the command result to be passed to inner actions.
		 * 
		 * <p>
		 * A function is expected taking the command result as first argument followed by other
		 * potentially configured arguments. The function result is passed as model to inner
		 * actions.
		 * </p>
		 */
		@Mandatory
		Expr getTransformation();
	}

	private final List<DirectLinking> _args;
	private final QueryExecutor _transformation;
	private final List<PostCreateAction> _actions;

	/**
	 * Creates a {@link ActionsWithTransformedModel}.
	 */
	public ActionsWithTransformedModel(InstantiationContext context, Config config) {
		_args = TypedConfiguration.getInstanceList(context, config.getAditionalArguments());
		_transformation = QueryExecutor.compile(config.getTransformation());
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public void handleNew(LayoutComponent component, Object newModel) {
		Args args = Args.none();
		for (int n = _args.size() - 1; n >= 0; n--) {
			args = Args.cons(_args.get(n).eval(component), args);
		}
		args = Args.cons(newModel, args);
		Object transformedModel = _transformation.executeWith(args);

		WithPostCreateActions.processCreateActions(_actions, component, transformedModel);
	}
}
