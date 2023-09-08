/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Base class for an immutable view of a {@link Collection} that converts values
 * back and forth on demand.
 * 
 * <p>
 * Note: The mapping from original members to view members must be
 * bidirectional.
 * </p>
 * 
 * @param <O> is the original Type
 * @param <V> is the View Type
 * 
 * @see #getOriginalMember(Object)
 * @see #getOriginalMember(Object)
 * @see #isCompatible(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ImmutableSetView<O, V> extends ImmutableSet<V> {
    
	protected final Set<? extends O> original;

	public ImmutableSetView(Set<? extends O> original) {
		this.original = original;
	}
	
    @Override
	public final boolean contains(Object o) {
		if (isCompatible(o)) {
			return original.contains(getOriginalMember(o));
		} else {
			return false;
		}
	}

	@Override
	public final Iterator<V> iterator() {
		return new Iterator<>() {
			private Iterator<? extends O> inner = original.iterator();

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}

			@Override
			public V next() {
				O originalMember = inner.next();
				return getViewMember(originalMember);
			}

			@Override
			public void remove() {
				throw failure();
			}
		};
	}

	/**
	 * Whether the given object is compatible with this view.
	 * 
	 * <p>
	 * Only for compatible objects, the call to {@link #getOriginalMember(Object)} is legal.
	 * </p>
	 * 
	 * @param o
	 *        The object to check for compatibility with this view.
	 * @return Whether there is a {@link #getOriginalMember(Object) original} of the given object
	 *         with regards to this view.
	 */
	protected abstract boolean isCompatible(Object o);

	/**
	 * Finds the original for the given object that potentially could be member
	 * of the original collection this view is based on.
	 * 
	 * <p>
	 * The given object is {@link #contains(Object) contained} in this
	 * collection if and only if its {@link #getOriginalMember(Object) original}
	 * is contained in the original collection this is a view of.
	 * </p>
	 * 
	 * @param o
	 *        The object to find its original for.
	 * @return The original of the given object with regards to this view.
	 */
	protected abstract O getOriginalMember(Object o);

	/**
	 * Maps the given original to a (potential) member of this view.
	 * 
	 * <p>
	 * This is the reverse operation of {@link #getOriginalMember(Object)}.
	 * </p>
	 * 
	 * @param o
	 *        The original to find a view for.
	 * @return the view of the given original object.
	 */
	protected abstract V getViewMember(Object o);

	@Override
	public final boolean isEmpty() {
		return original.isEmpty();
	}

	@Override
	public final int size() {
		return original.size();
	}

}
