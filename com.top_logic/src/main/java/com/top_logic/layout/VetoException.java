/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.treetable.component.SilentVetoException;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Exception} thrown by a {@link VetoListener} to roll back the current operation.
 * 
 * <p>
 * To inform the user about the veto, a dialog can be opened in {@link #process(WindowScope)}. If no
 * custom user interaction is required, {@link SilentVetoException} can be thrown.
 * </p>
 * 
 * @see SilentVetoException
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class VetoException extends Exception {

	private Command continuationCommand;

	/**
	 * Creates a {@link VetoException}.
	 */
	public VetoException() {
		super();
	}

	/**
	 * @param command
	 *        - that shall be executed, if the veto is revoked
	 */
	public void setContinuationCommand(Command command) {
		continuationCommand = command;
	}

	/**
	 * @see #setContinuationCommand(Command)
	 */
	public Command getContinuationCommand() {
		// Added indirection, because the getter is usually called before the setter
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return continuationCommand.executeCommand(context);
			}
		};
	}

	/**
	 * Inform the user about the veto, e.g. by opening a dialog.
	 * 
	 * <p>
	 * This method must be called after finally catching a {@link VetoException}. Normaly this is
	 * done by some UI element that knows the {@link FrameScope}, where the problem occurred.
	 * </p>
	 * 
	 * @param window
	 *        The scope, where the problem occured on the UI.
	 */
	public abstract void process(WindowScope window);

}

