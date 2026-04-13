/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A simple read-only control that displays a text value as a {@code <span>}.
 *
 * <p>
 * Renders as a {@code TLText} React component. An optional CSS class can be appended to the
 * default {@code tlText} class for custom styling.
 * </p>
 *
 * <p>
 * The caller is responsible for converting application objects to display strings, e.g. by using a
 * {@link com.top_logic.layout.LabelProvider}.
 * </p>
 */
public class ReactTextControl extends ReactControl {

	private static final String TEXT = "text";

	private static final String CSS_CLASS = "cssClass";

	/**
	 * Creates a {@link ReactTextControl} without an extra CSS class.
	 */
	public ReactTextControl(ReactContext context, String text) {
		this(context, text, null);
	}

	/**
	 * Creates a {@link ReactTextControl}.
	 *
	 * @param text
	 *        The text to display, or {@code null}.
	 * @param cssClass
	 *        Additional CSS class to append to the default {@code tlText} class, or {@code null}.
	 */
	public ReactTextControl(ReactContext context, String text, String cssClass) {
		super(context, null, "TLText");
		putState(TEXT, text != null ? text : "");
		if (cssClass != null) {
			putState(CSS_CLASS, cssClass);
		}
	}

	/**
	 * Updates the displayed text.
	 */
	public void setText(String text) {
		putState(TEXT, text != null ? text : "");
	}

	/**
	 * Updates the additional CSS class.
	 *
	 * @param cssClass
	 *        Additional CSS class to append, or {@code null} to clear.
	 */
	public void setCssClass(String cssClass) {
		putState(CSS_CLASS, cssClass != null ? cssClass : "");
	}
}
