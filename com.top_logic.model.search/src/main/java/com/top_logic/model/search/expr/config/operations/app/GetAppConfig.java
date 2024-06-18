/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.app;

import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Function retrieving application configurations.
 */
public class GetAppConfig extends GenericMethod {

	/**
	 * Creates a {@link GetAppConfig}.
	 */
	protected GetAppConfig(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new GetAppConfig(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object serviceArg = arguments[0];
		Object configArg = arguments[1];

		if (serviceArg != null && configArg != null) {
			throw new TopLogicException(I18NConstants.ERROR_EITHER_SEVICE_OR_CONFIG);
		}

		if (serviceArg != null) {
			String serviceName = asString(serviceArg);
			Class<? extends ManagedClass> serviceClass;
			try {
				serviceClass = Class.forName(serviceName).asSubclass(ManagedClass.class);
			} catch (ClassNotFoundException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CONFIGURATION_TYPE__NAME.fill(serviceName), ex);
			} catch (ClassCastException ex) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_CONFIGURATION_TYPE__NAME_EXPECTED
					.fill(serviceName, ManagedClass.class.getName()), ex);
			}
			try {
				return ApplicationConfig.getInstance().getServiceConfiguration(serviceClass);
			} catch (ConfigurationException ex) {
				throw new TopLogicException(
					I18NConstants.ERROR_ACCESS_TO_CONFIGURATION_FAILED__NAME_MSG.fill(serviceName, ex.getMessage()),
					ex);
			}
		}
		if (configArg != null) {
			String configName = asString(configArg);
			Class<? extends ConfigurationItem> configClass;
			try {
				configClass = Class.forName(configName).asSubclass(ConfigurationItem.class);
			} catch (ClassNotFoundException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CONFIGURATION_TYPE__NAME.fill(configName), ex);
			} catch (ClassCastException ex) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_CONFIGURATION_TYPE__NAME_EXPECTED
					.fill(configName, ConfigurationItem.class.getName()), ex);
			}
			return ApplicationConfig.getInstance().getConfig(configClass);
		}

		throw new TopLogicException(I18NConstants.ERROR_EITHER_SEVICE_OR_CONFIG);
	}

	/**
	 * It may be that the {@link ServletContextService} is not yet started.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link GetAppConfig}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<GetAppConfig> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("service")
			.optional("config")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public GetAppConfig build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new GetAppConfig(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}
	}

}
