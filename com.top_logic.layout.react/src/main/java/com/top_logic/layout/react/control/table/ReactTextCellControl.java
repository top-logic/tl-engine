/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactControl;

/**
 * A simple read-only cell control that displays a text value.
 *
 * <p>
 * Renders as a {@code TLTextCell} React component showing the given text string.
 * </p>
 *
 * <p>
 * The caller is responsible for converting application objects to display strings, e.g. by using a
 * {@link com.top_logic.layout.LabelProvider}.
 * </p>
 */
public class ReactTextCellControl extends ReactControl {

	/** State key for the displayed text. */
	private static final String TEXT = "text";

	/**
	 * Creates a new {@link ReactTextCellControl}.
	 *
	 * @param text
	 *        The text to display, or {@code null}.
	 */
	public ReactTextCellControl(String text) {
		super(null, "TLTextCell");
		putState(TEXT, text != null ? text : "");
	}

	/**
	 * Updates the displayed text.
	 *
	 * @param text
	 *        The new text to display, or {@code null}.
	 */
	public void setText(String text) {
		putState(TEXT, text != null ? text : "");
	}
}
