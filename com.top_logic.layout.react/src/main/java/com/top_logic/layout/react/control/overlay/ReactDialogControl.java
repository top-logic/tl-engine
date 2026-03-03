/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Modal dialog overlay for confirmations and focused tasks.
 *
 * <p>
 * Renders a centered modal with a semi-transparent backdrop. The dialog has a title bar, scrollable
 * body, and optional footer with action buttons. Focus is trapped inside.
 * </p>
 */
public class ReactDialogControl extends ReactControl {

	private static final String REACT_MODULE = "TLDialog";

	private static final String TITLE = "title";

	private static final String SIZE = "size";

	private static final String CLOSE_ON_BACKDROP = "closeOnBackdrop";

	private static final String OPEN = "open";

	private static final String CHILD = "child";

	private static final String ACTIONS = "actions";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new CloseCommand());

	private Runnable _closeHandler;

	private ReactControl _child;

	private List<ReactControl> _actions;

	/**
	 * Creates a dialog control.
	 *
	 * @param title
	 *        The dialog title.
	 * @param closeHandler
	 *        Called when the dialog is closed (via close button, Escape, or backdrop click).
	 */
	public ReactDialogControl(String title, Runnable closeHandler) {
		this(title, "medium", true, closeHandler);
	}

	/**
	 * Creates a dialog control with full configuration.
	 *
	 * @param title
	 *        The dialog title.
	 * @param size
	 *        "small", "medium", or "large".
	 * @param closeOnBackdrop
	 *        Whether clicking the backdrop closes the dialog.
	 * @param closeHandler
	 *        Called when the dialog is closed.
	 */
	public ReactDialogControl(String title, String size, boolean closeOnBackdrop, Runnable closeHandler) {
		super(null, REACT_MODULE, COMMANDS);
		_closeHandler = closeHandler;
		_actions = new ArrayList<>();
		putState(TITLE, title);
		putState(SIZE, size);
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
	 * Sets the dialog title.
	 *
	 * @param title
	 *        The new title.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	/**
	 * Sets the child content.
	 *
	 * @param child
	 *        The content control to display in the dialog body.
	 */
	public void setChild(ReactControl child) {
		_child = child;
		putState(CHILD, child);
	}

	/**
	 * Sets the footer action buttons.
	 *
	 * @param actions
	 *        The action controls to display in the dialog footer.
	 */
	public void setActions(List<? extends ReactControl> actions) {
		_actions = new ArrayList<>(actions);
		putState(ACTIONS, _actions);
	}

	@Override
	protected void cleanupChildren() {
		if (_child != null) {
			_child.cleanupTree();
		}
		for (ReactControl action : _actions) {
			action.cleanupTree();
		}
	}

	/**
	 * Command sent when the dialog is closed.
	 */
	public static class CloseCommand extends ControlCommand {

		/** Creates a {@link CloseCommand}. */
		public CloseCommand() {
			super("close");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_DIALOG_CLOSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactDialogControl dialog = (ReactDialogControl) control;
			dialog.close();
			dialog._closeHandler.run();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
