/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;

/**
 * Base class for request parameter parsers.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class RequestParameter<C extends RequestParameter.Config<?>> extends AbstractConfiguredInstance<C> {

	/**
	 * Configuration options of {@link RequestParameter}.
	 */
	@Abstract
	public interface Config<I extends RequestParameter<?>> extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Regular expression, that a variable name must match. */
		String VARIABLE_NAME_PATTERN = "[\\w\\-]+";

		@Override
		@Hidden
		Class<? extends I> getImplementationClass();

		/**
		 * The name of the parameter.
		 * 
		 * <p>
		 * Parameter names are case sensitive. The name of the parameter determines its name in the
		 * API (e.g. in case of a query argument parameter) and its name to access it in the service
		 * method implementation.
		 * </p>
		 */
		@Override
		@RegexpConstraint(VARIABLE_NAME_PATTERN)
		String getName();

		/**
		 * Resolves the concrete parameter configuration.
		 * 
		 * @param globalParams
		 *        Globally defined parameters.
		 * @return A concrete request parameter, i.e. not a reference to a parameter.
		 */
		default ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> resolveParameter(
				Map<String, ReferencedParameter> globalParams) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@link Function} that resolves the given {@link RequestParameter.Config parameter
		 * configuration}.
		 * 
		 * @param globalParams
		 *        Globally defined parameters.
		 * 
		 * @see #resolveParameter(Map)
		 */
		static Function<RequestParameter.Config<?>, ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>>> resolveUsing(
				Map<String, ReferencedParameter> globalParams) {
			return config -> config.resolveParameter(globalParams);
		}

	}

	/**
	 * Creates a {@link RequestParameter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RequestParameter(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The name of this parameter.
	 */
	public String getName() {
		return getConfig().getName();
	}

}
