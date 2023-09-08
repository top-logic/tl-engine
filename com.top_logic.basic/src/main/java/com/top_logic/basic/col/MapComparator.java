/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.Map;

/**
 * {@link Comparator} for {@link Map}s that compares {@link Map}s according to
 * the comparison result of a delegate {@link Comparator} for values looked up
 * under a certain key.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapComparator implements Comparator {

	private final Object       compareKey;
	private final Comparator   valueComparator;

	/**
	 * Creates a {@link Comparator} for {@link Map}s that compares the values
	 * at the given key assuming .
	 */
	public MapComparator(Object compareKey) {
		this(compareKey, ComparableComparator.INSTANCE, false);
	}

	public MapComparator(Object compareKey, boolean invert) {
		this(compareKey, ComparableComparator.INSTANCE, invert);
	}
	
	public MapComparator(Object compareKey, Comparator valueComparator, boolean invert) {
		this.compareKey       = compareKey;
        if (invert)
            this.valueComparator  = new InverseComparator(valueComparator);
        else 
		    this.valueComparator  = valueComparator;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		Map m1 = (Map) o1;
		Map m2 = (Map) o2;
		
		return valueComparator.compare(m1.get(compareKey), m2.get(compareKey));
	}

}
