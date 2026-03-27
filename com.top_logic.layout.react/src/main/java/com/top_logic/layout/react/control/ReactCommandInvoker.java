/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
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
	 *        Whether the method declares a {@link ReactContext} parameter.
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
	 *
	 * <p>
	 * Exceptions thrown by the command method are converted to error results:
	 * </p>
	 * <ul>
	 * <li>{@link I18NFailure} exceptions use the
	 * {@linkplain I18NFailure#getErrorKey() user-visible error message}.</li>
	 * <li>Other exceptions produce a generic error result with the exception message.</li>
	 * </ul>
	 */
	HandlerResult invoke(ReactControl control, ReactContext context,
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
		} catch (ChannelVetoException ex) {
			DialogManager dm = context.getDialogManager();
			if (dm != null) {
				DirtyConfirmDialogControl dialogContent =
					new DirtyConfirmDialogControl(context, ex.getDirtyHandlers(), ex.getContinuation(), dm);
				dm.openDialog(false, dialogContent, result -> {
					// Dialog closed. Save/discard/cancel handled by dialog's own commands.
				});
				return HandlerResult.DEFAULT_RESULT;
			}
			Logger.warn("No DialogManager available for dirty-check dialog.", ReactCommandInvoker.class);
			return HandlerResult.DEFAULT_RESULT;
		} catch (Throwable ex) {
			Logger.error("@ReactCommand failed on " + control.getClass().getName(), ex,
				ReactCommandInvoker.class);
			I18NFailure i18n = findI18NFailure(ex);
			if (i18n != null) {
				return HandlerResult.error(i18n.getErrorKey(), ex);
			}
			return HandlerResult.error(ResKey.text(ex.getMessage()), ex);
		}
	}

	/**
	 * Searches the exception cause chain for an {@link I18NFailure}.
	 */
	private static I18NFailure findI18NFailure(Throwable ex) {
		Throwable current = ex;
		while (current != null) {
			if (current instanceof I18NFailure) {
				return (I18NFailure) current;
			}
			current = current.getCause();
		}
		return null;
	}

	private HandlerResult castResult(Object result) {
		if (_returnsVoid) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return (HandlerResult) result;
	}
}
