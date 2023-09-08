/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link Filter} with pre-computed set of accepted objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetFilter implements Filter<Object> {

	/**
	 * Configuration options for {@link SetFilter}.
	 * 
	 * <p>
	 * Note: In a configuration, the accepted objects are restricted to {@link String}s.
	 * </p>
	 */
	@TagName("equals")
	public interface Config<I extends SetFilter> extends PolymorphicConfiguration<I> {
		/**
		 * The {@link Set} of accepted {@link String}s.
		 */
		@Format(CommaSeparatedStringSet.class)
		Set<String> getValues();
	}

	/**
	 * The set of objects {@link #accept(Object) accepted} by this filter.
	 */
	private final Set<?> acceptedObjects;

	/**
	 * Creates a {@link SetFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SetFilter(InstantiationContext context, Config<?> config) {
		this(config.getValues());
	}

	/**
	 * Construct a new {@link Filter} that accepts exactly the elements of the given set.
	 */
	public SetFilter(Set<?> acceptedObjects) {
		this.acceptedObjects = acceptedObjects;
	}
	
    /**
     * Construct a new {@link Filter} that accepts exactly the elements of the
     * given Collection.
     */
    public SetFilter(Collection<?> acceptedObjects) {
        this(CollectionUtil.toSet(acceptedObjects));
    }

    @Override
	public boolean accept(Object anObject) {
		return acceptedObjects.contains(anObject);
	}
        
    /**
     * Return the string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        StringBuffer theFilters = new StringBuffer();

        theFilters.append(this.getClass().getName());
        theFilters.append(" for ");
        if (acceptedObjects != null) {
            theFilters.append(acceptedObjects);
        }
        return theFilters.toString();
    }

}
