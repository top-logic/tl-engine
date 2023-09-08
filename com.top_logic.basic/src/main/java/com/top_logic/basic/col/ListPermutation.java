/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * An (immutable) permuted view of a {@link List}.
 * 
 * <p>
 * Note: This implementation is limited to {@link RandomAccess} base
 * {@link List}s.
 * </p>
 * 
 * <p>
 * Note: This implementation is not strictly limited to permutations of the base
 * list. The index selection list is not required to contain all indices of the
 * base list and it may contain duplicate indices.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListPermutation<T> extends AbstractList<T> implements RandomAccess {

	private final List<Integer> _indices;

	private final List<? extends T> _base;

	/**
	 * Creates a {@link ListPermutation}.
	 * 
	 * @param indices
	 *        The indices of the base list to include in this view. If the index
	 *        list is a permutation of the numbers from <code>0</code> to the
	 *        size of the base list minus one, then this {@link ListPermutation}
	 *        is a permutation of the base list.
	 * @param base
	 *        The base list from which elements are taken.
	 */
	public ListPermutation(List<Integer> indices, List<? extends T> base) {
		assert base instanceof RandomAccess : "Only a random-access list can be permuted.";
		_indices = indices;
		_base = base;
	}

	@Override
	public T get(int index) {
		return _base.get(_indices.get(index));
	}

	@Override
	public int size() {
		return _indices.size();
	}

}
