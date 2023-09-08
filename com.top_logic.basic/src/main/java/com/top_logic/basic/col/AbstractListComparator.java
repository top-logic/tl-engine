/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.List;

/**
 * Abstract super class for comparators that compares lists lexicographically.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractListComparator<E> {

	/**
	 * Creates a new {@link AbstractListComparator}.
	 * 
	 */
	public AbstractListComparator() {
		// nothing to do here
	}

	/**
	 * Actual implementation of comparison of lists lexicographically.
	 */
	protected int compareLists(List<? extends E> list1, List<? extends E> list2) {
		int length1 = list1.size();
		int length2 = list2.size();
		int comparedLength = Math.min(length1, length2);
		for (int n = 0; n < comparedLength; n++) {
			int result = elementCompare(list1.get(n), list2.get(n));
			if (result != 0) {
				return result;
			}
		}
		// Both lists are equal, or one list is the prefix of the other.
		return length1 - length2;
	}

	/**
	 * Compares elements of the compared list.
	 * 
	 * <p>
	 * This is the actual compare method on the elements in the list.
	 * </p>
	 * 
	 * @param o1
	 *        The first object to be compared.
	 * @param o2
	 *        The second object to be compared.
	 * @return A negative integer,zero, or a positive integer as the first argument is less than,
	 *         equal to, or greater than the second.
	 * 
	 * @throws NullPointerException
	 *         if an argument is null and this comparator does not permit <code>null</code>
	 *         arguments
	 * @throws ClassCastException
	 *         if the arguments' types prevent them from being compared by this comparator.
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	protected abstract int elementCompare(E o1, E o2);

}

