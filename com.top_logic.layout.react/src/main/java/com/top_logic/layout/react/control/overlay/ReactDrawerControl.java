/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new CloseCommand());

	private Runnable _closeHandler;

	private ReactControl _child;

	/**
	 * Creates a drawer with default settings (right position, medium size).
	 *
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(Runnable closeHandler) {
		this(null, "right", "medium", closeHandler);
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
	public ReactDrawerControl(String title, String position, String size, Runnable closeHandler) {
		super(null, REACT_MODULE, COMMANDS);
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
		putState(OPEN, true);
	}

	/**
	 * Closes the drawer.
	 */
	public void close() {
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
	 * Command sent when the drawer is closed.
	 */
	public static class CloseCommand extends ControlCommand {

		/** Creates a {@link CloseCommand}. */
		public CloseCommand() {
			super("close");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_DRAWER_CLOSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactDrawerControl drawer = (ReactDrawerControl) control;
			drawer.close();
			drawer._closeHandler.run();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
