/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.col.Filter;

/**
 * Base class for {@link Filter}s that are composed of several basic filters.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCompositeFilter implements CompositeFilter {

	/** The list of composed filters. */
    private final ArrayList<Filter> filters;
	
    public AbstractCompositeFilter(Filter[] someFilters) {
    	this.filters = new ArrayList<>(Arrays.asList(someFilters));
	}

	public AbstractCompositeFilter(Collection<Filter> someFilters) {
		this.filters = new ArrayList<> (someFilters);
	}

	public AbstractCompositeFilter() {
		this.filters = new ArrayList<>();
        // empty CTor, use appendFilter, later
    	
    	// Note: Changing an existing filter that is already linked to some
		// component is unsafe, because this component might not expect that
		// already computed filter results become invalid at some time. 
    	// Therefore, all filter implementation should be either immutable or
    	// observable for changes.
	}
	
	@Override
	public final int size() {
		return this.filters.size();
	}
	
	@Override
	public final Filter getFilter(int n) {
		return this.filters.get(n);
	}
	
    @Override
	public final Iterator<Filter> iterator() {
    	return Collections.unmodifiableList(this.filters).iterator();
    }
	
    /**
     * Append a filter to the list of filters
     * 
     * @param   aFilter   The filter to be appended
     */
    public void appendFilter(Filter aFilter) {
        if (aFilter != null) {
            this.filters.add(aFilter);
        }
    }

	protected final String toString(String infixOperator) {
		int cnt = size();
		StringBuilder buffer = new StringBuilder(64 + (cnt << 6)); // * 64
        buffer.append(this.getClass());
        if (cnt > 0) {
            buffer.append("( :");
            buffer.append(getFilter(0));
            for (int n = 1; n < cnt; n++) {
                buffer.append(infixOperator);
                buffer.append(getFilter(n));
            }
            buffer.append(" )");
        }
        return buffer.toString();
	}

}
