/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.mig.html.layout.Action;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Queue to register invalid {@link ToBeValidated} and send {@link Action actions}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ValidationQueue {

	/**
	 * Notifies that the given {@link ToBeValidated} is invalid and must be validated.
	 * 
	 * <p>
	 * The caller is responsible for not adding the same {@link ToBeValidated} twice, since calling
	 * this method twice for the same object will add it again.
	 * </p>
	 * 
	 * <p>
	 * Note: Deferred validation must only be used during command processing, see
	 * {@link LayoutContext#isInCommandPhase()}.
	 * </p>
	 * 
	 * @param o
	 *        The object to be validated. Must not be <code>null</code>.
	 */
	void notifyInvalid(ToBeValidated o);

	/**
	 * Starts this {@link ValidationQueue}. It runs as long as any {@link ToBeValidated} is
	 * registered.
	 * 
	 * @param context
	 *        The context in which validation is executed
	 * @return A result containing potential problems.
	 */
	HandlerResult runValidation(DisplayContext context);

}
