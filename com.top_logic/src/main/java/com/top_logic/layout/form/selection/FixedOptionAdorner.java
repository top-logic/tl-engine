/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.list.ListItemCssAdorner;

/**
 * {@link ListItemCssAdorner}, that highlights arbitrary options as fixed options.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FixedOptionAdorner implements ListItemCssAdorner {
	
	private Filter<Object> _fixedOptions;
	
	/**
	 * Creates a {@link FixedOptionAdorner}
	 */
	public FixedOptionAdorner(Filter<Object> fixedOptions) {
		_fixedOptions = fixedOptions;
	}

	@Override
	public void addClasses(Appendable out, Object listItem, boolean isSelected, boolean isSelectable, boolean isFocused)
			throws IOException {
		if (_fixedOptions.accept(listItem)) {
			out.append("fixedSelectable");
		}
	}
}
