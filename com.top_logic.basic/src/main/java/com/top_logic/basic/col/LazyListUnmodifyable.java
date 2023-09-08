/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * Unmodifyable proxy for a {@link List} that is created lazily.
 * 
 * @see LazyListModifyable
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LazyListUnmodifyable<T> extends AbstractList<T> {

	private List<? extends T> cachedImplementation;

	private List<? extends T> internalGetList() {
		List<? extends T> implementation = this.cachedImplementation;
		
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
	protected abstract List<? extends T> initInstance();

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
	public int lastIndexOf(Object o) {
		return internalGetList().lastIndexOf(o);
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
