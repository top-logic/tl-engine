/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Adapts an old-world {@link DisplayContext} to the lean {@link ReactContext} contract.
 *
 * <p>
 * Extracts the values that view-system controls need from the request-scoped
 * {@link DisplayContext} during construction, so the adapter can safely outlive the request.
 * </p>
 */
class DisplayContextAdapter implements ReactContext {

	private final FrameScope _frameScope;

	private final String _windowName;

	private final String _contextPath;

	private final SSEUpdateQueue _sseQueue;

	DisplayContextAdapter(DisplayContext context) {
		_frameScope = context.getExecutionScope().getFrameScope();
		_windowName = context.getLayoutContext().getWindowId().getWindowName();
		_contextPath = context.getContextPath();
		_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession());
	}

	@Override
	public String allocateId() {
		return _frameScope.createNewID();
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
