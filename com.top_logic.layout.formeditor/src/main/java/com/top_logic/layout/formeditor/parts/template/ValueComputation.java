/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link VariableDefinition} that computes the variable's value through a <i>TL-Script</i>
 * function.
 */
public class ValueComputation extends AbstractConfiguredInstance<ValueComputation.Config>
		implements VariableDefinition<ValueComputation.Config> {

	/**
	 * Definition of a local template variable.
	 */
	@DisplayOrder({
		Config.NAME,
		Config.FUNCTION,
	})
	@TagName("value-computation")
	public interface Config extends VariableDefinition.Config<ValueComputation> {
		/**
		 * @see #getFunction()
		 */
		String FUNCTION = "function";

		/**
		 * Function taking the currently rendered object as first argument and the additionally
		 * specified input model (if one is given) as second argument. The value computed can be
		 * accessed from the template through the variable with the given {@link #getName()}.
		 */
		@Name(FUNCTION)
		@Mandatory
		Expr getFunction();
	}

	private QueryExecutor _function;

	/**
	 * Creates a {@link ValueComputation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ValueComputation(InstantiationContext context, Config config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public EvalResult eval(DisplayContext displayContext, LayoutComponent component, Object model) {
		return new ResultValue(_function.execute(displayContext, null, model));
	}
}
