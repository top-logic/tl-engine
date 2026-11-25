/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAction;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * Placeholder shown in place of a view that could not be loaded or built.
 *
 * <p>
 * A view that fails to load replaces only itself, so the rest of the application keeps working and
 * the defect stays reachable: the navigation and the app bar still render, and the View Designer can
 * still be opened to repair the view. The placeholder names the view and reports why loading failed,
 * so the problem is visible where it occurs rather than only in the log — as an error message of the design system
 * ({@code tl-alert}), inset from the border like a page.
 * </p>
 *
 * <p>
 * A unit of the view that fails while it is built (see {@link ViewUnitControl}) is replaced by a
 * placeholder of its own ({@link #createBuildErrorControl}): it tells when the failure occurred, so
 * that it can be found in the log, and offers to build the unit again. The reason of the failure is
 * shown to an administrator only, since it may reveal internals of the application; a view that
 * fails to load is a configuration error and names its reason to everybody.
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
	 * Creates the placeholder control for a unit of the view that could not be built.
	 *
	 * @param context
	 *        The context to create the controls in.
	 * @param viewPath
	 *        The view file the unit shows, {@code null} for a unit not backed by a view file.
	 * @param time
	 *        When building failed.
	 * @param problem
	 *        The reason building failed, shown to an administrator only.
	 * @param retry
	 *        Builds the unit again.
	 * @return A control to render instead of the unit.
	 */
	public static ReactControl createBuildErrorControl(ReactContext context, String viewPath, Date time,
			Throwable problem, ButtonAction retry) {
		Resources resources = Resources.getInstance();
		StringBuilder message = new StringBuilder(resources.getString(
			I18NConstants.ERROR_VIEW_NOT_DISPLAYED_TIME__TIME.fill(HTMLFormatter.getInstance().formatDateTime(time))));
		if (ThreadContext.isAdmin()) {
			if (viewPath != null) {
				message.append('\n').append(viewPath);
			}
			String detail = detail(problem);
			if (!detail.isEmpty()) {
				message.append('\n').append(detail);
			}
		}

		ReactAlertControl alert = new ReactAlertControl(context);
		alert.setVariant(Variant.ERROR);
		alert.setTitle(resources.getString(I18NConstants.ERROR_VIEW_NOT_DISPLAYED));
		alert.setMessage(message.toString());
		ReactButtonControl retryButton =
			new ReactButtonControl(context, resources.getString(I18NConstants.VIEW_RETRY), retry);
		retryButton.setTooltip(resources.getString(I18NConstants.VIEW_RETRY.tooltip()));
		alert.setActions(List.of(retryButton));
		alert.show();
		return new ReactInsetControl(context, alert);
	}

	/**
	 * The error message naming the view and, below it, the cause of the failure.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param title
	 *        The title of the message.
	 * @param viewPath
	 *        The view that failed to load, {@code null} for a view not read from a file.
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
		if (viewPath == null) {
			alert.setMessage(detail);
		} else {
			alert.setMessage(detail.isEmpty() ? viewPath : viewPath + "\n" + detail);
		}
		alert.show();
		return alert;
	}

	/**
	 * The message of the root cause, which names the offending setting; the wrapping
	 * {@link ConfigurationException}s only repeat the view path. A root cause without a message is
	 * named by its type.
	 */
	private static String detail(Throwable problem) {
		Throwable cause = problem;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return message == null ? cause.getClass().getName() : message;
	}
}
