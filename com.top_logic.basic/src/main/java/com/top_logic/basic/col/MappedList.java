/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * {@link List} view through a {@link Mapping}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappedList<S, D> extends AbstractList<D> {

	private final List<? extends S> _source;

	private final Mapping<S, D> _mapping;

	/**
	 * Creates a {@link MappedList}.
	 * 
	 * @param mapping
	 *        The {@link Mapping} to apply to the source elements.
	 * @param source
	 *        The source elements.
	 */
	protected MappedList(Mapping<S, D> mapping, List<? extends S> source) {
		this._mapping = mapping;
		this._source = source;
	}

	@Override
	public int size() {
		return _source.size();
	}

	@Override
	public D get(int index) {
		return _mapping.map(_source.get(index));
	}

	/**
	 * Creates a {@link MappedList} with the given {@link Mapping} and source {@link List}.
	 * 
	 * <p>
	 * The result list is {@link RandomAccess} if the specified source is.
	 * </p>
	 *
	 * @param mapping
	 *        The {@link Mapping} to apply to the source elements.
	 * @param source
	 *        The source elements.
	 */
	public static <S, D> MappedList<S, D> create(Mapping<S, D> mapping, List<? extends S> source) {
		if (source instanceof RandomAccess) {
			return new RandomAccessMappedList<>(mapping, source);
		}
		return new MappedList<>(mapping, source);
	}

	private static class RandomAccessMappedList<S, D> extends MappedList<S, D> implements RandomAccess {

		private RandomAccessMappedList(Mapping<S, D> mapping, List<? extends S> source) {
			super(mapping,  source);
			if (!(source instanceof RandomAccess)) {
				throw new IllegalArgumentException("Source is not " + RandomAccess.class.getName());
			}
		}

	}
}

