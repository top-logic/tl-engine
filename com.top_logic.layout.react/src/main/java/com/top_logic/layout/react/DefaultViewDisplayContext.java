/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple implementation of {@link ViewDisplayContext} for the view system.
 */
public class DefaultViewDisplayContext implements ViewDisplayContext {

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private final String _contextPath;

	private final String _windowName;

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a {@link DefaultViewDisplayContext}.
	 *
	 * @param contextPath
	 *        The webapp context path.
	 * @param windowName
	 *        The window name for command routing.
	 * @param sseQueue
	 *        The SSE queue for the current session.
	 */
	public DefaultViewDisplayContext(String contextPath, String windowName, SSEUpdateQueue sseQueue) {
		_contextPath = contextPath;
		_windowName = windowName;
		_sseQueue = sseQueue;
	}

	@Override
	public String allocateId() {
		return "v" + _nextId.getAndIncrement();
	}

	@Override
	public String getWindowName() {
		return _windowName;
	}

	@Override
	public String getContextPath() {
		return _contextPath;
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
