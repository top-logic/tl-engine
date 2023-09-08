/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Adapter to make a {@link KeyEventListener} from a {@link KeyEventHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeyEventListenerAdapter extends KeyEventListener {

	private final KeyEventHandler _handler;

	/**
	 * Create a {@link KeyEventListenerAdapter}.
	 * 
	 * @param handler
	 *        The listener implementation.
	 * @param subscribedKeys
	 *        The key strokes to listen for.
	 */
	public KeyEventListenerAdapter(KeyEventHandler handler, KeySelector... subscribedKeys) {
		super(subscribedKeys);

		assert handler != null : "No implementation given.";
		_handler = handler;
	}

	@Override
	public HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event) {
		return _handler.handleKeyEvent(commandContext, event);
	}

}
