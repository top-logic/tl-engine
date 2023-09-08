/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.service.openapi.client.registry.conf.InParameterContext;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Factory for {@link ValueProducer}s in a given context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueProducerFactory {

	/**
	 * Configuration options for {@link ParameterValue}.
	 */
	public interface Config<I extends ValueProducerFactory> extends PolymorphicConfiguration<I>, InParameterContext {
		// Pure sum interface.
	}

	/**
	 * Builds the {@link ValueProducer}.
	 * 
	 * @param method
	 *        The dynamic TL-Script function definition.
	 */
	ValueProducer build(MethodSpec method);

	/**
	 * Creates the {@link ValueProducer} specified by the given configuration.
	 * 
	 * <p>
	 * If no configuration is given, a parameter lookup for the argument of the parameter with the
	 * given name is created.
	 * </p>
	 */
	static ValueProducer createValueProducer(MethodSpec method, String argumentName,
			PolymorphicConfiguration<? extends ValueProducerFactory> valueConfig) {
		if (valueConfig == null) {
			return method.createParameterLookup(argumentName);
		} else {
			ValueProducerFactory builder = TypedConfigUtil.createInstance(valueConfig);
			return builder.build(method);
		}
	}

}
