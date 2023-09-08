/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This {@link Enumeration} is an Adapter for using an {@link Iterator} as an {@link Enumeration}.
 * 
 * @see EnumerationAdapterIterator
 * @see IteratorUtil
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class IteratorAdapterEnumeration<T> implements Enumeration<T> {
	
	private final Iterator<T> iterator;
	
	/**
	 * @throws NullPointerException if the iterator is <code>null</code>.
	 */
	public IteratorAdapterEnumeration(Iterator<T> iterator) {
		if (iterator == null) {
			throw new NullPointerException("The iterator to adapt must not be null!");
		}
		this.iterator = iterator;
	}
	
	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}
	
	@Override
	public T nextElement() {
		return iterator.next();
	}
	
	@Override
	public String toString() {
		return getClass().getCanonicalName() + " (Adapting iterator: " + iterator.toString() + ")";
	}
	
}