/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.base.services.simpleajax.RequestLock;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.mig.html.layout.Action;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.ToBeValidated;

/**
 * {@link LayoutContext} for the view system.
 *
 * <p>
 * Provides a {@link WindowId} for {@link com.top_logic.layout.react.ReactControl} rendering without
 * depending on a {@link MainLayout} component tree.
 * </p>
 */
public class ViewLayoutContext implements LayoutContext {

	private final WindowId _windowId;

	/**
	 * Creates a {@link ViewLayoutContext}.
	 *
	 * @param windowId
	 *        The {@link WindowId} identifying this view session.
	 */
	public ViewLayoutContext(WindowId windowId) {
		_windowId = windowId;
	}

	@Override
	public WindowId getWindowId() {
		return _windowId;
	}

	@Override
	public MainLayout getMainLayout() {
		// The view system does not use the traditional component tree.
		return null;
	}

	@Override
	public void checkUpdate(Object updater) {
		// No-op: The view system does not enforce single-threaded update checks during rendering.
	}

	@Override
	public boolean isInCommandPhase() {
		return false;
	}

	@Override
	public RequestLock getLock() {
		// The view system does not use request locking during rendering.
		return null;
	}

	@Override
	public void notifyInvalid(ToBeValidated o) {
		// No-op: Deferred validation is not supported during view rendering.
	}

	@Override
	public HandlerResult runValidation(DisplayContext context) {
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void enqueueAction(Action action) {
		// No-op: The view system does not use the action queue during rendering.
	}

	@Override
	public void forceQueueing() {
		// No-op.
	}

	@Override
	public void processActions() {
		// No-op.
	}

}
