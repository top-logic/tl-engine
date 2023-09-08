/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator that compares lists lexicographically.
 * 
 * <p>
 * This comparator treats objects which are not {@link List}s as singleton lists.
 * </p>
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class FuzzyListComparator extends AbstractListComparator<Object> implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o1 instanceof List<?>) {
			List<?> list1 = (List<?>) o1;
			if (o2 instanceof List<?>) {
				List<?> list2 = (List<?>) o2;
				return compareLists(list1, list2);
			} else if (o2 == null) {
				return compareListToNull(list1);
			} else {
				return compareListToObject(list1, o2);
			}
		} else if (o1 == null) {
			if (o2 instanceof List<?>) {
				List<?> list2 = (List<?>) o2;
				return -compareListToNull(list2);
			} else if (o2 == null) {
				throw new AssertionError("Equality is checked before");
			} else {
				// Null is equal to empty list, less than everything else
				return -1;
			}
		} else {
			if (o2 instanceof List<?>) {
				List<?> list2 = (List<?>) o2;
				return -compareListToObject(list2, o1);
			} else if (o2 == null) {
				// Non empty element is greater than null
				return 1;
			} else {
				return elementCompare(o1, o2);
			}

		}
	}

	/**
	 * Empty list is equal to <code>null</code>, each other list is greater.
	 */
	private int compareListToObject(List<?> l1, Object o2) {
		assert o2 != null;
		switch (l1.size()) {
			case 0:
				return -1;
			case 1:
				return elementCompare(l1.get(0), o2);
			default:
				int firstElementCompare = elementCompare(l1.get(0), o2);
				if (firstElementCompare != 0) {
					return firstElementCompare;
				}
				return 1;
		}
	}

	/**
	 * Empty list is equal to <code>null</code>, each other list is greater.
	 */
	private int compareListToNull(List<?> l1) {
		switch (l1.size()) {
			case 0:
				return 0;
			default:
				return 1;
		}
	}

}

