/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.react.control.SSEUpdateQueue;

/**
 * Adapts an old-world {@link DisplayContext} to the lean {@link ReactDisplayContext} contract.
 *
 * <p>
 * Extracts the values that view-system controls need from the richer {@link DisplayContext}.
 * </p>
 */
class DisplayContextAdapter implements ReactDisplayContext {

	private final DisplayContext _context;

	private final FrameScope _frameScope;

	private final SSEUpdateQueue _sseQueue;

	DisplayContextAdapter(DisplayContext context) {
		_context = context;
		_frameScope = context.getExecutionScope().getFrameScope();
		_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession());
	}

	@Override
	public String allocateId() {
		return _frameScope.createNewID();
	}

	@Override
	public String getWindowName() {
		return _context.getLayoutContext().getWindowId().getWindowName();
	}

	@Override
	public String getContextPath() {
		return _context.getContextPath();
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
