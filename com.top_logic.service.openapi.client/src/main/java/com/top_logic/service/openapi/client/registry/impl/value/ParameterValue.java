/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import java.util.Collections;

import org.apache.hc.core5.http.ClassicHttpResponse;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseExtractor;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseExtractorFactory;

/**
 * Creates a {@link ValueProducer} accessing the function parameter with a certain name.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParameterValue extends AbstractConfiguredInstance<ParameterValue.Config<?>>
		implements ValueProducerFactory, ResponseExtractorFactory {

	/**
	 * Configuration options for {@link ParameterValue}.
	 */
	@TagName("parameter-value")
	public interface Config<I extends ParameterValue> extends ValueProducerFactory.Config<I> {

		/**
		 * Name of the function parameter to take the value from.
		 */
		@Name("parameter")
		@Mandatory
		@Options(fun = OptionsFromProperty.class, args = @Ref(PARAMETER_NAMES))
		String getParameter();

		/**
		 * Setter for {@link #getParameter()}.
		 */
		void setParameter(String value);

		/**
		 * Fetches property options from another property value passed as argument.
		 */
		class OptionsFromProperty extends Function1<Iterable<?>, Object> {
			@Override
			public Iterable<?> apply(Object arg) {
				if (arg == null) {
					return Collections.emptyList();
				}
				if (arg instanceof Iterable<?>) {
					return (Iterable<?>) arg;
				} else {
					return Collections.singletonList(arg);
				}
			}
		}
	}

	/**
	 * Creates a {@link ParameterValue} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ParameterValue(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ValueProducer build(MethodSpec method) {
		return method.createParameterLookup(getConfig().getParameter());
	}

	@Override
	public ResponseExtractor newExtractor(MethodSpec method) {
		int index = method.getParameterIndex(getConfig().getParameter());
		return new ResponseExtractor() {
			
			@Override
			public Object extract(Call call, ClassicHttpResponse response) {
				return call.getArgument(index);
			}
		};
	}

}
