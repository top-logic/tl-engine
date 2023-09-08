/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import static java.util.Objects.*;

import com.top_logic.layout.basic.Command;

/**
 * Listener for client-side key press events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class KeyEventListener implements KeyEventHandler {

	private final KeySelector[] _subscribedKeys;

	/**
	 * Create a {@link KeyEventListener}.
	 * 
	 * @param subscribedKeys
	 *        All keys that should be requested from the client.
	 */
	public KeyEventListener(KeySelector... subscribedKeys) {
		assert subscribedKeys != null : "No keys given.";

		_subscribedKeys = subscribedKeys;
	}

	/**
	 * Descriptions of {@link KeyEvent}s that are processed by the
	 * {@link #handleKeyEvent(DisplayContext, KeyEvent)} method.
	 * 
	 * <p>
	 * Only key events matching this specification are routed from the client to the server.
	 * </p>
	 * 
	 * @return Descriptions of key events that are requested by this handler.
	 */
	public final KeySelector[] subscribedKeys() {
		return _subscribedKeys;
	}

	/**
	 * Creates a {@link KeyEventListener} that executes the given command on enter.
	 * 
	 * @param command
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static KeyEventListener onEnter(Command command) {
		requireNonNull(command, "Command must not be null.");
		KeyEventHandler handler = (displayContext, event) -> command.executeCommand(displayContext);
		KeySelector keySelector = new KeySelector(KeyCode.RETURN, 0);
		return new KeyEventListenerAdapter(handler, keySelector);
	}

}
