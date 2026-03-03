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
 * Renders as a {@code TLTextCell} React component showing the string representation of the cell
 * value.
 * </p>
 */
public class ReactTextCellControl extends ReactControl {

	/** State key for the displayed text. */
	private static final String TEXT = "text";

	/**
	 * Creates a new {@link ReactTextCellControl}.
	 *
	 * @param value
	 *        The value to display, or {@code null}.
	 */
	public ReactTextCellControl(Object value) {
		super(null, "TLTextCell");
		putState(TEXT, value != null ? value.toString() : "");
	}

	/**
	 * Updates the displayed text.
	 */
	public void setText(Object value) {
		putState(TEXT, value != null ? value.toString() : "");
	}
}
