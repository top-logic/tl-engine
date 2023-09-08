/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Default implementation of {@link FixedOptionMarker}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultFixedMarker implements FixedOptionMarker {

	/**
	 * Static instance of {@link DefaultFixedMarker}
	 */
	public static final FixedOptionMarker INSTANCE = new DefaultFixedMarker();

	private DefaultFixedMarker() {
		// Singleton
	}

	@Override
	public void markFixed(TagWriter out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused)
			throws IOException {
		if (!isSelected && !isSelectable) {
			out.writeAttribute(OPTION_FIXED_ATTRIBUTE, true);
		}
	}
}
