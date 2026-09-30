/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.navigation;

import com.top_logic.layout.react.ReactContext;

/**
 * Leads the user to the place a business object is displayed at.
 *
 * <p>
 * A control displaying a business object asks {@link #canShow(Object)} whether the object has a
 * place of its own in the application, and offers it as a link if it has. Following that link hands
 * the object to {@link #show(ReactContext, Object)}, which brings the place it belongs into view.
 * </p>
 *
 * @see ReactContext#getObjectNavigator()
 */
public interface ObjectNavigator {

	/**
	 * Whether the given value is displayed somewhere, so that it can be offered as a link.
	 *
	 * @param value
	 *        The value a control displays. Anything the application does not display - a number, a
	 *        string, an object of a type with no place of its own - answers {@code false}.
	 */
	boolean canShow(Object value);

	/**
	 * Displays the given value where it belongs.
	 *
	 * <p>
	 * Returns as soon as the display was requested, which is before the user sees it: what is
	 * displayed on the way may hold unsaved changes and ask the user how to proceed, and the
	 * display then continues once they answered. What stands in the way of displaying the value is
	 * reported to the user rather than thrown at the caller, so a control follows a link by calling
	 * this and nothing else.
	 * </p>
	 *
	 * @param context
	 *        Where the request comes from; decides which of several places is the nearest one.
	 * @param value
	 *        The value to display, one that {@link #canShow(Object)} accepts.
	 */
	void show(ReactContext context, Object value);
}
