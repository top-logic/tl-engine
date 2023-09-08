/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

/**
 * This class compares two numbers by value.
 * This class uses the singleton pattern via {@link #INSTANCE}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class NumberComparator implements Comparator<Number> {

    /** Single instance of this class. */
    public final static NumberComparator INSTANCE = new NumberComparator();

    private NumberComparator() {
        super();
    }

    /**
	 * This Method compares two numbers.
	 * 
	 * @param o1
	 *        Number 1
	 * @param o2
	 *        Number 2
	 * @return Returns a negative integer, zero, or a positive integer as the first argument is less
	 *         than, equal to, or greater than the second.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
    @Override
	@SuppressWarnings("unchecked") // All comparable Number implementations are comparable to their own type.
	public int compare(Number o1, Number o2) {
		if ((o1.getClass() == o2.getClass()) && o1 instanceof Comparable) {
			return ((Comparable<Number>) o1).compareTo(o2);
        }

        return Double.compare(o1.doubleValue(), o2.doubleValue());
    }


}
