/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This {@link Iterator} is an Adapter for using an {@link Enumeration} as an {@link Iterator}.
 * 
 * @see IteratorAdapterEnumeration
 * @see IteratorUtil
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class EnumerationAdapterIterator<T> implements Iterator<T> {
	
	private final Enumeration<T> enumeration;
	
	/**
	 * @throws NullPointerException if the enumeration is <code>null</code>.
	 */
	public EnumerationAdapterIterator(Enumeration<T> enumeration) {
		if (enumeration == null) {
			throw new NullPointerException("The enumeration to adapt must not be null!");
		}
		this.enumeration = enumeration;
	}
	
	@Override
	public boolean hasNext() {
		return enumeration.hasMoreElements();
	}
	
	@Override
	public T next() {
		return enumeration.nextElement();
	}
	
	/**
	 * Not supported.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("This Iterator is an adapter / wrapper around an Enumeration. And Enumerations don't have a 'remove' method.");
	}
	
	@Override
	public String toString() {
		return getClass().getCanonicalName() + " (Adapting enumeration: " + enumeration.toString() + ")";
	}
	
}