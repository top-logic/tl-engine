/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Pure overlay control providing backdrop, focus trap, Escape key, and backdrop-click handling.
 *
 * <p>
 * This control renders only the overlay mechanics (backdrop + child). Visual chrome (title bar,
 * footer, resize handles) is provided by {@link ReactWindowControl}, which is typically set as the
 * child of this control.
 * </p>
 */
public class ReactDialogControl extends ReactControl {

	private static final String REACT_MODULE = "TLDialog";

	private static final String CLOSE_ON_BACKDROP = "closeOnBackdrop";

	private static final String OPEN = "open";

	private static final String CHILD = "child";

	private Runnable _closeHandler;

	private ReactControl _child;

	/**
	 * Creates a dialog overlay control.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param closeOnBackdrop
	 *        Whether clicking the backdrop closes the dialog.
	 * @param closeHandler
	 *        Called when the dialog is closed (via Escape or backdrop click).
	 */
	public ReactDialogControl(ReactContext context, boolean closeOnBackdrop, Runnable closeHandler) {
		super(context, null, REACT_MODULE);
		_closeHandler = closeHandler;
		putState(CLOSE_ON_BACKDROP, closeOnBackdrop);
		putState(OPEN, false);
	}

	/**
	 * Opens the dialog.
	 */
	public void open() {
		putState(OPEN, true);
	}

	/**
	 * Closes the dialog.
	 */
	public void close() {
		putState(OPEN, false);
	}

	/**
	 * Sets the child content.
	 *
	 * @param child
	 *        The content control to display inside the overlay.
	 */
	public void setChild(ReactControl child) {
		_child = child;
		putState(CHILD, child);
	}

	@Override
	protected void cleanupChildren() {
		if (_child != null) {
			_child.cleanupTree();
		}
	}

	/**
	 * Handles the close command sent when the dialog overlay is dismissed.
	 */
	@ReactCommand("close")
	void handleClose() {
		close();
		_closeHandler.run();
	}

}
