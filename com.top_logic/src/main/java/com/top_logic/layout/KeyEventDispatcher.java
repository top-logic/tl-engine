/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.tool.boundsec.HandlerResult;


/**
 * Interface for receivers of {@link KeyEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KeyEventDispatcher {

	/**
	 * Accepts the given {@link KeyEvent} and forwards it to registered {@link KeyEventListener}s.
	 * 
	 * @param commandContext
	 *        See {@link KeyEventListener#handleKeyEvent(DisplayContext, KeyEvent)}.
	 * @param event
	 *        See {@link KeyEventListener#handleKeyEvent(DisplayContext, KeyEvent)}.
	 * @return The result of the command invocation.
	 */
	HandlerResult dispatchKeyEvent(DisplayContext commandContext, KeyEvent event);
	
}