/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Writer of the data-fixed attribute, to mark fixed options, rendered by
 * {@link DefaultListRenderer}, regardless of the css classes used for their styling.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface FixedOptionMarker {

	/** Data attribute to mark an option as fixed */
	public static final String OPTION_FIXED_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "fixed";

	/** Generate the data-fixed attribute, if the given list item is an fixed option. */
	void markFixed(TagWriter out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused)
			throws IOException;
}
