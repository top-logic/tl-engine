/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.uri;

import java.lang.reflect.Array;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.value.ComputedValue;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;
import com.top_logic.service.openapi.client.utils.ServiceMethodRegistryUtils;

/**
 * Produces a {@link CallBuilder} adding a query parameter.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class QueryArgument extends AbstractConfiguredInstance<QueryArgument.Config<?>> implements CallBuilderFactory {

	/**
	 * Configuration options for {@link QueryArgument}.
	 */
	@TagName("query-argument")
	public interface Config<I extends QueryArgument> extends CallBuilderFactory.Config<I> {
		/**
		 * Name of the query parameter to fill.
		 * 
		 * <p>
		 * If {@link #getValue()} is not specified, the value of the function argument with the same
		 * name is used.
		 * </p>
		 */
		String getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String name);

		/**
		 * Computation of the value to pass to as query argument.
		 */
		@ImplementationClassDefault(ComputedValue.class)
		PolymorphicConfiguration<? extends ValueProducerFactory> getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(PolymorphicConfiguration<? extends ValueProducerFactory> value);
	}

	/**
	 * Creates a {@link QueryArgument} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public QueryArgument(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public CallBuilder createRequestModifier(MethodSpec method) {
		String name = getConfig().getName();

		ValueProducer valueProducer = ValueProducerFactory.createValueProducer(method, name, getConfig().getValue());

		return new CallBuilder() {
			@Override
			public void buildUrl(UriBuilder builder, Call call) {
				Object value = valueProducer.getValue(call);
				if (value == null) {
					return;
				}
				if (value instanceof Iterable<?>) {
					for (Object element : ((Iterable<?>) value)) {
						appendSingleton(builder, element);
					}
				} else if (value.getClass().isArray()) {
					for (int index = 0, size = Array.getLength(value); index < size; index++) {
						appendSingleton(builder, Array.get(value, index));
					}
				} else {
					appendSingleton(builder, value);
				}
			}

			private void appendSingleton(UriBuilder builder, Object value) {
				builder.addParameter(name, ServiceMethodRegistryUtils.serializeArgument(value));
			}
		};
	}


}
