/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Combine two {@link Filter}s using logical AND.
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ANDFilter<T> implements Filter<T> {

	/**
	 * Configuration options for {@link ANDFilter}.
	 */
	@TagName("and")
	public interface Config<T, I extends ANDFilter<?>> extends CompositeFilterConfig<T, I> {
		// Pure marker interface.
	}

    private final Filter<? super T> first;
	private final Filter<? super T> second;

	/**
	 * Creates a {@link ANDFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ANDFilter(InstantiationContext context, Config<T, ?> config) {
		this(head(context, config.getFilters()), tail(context, config.getFilters()));
	}

	private static <T> Filter<? super T> head(InstantiationContext context,
			List<PolymorphicConfiguration<? extends Filter<? super T>>> filters) {
		if (filters.isEmpty()) {
			return TrueFilter.INSTANCE;
		}
		return context.getInstance(filters.get(0));
	}

	private static <T> Filter<? super T> tail(InstantiationContext context,
			List<PolymorphicConfiguration<? extends Filter<? super T>>> filters) {
		if (filters.size() < 2) {
			return TrueFilter.INSTANCE;
		}
		Filter<? super T> second = context.getInstance(filters.get(1));
		if (filters.size() == 2) {
			return second;
		}
		return new ANDFilter<>(second, tail(context, filters.subList(1, filters.size())));
	}

	/**
	 * Combines two filters with AND.
	 * 
	 * @see FilterFactory#and(Filter, Filter)
	 */
	ANDFilter(Filter<? super T> first, Filter<? super T> second) {
		this.first = first;
		this.second = second;
    }
    
	/**
	 * Check whether both internal filters accept the given object.
	 * 
	 * The method will ask the internal filters. If one of them reject the
	 * object, this filter will also reject it.
	 * 
	 * @param anObject
	 *        The object to be checked.
	 *        
	 * @return <code>true</code>, if the given object is accepted by both
	 *         internal filters.
	 *         
	 * @see Filter#accept(Object)
	 */
    @Override
	public boolean accept(T anObject) {
    	if (!first.accept(anObject)) {
    		return false;
    	}
    	return second.accept(anObject);
    }

    /**
     * Return the string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return first + " AND " + second;
    }

}
