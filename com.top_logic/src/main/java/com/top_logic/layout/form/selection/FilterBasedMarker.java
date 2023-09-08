/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.list.FixedOptionMarker;

/**
 * {@link FixedOptionMarker}, that marks options as fixed, that will be accepted by a given
 * {@link Filter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterBasedMarker implements FixedOptionMarker {
	
	private Filter<Object> _fixedOptionsFilter;

	/**
	 * Create a new {@link FilterBasedMarker}.
	 */
	public FilterBasedMarker(Filter<Object> fixedOptionsFilter) {
		_fixedOptionsFilter = fixedOptionsFilter;
	}

	@Override
	public void markFixed(TagWriter out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused)
			throws IOException {
		if (_fixedOptionsFilter.accept(listItem)) {
			out.writeAttribute(OPTION_FIXED_ATTRIBUTE, true);
		}
	}
}
