/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Filters objects based on their classes. 
 * 
 * Only those objects are accepted by this filter that are instances of a given
 * class (or interface) or its subclasses (or subinterfaces).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassFilter<T> extends AbstractClassFilter<T> {
	
	/**
	 * Configuration options for {@link ClassFilter}.
	 */
	@TagName("type-cast")
	public interface Config<T, I extends ClassFilter<T>> extends DelegatingFilterConfig<T, I> {

		/**
		 * The required Java type of accepted objects.
		 */
		@Mandatory
		Class<? extends T> getType();

	}

	private final Filter<? super T> innerFilter;

	/**
	 * Creates a {@link ClassFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ClassFilter(InstantiationContext context, Config<T, ?> config) {
		this(config.getType(), context.getInstance(config.getFilter()));
	}

	/**
	 * Creates a {@link ClassFilter}.
	 *
	 * @see FilterFactory#createClassFilter(Class, Filter)
	 */
	ClassFilter(Class<? extends T> testClass, Filter<? super T> innerFilter) {
		super(testClass);
		this.innerFilter = innerFilter;
	}

	@Override
	protected boolean internalAccept(T object) {
		return innerFilter.accept(object);
	}


}
