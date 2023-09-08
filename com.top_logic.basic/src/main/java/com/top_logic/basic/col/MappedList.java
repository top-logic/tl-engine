/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.List;

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
	public MappedList(Mapping<S, D> mapping, List<? extends S> source) {
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

}
