/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.base.services.simpleajax.RequestLock;
import com.top_logic.mig.html.layout.Action;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.ActionQueue;
import com.top_logic.util.ValidationQueue;

/**
 * Representative for an independent layout within a user session.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutContext extends WindowContext, ValidationQueue, ActionQueue<Action> {

	/**
	 * The top-level component of the {@link LayoutContext}.
	 */
	MainLayout getMainLayout();

	/**
	 * Check, whether global state changes are allowed in this session's layout.
	 * 
	 * <p>
	 * Updates to global state are only allowed from a thread executing a command, because only
	 * command threads are guaranteed to be executed single-threaded.
	 * </p>
	 * 
	 * @param updater
	 *        The object that caused initiated the update.
	 */
	void checkUpdate(Object updater);

	/**
	 * Returns whether the current thread is in command phase or in rendering phase. If this method
	 * returns <code>false</code> it is highly recommended to do not fire events or do not something
	 * which has non local effects.
	 * 
	 * @return whether the current thread is writing or reading
	 */
	boolean isInCommandPhase();

	/**
	 * This subsession's {@link RequestLock}.
	 */
	RequestLock getLock();

}
