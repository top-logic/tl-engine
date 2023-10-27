/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.setting.OutputProducer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link OutputProducer} that can be implemented with TL-Script.
 */
@Label("TL-Script output")
public class OutputProducerByExpression extends AbstractConfiguredInstance<OutputProducerByExpression.Config<?>>
		implements OutputProducer {

	/**
	 * Configuration options for {@link OutputProducerByExpression}.
	 */
	public interface Config<I extends OutputProducerByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * The function computing the new output value.
		 * 
		 * <p>
		 * The function takes the command's model as single input and is expected to return the new
		 * output value that should be published to the <code>output</code> channel..
		 * </p>
		 */
		@NonNullable
		@FormattedDefault("model -> $model.copy()")
		Expr getFunction();
	}

	private final QueryExecutor _function;

	/**
	 * Creates a {@link OutputProducerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OutputProducerByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Object createOutput(DisplayContext context, LayoutComponent component, Object model) {
		return _function.execute(model);
	}

}
