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

	private final boolean _returnsVoid;

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
	 * @param returnsVoid
	 *        Whether the method returns {@code void} (as opposed to {@link HandlerResult}).
	 */
	ReactCommandInvoker(MethodHandle handle, boolean needsContext, boolean needsArgs,
			boolean returnsVoid) {
		_handle = handle;
		_needsContext = needsContext;
		_needsArgs = needsArgs;
		_returnsVoid = returnsVoid;
	}

	/**
	 * Invokes the command method on the given control.
	 */
	HandlerResult invoke(ReactControl control, ViewDisplayContext context,
			Map<String, Object> arguments) {
		try {
			if (_needsContext && _needsArgs) {
				return castResult(_handle.invoke(control, context, arguments));
			} else if (_needsContext) {
				return castResult(_handle.invoke(control, context));
			} else if (_needsArgs) {
				return castResult(_handle.invoke(control, arguments));
			} else {
				return castResult(_handle.invoke(control));
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to invoke @ReactCommand on " + control.getClass().getName(), ex);
		}
	}

	private HandlerResult castResult(Object result) {
		if (_returnsVoid) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return (HandlerResult) result;
	}
}
