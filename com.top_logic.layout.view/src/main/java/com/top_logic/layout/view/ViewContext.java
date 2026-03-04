/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.FrameScope;
import com.top_logic.layout.react.SSEUpdateQueue;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements
 * may create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final FrameScope _frameScope;

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ViewContext}.
	 *
	 * @param frameScope
	 *        Scope for allocating control IDs.
	 * @param sseQueue
	 *        Queue for SSE event delivery.
	 */
	public ViewContext(FrameScope frameScope, SSEUpdateQueue sseQueue) {
		_frameScope = frameScope;
		_sseQueue = sseQueue;
	}

	/**
	 * The frame scope for allocating control IDs.
	 */
	public FrameScope getFrameScope() {
		return _frameScope;
	}

	/**
	 * The SSE update queue for the current session.
	 */
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
