/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link Collection} view that allows accessing a {@link Collection} of {@link ObjectData}
 * instances as their {@link DefaultSharedObject} handles.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class HandleCollectionWrapper<T extends DefaultSharedObject> extends AbstractCollection<T> {
	private final Collection<? extends ObjectData> _objs;

	/**
	 * Creates a {@link HandleCollectionWrapper}.
	 *
	 * @param objs
	 *        The underlying {@link ObjectData}s.
	 */
	public HandleCollectionWrapper(Collection<? extends ObjectData> objs) {
		_objs = objs;
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends ObjectData> base = _objs.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return base.hasNext();
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				return (T) base.next().handle();
			}

			@Override
			public void remove() {
				base.remove();
			}
		};
	}

	@Override
	public int size() {
		return _objs.size();
	}
}