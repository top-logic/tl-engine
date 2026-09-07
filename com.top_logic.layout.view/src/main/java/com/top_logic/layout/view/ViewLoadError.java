/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.util.Resources;

/**
 * Placeholder shown in place of a view that could not be loaded.
 *
 * <p>
 * A view that fails to load replaces only itself, so the rest of the application keeps working and
 * the defect stays reachable: the navigation and the app bar still render, and the View Designer can
 * still be opened to repair the view. The placeholder names the view and reports why loading failed,
 * so the problem is visible where it occurs rather than only in the log.
 * </p>
 */
public class ViewLoadError {

	/** CSS class of the placeholder, and prefix of the CSS classes of its parts. */
	private static final String CSS_CLASS = "tlViewError";

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
		List<ReactControl> lines = new ArrayList<>();
		lines.add(new ReactTextControl(context,
			Resources.getInstance().getString(I18NConstants.ERROR_VIEW_NOT_LOADED),
			CSS_CLASS + "__title"));
		lines.add(new ReactTextControl(context, viewPath, CSS_CLASS + "__view"));

		String detail = detail(problem);
		if (!detail.isEmpty()) {
			lines.add(new ReactTextControl(context, detail, CSS_CLASS + "__detail"));
		}

		ReactStackControl placeholder = new ReactStackControl(context, lines);
		placeholder.setCssClass(CSS_CLASS);
		return placeholder;
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
