/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Cached mapping from command IDs to {@link ReactCommandInvoker}s for a single
 * {@link ReactControl} subclass.
 *
 * <p>
 * Built lazily on first instantiation of each class via {@link #forClass(Class)}. Scans the class
 * hierarchy for {@link ReactCommand}-annotated methods, builds {@link MethodHandle}s, and
 * determines parameter injection flags.
 * </p>
 */
class ReactCommandMap {

	private final Map<String, ReactCommandInvoker> _invokers;

	private ReactCommandMap(Map<String, ReactCommandInvoker> invokers) {
		_invokers = invokers;
	}

	/**
	 * Looks up the invoker for the given command ID.
	 *
	 * @return The invoker, or {@code null} if no command is registered for the given ID.
	 */
	ReactCommandInvoker get(String commandId) {
		return _invokers.get(commandId);
	}

	/**
	 * Scans the given class hierarchy for {@link ReactCommand}-annotated methods and builds a
	 * {@link ReactCommandMap}.
	 *
	 * @throws IllegalStateException
	 *         If a method has an unsupported parameter type or return type.
	 */
	static ReactCommandMap forClass(Class<?> controlClass) {
		Map<String, ReactCommandInvoker> invokers = new HashMap<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		for (Class<?> clazz = controlClass; clazz != null && clazz != Object.class;
				clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getDeclaredMethods()) {
				ReactCommand annotation = method.getAnnotation(ReactCommand.class);
				if (annotation == null) {
					continue;
				}

				String commandId = annotation.value();
				if (invokers.containsKey(commandId)) {
					continue;
				}

				validate(method);
				method.setAccessible(true);

				boolean needsContext = false;
				boolean needsArgs = false;
				for (Class<?> paramType : method.getParameterTypes()) {
					if (ViewDisplayContext.class.isAssignableFrom(paramType)) {
						needsContext = true;
					} else if (Map.class.isAssignableFrom(paramType)) {
						needsArgs = true;
					}
				}

				boolean returnsVoid = method.getReturnType() == void.class;

				try {
					MethodHandle handle = lookup.unreflect(method);
					invokers.put(commandId,
						new ReactCommandInvoker(handle, needsContext, needsArgs, returnsVoid));
				} catch (IllegalAccessException ex) {
					throw new IllegalStateException(
						"Cannot access @ReactCommand method " + method + " on " + controlClass.getName(), ex);
				}
			}
		}
		return new ReactCommandMap(invokers);
	}

	private static void validate(Method method) {
		Class<?> returnType = method.getReturnType();
		if (returnType != HandlerResult.class && returnType != void.class) {
			throw new IllegalStateException(
				"@ReactCommand method " + method.getName()
					+ " must return HandlerResult or void, but returns "
					+ returnType.getName());
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length > 2) {
			throw new IllegalStateException(
				"@ReactCommand method " + method.getName() + " declares " + paramTypes.length
					+ " parameters, but at most 2 are allowed: "
					+ "(ViewDisplayContext, Map<String, Object>).");
		}
		boolean contextSeen = false;
		boolean argsSeen = false;
		for (Class<?> paramType : paramTypes) {
			if (ViewDisplayContext.class.isAssignableFrom(paramType)) {
				if (contextSeen) {
					throw new IllegalStateException(
						"@ReactCommand method " + method.getName()
							+ " declares ViewDisplayContext more than once.");
				}
				if (argsSeen) {
					throw new IllegalStateException(
						"@ReactCommand method " + method.getName()
							+ " declares ViewDisplayContext after Map. "
							+ "Required order: (ViewDisplayContext, Map<String, Object>).");
				}
				contextSeen = true;
			} else if (Map.class.isAssignableFrom(paramType)) {
				if (argsSeen) {
					throw new IllegalStateException(
						"@ReactCommand method " + method.getName()
							+ " declares Map more than once.");
				}
				argsSeen = true;
			} else {
				throw new IllegalStateException(
					"@ReactCommand method " + method.getName() + " has unsupported parameter type: "
						+ paramType.getName()
						+ ". Allowed: ViewDisplayContext, Map<String, Object>.");
			}
		}
	}
}
