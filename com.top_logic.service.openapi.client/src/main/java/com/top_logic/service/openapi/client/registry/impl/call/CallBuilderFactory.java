/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.service.openapi.client.registry.conf.InParameterContext;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;

/**
 * Factory for {@link CallBuilder}s.
 * 
 * @see MethodDefinition#getCallBuilders()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CallBuilderFactory {

	/**
	 * Base configuration for {@link CallBuilderFactory} implementations.
	 */
	public interface Config<T> extends PolymorphicConfiguration<T>, InParameterContext {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link CallBuilder} with the given method definition context.
	 */
	CallBuilder createRequestModifier(MethodSpec method);
	

}
