/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * {@link List} implementation that supports infinite <code>add(Object)</code> 
 *  or <code>addAll(}</code> methods.
 * 
 * KHA: this looks like a List-shaped nul-device.
 * 
 * <p>
 * Calling any other method other than variants<code>add()</code> or
 * variants of <code>addAll()</code> throws an {@link UnsupportedOperationException}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class OneWayListSink implements List<Object> {
	
	/**
	 * Singleton {@link OneWayListSink} instance.
	 */
	public static final OneWayListSink INSTANCE = new OneWayListSink();

	@Override
	public boolean add(Object o) {
		return true;
	}

	@Override
	public void add(int index, Object element) {
        /* OK */
    }

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		return true;
	}
	
    @Override
	public boolean addAll(int index, Collection<? extends Object> c) {
        return true;
    }

    @Override
	public Object get(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Object> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Object> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException();
	}
}