/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.rank;

import java.util.Comparator;

/**
 * This interface defines a Distance between two Objects. 
 * 
 * This is simliar to {@link Comparator}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public interface Distance {
    
    /** The maximum distance two objects can have */
    public static final float INFINITE = Float.POSITIVE_INFINITY;
    
    /**
     * Define the distance between o1 and o2.
     * 
     * For o1.equals(o2) this <em>should</em> be 0.
     * In case o1 and o2 have no common super class this should be {@link #INFINITE}.
     * For any two A and B distance(A,B) == distance (B,A) must hold.
     */
    public float distance(Object o1, Object o2);
}

