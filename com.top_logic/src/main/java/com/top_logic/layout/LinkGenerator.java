/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.form.tag.js.JSValue;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Factory class to generate links to execute a server side command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LinkGenerator {

	static final Property<Object> COMMAND_ARGUMENTS =
		TypedAnnotatable.property(Object.class, "commandArguments", Collections.emptyMap());

	/**
	 * A handle for an anonymously registered command.
	 * 
	 * @see #dispose()
	 */
	public interface Handle {

		/**
		 * Unregisters the command and frees allocated resources.
		 * 
		 * <p>
		 * If this method is not explicitly called, the handle is automatically disposed, at the
		 * time its parent scope (the context in which the command was rendered) is detached.
		 * </p>
		 */
		void dispose();

	}

	public static abstract class Callback implements CommandListener, Handle, UpdateListener {

		private final String _id;

		private final ControlScope _scope;

		private boolean _disabled;

		protected Callback(ControlScope scope) {
			_scope = scope;
			_id = scope.getFrameScope().createNewID();
		}

		@Override
		public void notifyAttachedTo(Object aModel) {
			_scope.getFrameScope().addCommandListener(this);
		}

		@Override
		public void notifyDetachedFrom(Object aModel) {
			_scope.getFrameScope().removeCommandListener(this);
		}

		@Override
		public void dispose() {
			_scope.removeUpdateListener(this);
		}

		@Override
		public String getID() {
			return _id;
		}

		/**
		 * Whether this command temporarily cannot be executed.
		 */
		public boolean isDisabled() {
			return _disabled;
		}

		@Override
		public void notifyDisabled(boolean disabled) {
			_disabled = disabled;
		}

		@Override
		public boolean isInvalid() {
			return false;
		}

		@Override
		public void revalidate(DisplayContext context, UpdateQueue actions) {
			// Ignore.
		}
	}

	static final class CommandExecution extends Callback {

		private final Command _command;

		CommandExecution(ControlScope scope, Command command) {
			super(scope);
			_command = command;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context, String commandName, Map<String, Object> arguments) {
			if (isDisabled()) {
				// Skip.
				return HandlerResult.DEFAULT_RESULT;
			}
			context.set(COMMAND_ARGUMENTS, arguments);
			return _command.executeCommand(context);
		}

	}

	/**
	 * @see LinkGenerator#renderLink(DisplayContext, Appendable, Command, Map)
	 */
	public static String createLink(DisplayContext context, Command command, Map<String, JSValue> additionalArguments) {
		StringBuilder out = new StringBuilder();
		try {
			renderLink(context, out, command, additionalArguments);
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder does not throw IOException.", ex);
		}
		return out.toString();
	}

	/**
	 * @see LinkGenerator#renderLink(DisplayContext, Appendable, Command)
	 */
	public static String createLink(DisplayContext context, Command command) {
		return createLink(context, command, Collections.emptyMap());
	}

	/**
	 * Renders a link without additional arguments.
	 * 
	 * @see #renderLink(DisplayContext, Appendable, Command, Map)
	 */
	public static Handle renderLink(DisplayContext context, Appendable out, Command command)
			throws IOException {
		return renderLink(context, out, command, Collections.emptyMap());
	}

	/**
	 * Renders a link to execute the given {@link Command}.
	 * 
	 * <p>
	 * This method can be used to produce javascript links if it is not possible to use
	 * {@link ButtonControl}, e.g. in {@link ResourceProvider#getLink(DisplayContext, Object)}.
	 * </p>
	 * 
	 * <p>
	 * This method must only be used during rendering.
	 * </p>
	 * 
	 * @param context
	 *        Rendering context.
	 * @param out
	 *        Output to append link to.
	 * @param command
	 *        The command to execute when link is invoked.
	 * @param additionalArguments
	 *        Additional arguments to used by the command. These arguments are transfered to the
	 *        client and are evaluated there. Therefore this map should contain only values that
	 *        must be evaluated on the client.
	 * @return The {@link Handle} for the registered command, see {@link Handle#dispose()}.
	 * 
	 * @throws IOException
	 *         iff given {@link Appendable} throws one.
	 */
	public static Handle renderLink(DisplayContext context, Appendable out, Command command,
			Map<String, JSValue> additionalArguments) throws IOException {
		ControlScope scope = context.getExecutionScope();
		if (scope == null) {
			throw new IllegalStateException(
				"Generating link is not allowed in command phase. A ControlScope must be available to create link.");
		}
		CommandExecution execution = new CommandExecution(scope, command);
		scope.addUpdateListener(execution);

		Map<String, JSValue> arguments = new HashMap<>(additionalArguments);
		arguments.put(ControlCommand.CONTROL_ID_PARAM, new JSString(execution.getID()));
		DispatchAction dispatchAction =
			(DispatchAction) CommandHandlerFactory.getInstance().getHandler(DispatchAction.COMMAND_NAME);
		dispatchAction.appendInvokeExpression(out, new JSObject(arguments));
		return execution;
	}

	/**
	 * Returns the command arguments that are used by the client to trigger the command.
	 * 
	 * <p>
	 * This Method must only be called from within the command execution method to get the arguments
	 * that were used to execute the command.
	 * </p>
	 * 
	 * @param context
	 *        The {@link DisplayContext} used in {@link Command#executeCommand(DisplayContext)
	 *        execution} method of the command that were given in render link method.
	 */
	public static Map<String, Object> getArguments(DisplayContext context) {
		@SuppressWarnings("unchecked")
		Map<String, Object> args = (Map<String, Object>) context.get(COMMAND_ARGUMENTS);
		return args;
	}

}

