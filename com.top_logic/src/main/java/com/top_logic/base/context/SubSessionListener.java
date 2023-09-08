/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.context;

import java.util.EventListener;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;

/**
 * Listener that is triggered when a {@link SubSessionContext} is removed from its
 * {@link SessionContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface SubSessionListener extends EventListener {

	/**
	 * Informs the listener that the given {@link SubSessionContext} is removed from its
	 * {@link SessionContext}.
	 * 
	 * @param context
	 *        The removed context.
	 */
	void notifySubSessionUnbound(TLSubSessionContext context);

}

