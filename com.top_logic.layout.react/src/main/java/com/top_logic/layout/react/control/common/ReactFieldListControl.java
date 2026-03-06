/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a vertical list of child controls via the {@code TLFieldList}
 * React component.
 *
 * <p>
 * The React component receives the following state:
 * </p>
 * <ul>
 * <li>{@code title} - optional heading text</li>
 * <li>{@code fields} - list of child {@link ReactControl} descriptors</li>
 * </ul>
 */
public class ReactFieldListControl extends ReactControl {

	private static final String REACT_MODULE = "TLFieldList";

	/** State key for the heading text. */
	private static final String TITLE = "title";

	/** State key for the list of child control descriptors. */
	private static final String FIELDS = "fields";

	/**
	 * Creates a new {@link ReactFieldListControl}.
	 *
	 * @param title
	 *        The heading text, or {@code null} for no heading.
	 * @param children
	 *        The child controls to display.
	 */
	public ReactFieldListControl(String title, List<? extends ReactControl> children) {
		super(null, REACT_MODULE);
		if (title != null) {
			putState(TITLE, title);
		}
		putState(FIELDS, new ArrayList<>(children));
	}

	/**
	 * Creates a new {@link ReactFieldListControl} without a title.
	 *
	 * @param children
	 *        The child controls to display.
	 */
	public ReactFieldListControl(List<? extends ReactControl> children) {
		this(null, children);
	}

	/**
	 * Updates the heading text.
	 *
	 * @param title
	 *        The new heading, or {@code null} to remove it.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

}
