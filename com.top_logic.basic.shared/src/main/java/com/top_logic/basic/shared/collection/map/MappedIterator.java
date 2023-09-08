/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection.map;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Iterator that applies a mapping to the iterated values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MappedIterator<S, D> implements Iterator<D> {
	
	/**
	 * The {@link java.util.Iterator} that provides the input objects for the
	 * {@link #map(Object)} function.
	 */
	Iterator<? extends S> source;

	/**
	 * Creates a {@link MappedIterator}.
	 *
	 * @param aSource The source iterator.
	 */
	public MappedIterator(Iterator<? extends S> aSource) {
		this.source  = aSource;
	}

	/**
	 * Checks, whether the {@link #source} iterator has more elements.
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	/**
	 * the next result from the {@link #map(Object)} function called with
	 *         the next object returned from the {@link #source} iterator.
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public D next() {
		return map(source.next());
	}

	/**
	 * Removes the last object returned by {@link #next()} from the
	 * {@link #source} iterator.
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		source.remove();
	}
	
	/**
	 * See {@link Function#apply(Object)}.
	 */
	protected abstract D map(S input);

}
