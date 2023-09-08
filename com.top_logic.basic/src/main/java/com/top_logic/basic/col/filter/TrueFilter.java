/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.typed.AbstractTypedFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.InapplicableFilter;

/**
 * Filter returning always <code>true</code>.
 * 
 * @see FalseFilter
 * @see InapplicableFilter
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TrueFilter extends AbstractTypedFilter<Object> implements Filter<Object> {

    /** Use this so there is only one instance */
    public static final TrueFilter INSTANCE = new TrueFilter();

	/** Force usage of {@link #INSTANCE}. */
    private TrueFilter() {
		super();
	}

	@Override
	public Class<?> getType() {
		return Object.class;
    }

    /**
     * Return always <code>true</code>.
     * 
     * @param    anObject    The object to be checked.
     * @return   Always <code>true</code>.
     */
    @Override
	public boolean accept(Object anObject) {
        return true;
    }

	@Override
	public FilterResult matchesTypesafe(Object object) {
		return FilterResult.TRUE;
	}

	@Override
	public FilterResult matchesNull() {
		return FilterResult.TRUE;
	}

    @Override
    public String toString() {
    	return "TrueFilter";
    }
}
