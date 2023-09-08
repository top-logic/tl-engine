/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import org.apache.commons.collections4.comparators.BooleanComparator;

/**
 * {@link Comparator} for {@link Boolean} values that treats <code>null</code> as <code>false</code>
 * .
 * 
 * <p>
 * The {@link #INSTANCE} sorts in the order {<code>null</code> or <code>false</code> ,
 * <code>true</code> .
 * </p>
 * 
 * @see NullSafeComparator Wrapper for <code>null</code>-safe comparison that allows to define where
 *      to insert <code>null</code> values.
 * @see BooleanComparatorNullSafe
 * @see BooleanComparator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BooleanComparatorNullAsFalse implements Comparator<Boolean> {

	/**
	 * Ascending {@link BooleanComparatorNullAsFalse}.
	 */
	public static final Comparator<Boolean> INSTANCE = new BooleanComparatorNullAsFalse();

	/**
	 * Descending {@link BooleanComparatorNullAsFalse}.
	 */
	public static final Comparator<Boolean> DESCENDING_INSTANCE = new InverseComparator<>(INSTANCE);

	private BooleanComparatorNullAsFalse() {
		// Singleton constructor.
	}

	@Override
	public int compare(Boolean o1, Boolean o2) {
		if (o1 != null && o1) {
			if (o2 != null && o2) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (o2 != null && o2) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
