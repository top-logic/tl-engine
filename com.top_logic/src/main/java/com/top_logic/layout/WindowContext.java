/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.internal.WindowHandler;
import com.top_logic.layout.internal.WindowId;

/**
 * Representative for a client-side browser window.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WindowContext {

	/**
	 * The identifier that must be sent from the client that enables the server to decide whether a
	 * client-side window was cloned (URL copied to a fresh window or tab).
	 * 
	 * <p>
	 * A window is only rendered, if the subsession ID matches to one transmitted by the client and
	 * the ID is confirmed by the client.
	 * </p>
	 * 
	 * @see WindowHandler#provideRenderToken()
	 */
	WindowId getWindowId();

}
