/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Comparator;


/**
 * Comparator that compares the replacement for {@link ProtectedValue} whose value must not be
 * available after all other values (in ascending mode).
 * 
 * @see ProtectedValueReplacement
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtectedValueComparator implements Comparator<Object> {

	private final Comparator<Object> _comparator;

	private final int _restrictedCompare;

	/**
	 * Creates a new {@link ProtectedValueComparator}.
	 * 
	 * @param comparator
	 *        The comparator to use for ordinary objects.
	 * @param ascending
	 *        Whether the comparator is an ascending comparator.
	 */
	public ProtectedValueComparator(Comparator<Object> comparator, boolean ascending) {
		_comparator = comparator;
		_restrictedCompare = ascending ? 1 : -1;
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == ProtectedValueReplacement.INSTANCE) {
			if (o2 == ProtectedValueReplacement.INSTANCE) {
				return 0;
			} else {
				return _restrictedCompare;
			}
		} else {
			if (o2 == ProtectedValueReplacement.INSTANCE) {
				return -_restrictedCompare;
			} else {
				return _comparator.compare(o1, o2);
			}

		}
	}

}
