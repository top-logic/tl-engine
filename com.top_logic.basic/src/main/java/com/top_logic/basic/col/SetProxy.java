/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


/**
 * Abstract proxy for {@link Set}.
 * 
 * @see #impl()
 * 
 * @author Automatically generated by <code>com.top_logic.basic.generate.ProxyGenerator</code>
 */
public abstract class SetProxy<E> implements Set<E> {

	/**
	 * The underlying implementation.
	 */
	protected abstract Set<E> impl();

	@Override
	public boolean add(E a1) {
		return impl().add(a1);
	}

	@Override
	public boolean remove(Object a1) {
		return impl().remove(a1);
	}

	@Override
	public boolean equals(Object a1) {
		return impl().equals(a1);
	}

	@Override
	public int hashCode() {
		return impl().hashCode();
	}

	@Override
	public String toString() {
		return impl().toString();
	}

	@Override
	public void clear() {
		impl().clear();
	}

	@Override
	public boolean contains(Object a1) {
		return impl().contains(a1);
	}

	@Override
	public boolean isEmpty() {
		return impl().isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return impl().iterator();
	}

	@Override
	public int size() {
		return impl().size();
	}

	@Override
	public Object[] toArray() {
		return impl().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a1) {
		return impl().toArray(a1);
	}

	@Override
	public boolean addAll(Collection<? extends E> a1) {
		return impl().addAll(a1);
	}

	@Override
	public boolean containsAll(Collection<?> a1) {
		return impl().containsAll(a1);
	}

	@Override
	public boolean removeAll(Collection<?> a1) {
		return impl().removeAll(a1);
	}

	@Override
	public boolean retainAll(Collection<?> a1) {
		return impl().retainAll(a1);
	}

}
