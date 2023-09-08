/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Reverse meaning of filter wrapped by this class.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class NOTFilter<T> implements Filter<T> {

	/**
	 * Configuration options for {@link NOTFilter}.
	 */
	@TagName("not")
	public interface Config<T, I extends NOTFilter<?>> extends DelegatingFilterConfig<T, I> {
		// Pure marker interface.
	}

    private final Filter<? super T> inner;
    
	/**
	 * Creates a {@link NOTFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NOTFilter(InstantiationContext context, Config<T, ?> config) {
		this(context.getInstance(config.getFilter()));
	}

    /**
	 * Inverts the given filter.
	 * 
	 * @see FilterFactory#not(Filter)
	 */
	NOTFilter(Filter<? super T> innerFilter) {
    	assert innerFilter != null : "Negated filter must not be null";
        inner = innerFilter;
    }

    /** 
     * return reverse of inner filter.
     */
    @Override
	public boolean accept(T anObject) {
        return !inner.accept(anObject);
    }
    
    @Override
    public String toString() {
    	return "NOT "  + inner;
    }

	Filter<? super T> getInnerFilter() {
		return inner;
	}

}
