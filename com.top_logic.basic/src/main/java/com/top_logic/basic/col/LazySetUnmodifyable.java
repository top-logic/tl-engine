/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Unmodifyable proxy for a {@link Set} that is created lazily.
 * 
 * @see LazySetModifyable
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LazySetUnmodifyable<T> extends AbstractSet<T> {

	private Set<? extends T> cachedImplementation;

	private Set<? extends T> internalGetSet() {
		Set<? extends T> implementation = this.cachedImplementation;
		
		if (implementation == null) {
			implementation = this.cachedImplementation = initInstance();
			assert implementation != null : "Set initialization must not return null.";
		}
		
		return implementation;
	}

	/**
	 * Create the implementation set that this {@link LazySetModifyable} is a
	 * lazy proxy for.
	 * 
	 * @return The base set to which all further accesses are redirected.
	 */
	protected abstract Set<? extends T> initInstance();

	@Override
	public boolean isEmpty() {
		return internalGetSet().isEmpty();
	}
	
	@Override
	public final int size() {
		return internalGetSet().size();
	}
	
	@Override
	public Iterator<T> iterator() {
		// Note: Prevent modification through Iterator.remove().
		return Collections.<T> unmodifiableSet(internalGetSet()).iterator();
	}

	@Override
	public boolean contains(Object o) {
		return internalGetSet().contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return internalGetSet().containsAll(c);
	}
	
	@Override
	public Object[] toArray() {
		return internalGetSet().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return internalGetSet().toArray(a);
	}

	// Overrides of Object.
	
	@Override
	public boolean equals(Object o) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
    	if (o == this) {
    		return true;
    	}
		return internalGetSet().equals(o);
	}

	@Override
	public int hashCode() {
		return internalGetSet().hashCode();
	}
	
	@Override
	public String toString() {
		return internalGetSet().toString();
	}

}
