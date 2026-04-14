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
 * Slide-in panel from a screen edge.
 *
 * <p>
 * Slides in via CSS transform. No backdrop, main content stays interactive. Optional title bar with
 * close button. Escape sends close command.
 * </p>
 */
public class ReactDrawerControl extends ReactControl {

	private static final String REACT_MODULE = "TLDrawer";

	private static final String TITLE = "title";

	private static final String POSITION = "position";

	private static final String SIZE = "size";

	private static final String OPEN = "open";

	private static final String CHILD = "child";

	private Runnable _closeHandler;

	private ReactControl _child;

	private boolean _open;

	/**
	 * Creates a drawer with default settings (right position, medium size).
	 *
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(ReactContext context, Runnable closeHandler) {
		this(context, null, "right", "medium", closeHandler);
	}

	/**
	 * Creates a drawer with full configuration.
	 *
	 * @param title
	 *        Optional title (null = no header).
	 * @param position
	 *        "left", "right", or "bottom".
	 * @param size
	 *        "narrow", "medium", or "wide".
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(ReactContext context, String title, String position, String size, Runnable closeHandler) {
		super(context, null, REACT_MODULE);
		_closeHandler = closeHandler;
		if (title != null) {
			putState(TITLE, title);
		}
		putState(POSITION, position);
		putState(SIZE, size);
		putState(OPEN, false);
	}

	/**
	 * Opens the drawer.
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
	 * Closes the drawer.
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
	 * Sets the title. Pass null to remove the header.
	 *
	 * @param title
	 *        The new title, or null.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	/**
	 * Sets the child content.
	 *
	 * @param child
	 *        The content control to display in the drawer body.
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

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (_open && _child != null) {
			_child.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_open && _child != null) {
			_child.detach();
		}
	}

	@Override
	protected void cleanupChildren() {
		if (_child != null) {
			_child.cleanupTree();
		}
	}

	/**
	 * Handles the close command sent when the drawer is closed.
	 */
	@ReactCommand("close")
	void handleClose() {
		close();
		_closeHandler.run();
	}

}
