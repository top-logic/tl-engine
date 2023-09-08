/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractSet;
import java.util.Collection;

/**
 * A base class for {@link Collection} implementations that guarantees that the
 * contents is immutable (not modifiable through the {@link Collection}
 * interface).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ImmutableSet<E> extends AbstractSet<E> {
	
	@Override
	public final boolean add(E o) {
		throw failure();
	}

	@Override
	public final boolean addAll(Collection<? extends E> c) {
		throw failure();
	}

	@Override
	public final void clear() {
		throw failure();
	}
	
	@Override
	public final boolean remove(Object o) {
		throw failure();
	}

	@Override
	public final boolean removeAll(Collection<?> c) {
		throw failure();
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		throw failure();
	}

	protected UnsupportedOperationException failure() {
		return new UnsupportedOperationException("Immutable collection cannot be modified.");
	}

}
