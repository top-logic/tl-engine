/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;


/**
 * {@link Comparator} implementation that
 * {@link DeferredReference#get() unwrapps} {@link DeferredReference} values,
 * before forwarding them to an underlying {@link Comparator} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DeferredReferenceComparator implements Comparator {
	private final Comparator impl;

	/**
	 * Creates a new {@link DeferredReferenceComparator} that is based on the
	 * given underlying {@link Comparator} implementation.
	 */
	public DeferredReferenceComparator(Comparator impl) {
		this.impl = impl;
	}

	@Override
	public int compare(Object o1, Object o2) {
		Object value1 = 
			(o1 instanceof DeferredReference) ? ((DeferredReference) o1).get() : o1;
		Object value2 = 
			(o2 instanceof DeferredReference) ? ((DeferredReference) o2).get() : o2;

		return impl.compare(value1, value2);
	}
}