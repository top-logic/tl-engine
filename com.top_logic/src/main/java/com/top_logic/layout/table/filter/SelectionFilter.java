/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.col.filter.TrueFilter;

/**
 * {@link AbstractConfiguredFilter} of selectable values.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectionFilter extends AbstractSelectionBasedFilter<SelectionFilterConfiguration> {

	/**
	 * Creates a new {@link SelectionFilter}
	 */
	public SelectionFilter(SelectionFilterConfiguration filterConfiguration, SelectionFilterViewBuilder viewBuilder,
			boolean showNonMatchingOptions) {
		super(filterConfiguration, viewBuilder, filterConfiguration.getSupportedObjectTypes(), showNonMatchingOptions,
			TrueFilter.INSTANCE);
	}

	@Override
	protected boolean ruleBasedFilterActive() {
		return false;
	}
}