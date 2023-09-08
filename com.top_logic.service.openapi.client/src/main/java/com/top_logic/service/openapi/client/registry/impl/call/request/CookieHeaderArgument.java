/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.request;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.builders.HeaderBuilder;
import com.top_logic.service.openapi.client.registry.impl.value.ParameterValue;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;

/**
 * Forwards a function parameter directly to a cookie value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CookieHeaderArgument extends HeaderArgument {

	/**
	 * Configuration options for {@link CookieHeaderArgument}.
	 */
	@TagName("parameter-as-cookie-header")
	public interface Config<I extends CookieHeaderArgument> extends HeaderArgument.Config<I> {

		/**
		 * Name of the function parameter and cookie name to pass the value to.
		 */
		@Options(fun = ParameterValue.Config.OptionsFromProperty.class, args = @Ref(PARAMETER_NAMES))
		@Override
		String getName();

		@Hidden
		@Override
		PolymorphicConfiguration<? extends ValueProducerFactory> getValue();

	}

	/**
	 * Creates a {@link CookieHeaderArgument}.
	 */
	public CookieHeaderArgument(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected CallBuilder callBuilder(String name, ValueProducer valueProducer) {
		return HeaderBuilder.cookieBuilder(name, valueProducer);
	}

}
