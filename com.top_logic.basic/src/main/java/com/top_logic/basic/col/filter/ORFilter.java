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
 * Combine two {@link Filter}s using logical OR.
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ORFilter<T> implements Filter<T> {

	/**
	 * Configuration options for {@link ORFilter}.
	 */
	@TagName("or")
	public interface Config<T, I extends ORFilter<?>> extends CompositeFilterConfig<T, I> {
		// Pure marker interface.
	}

    private final Filter<? super T> first;
	private final Filter<? super T> second;

	/**
	 * Creates a {@link ORFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ORFilter(InstantiationContext context, Config<T, ?> config) {
		this(head(context, config.getFilters()), tail(context, config.getFilters()));
	}

	private static <T> Filter<? super T> head(InstantiationContext context,
			List<PolymorphicConfiguration<? extends Filter<? super T>>> filters) {
		if (filters.isEmpty()) {
			return FalseFilter.INSTANCE;
		}
		return context.getInstance(filters.get(0));
	}

	private static <T> Filter<? super T> tail(InstantiationContext context,
			List<PolymorphicConfiguration<? extends Filter<? super T>>> filters) {
		if (filters.size() < 2) {
			return FalseFilter.INSTANCE;
		}
		Filter<? super T> second = context.getInstance(filters.get(1));
		if (filters.size() == 2) {
			return second;
		}
		return new ORFilter<>(second, tail(context, filters.subList(1, filters.size())));
	}

	/**
	 * Combines two {@link Filter}s with OR.
	 * 
	 * @see FilterFactory#or(Filter, Filter)
	 */
	ORFilter(Filter<? super T> first, Filter<? super T> second) {
		this.first = first;
		this.second = second;
    }

	/**
	 * Check whether at least one internal filters accept the given object.
	 * 
	 * The method will ask the internal filters. If one of them accept the
	 * object, this filter will also accept it.
	 * 
	 * @param anObject
	 *        The object to be checked.
	 *        
	 * @return <code>true</code>, if the given object is accepted.
	 * 
	 * @see Filter#accept(Object)
	 */
    @Override
	public boolean accept(T anObject) {
    	if (first.accept(anObject)) {
    		return true;
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
        return first + " OR " + second;
    }

}
