/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Comparator;

/**
 * {@link Comparator} defining the order by mapping objects to integer numbers.
 * 
 * @see #index(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class IndexOrder<T> implements Comparator<T> {
	@Override
	public int compare(T o1, T o2) {
		int indexValue1 = intValue(index(o1));
		int indexValue2 = intValue(index(o2));
		return Integer.compare(indexValue1, indexValue2);
	}

	private int intValue(Integer index1) {
		if (index1 == null) {
			return Integer.MAX_VALUE;
		} else {
			return index1.intValue();
		}
	}

	protected abstract Integer index(T o1);
}
