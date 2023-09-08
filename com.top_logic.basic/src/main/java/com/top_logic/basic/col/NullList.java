/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractList;
import java.util.List;

/**
 * Unmodifiable constant {@link List} of a given length containing only
 * <code>null</code> values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NullList<E> extends AbstractList<E> {
	
    private final int size;

	/**
	 * Creates an unmodifiable {@link List} of the given size containing only
	 * <code>null</code> values.
	 */
	public NullList(int size) {
		this.size = size;
	}

    /**
     * Always return null (for valid index).
     * 
     * @throws IndexOutOfBoundsException as of contract with List.
     */
	@Override
	public E get(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		return null;
	}

	@Override
	public int size() {
		return size;
	}
}