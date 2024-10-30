/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Decides whether inner UI actions are executed by evaluating a TL-Script function.
 */
@InApp
public class ConditionalActions extends AbstractConfiguredInstance<ConditionalActions.Config<?>>
		implements PostCreateAction {

	private final List<ChannelLinking> _args;

	private final QueryExecutor _condition;

	private final List<PostCreateAction> _actions;

	/**
	 * Configuration options for {@link ConditionalActions}.
	 */
	@TagName(Config.TAG_NAME)
	@DisplayInherited(DisplayStrategy.APPEND)
	public interface Config<I extends ConditionalActions>
			extends PolymorphicConfiguration<I>, WithPostCreateActions.Config {

		/**
		 * Short-cut tag name for configuring a {@link ConditionalActions} action.
		 */
		String TAG_NAME = "conditional-actions";

		/**
		 * Additional arguments for the given {@link #getCondition()}.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends DirectLinking>> getAditionalArguments();

		/**
		 * Function taking the command result and deciding whether to execute the inner actions.
		 * 
		 * <p>
		 * A function is expected taking the command result as first argument followed by other
		 * potentially configured arguments. Only if the function returns <code>true</code>, the
		 * inner actions are executed.
		 * </p>
		 */
		@Mandatory
		Expr getCondition();
	}

	/**
	 * Creates a {@link ConditionalActions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConditionalActions(InstantiationContext context, Config<?> config) {
		super(context, config);
		_args = TypedConfiguration.getInstanceList(context, config.getAditionalArguments());
		_condition = QueryExecutor.compile(config.getCondition());
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public void handleNew(LayoutComponent component, Object newModel) {
		Args args = Args.none();
		for (int n = _args.size() - 1; n >= 0; n--) {
			args = Args.cons(_args.get(n).eval(component), args);
		}
		args = Args.cons(newModel, args);
		Object decision = _condition.executeWith(args);
		if (SearchExpression.isTrue(decision)) {
			WithPostCreateActions.processCreateActions(_actions, component, newModel);
		}
	}
}
