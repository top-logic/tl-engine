/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Simple implementation of {@link ReactContext} for the view system.
 */
public class DefaultReactContext implements ReactContext {

	private final String _contextPath;

	private final String _windowName;

	private final SSEUpdateQueue _sseQueue;

	private ErrorSink _errorSink;

	/**
	 * Creates a {@link DefaultReactContext}.
	 *
	 * @param contextPath
	 *        The webapp context path.
	 * @param windowName
	 *        The window name for command routing.
	 * @param sseQueue
	 *        The SSE queue for the current session.
	 */
	public DefaultReactContext(String contextPath, String windowName, SSEUpdateQueue sseQueue) {
		_contextPath = contextPath;
		_windowName = windowName;
		_sseQueue = sseQueue;
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
	public ErrorSink getErrorSink() {
		return _errorSink;
	}

	/**
	 * Sets the {@link ErrorSink} for the current rendering scope.
	 *
	 * <p>
	 * Called by the app shell control during rendering to make its snackbar available to all
	 * descendant controls.
	 * </p>
	 *
	 * @param errorSink
	 *        The error sink.
	 */
	public void setErrorSink(ErrorSink errorSink) {
		_errorSink = errorSink;
	}
}
