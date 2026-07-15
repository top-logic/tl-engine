/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Cached mapping from command IDs to {@link ReactCommandInvoker}s for a single
 * {@link ReactControl} subclass.
 *
 * <p>
 * Built lazily on first instantiation of each class via {@link #forClass(Class)}. Scans the class
 * hierarchy for {@link ReactCommandHandler}-annotated methods, builds {@link MethodHandle}s, and
 * determines parameter injection flags.
 * </p>
 */
class ReactCommandMap {

	private final Map<String, ReactCommandInvoker> _invokers;

	private final Map<String, ReactParam[]> _params;

	private final Map<String, ConfigurationDescriptor> _argTypes;

	private final Set<String> _technicalCommands;

	private ReactCommandMap(Map<String, ReactCommandInvoker> invokers, Map<String, ReactParam[]> params,
			Map<String, ConfigurationDescriptor> argTypes, Set<String> technicalCommands) {
		_invokers = invokers;
		_params = params;
		_argTypes = argTypes;
		_technicalCommands = technicalCommands;
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
	 * The set of all command IDs handled by this control class.
	 *
	 * <p>
	 * Used by the headless agent interface to advertise the action space of a control without
	 * actually invoking any command.
	 * </p>
	 */
	Set<String> commandIds() {
		return Collections.unmodifiableSet(_invokers.keySet());
	}

	/**
	 * The declared {@link ReactParam argument schema} of the given command, or an empty array if the
	 * command declares none.
	 */
	ReactParam[] paramsFor(String commandId) {
		ReactParam[] params = _params.get(commandId);
		return params != null ? params : new ReactParam[0];
	}

	/**
	 * The {@link ConfigurationDescriptor} of the typed argument the given command declares, or
	 * {@code null} if the command takes a raw {@code Map} (or no arguments).
	 *
	 * <p>
	 * Used by the headless agent interface to advertise a command's argument schema and to render a
	 * recorded step, both derived from the one argument interface.
	 * </p>
	 */
	ConfigurationDescriptor argTypeFor(String commandId) {
		return _argTypes.get(commandId);
	}

	/**
	 * The ids of commands marked {@link ReactCommandHandler#technical() technical} — omitted from the agent
	 * action space and never recorded.
	 */
	Set<String> technicalCommands() {
		return _technicalCommands;
	}

	/**
	 * Scans the given class hierarchy for {@link ReactCommandHandler}-annotated methods and builds a
	 * {@link ReactCommandMap}.
	 *
	 * @throws IllegalStateException
	 *         If a method has an unsupported parameter type or return type.
	 */
	static ReactCommandMap forClass(Class<?> controlClass) {
		Map<String, ReactCommandInvoker> invokers = new HashMap<>();
		Map<String, ReactParam[]> params = new HashMap<>();
		Map<String, ConfigurationDescriptor> argTypes = new HashMap<>();
		Set<String> technicalCommands = new java.util.HashSet<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		for (Class<?> clazz = controlClass; clazz != null && clazz != Object.class;
				clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getDeclaredMethods()) {
				ReactCommandHandler annotation = method.getAnnotation(ReactCommandHandler.class);
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
				ConfigurationDescriptor argType = null;
				for (Class<?> paramType : method.getParameterTypes()) {
					if (ReactContext.class.isAssignableFrom(paramType)) {
						needsContext = true;
					} else if (ConfigurationItem.class.isAssignableFrom(paramType)) {
						argType = TypedConfiguration.getConfigurationDescriptor(paramType);
					} else if (Map.class.isAssignableFrom(paramType)) {
						needsArgs = true;
					}
				}

				boolean returnsVoid = method.getReturnType() == void.class;

				try {
					MethodHandle handle = lookup.unreflect(method);
					invokers.put(commandId,
						new ReactCommandInvoker(handle, needsContext, needsArgs, argType, returnsVoid));
					params.put(commandId, annotation.params());
					if (argType != null) {
						argTypes.put(commandId, argType);
					}
					if (annotation.technical()) {
						technicalCommands.add(commandId);
					}
				} catch (IllegalAccessException ex) {
					throw new IllegalStateException(
						"Cannot access @ReactCommandHandler method " + method + " on " + controlClass.getName(), ex);
				}
			}
		}
		return new ReactCommandMap(invokers, params, argTypes, technicalCommands);
	}

	private static void validate(Method method) {
		Class<?> returnType = method.getReturnType();
		if (returnType != HandlerResult.class && returnType != void.class) {
			throw new IllegalStateException(
				"@ReactCommandHandler method " + method.getName()
					+ " must return HandlerResult or void, but returns "
					+ returnType.getName());
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length > 2) {
			throw new IllegalStateException(
				"@ReactCommandHandler method " + method.getName() + " declares " + paramTypes.length
					+ " parameters, but at most 2 are allowed: "
					+ "(ReactContext, Map<String, Object> | ConfigurationItem).");
		}
		boolean contextSeen = false;
		boolean argsSeen = false;
		for (Class<?> paramType : paramTypes) {
			if (ReactContext.class.isAssignableFrom(paramType)) {
				if (contextSeen) {
					throw new IllegalStateException(
						"@ReactCommandHandler method " + method.getName()
							+ " declares ReactContext more than once.");
				}
				if (argsSeen) {
					throw new IllegalStateException(
						"@ReactCommandHandler method " + method.getName()
							+ " declares ReactContext after the argument. "
							+ "Required order: (ReactContext, Map<String, Object> | ConfigurationItem).");
				}
				contextSeen = true;
			} else if (ConfigurationItem.class.isAssignableFrom(paramType)) {
				if (!ReactCommand.class.isAssignableFrom(paramType)) {
					// The typed argument doubles as the recorded step item, so it must carry the
					// address/name envelope of the base interface.
					throw new IllegalStateException(
						"@ReactCommandHandler method " + method.getName() + " declares argument type "
							+ paramType.getName() + " that does not extend " + ReactCommand.class.getName() + ".");
				}
				if (argsSeen) {
					throw new IllegalStateException(
						"@ReactCommandHandler method " + method.getName()
							+ " declares more than one argument parameter. Use either a raw Map or a "
							+ "single ReactCommand argument type.");
				}
				argsSeen = true;
			} else if (Map.class.isAssignableFrom(paramType)) {
				if (argsSeen) {
					throw new IllegalStateException(
						"@ReactCommandHandler method " + method.getName()
							+ " declares more than one argument parameter. Use either a raw Map or a "
							+ "single ReactCommand argument type.");
				}
				argsSeen = true;
			} else {
				throw new IllegalStateException(
					"@ReactCommandHandler method " + method.getName() + " has unsupported parameter type: "
						+ paramType.getName()
						+ ". Allowed: ReactContext, Map<String, Object>, or a ReactCommand argument type.");
			}
		}
	}
}
