/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configurable {@link ModelProvider} that calls a static method by Java reflection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticMethodModelProvider implements ModelProvider {

	/**
	 * Configuration options for {@link StaticMethodModelProvider}
	 */
	public interface Config extends PolymorphicConfiguration<ModelProvider> {
		@Override
		@ClassDefault(StaticMethodModelProvider.class)
		Class<? extends ModelProvider> getImplementationClass();

		/**
		 * The class to search the static method in.
		 */
		@Mandatory
		@Name("type")
		Class<?> getType();

		/**
		 * @see #getType()
		 */
		void setType(Class<?> value);

		/**
		 * Name of the static method to call on {@link #getType()}.
		 */
		@Mandatory
		@Name("method")
		String getMethod();

		/**
		 * @see #getMethod()
		 */
		void setMethod(String value);
	}

	private static final Class<?>[] NO_ARGS = {};
	private final Method _method;

	/**
	 * Creates a {@link StaticMethodModelProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StaticMethodModelProvider(InstantiationContext context, Config config) {
		Method method;
		try {
			method = config.getType().getMethod(config.getMethod(), NO_ARGS);

			int modifiers = method.getModifiers();
			if (!Modifier.isPublic(modifiers)) {
				context.error("Non-public method '" + config.getClass().getName() + "#" + config.getMethod()
					+ "()' in model provider: " + config.location());
			}
			if (!Modifier.isStatic(modifiers)) {
				context.error("Non-static method '" + config.getClass().getName() + "#" + config.getMethod()
					+ "()' in model provider: " + config.location());
			}
			if (method.getParameterTypes().length > 0) {
				context.error("Method with arguments '" + config.getClass().getName() + "#" + config.getMethod()
					+ "()' in model provider: " + config.location());
			}
		} catch (NoSuchMethodException ex) {
			context.error("Undefined method '" + config.getClass().getName() + "#" + config.getMethod()
				+ "()' in model provider: " + config.location(), ex);
			method = null;
		} catch (SecurityException ex) {
			context.error("Inaccessible method '" + config.getClass().getName() + "#" + config.getMethod()
				+ "()' in model provider: " + config.location());
			method = null;
		}
		_method = method;
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		try {
			return _method.invoke(null);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

}
