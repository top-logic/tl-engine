/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Creates a {@link ValueProducer} using a constant configured value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantValue extends AbstractConfiguredInstance<ConstantValue.Config<?>>
		implements ValueProducerFactory {

	/**
	 * Configuration options for {@link ConstantValue}.
	 */
	@TagName("constant-value")
	public interface Config<I extends ConstantValue> extends ValueProducerFactory.Config<I> {

		/**
		 * Value to use.
		 */
		@Name("value")
		@Mandatory
		String getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(String value);

	}

	/**
	 * Creates a {@link ConstantValue} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstantValue(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ValueProducer build(MethodSpec method) {
		return new ValueProducer() {

			@Override
			public Object getValue(Call call) {
				return getConfig().getValue();
			}
		};
	}

}
