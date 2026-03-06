/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Invokes a single {@link ReactCommand}-annotated method on a {@link ReactControl}.
 *
 * <p>
 * Built once per annotated method during {@link ReactCommandMap} resolution. Uses a
 * {@link MethodHandle} for zero-reflection dispatch on the hot path.
 * </p>
 */
class ReactCommandInvoker {

	private final MethodHandle _handle;

	private final boolean _needsContext;

	private final boolean _needsArgs;

	/**
	 * Creates a new invoker.
	 *
	 * @param handle
	 *        The method handle for the annotated method. First parameter is always the receiver
	 *        (ReactControl subclass).
	 * @param needsContext
	 *        Whether the method declares a {@link ViewDisplayContext} parameter.
	 * @param needsArgs
	 *        Whether the method declares a {@code Map<String, Object>} parameter.
	 */
	ReactCommandInvoker(MethodHandle handle, boolean needsContext, boolean needsArgs) {
		_handle = handle;
		_needsContext = needsContext;
		_needsArgs = needsArgs;
	}

	/**
	 * Invokes the command method on the given control.
	 */
	HandlerResult invoke(ReactControl control, ViewDisplayContext context,
			Map<String, Object> arguments) {
		try {
			if (_needsContext && _needsArgs) {
				return (HandlerResult) _handle.invoke(control, context, arguments);
			} else if (_needsContext) {
				return (HandlerResult) _handle.invoke(control, context);
			} else if (_needsArgs) {
				return (HandlerResult) _handle.invoke(control, arguments);
			} else {
				return (HandlerResult) _handle.invoke(control);
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke @ReactCommand on " + control.getClass().getName(), ex);
		}
	}
}
