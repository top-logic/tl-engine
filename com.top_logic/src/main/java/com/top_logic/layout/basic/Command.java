/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Self contained command with all its arguments and other context information.
 * 
 * <p>
 * A {@link Command} encapsulates only the execution aspect, a {@link CommandModel} adds the
 * presentation aspects.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Command {
	
	/**
	 * Key to get the executing control from the command context.
	 * 
	 * <p>
	 * If this {@link Command} was executed by a {@link ButtonControl} the executing control can be
	 * fetched from the {@link DisplayContext executing context}, by using this {@link Property}.
	 * </p>
	 */
	public static final Property<ButtonControl> EXECUTING_CONTROL = TypedAnnotatable.property(ButtonControl.class,
		"executing control");

	public static final ResKey COMMAND_NOT_EXECUTABLE_ERROR_KEY = I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE;

	/**
	 * {@link #DO_NOTHING} is a dummy {@link Command} whose
	 * {@link #executeCommand(DisplayContext)} method does nothing.
	 */
	public static final Command DO_NOTHING = new Command() {
		
		/**
		 * Just returns {@link HandlerResult#DEFAULT_RESULT}.
		 * 
		 * @see Command#executeCommand(DisplayContext)
		 */
		@Override
		public final HandlerResult executeCommand(DisplayContext context) {
			return HandlerResult.DEFAULT_RESULT;
		}
	};

	/**
	 * Executes the given {@link Command}s in the given order. The returned {@link HandlerResult} is
	 * that of the last {@link Command}. If one of the {@link Command}s fails, its
	 * {@link HandlerResult} is returned with an additional error message, telling that a
	 * {@link CommandChain} failed. No further {@link Command}s are executed in that case.
	 */
	public static class CommandChain implements Command {

		private final Command[] parts;

		/**
		 * Creates a new {@link CommandChain} that executes the given actions in the given
		 * order.
		 */
		public CommandChain(Command... parts) {
			this.parts = parts;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			if (parts.length == 0) {
				return HandlerResult.DEFAULT_RESULT;
			}
			HandlerResult result = null;
			for (Command part : parts) {
				result = part.executeCommand(context);
				if (!result.isSuccess()) {
					return result;
				}
			}
			return result;
		}

	}

	/**
	 * This method executes some command. This method is triggered by pressing the client side
	 * representation of this {@link CommandModel} on the GUI.
	 * 
	 * @param context
	 *        the {@link DisplayContext} holding request related data.
	 * @return The result of the processing, must not be <code>null</code>.
	 */
	public HandlerResult executeCommand(DisplayContext context);

}
