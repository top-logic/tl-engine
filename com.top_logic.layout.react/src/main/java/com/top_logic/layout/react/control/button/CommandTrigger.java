/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.util.function.Supplier;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The client-side element the command currently being executed was triggered from.
 *
 * <p>
 * A command that answers with a follow-up menu - a choice among the types of an object to create,
 * say - places that menu at the element the user clicked, through
 * {@link com.top_logic.layout.react.control.overlay.ContextMenuOpener#open(String, java.util.List, Runnable)}.
 * The command does not know which button displays it: a {@link ReactButtonControl} announces
 * itself here for the duration of its click, see {@link #runTriggeredBy(String, Supplier)}.
 * </p>
 *
 * <p>
 * The trigger is bound to the executing thread for exactly the duration of the command, so a
 * control created by the command never sees a trigger that is long gone.
 * </p>
 */
public final class CommandTrigger {

	private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

	private CommandTrigger() {
		// Static utility.
	}

	/**
	 * The ID of the client-side element the command currently being executed was triggered from,
	 * or {@code null} when the command was not triggered by an element - a keyboard gesture
	 * handled elsewhere, an entry of a menu that is already closed, a programmatic call.
	 */
	public static String currentId() {
		return CURRENT.get();
	}

	/**
	 * Runs the given command execution as triggered from the element with the given ID.
	 *
	 * @param triggerId
	 *        The ID of the client-side element the command is triggered from.
	 * @param execution
	 *        The execution of the command.
	 * @return The result of the execution.
	 */
	public static HandlerResult runTriggeredBy(String triggerId, Supplier<HandlerResult> execution) {
		String before = CURRENT.get();
		CURRENT.set(triggerId);
		try {
			return execution.get();
		} finally {
			if (before == null) {
				CURRENT.remove();
			} else {
				CURRENT.set(before);
			}
		}
	}

}
