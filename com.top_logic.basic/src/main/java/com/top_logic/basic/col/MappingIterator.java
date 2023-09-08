/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

import com.top_logic.basic.shared.collection.map.MappedIterator;


/**
 * A mapping iterator combines a regular {@link java.util.Iterator} with a
 * function (see {@link Mapping}.
 * 
 * The objects returned from a {@link MappingIterator} are objects returned from
 * a source {@link java.util.Iterator} transformed by a {@link Mapping mapping}
 * function.
 * 
 * @param <S>
 *        The source type. Type of the base iterator and source type of the
 *        mapping.
 * @param <D>
 *        The destination type. Type of this iterator and destination type of
 *        the mapping.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MappingIterator<S, D> extends MappedIterator<S, D> {
	/**
	 * The mapping function that translates objects returned from the
	 * {@link #source} {@link java.util.Iterator}.
	 */
	Mapping<? super S, ? extends D> mapping;

	/**
	 * Creates a {@link MappingIterator}.
	 * 
	 * @param aMapping
	 *        The {@link Mapping} to apply to the iterated values.
	 * @param aSource
	 *        See {@link MappedIterator#MappedIterator(Iterator)}
	 */
	public MappingIterator(Mapping<? super S, ? extends D> aMapping, Iterator<? extends S> aSource) {
		super(aSource);
		this.mapping = aMapping;
	}

	@Override
	public final D map(S input) {
		return mapping.map(input);
	}
	
}
