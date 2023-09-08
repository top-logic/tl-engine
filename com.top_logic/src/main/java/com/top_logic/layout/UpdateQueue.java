/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.base.services.simpleajax.ClientAction;

/**
 * The {@link UpdateQueue} is a framework internal class to collect {@link ClientAction}s which are
 * produced during executing an AJAX command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UpdateQueue {

	/**
	 * This method adds a new {@link ClientAction} for being transferred to the client.
	 * 
	 * @param update
	 *            The action to transmit. <code>null</code> actions will be ignored.
	 */
	public void add(ClientAction update);

}
