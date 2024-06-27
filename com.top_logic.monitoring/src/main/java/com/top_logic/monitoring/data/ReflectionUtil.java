/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.top_logic.basic.Logger;

/**
 * Utilities using reflection.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ReflectionUtil {

	/**
	 * Looks for a method and returns it. If this method cannot be found <code>null</code> is
	 * returned.
	 * 
	 * @param type
	 *        The class which has the method to look for.
	 * @param name
	 *        The name of the method.
	 * @param parameterTypes
	 *        The types of the parameters. Can contain var args.
	 * @return The found method or <code>null</code> (if not found).
	 */
	public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		// Uses getDeclaredMethods() to be able to use varArgs which need to be wrapped in an extra
		// array to fit to the parameters
		Method[] declaredMethods = type.getDeclaredMethods();
		for (int i = 0; i < declaredMethods.length; i++) {
			// is the same name?
			if (name.equals(declaredMethods[i].getName())) {
				Class<?>[] methodeParameterTypes = declaredMethods[i].getParameterTypes();
				boolean isVarArgs = declaredMethods[i].isVarArgs();
				Class<?> varArgType = null;

				if (isVarArgs) {
					varArgType = methodeParameterTypes[methodeParameterTypes.length - 1].getComponentType();
				}

				// does the number of parameters fit?
				if (parameterTypes.length == methodeParameterTypes.length || isVarArgs) {
					boolean match = true;
					int simpleArgLength = isVarArgs ? methodeParameterTypes.length - 1 : methodeParameterTypes.length;

					for (int j = 0; j < parameterTypes.length; j++) {
						// is simple arg?
						if (i < simpleArgLength) {
							if (!parameterTypes[j].isAssignableFrom(methodeParameterTypes[j])) {
								match = false;
								break;
							}
						} else {
							if (varArgType != null && !varArgType.isAssignableFrom(parameterTypes[j])) {
								match = false;
								break;
							}
						}
					}

					if (match) {
						return declaredMethods[i];
					}
				}
			}
		}

		return null;
	}

	/**
	 * Tries to invoke a method for a given object with given arguments.
	 * 
	 * @param object
	 *        The object on which the method should be called.
	 * @param method
	 *        The method to invoke.
	 * @param args
	 *        The arguments for the given method. Can contain var args.
	 * @return The object which may be returned by the given method.
	 */
	public static Object invokeMethod(Object object, Method method, Object... args) {
		if (method != null) {
			try {
				args = transformArgs(method, args);
				return method.invoke(object, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Logger.error(
					"Cannot invoke method '" + method.getName() + "' in '" + object.getClass() + "'.",
					ex, object.getClass());
			}
		}

		return null;
	}

	private static Object[] transformArgs(Method method, Object... args) {
		if (method.getParameterCount() > 0) {
			Parameter[] parameters = method.getParameters();
			Parameter lastParameter = parameters[parameters.length - 1];
			boolean isVarArgs = lastParameter.isVarArgs();

			if (isVarArgs) {
				if (args == null) {
					// no args
					args = new Object[0];
				} else if (args.length < parameters.length) {
					// empty var args -> put an empty array in the end
					Object[] varArgs = new Object[0];
					args = ArrayUtils.add(args, varArgs);
				} else {
					// with var args -> wrap var args and put them in the end
					Object[] simpleArgs = Arrays.copyOfRange(args, 0, (parameters.length - 1));
					Object[] varArgs =
						Arrays.copyOfRange(args, (parameters.length - 1), args.length);

					args = ArrayUtils.add(simpleArgs, varArgs);
				}
			}
		}

		return args;
	}

}
