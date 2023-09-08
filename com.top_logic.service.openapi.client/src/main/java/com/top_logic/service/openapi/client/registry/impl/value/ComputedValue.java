/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Value computed from other function arguments.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComputedValue extends AbstractConfiguredInstance<ComputedValue.Config<?>>
		implements ValueProducerFactory {

	/**
	 * Configuration options for {@link ComputedValue}.
	 */
	@TagName("computed-value")
	public interface Config<I extends ComputedValue> extends ValueProducerFactory.Config<I> {

		/** Configuration name of {@link #getFunction()}. */
		String FUNCTION = "function";

		/** Configuration name of {@link #getArguments()}. */
		String ARGUMENTS = "arguments";

		/**
		 * Arguments passed to {@link #getFunction()}.
		 */
		@ImplementationClassDefault(ParameterValue.class)
		@Name(ARGUMENTS)
		List<PolymorphicConfiguration<? extends ParameterValue>> getArguments();

		/**
		 * Function computing the final value.
		 * 
		 * <p>
		 * The function is expected to take as many arguments as specified in
		 * {@link #getArguments()}.
		 * </p>
		 */
		@Mandatory
		@Name(FUNCTION)
		Expr getFunction();

		/**
		 * Setter for {@link #getFunction()}.
		 */
		void setFunction(Expr value);

	}

	/**
	 * Creates a {@link ComputedValue} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComputedValue(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ValueProducer build(MethodSpec method) {
		ValueProducer[] producers = new ValueProducer[getConfig().getArguments().size()];
		{
			int n = 0;
			for (PolymorphicConfiguration<? extends ValueProducerFactory> builderConfig : getConfig().getArguments()) {
				producers[n++] = TypedConfigUtil.createInstance(builderConfig).build(method);
			}
		}

		QueryExecutor function = QueryExecutor.compile(getConfig().getFunction());

		return new ValueProducer() {
			@Override
			public Object getValue(Call call) {
				int cnt = producers.length;
				Object[] arguments = new Object[cnt];
				for (int n = 0; n < cnt; n++) {
					arguments[n] = producers[n].getValue(call);
				}
				return function.execute(arguments);
			}
		};
	}

}
