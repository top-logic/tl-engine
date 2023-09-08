/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;


/**
 * {@link Iterable} that is a mapped view of a source {@link Iterable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappedIterable<S, D> implements Iterable<D> {

	private final Mapping<S, D> mapping;
	protected final Iterable<? extends S> source;

	/**
	 * Creates a {@link MappedIterable}.
	 * 
	 * @param mapping
	 *        The mapping to apply to all elements of the given source
	 *        {@link Iterable}.
	 * @param source
	 *        The source of elements to be mapped.
	 */
	public MappedIterable(Mapping<S, D> mapping, Iterable<? extends S> source) {
		this.mapping = mapping;
		this.source = source;
	}

	@Override
	public Iterator<D> iterator() {
		return new MappingIterator<S, D>(mapping, source.iterator());
	}

}
