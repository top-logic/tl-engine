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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.form.component.ValueTransformation;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ValueTransformation} that can be implemented in TL-Script.
 * 
 * <p>
 * The transformation can receive additional arguments from configured component channels.
 * </p>
 */
@Label("TL-Script transformation")
@InApp
public class ValueTransformationByExpression
		extends AbstractConfiguredInstance<ValueTransformationByExpression.Config<?>> implements ValueTransformation {

	/**
	 * Configuration options for {@link ValueTransformationByExpression}.
	 */
	public interface Config<I extends ValueTransformationByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * Additional arguments for the given {@link #getFunction()}.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends DirectLinking>> getAditionalArguments();

		/**
		 * The transformation to apply to the input value and potentially given additional
		 * arguments.
		 * 
		 * <p>
		 * The function receives the value of the input as first argument and for each configured
		 * additional argument another value.
		 * </p>
		 */
		@Mandatory
		Expr getFunction();
	}

	private List<ChannelLinking> _args;

	private QueryExecutor _fun;

	/**
	 * Creates a {@link ValueTransformationByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ValueTransformationByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_args = TypedConfiguration.getInstanceList(context, config.getAditionalArguments());
		_fun = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Object transform(LayoutComponent component, Object model) {
		Args args = Args.none();
		for (int n = _args.size() - 1; n >= 0; n--) {
			args = Args.cons(_args.get(n).eval(component), args);
		}
		args = Args.cons(model, args);
		return _fun.executeWith(args);
	}

}
