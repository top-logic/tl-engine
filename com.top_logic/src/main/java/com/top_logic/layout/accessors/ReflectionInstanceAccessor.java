/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Accessor;

/**
 * {@link Accessor} that is implemented by calling a method through reflection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReflectionInstanceAccessor extends ConfiguredTypeSafeAccessor<Object, ReflectionInstanceAccessor.Config> {

	/**
	 * Configuration options for {@link ReflectionInstanceAccessor}.
	 */
	public interface Config extends ConfiguredTypeSafeAccessor.Config<ReflectionInstanceAccessor, Object> {

		/**
		 * @see #getMethod()
		 */
		String METHOD = "method";

		/**
		 * The name of the no-argument method to invoke on the instances.
		 */
		@Name(METHOD)
		@Mandatory
		String getMethod();

		/**
		 * @see #getMethod()
		 */
		void setMethod(String value);

	}

	private static final Class<?>[] NO_ARGS = {};

	private final Method _method;

	/**
	 * Creates a {@link ReflectionInstanceAccessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReflectionInstanceAccessor(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_method = resolveMethod(config);
	}

	private static Method resolveMethod(Config config) throws ConfigurationException {
		String methodName = config.getMethod();
		Class<?> type = config.getType();
		return resolveMethod(type, methodName);
	}

	private static Method resolveMethod(Class<?> type, String methodName) throws ConfigurationException {
		Method method;
		try {
			method = type.getMethod(methodName, NO_ARGS);
			int modifiers = method.getModifiers();
			if (!Modifier.isPublic(modifiers)) {
				throw new ConfigurationException("Accessor method '" + method(type, methodName) + "' must be public.");
			}
			if (Modifier.isStatic(modifiers)) {
				throw new ConfigurationException("Accessor method '" + method(type, methodName)
					+ "' must not be static.");
			}
			return method;
		} catch (NoSuchMethodException | SecurityException ex) {
			throw (ConfigurationException) new ConfigurationException("Failed resolving the accessor method '"
				+ method(type, methodName) + "'.").initCause(ex);
		}
	}

	private static String method(Class<?> type, String methodName) {
		return type.getName() + "#" + methodName + "()";
	}

	/**
	 * The method to call.
	 * 
	 * @see Config#getMethod()
	 */
	public Method getMethod() {
		return _method;
	}

	@Override
	protected Object getValueTyped(Object object, String property) {
		try {
			return _method.invoke(object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

}
