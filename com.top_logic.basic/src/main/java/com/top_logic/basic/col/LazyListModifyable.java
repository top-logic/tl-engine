/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Modifiable proxy for a {@link List} that is created lazily.
 * 
 * @see LazyListUnmodifyable
 * @see LazySetModifyable
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LazyListModifyable<T> implements List<T> {

	private List<T> cachedImplementation;

	private List<T> internalGetList() {
		List<T> implementation = this.cachedImplementation;
		
		if (implementation == null) {
			implementation = this.cachedImplementation = initInstance();
			assert implementation != null : "List initialization must not return null.";
		}
		
		return implementation;
	}

	/**
	 * Create the implementation list that this {@link LazyListModifyable} is a
	 * lazy proxy for.
	 * 
	 * @return The base list to which all further accesses are redirected.
	 */
	protected abstract List<T> initInstance();

	@Override
	public final T get(int index) {
		return internalGetList().get(index);
	}

	@Override
	public final int size() {
		return internalGetList().size();
	}
	
	@Override
	public boolean contains(Object o) {
		return internalGetList().contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return internalGetList().containsAll(c);
	}
	
	@Override
	public boolean isEmpty() {
		return internalGetList().isEmpty();
	}
	
	@Override
	public int indexOf(Object o) {
		return internalGetList().indexOf(o);
	}
	
	@Override
	public boolean add(T o) {
		return internalGetList().add(o);
	}

	@Override
	public void add(int index, T element) {
		internalGetList().add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return internalGetList().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return internalGetList().addAll(index, c);
	}

	@Override
	public void clear() {
		internalGetList().clear();
	}

	@Override
	public Iterator<T> iterator() {
		return internalGetList().iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return internalGetList().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return internalGetList().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return internalGetList().listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return internalGetList().remove(o);
	}

	@Override
	public T remove(int index) {
		return internalGetList().remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return internalGetList().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return internalGetList().removeAll(c);
	}

	@Override
	public T set(int index, T element) {
		return internalGetList().set(index, element);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return internalGetList().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return internalGetList().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return internalGetList().toArray(a);
	}

	// Overrides of Object.
	
	@Override
	public boolean equals(Object o) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
    	if (o == this) {
    		return true;
    	}
		return internalGetList().equals(o);
	}

	@Override
	public int hashCode() {
		return internalGetList().hashCode();
	}
	
	@Override
	public String toString() {
		return internalGetList().toString();
	}
	
}
