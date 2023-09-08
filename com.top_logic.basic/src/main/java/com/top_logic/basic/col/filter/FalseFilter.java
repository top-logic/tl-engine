/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.typed.AbstractTypedFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.InapplicableFilter;

/**
 * Filter returning always <code>false</code>.
 * 
 * @see TrueFilter
 * @see InapplicableFilter
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FalseFilter extends AbstractTypedFilter<Object> implements Filter<Object> {

    /** Use this so there is only one instance */
    public static final FalseFilter INSTANCE = new FalseFilter();

	/** Force usage of {@link #INSTANCE}. */
    private FalseFilter() {
		super();
	}

	@Override
	public Class<?> getType() {
		return Object.class;
    }

    /**
     * Return always <code>false</code>.
     * 
     * @param    anObject    The object to be checked.
     * @return   Always <code>false</code>.
     */
    @Override
	public boolean accept(Object anObject) {
        return false;
    }

	@Override
	public FilterResult matchesTypesafe(Object object) {
		return FilterResult.FALSE;
	}

	@Override
	public FilterResult matchesNull() {
		return FilterResult.FALSE;
	}

    @Override
    public String toString() {
    	return "FalseFilter";
    }
}
