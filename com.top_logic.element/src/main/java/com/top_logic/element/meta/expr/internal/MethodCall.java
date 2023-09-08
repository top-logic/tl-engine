/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link AttributeValueLocator} calling a business method declared on the target object itself.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MethodCall extends CustomSingleSourceValueLocator implements ConfiguredInstance<MethodCall.Config> {

	/**
	 * Configuration options for {@link MethodCall}.
	 */
	@TagName("method-call")
	public interface Config extends PolymorphicConfiguration<MethodCall> {

		/**
		 * @see #getMethod()
		 */
		String METHOD = "method";

		/**
		 * Name of the no-arg method to invoke.
		 * 
		 * <p>
		 * To allow parsing the configuration, even if the referenced class does not yet exist, the
		 * {@link #getMethod()} is only optionally resolved.
		 * </p>
		 */
		@Name(METHOD)
		@Mandatory
		MethodRef getMethod();

		/**
		 * @see #getMethod()
		 */
		void setMethod(MethodRef methodName);

	}

	private static final Object[] NO_ARGS = {};

	private final Method _method;

	private final Config _config;

	/**
	 * Creates a {@link MethodCall} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MethodCall(InstantiationContext context, Config config) {
		_config = config;

		Method method;
		try {
			method = config.getMethod().resolve();
		} catch (ConfigurationException ex) {
			method = null;
			context.info("Method cannot be resolved: " + config.getMethod(), InstantiationContext.WARN);
		}
		_method = method;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object internalLocateAttributeValue(Object obj) {
		if (_method == null) {
			throw new KnowledgeBaseRuntimeException("Invalid method reference: " + _config.getMethod());
		}

		if (!_method.getDeclaringClass().isInstance(obj)) {
			return null;
		}

		try {
			return _method.invoke(obj, NO_ARGS);
		} catch (IllegalAccessException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	/**
	 * Creates a {@link MethodCall} configuration.
	 * 
	 * @param className
	 *        The class declaring the method.
	 * @param methodName
	 *        Name of the method.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(String className,
			String methodName) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setMethod(new MethodRef(className, methodName));
		return config;
	}

}
