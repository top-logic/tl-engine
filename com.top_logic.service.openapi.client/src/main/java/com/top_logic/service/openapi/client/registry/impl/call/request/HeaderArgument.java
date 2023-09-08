/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.request;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.call.builders.HeaderBuilder;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;

/**
 * Produces a {@link CallBuilder} creating a request header value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HeaderArgument extends AbstractConfiguredInstance<HeaderArgument.Config<?>> implements CallBuilderFactory {

	/**
	 * Configuration options for {@link HeaderArgument}.
	 */
	@TagName("header-argument")
	public interface Config<I extends HeaderArgument> extends CallBuilderFactory.Config<I>, NamedConfigMandatory {

		/**
		 * Name of the header.
		 * 
		 * <p>
		 * If no {@link #getValue()} is specified, the value of the function argument with the same
		 * name is used.
		 * </p>
		 */
		@Override
		String getName();

		/**
		 * Specification of the value to pass as request header value.
		 */
		PolymorphicConfiguration<? extends ValueProducerFactory> getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(PolymorphicConfiguration<? extends ValueProducerFactory> value);
	}

	/**
	 * Creates a {@link HeaderArgument} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HeaderArgument(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public CallBuilder createRequestModifier(MethodSpec method) {
		String name = getConfig().getName();
		ValueProducer valueProducer = ValueProducerFactory.createValueProducer(method, name, getConfig().getValue());
		return callBuilder(name, valueProducer);
	}

	/**
	 * Creates the actual {@link CallBuilder}.
	 * 
	 * @param name
	 *        Configured header name.
	 * @param valueProducer
	 *        Producer creating the header value.
	 * 
	 * @see #createRequestModifier(MethodSpec)
	 */
	protected CallBuilder callBuilder(String name, ValueProducer valueProducer) {
		return new HeaderBuilder(name, valueProducer);
	}

}
