/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import static com.top_logic.layout.list.DefaultListRenderer.*;

import java.io.IOException;

/**
 * Adds default css classes to list items.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultCssAdorner implements ListItemCssAdorner {

	/**
	 * Static instance of {@link DefaultCssAdorner}
	 */
	public static final ListItemCssAdorner INSTANCE = new DefaultCssAdorner();

	private DefaultCssAdorner() {
		// Singleton
	}

	@Override
	public void addClasses(Appendable out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused)
			throws IOException {
		if (isSelected) {
			out.append(SELECTED_CSS_CLASS);
			if (isFocused) {
				out.append(FOCUS_CSS_CLASS);
			}
		} else {
			if (isSelectable) {
				if (isFocused) {
					out.append(FOCUS_CSS_CLASS);
				}
			} else {
				out.append(FIXED_CSS_CLASS);
			}
		}
	}
}