/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import org.apache.commons.collections4.comparators.BooleanComparator;

/**
 * {@link Comparator} for {@link Boolean} values that treats <code>null</code> as smallest value.
 * 
 * <p>
 * The {@link #INSTANCE} sorts in the order <code>null</code>, <code>false</code>,
 * <code>true</code>.
 * </p>
 * 
 * @see BooleanComparatorNullAsFalse
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BooleanComparatorNullSafe {

	/**
	 * Ascending {@link BooleanComparatorNullSafe}.
	 */
	@SuppressWarnings("unchecked") // Commons collection is not typed.
	public static final Comparator<Boolean> INSTANCE = 
		new NullSafeComparator<>(BooleanComparator.getFalseFirstComparator(), true);

	/**
	 * Descending {@link BooleanComparatorNullSafe}.
	 */
	@SuppressWarnings("unchecked") // Commons collection is not typed.
	public static final Comparator<Boolean> DESCENDING_INSTANCE = 
		new NullSafeComparator<>(BooleanComparator.getTrueFirstComparator(), false);

	private BooleanComparatorNullSafe() {
		// No-instance class.
	}

}
