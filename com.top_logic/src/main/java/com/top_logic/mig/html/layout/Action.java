/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.util.ActionQueue;

/**
 * Action that needs to be executed via the {@link ActionQueue}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Action {

	/**
	 * Execute this action. Must not be called twice!
	 */
	public void execute();

	/**
	 * Determines whether the given {@link Action} is an updated version of this {@link Action},
	 * i.e. whether it is of the same type, send by the same component, but has a different model.
	 * 
	 * @param potentialUpdate
	 *        The {@link Action} to check. Must not be <code>null</code>
	 */
	public boolean isUpdate(Action potentialUpdate);
}

