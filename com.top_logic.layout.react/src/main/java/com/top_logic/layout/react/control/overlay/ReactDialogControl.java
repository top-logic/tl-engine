/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
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

	/** Client state field telling whether the user can dismiss this dialog. */
	public static final String CLOSABLE = "closable";

	/** The {@link ReactCommandHandler} dismissing this dialog. */
	public static final String CLOSE_COMMAND = "close";

	private static final String OPEN = "open";

	private static final String CHILD = "child";

	private Runnable _closeHandler;

	private ReactControl _child;

	private boolean _open;

	private boolean _closable = true;

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
		putState(CLOSABLE, _closable);
	}

	/**
	 * Opens the dialog.
	 */
	public void open() {
		if (_open) {
			return;
		}
		_open = true;
		putState(OPEN, true);
		if (isAttached() && _child != null) {
			_child.attach();
		}
	}

	/**
	 * Closes the dialog.
	 */
	public void close() {
		if (!_open) {
			return;
		}
		if (_child != null) {
			_child.detach();
		}
		_open = false;
		putState(OPEN, false);
	}

	/**
	 * Sets the child content.
	 *
	 * @param child
	 *        The content control to display inside the overlay.
	 */
	public void setChild(ReactControl child) {
		if (_child != null && _open) {
			_child.detach();
		}
		_child = child;
		putState(CHILD, child);
		if (child != null && _open && isAttached()) {
			child.attach();
		}
	}

	/**
	 * Whether this dialog can be closed.
	 *
	 * @see #setClosable(boolean)
	 */
	public boolean isClosable() {
		return _closable;
	}

	/**
	 * Sets whether this dialog can be closed.
	 *
	 * <p>
	 * A dialog that is not closable stays on screen: the client neither dismisses it on Escape nor
	 * on a backdrop click, {@link #CLOSE_COMMAND} is ignored, and the {@link DialogManager} refuses
	 * every close of it. A dialog is closable unless marked otherwise.
	 * </p>
	 *
	 * @param closable
	 *        Whether the dialog may be closed.
	 */
	public void setClosable(boolean closable) {
		if (closable == _closable) {
			return;
		}
		_closable = closable;
		putState(CLOSABLE, closable);
	}

	/**
	 * Handles the close command sent when the dialog overlay is dismissed.
	 *
	 * <p>
	 * The command is ignored while the dialog is not {@link #isClosable() closable}.
	 * </p>
	 */
	@ReactCommandHandler(CLOSE_COMMAND)
	void handleClose() {
		if (!_closable) {
			return;
		}
		close();
		_closeHandler.run();
	}

}
