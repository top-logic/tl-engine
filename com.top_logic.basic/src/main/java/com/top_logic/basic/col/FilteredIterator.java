/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * This wraps an iterator and returns only elements matching a test.
 * 
 * <p>
 * Here an Example of an instanceof filter, using an anonymous class:
 * </p>
 * 
 * <xmp>
 * Iterator filter = new FilteredIterator(someIterator) {
 *    protected boolean acceptSource(T aNumber) {
 *         return aNumber instanceof Integer;
 *    }
 * };
 *</xmp>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class FilteredIterator<T> extends TransformIterator<T, T> {

	/**
	 * Creates a {@link FilteredIterator}.
	 *
	 * @param source See {@link TransformIterator#TransformIterator(Iterator)}.
	 */
    public FilteredIterator(Iterator<? extends T> source) {
    	super(source);
    }
    
    @Override
	protected final T transform(T value) {
    	return value;
    }
    
    @Override
	protected final boolean acceptDestination(T value) {
    	return true;
    }

}
