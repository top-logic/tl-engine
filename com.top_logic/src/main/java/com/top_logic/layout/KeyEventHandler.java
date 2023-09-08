/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Server-side handler for client-side {@link KeyEvent}s.
 * 
 * @see KeyEventListener
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KeyEventHandler {

	/**
	 * Handler method that is called, if a {@link KeyEvent} matching one of the subscribed key
	 * presses occurs on the client in the view this listener is attached to.
	 * 
	 * @param commandContext
	 *        The {@link DisplayContext} of the invocation.
	 * @param event
	 *        The {@link KeyEvent} that describes the event.
	 * @return See {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 */
	HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event);

}
