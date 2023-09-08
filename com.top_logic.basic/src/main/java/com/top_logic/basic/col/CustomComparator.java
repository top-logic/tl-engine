/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Comparator} that compares using a given list of objects as comparison hint.
 * 
 * <p>
 * The comparator receives a list of objects which describe the order of the objects compared by
 * this comparator. All unknown objects are equal and treated as larger than each known object.
 * </p>
 * 
 * <p>
 * It is possible to insert a {@link #wildcard() wild card} into the order hint to say
 * "all unknown object must be sorted here".
 * </p>
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CustomComparator<T> implements Comparator<T> {

	private static Object WILDCARD = new Object();

	/**
	 * Object to insert into the custom order list to indicate that all object before the wild card
	 * are less than unknown objects and all objects after the wild card are larger than unknown
	 * objects.
	 * 
	 * <p>
	 * There must only be one wild card in the custom order.
	 * </p>
	 */
	public static Object wildcard() {
		return WILDCARD;
	}

	private final int _others;

	private final Map<Object, Integer> _indexMap;

	/**
	 * Creates a new {@link CustomComparator}.
	 * 
	 * @param customOrder
	 *        The expected order.
	 */
	protected CustomComparator(Iterable<?> customOrder) {
		int others = Integer.MAX_VALUE;
		Map<Object, Integer> indexMap = new HashMap<>();
		int i = 0;
		for (Object name : customOrder) {
			if (WILDCARD.equals(name)) {
				if (others != Integer.MAX_VALUE) {
					if (others == i - 1) {
						// consecutive wild card. This is no problem.
					} else {
						throw new IllegalArgumentException("More than one wildcard in " + customOrder);
					}
				}
				others = i++;
			} else {
				indexMap.put(name, i++);
			}
		}
		_others = others;
		_indexMap = indexMap;

	}

	@Override
	public int compare(T o1, T o2) {
		int o1Index = getIndex(o1);
		int o2Index = getIndex(o2);
		if (o1Index < o2Index) {
			return -1;
		}
		if (o2Index < o1Index) {
			return 1;
		}
		assert o1Index == o2Index;
		return 0;
	}

	private int getIndex(T o) {
		Integer index = _indexMap.get(o);
		int ordinal;
		if (index != null) {
			ordinal = index.intValue();
		} else {
			ordinal = _others;
		}
		return ordinal;
	}
	
	/**
	 * Creates a new {@link CustomComparator} using given order.
	 */
	public static <T> Comparator<T> newCustomComparator(Iterable<?> customOrder) {
		return new CustomComparator<>(customOrder);
	}

}
