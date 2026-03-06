/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.control.SSEUpdateQueue;

/**
 * Lean rendering context for the view system.
 *
 * <p>
 * Replaces the combination of {@link com.top_logic.layout.DisplayContext},
 * {@link com.top_logic.layout.ControlScope}, {@link com.top_logic.layout.FrameScope}, and
 * {@link com.top_logic.layout.LayoutContext} for view-system rendering with a minimal contract.
 * </p>
 *
 * @see DefaultReactDisplayContext
 */
public interface ReactDisplayContext {

	/**
	 * Allocates a unique ID for a control's DOM element.
	 */
	String allocateId();

	/**
	 * The window name sent to the client for command routing.
	 */
	String getWindowName();

	/**
	 * The webapp context path for constructing URLs.
	 */
	String getContextPath();

	/**
	 * The SSE queue for pushing state updates and registering controls.
	 */
	SSEUpdateQueue getSSEQueue();

	/**
	 * Creates a {@link ReactDisplayContext} adapter from an old-world
	 * {@link com.top_logic.layout.DisplayContext}.
	 *
	 * <p>
	 * Extracts values from the richer {@link com.top_logic.layout.DisplayContext}: context path
	 * from the request, window name from
	 * {@link com.top_logic.layout.LayoutContext#getWindowId()}, ID allocation from
	 * {@link com.top_logic.layout.FrameScope#createNewID()}, and SSE queue from the HTTP session.
	 * </p>
	 */
	static ReactDisplayContext fromDisplayContext(com.top_logic.layout.DisplayContext context) {
		return new DisplayContextAdapter(context);
	}
}
