/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * The (immutable) list of {@link Integer}s starting with <code>0</code> up to a
 * given <code>size- 1</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IdentityPermutation extends AbstractList<Integer> implements RandomAccess {

	private final int _size;

	/**
	 * Creates a {@link IdentityPermutation}.
	 *
	 * @param size See {@link #size()}.
	 */
	public IdentityPermutation(int size) {
		_size = size;
	}

	@Override
	public Integer get(int index) {
		if (index >= _size) {
			throw new IndexOutOfBoundsException(index + " >= " + size());
		}
		return Integer.valueOf(index);
	}

	@Override
	public int size() {
		return _size;
	}


}
