/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.EventListener;

import com.top_logic.basic.InteractionContext;

/**
 * Listener to be informed when an {@link InteractionContext} is removed from its thread.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UnboundListener extends EventListener {

	/**
	 * Will be called when the given {@link InteractionContext} is removed from thread.
	 * 
	 * @param context
	 *        context that is un-installed.
	 */
	void threadUnbound(InteractionContext context) throws Throwable;

}

