/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.col.filter.NoValueFilter;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * Utilities for {@link TableFilterProvider} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableFilterProviderUtil {

	/**
	 * Creates a {@link ConfiguredFilter} with the {@link NoValueFilter} implementation.
	 */
	public static StaticFilterWrapper createNoValueFilter() {
		return new StaticFilterWrapper(NoValueFilter.INSTANCE, new ResourceText(I18NConstants.NO_VALUE));
	}

	/**
	 * Shortcut to prepend the given {@link ConfiguredFilter} with the
	 * {@link #createNoValueFilter()}.
	 */
	public static List<ConfiguredFilter> includeNoValueOption(ConfiguredFilter theFilter) {
		return Arrays.asList(theFilter, createNoValueFilter());
	}

}
