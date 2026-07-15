/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * {@link ReactContext} that forwards every call to a wrapped delegate.
 *
 * <p>
 * Subclass or instantiate with overriding methods to augment a {@link ReactContext} with scoped
 * state (e.g. a context-menu opener) without materializing a new {@link DefaultReactContext}.
 * </p>
 */
public class ForwardingReactContext implements ReactContext {

	private final ReactContext _delegate;

	/**
	 * Creates a {@link ForwardingReactContext} wrapping the given delegate.
	 */
	public ForwardingReactContext(ReactContext delegate) {
		_delegate = delegate;
	}

	/**
	 * The wrapped delegate.
	 */
	protected final ReactContext delegate() {
		return _delegate;
	}

	@Override
	public String allocateId() {
		return _delegate.allocateId();
	}

	@Override
	public String getWindowName() {
		return _delegate.getWindowName();
	}

	@Override
	public String getContextPath() {
		return _delegate.getContextPath();
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _delegate.getSSEQueue();
	}

	@Override
	public ReactWindowRegistry getWindowRegistry() {
		return _delegate.getWindowRegistry();
	}

	@Override
	public ErrorSink getErrorSink() {
		return _delegate.getErrorSink();
	}

	@Override
	public DialogManager getDialogManager() {
		return _delegate.getDialogManager();
	}

	@Override
	public ModelScope getModelScope() {
		return _delegate.getModelScope();
	}

	@Override
	public com.top_logic.layout.react.control.overlay.ContextMenuOpener getContextMenuOpener() {
		return _delegate.getContextMenuOpener();
	}
}
