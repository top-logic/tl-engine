/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call;

import java.util.List;

import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;

/**
 * Properties of a dynamic TL-Script function definition.
 * 
 * @see CallBuilderFactory#createRequestModifier(MethodSpec)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MethodSpec {

	/**
	 * The defined parameter names.
	 */
	List<String> getParameterNames();

	/**
	 * Index of the parameter with the given name.
	 * 
	 * @see Call#getArgument(int)
	 */
	int getParameterIndex(String name);

	/**
	 * Creates a {@link ValueProducer} returning the argument passed to the paramter with the given
	 * name.
	 */
	default ValueProducer createParameterLookup(String parameterName) {
		int index = getParameterIndex(parameterName);
		return new ValueProducer() {
			@Override
			public Object getValue(Call call) {
				return call.getArgument(index);
			}
		};
	}

}
