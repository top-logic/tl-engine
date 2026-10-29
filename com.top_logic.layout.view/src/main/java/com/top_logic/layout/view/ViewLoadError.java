/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.util.Resources;

/**
 * Placeholder shown in place of a view that could not be loaded.
 *
 * <p>
 * A view that fails to load replaces only itself, so the rest of the application keeps working and
 * the defect stays reachable: the navigation and the app bar still render, and the View Designer can
 * still be opened to repair the view. The placeholder names the view and reports why loading failed,
 * so the problem is visible where it occurs rather than only in the log — as an error message of the design system
 * ({@code tl-alert}), inset from the border like a page.
 * </p>
 */
public class ViewLoadError {

	/**
	 * Creates the placeholder control for a view that could not be loaded.
	 *
	 * @param context
	 *        The context to create the controls in.
	 * @param viewPath
	 *        The view that failed to load.
	 * @param problem
	 *        The reason loading failed.
	 * @return A control to render instead of the view.
	 */
	public static ReactControl createControl(ReactContext context, String viewPath, Throwable problem) {
		String title = Resources.getInstance().getString(I18NConstants.ERROR_VIEW_NOT_LOADED);
		return new ReactInsetControl(context, createAlert(context, title, viewPath, problem));
	}

	/**
	 * The error message naming the view and, below it, the cause of the failure.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param title
	 *        The title of the message.
	 * @param viewPath
	 *        The view that failed to load.
	 * @param problem
	 *        The reason loading failed.
	 * @return The error message.
	 */
	public static ReactAlertControl createAlert(ReactContext context, String title, String viewPath,
			Throwable problem) {
		ReactAlertControl alert = new ReactAlertControl(context);
		alert.setVariant(Variant.ERROR);
		alert.setTitle(title);
		String detail = detail(problem);
		alert.setMessage(detail.isEmpty() ? viewPath : viewPath + "\n" + detail);
		alert.show();
		return alert;
	}

	/**
	 * The message of the root cause, which names the offending setting; the wrapping
	 * {@link ConfigurationException}s only repeat the view path.
	 */
	private static String detail(Throwable problem) {
		Throwable cause = problem;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return message == null ? "" : message;
	}
}
