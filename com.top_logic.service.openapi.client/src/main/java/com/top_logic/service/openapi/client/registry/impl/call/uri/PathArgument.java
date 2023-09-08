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
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.call.request.HeaderArgument;
import com.top_logic.service.openapi.client.registry.impl.value.ParameterValue;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;
import com.top_logic.service.openapi.client.utils.ServiceMethodRegistryUtils;

/**
 * Produces a {@link CallBuilder} adding a path element.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PathArgument extends AbstractConfiguredInstance<PathArgument.Config<?>> implements CallBuilderFactory {

	/**
	 * Configuration options for {@link HeaderArgument}.
	 */
	@TagName("path-argument")
	public interface Config<I extends PathArgument> extends CallBuilderFactory.Config<I> {

		/**
		 * Computation of the value to pass as path element.
		 */
		@ImplementationClassDefault(ParameterValue.class)
		@ItemDefault
		@NonNullable
		PolymorphicConfiguration<? extends ValueProducerFactory> getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(PolymorphicConfiguration<? extends ValueProducerFactory> value);
	}

	/**
	 * Creates a {@link PathArgument} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PathArgument(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public CallBuilder createRequestModifier(MethodSpec method) {
		ValueProducerFactory valueBuilder = TypedConfigUtil.createInstance(getConfig().getValue());
		ValueProducer valueProducer = valueBuilder.build(method);
		
		return new CallBuilder() {
			@Override
			public void buildUrl(UriBuilder builder, Call call) {
				appendValuePath(builder, valueProducer.getValue(call));
			}

			private void appendValuePath(UriBuilder builder, Object value) {
				if (value == null) {
					return;
				}
				if (value instanceof Iterable<?>) {
					for (Object element : ((Iterable<?>) value)) {
						appendPathElement(builder, element);
					}
				} else if (value.getClass().isArray()) {
					for (int index = 0, size = Array.getLength(value); index < size; index++) {
						appendPathElement(builder, Array.get(value, index));
					}
				} else {
					appendPathElement(builder, value);
				}
			}

			private void appendPathElement(UriBuilder builder, Object value) {
				builder.appendPathElement(ServiceMethodRegistryUtils.serializeArgument(value));
			}

		};
	}

}
