/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;

/**
 * Adorner of some css classes to list items, rendered by {@link DefaultListRenderer}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ListItemCssAdorner {

	/**
	 * Adds space separated css classes for a list item.
	 */
	void addClasses(Appendable out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused) throws IOException;
}
