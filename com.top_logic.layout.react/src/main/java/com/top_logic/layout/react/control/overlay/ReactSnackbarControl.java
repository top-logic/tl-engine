/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.Map;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Transient notification message displayed at the bottom of the screen.
 *
 * <p>
 * Auto-dismisses after a configurable duration. Supports four {@link Variant variants} (info,
 * success, warning, error).
 * </p>
 */
public class ReactSnackbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLSnackbar";

	private static final String MESSAGE = "message";

	private static final String CONTENT = "content";

	private static final String VARIANT = "variant";

	private static final String DURATION = "duration";

	private static final String VISIBLE = "visible";

	private static final String GENERATION = "generation";

	/**
	 * The visual variant of a snackbar notification.
	 */
	public enum Variant implements ExternallyNamed {

		/** Informational notification. */
		INFO("info"),

		/** Success notification. */
		SUCCESS("success"),

		/** Warning notification. */
		WARNING("warning"),

		/** Error notification. */
		ERROR("error");

		private final String _externalName;

		Variant(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	private Runnable _dismissHandler;

	private int _generation;

	/**
	 * Creates a snackbar control.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        The visual variant.
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, Variant variant, Runnable dismissHandler) {
		this(context, message, variant, 5000, dismissHandler);
	}

	/**
	 * Creates a snackbar control with custom duration.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        The visual variant.
	 * @param duration
	 *        Auto-dismiss delay in ms (0 = sticky).
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, Variant variant, int duration,
			Runnable dismissHandler) {
		super(context, null, REACT_MODULE);
		_dismissHandler = dismissHandler;
		setMessage(message);
		setVariant(variant);
		putState(DURATION, duration);
		hide();
		putState(GENERATION, 0);
	}

	/**
	 * Sets the notification message.
	 */
	public void setMessage(String message) {
		putState(MESSAGE, message);
	}

	/**
	 * Sets the visual variant.
	 */
	public void setVariant(Variant variant) {
		putState(VARIANT, variant.getExternalName());
	}

	/**
	 * Shows the snackbar with the current message.
	 */
	public void show() {
		Object tx = beginShow();
		commitUpdate(tx);
	}

	/**
	 * Shows the snackbar with a new message.
	 *
	 * @param message
	 *        The new notification message.
	 */
	public void show(String message) {
		Object tx = beginShow();
		setMessage(message);
		commitUpdate(tx);
	}

	/**
	 * Shows the snackbar with HTML content and a variant.
	 *
	 * @param htmlContent
	 *        The HTML content to display.
	 * @param variant
	 *        The visual variant.
	 */
	public void showHtml(String htmlContent, Variant variant) {
		Object tx = beginShow();
		putState(CONTENT, htmlContent);
		setVariant(variant);
		commitUpdate(tx);
	}

	private Object beginShow() {
		_generation++;
		Object tx = beginUpdate();
		setVisible(true);
		putState(GENERATION, _generation);
		return tx;
	}

	private void setVisible(boolean visible) {
		putState(VISIBLE, visible);
	}

	/**
	 * Hides the snackbar.
	 */
	public void hide() {
		setVisible(false);
	}

	/**
	 * Handles the dismiss command sent when the snackbar is dismissed (by timer or user).
	 *
	 * <p>
	 * The generation parameter prevents a stale dismiss (from a previous snackbar that timed out)
	 * from hiding a newly shown snackbar.
	 * </p>
	 */
	@ReactCommand("dismiss")
	void handleDismiss(Map<String, Object> arguments) {
		int dismissGeneration = ((Number) arguments.getOrDefault(GENERATION, -1)).intValue();
		if (dismissGeneration != _generation) {
			return;
		}
		hide();
		_dismissHandler.run();
	}

}
