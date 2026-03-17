/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * Simple implementation of {@link ReactContext} for the view system.
 */
public class DefaultReactContext implements ReactContext {

	private final String _contextPath;

	private final String _windowName;

	private final SSEUpdateQueue _sseQueue;

	private final ReactWindowRegistry _windowRegistry;

	/**
	 * Creates a {@link DefaultReactContext}.
	 *
	 * @param contextPath
	 *        The webapp context path.
	 * @param windowName
	 *        The window name for command routing.
	 * @param sseQueue
	 *        The SSE queue for the current session.
	 * @param windowRegistry
	 *        The window registry for the current session.
	 */
	public DefaultReactContext(String contextPath, String windowName, SSEUpdateQueue sseQueue,
			ReactWindowRegistry windowRegistry) {
		_contextPath = contextPath;
		_windowName = windowName;
		_sseQueue = sseQueue;
		_windowRegistry = windowRegistry;
	}

	/**
	 * Creates a {@link DefaultReactContext} without a window registry.
	 *
	 * @param contextPath
	 *        The webapp context path.
	 * @param windowName
	 *        The window name for command routing.
	 * @param sseQueue
	 *        The SSE queue for the current session.
	 */
	public DefaultReactContext(String contextPath, String windowName, SSEUpdateQueue sseQueue) {
		this(contextPath, windowName, sseQueue, null);
	}

	@Override
	public String allocateId() {
		return _sseQueue.allocateId();
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

	@Override
	public ReactWindowRegistry getWindowRegistry() {
		return _windowRegistry;
	}

	@Override
	public ModelScope getModelScope() {
		return _windowRegistry.getOrCreateModelScope(_windowName);
	}
}
