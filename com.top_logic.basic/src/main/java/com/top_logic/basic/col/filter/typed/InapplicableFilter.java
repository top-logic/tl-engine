/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.typed;

import com.top_logic.basic.col.filter.FalseFilter;
import com.top_logic.basic.col.filter.TrueFilter;

/**
 * A {@link TypedFilter} that always returns {@link FilterResult#INAPPLICABLE}.
 * 
 * @see TrueFilter
 * @see FalseFilter
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class InapplicableFilter extends AbstractTypedFilter<Object> {

	/**
	 * The instance of the {@link InapplicableFilter}.
	 */
	public static final InapplicableFilter INSTANCE = new InapplicableFilter();

	private InapplicableFilter() {
		super();
	}

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	@Override
	public FilterResult matchesTypesafe(Object object) {
		return FilterResult.INAPPLICABLE;
	}

}
