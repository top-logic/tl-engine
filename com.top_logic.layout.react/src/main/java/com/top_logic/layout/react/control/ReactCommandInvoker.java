/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.data.DataObject;

/**
 * Invokes a single {@link ReactCommandHandler}-annotated method on a {@link ReactControl}.
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

	private final ConfigurationDescriptor _argType;

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
	 *        Whether the method declares a raw {@code Map<String, Object>} argument parameter.
	 * @param argType
	 *        The {@link ConfigurationDescriptor} of the method's typed argument parameter, or
	 *        {@code null} if it takes the raw {@code Map} (or no argument). When set, the client
	 *        arguments bind into a {@link ConfigurationItem} of this descriptor before dispatch.
	 * @param returnsVoid
	 *        Whether the method returns {@code void} (as opposed to a reply, see
	 *        {@link ReactCommandHandler}).
	 */
	ReactCommandInvoker(MethodHandle handle, boolean needsContext, boolean needsArgs,
			ConfigurationDescriptor argType, boolean returnsVoid) {
		_handle = handle;
		_needsContext = needsContext;
		_needsArgs = needsArgs;
		_argType = argType;
		_returnsVoid = returnsVoid;
	}

	/**
	 * Invokes the command method on the given control.
	 *
	 * <p>
	 * A {@link ChannelVetoException} the command throws opens the dialog asking about the unsaved
	 * changes it names; the command then counts as handled. Any other exception is the failure of
	 * the command and is passed on to the caller, a checked one wrapped into a
	 * {@link TopLogicException} if it carries a message for the user, into a
	 * {@link RuntimeException} otherwise.
	 * </p>
	 *
	 * @return The reply of the command method, <code>null</code> if it replies nothing.
	 */
	DataObject invoke(ReactControl control, ReactContext context, Map<String, Object> arguments) {
		try {
			boolean hasArg = _needsArgs || _argType != null;
			Object arg = _argType != null ? bind(arguments) : arguments;
			if (_needsContext && hasArg) {
				return castResult(_handle.invoke(control, context, arg));
			} else if (_needsContext) {
				return castResult(_handle.invoke(control, context));
			} else if (hasArg) {
				return castResult(_handle.invoke(control, arg));
			} else {
				return castResult(_handle.invoke(control));
			}
		} catch (ChannelVetoException ex) {
			DialogManager dm = context.getDialogManager();
			if (dm != null) {
				DirtyConfirmDialogControl.openDialog(context, dm, ex.getDirtyHandlers(),
					ex.getContinuation(), ex.getRollback());
				return null;
			}
			Logger.warn("No DialogManager available for dirty-check dialog.", ReactCommandInvoker.class);
			return null;
		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (I18NException ex) {
			// E.g. arguments of the client that do not bind to the declared argument type.
			throw new TopLogicException(ex.getErrorKey(), ex);
		} catch (Throwable ex) {
			throw new RuntimeException(
				"@ReactCommandHandler on " + control.getClass().getName() + " failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Binds the raw client arguments into a {@link ConfigurationItem} of the declared
	 * {@link #_argType argument descriptor}.
	 *
	 * <p>
	 * The {@code arguments} map already holds the parsed client JSON; it is re-serialized and read
	 * through {@link JsonConfigurationReader} so the typed argument binds by exactly the JSON
	 * property names the schema advertises.
	 * </p>
	 */
	private ConfigurationItem bind(Map<String, Object> arguments) throws Exception {
		return ReactControl.bindArguments(_argType, arguments);
	}

	private DataObject castResult(Object result) {
		if (_returnsVoid) {
			return null;
		}
		return (DataObject) result;
	}
}
