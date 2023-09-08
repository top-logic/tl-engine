/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection.map;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * {@link Collection} view through a {@link Function}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappedCollection<S, D> extends AbstractCollection<D> {

	private final Collection<? extends S> _source;

	private final Function<S, D> _mapping;

	/**
	 * Creates a {@link MappedCollection}.
	 * 
	 * @param mapping
	 *        The {@link Function} to apply to the source elements.
	 * @param source
	 *        The source elements.
	 */
	public MappedCollection(Function<S, D> mapping, Collection<? extends S> source) {
		this._mapping = mapping;
		this._source = source;
	}

	@Override
	public int size() {
		return _source.size();
	}

	@Override
	public boolean isEmpty() {
		return _source.isEmpty();
	}

	@Override
	public Iterator<D> iterator() {
		final Function<S, D> mapping = _mapping;
		return new MappedIterator<S, D>(_source.iterator()) {
			@Override
			protected D map(S input) {
				return mapping.apply(input);
			}
		};
	}

}
