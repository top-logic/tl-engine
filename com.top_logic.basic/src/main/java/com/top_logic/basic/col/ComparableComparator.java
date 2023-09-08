/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Named;
import com.top_logic.basic.thread.ThreadContext;

/**
 * {@link Comparator} implementation that bases its decision on the
 * {@link Comparable#compareTo(Object)} method of the compared values.
 * 
 * <p>
 * The order implied by this comparator is extended to {@link List}s of
 * {@link ComparableComparator} items. Lists are sorted by comparing their
 * elements at corresponding indexes. The comparison stops at the first index at
 * which the compared lists differ. Shorter lists always appear before longer
 * lists, if the shorter list is a prefix of the longer list.
 * </p>
 * 
 * <p>
 * <b>Note:</b> This {@link Comparator} cannot deal with {@link List}s of
 * {@link List}s.
 * </p>
 * 
 * <p>
 * <b>Note:</b> This {@link Comparator} cannot deal with <code>null</code>
 * values in compared lists.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComparableComparator implements Comparator<Object> {

    public static final Comparator<Object> INSTANCE = new ComparableComparator();

	public static final Comparator<Object> INSTANCE_DESCENDING = Collections.reverseOrder(INSTANCE);

	/**
	 * Singleton constructor.
	 */
	protected ComparableComparator() {
		super();
	}
	
	/**
	 * Implements the {@link Comparator#compare(Object, Object)} interface by
	 * assuming the first of the given objects implements the {@link Comparable}
	 * interface.
	 * 
	 * <p>
	 * If none of the given objects is <code>null</code>, the comparison is
	 * delegated to the first given object's
	 * {@link Comparable#compareTo(Object)} method by passing the second
	 * parameter as argument.
	 * </p>
	 * 
	 * <p>
	 * If both given objects are <code>null</code>, the result of the
	 * comparison is <code>"equals"</code>. If only the first given object
	 * is <code>null</code>, the result of the comparison is
	 * <code>"less than"</code>. Otherwise, the result is
	 * <code>"greater than"</code>. 
	 * </p>
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	@Override
	public int compare(Object value1, Object value2) {
		// Unwrap potential list from value 1.
		List<?> list1;
		Object comparedValue1;
		if (value1 instanceof Collection<?>) {
			list1 = (value1 instanceof List<?>) ? (List<?>) value1 : null;
			Collection<?> coll1 = (Collection<?>) value1;
			
			Iterator<?> it1 = coll1.iterator();
			
			if (!it1.hasNext()) {
				comparedValue1 = null;
			} else {
				comparedValue1 = it1.next();
				if (!it1.hasNext()) {
					// Pretend that value 1 was not a list, but a single value.
					// This simplifies the checks below.
					list1 = null;
				}
				else {
					if (list1 == null) {
						comparedValue1 = value1;
					}
				}
			}
		} else {
			list1 = null;
			comparedValue1 = value1;
		}
		
		// Unwrap potential list from value 2.
		List<?> list2;
		Object comparedValue2;
		if (value2 instanceof Collection<?>) {
			list2 = (value2 instanceof List<?>) ? (List<?>) value2 : null;
			Collection<?> coll2 = (Collection<?>) value2;
			
			Iterator<?> it2 = coll2.iterator();
			
			if (!it2.hasNext()) {
				comparedValue2 = null;
			} else {
				comparedValue2 = it2.next();
				if (!it2.hasNext()) {
					// Pretend that value 2 was not a list, but a single value.
					// This simplifies the checks below.
					list2 = null;
				}
				else {
					if (list2 == null) {
						comparedValue2 = value2;
					}
				}
			}
		} else {
			list2 = null;
			comparedValue2 = value2;
		}

		if (comparedValue1 == null) {
			if (comparedValue2 == null) {
				return 0;
			} else {
				// The empty value (or null) is sorted before any other
				// value in ascending order (value 1 < value 2).
				return -1;
			}
		} else if (comparedValue2 == null) {
			// Value 1 is not equal to null.
			return 1;
		} else {
			// Both, value 1 and value 2 are not null (or both list values are not empty).
		    int firstResult = directCompare(comparedValue1, comparedValue2);
			
			if (firstResult != 0 || (list1 == null && list2 == null)) {
				// Both values are equal, or both lists start with the same
				// element, or the one list starts with the other value.
				return firstResult;
			} else if (list1 == null) {
				// There are no more elements in list 1 but in list 2: value 1 < value 2.
				return -1;
			} else if (list2 == null) {
				// There are no more elements in list 2 but in list 1: value 1 > value 2.
				return 1;
			} else {
				// Both values are lists and the first element of both
				// lists are equal to each other. Compare the rest of the list.
				int length1 = list1.size();
				int length2 = list2.size();
				double comparedLength = Math.min(length1, length2);
				for (int n = 1; n < comparedLength; n++) {
					int result = directCompare(list1.get(n), list2.get(n));
					if (result != 0) {
						return result;
					}
				}
				// Both lists are equal, or one list is the prefix of the other.
				return length1 - length2;
			}
		}
	}

    private int directCompare(Object aComparedValue1, Object aComparedValue2) {
		if (aComparedValue1 instanceof String && aComparedValue2 instanceof String) {
			// Compare strings user friendly in a localized way by Collator
			Collator collator = Collator.getInstance(ThreadContext.getLocale());
			return collator.compare((String) aComparedValue1, (String) aComparedValue2);
		}

		// Numbers itself are not comparable, but different implementations implement the comparable
		// interface
		if (aComparedValue1 instanceof Number && aComparedValue2 instanceof Number) {
			return NumberComparator.INSTANCE.compare((Number) aComparedValue1, (Number) aComparedValue2);
		}
		// Booleans are not comparable, therefore this code
		// (needed for example for checkbox controls to sort by the checked state)
		if (aComparedValue1 instanceof Boolean && aComparedValue2 instanceof Boolean) {
			if (aComparedValue1 == aComparedValue2)
				return 0;
			if (Boolean.TRUE.equals(aComparedValue1))
				return 1;
			return -1;
		}
		if (aComparedValue1 instanceof Comparable && aComparedValue2 instanceof Comparable) {
			try {
				return ((Comparable) aComparedValue1).compareTo(aComparedValue2);
			} catch (Exception ex) {
				// If the two values are not the same type, most compareTo() methods will throw a
				// ClassCastException. In this case, the class names of the values are compared.
				return compareClassName(aComparedValue1, aComparedValue2);
			}
		}
		// Check for Named at last to ensure that a more special Comparable-implementation wins.
		if (aComparedValue1 instanceof Named && aComparedValue2 instanceof Named) {
			// Compare strings user friendly in a localized way by Collator
			Collator collator = Collator.getInstance(ThreadContext.getLocale());
			return collator.compare(((Named) aComparedValue1).getName(), ((Named) aComparedValue2).getName());
		}

		return compareClassName(aComparedValue1, aComparedValue2);
    }

	private int compareClassName(Object value1, Object value2) {
		// This results in fact that different object types will be combined into groups
		// and each group will be sorted within itself correctly.
		return value1.getClass().getName().compareTo(value2.getClass().getName());
	}

}
