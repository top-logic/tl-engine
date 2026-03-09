/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Transient notification message displayed at the bottom of the screen.
 *
 * <p>
 * Auto-dismisses after a configurable duration. Supports four variants (info, success, warning,
 * error) and an optional action button.
 * </p>
 */
public class ReactSnackbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLSnackbar";

	/** @see #show(String) */
	private static final String MESSAGE = "message";

	/** @see #ReactSnackbarControl(ReactContext, String, String, int, Runnable) */
	private static final String VARIANT = "variant";

	/** @see #ReactSnackbarControl(ReactContext, String, String, int, Runnable) */
	private static final String DURATION = "duration";

	/** @see #show() */
	private static final String VISIBLE = "visible";

	/** @see #setAction(String, String) */
	private static final String ACTION = "action";

	/** @see #setAction(String, String) */
	private static final String ACTION_LABEL = "label";

	/** @see #setAction(String, String) */
	private static final String ACTION_COMMAND_NAME = "commandName";

	private Runnable _dismissHandler;

	/**
	 * Creates a snackbar control.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, String variant, Runnable dismissHandler) {
		this(context, message, variant, 5000, dismissHandler);
	}

	/**
	 * Creates a snackbar control with custom duration.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 * @param duration
	 *        Auto-dismiss delay in ms (0 = sticky).
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, String variant, int duration, Runnable dismissHandler) {
		super(context, null, REACT_MODULE);
		_dismissHandler = dismissHandler;
		putState(MESSAGE, message);
		putState(VARIANT, variant);
		putState(DURATION, duration);
		putState(VISIBLE, false);
	}

	/**
	 * Shows the snackbar.
	 */
	public void show() {
		putState(VISIBLE, true);
	}

	/**
	 * Shows the snackbar with a new message.
	 *
	 * @param message
	 *        The new notification message.
	 */
	public void show(String message) {
		putState(MESSAGE, message);
		putState(VISIBLE, true);
	}

	/**
	 * Hides the snackbar.
	 */
	public void hide() {
		putState(VISIBLE, false);
	}

	/**
	 * Sets an action button on the snackbar.
	 *
	 * @param label
	 *        The button label.
	 * @param commandName
	 *        The command to send when clicked.
	 */
	public void setAction(String label, String commandName) {
		Map<String, String> action = new HashMap<>();
		action.put(ACTION_LABEL, label);
		action.put(ACTION_COMMAND_NAME, commandName);
		putState(ACTION, action);
	}

	/**
	 * Handles the dismiss command sent when the snackbar is dismissed (by timer or user).
	 */
	@ReactCommand("dismiss")
	void handleDismiss() {
		hide();
		_dismissHandler.run();
	}

}
