/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;


/**
 * An Iterator using Filters to do its Testing.
 * 
 * Historically both aproachens (Filtered Iterator and Filter)
 * both where used. This is a usefull synthesis of both.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public final class FilterIterator<T> extends FilteredIterator<T> {    

    /** The filter used for testing */
    protected Filter<? super T> filter;

	/**
	 * Creates a {@link FilterIterator}.
	 * 
	 * @param source
	 *        See {@link TransformIterator#TransformIterator(Iterator)}.
	 * @param aFilter
	 *        The {@link Filter} that must match source elements.
	 */
    public FilterIterator(Iterator<? extends T> source, Filter<? super T> aFilter) {
        super(source);
        filter = aFilter;
    }

    @Override
	protected boolean test(T value) {
    	return filter.accept(value);
    }

}
