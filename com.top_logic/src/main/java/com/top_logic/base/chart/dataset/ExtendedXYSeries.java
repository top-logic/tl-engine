/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.dataset;

import java.util.HashMap;

import org.jfree.data.xy.XYSeries;

/**
 * The ExtendedXYSeries extends the {@link org.jfree.data.xy.XYSeries}. The 
 * ExtendedXYSeries stores additionally objects.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExtendedXYSeries extends XYSeries {

    /** The additional objects. */
    HashMap objects;

    /** 
     * Creates a {@link ExtendedXYSeries} with the
     * given parameters.
     */
    public ExtendedXYSeries(Comparable aName) {
        super(aName, /* Auto-sort*/ false, /* Allow duplicate x-values */ true);
        
        this.objects = new HashMap(50);
    }

    /** 
     * @see org.jfree.data.xy.XYSeries#add(double, double)
     */
    public void add(double aX, double aY, Object anObject) {
        super.add(aX, aY);
        
         this.objects.put(String.valueOf(getItemCount() - 1), anObject);
    }

    /** 
     * @see org.jfree.data.xy.XYSeries#add(java.lang.Number, java.lang.Number)
     */
    public void add(Number aX, Number aY, Object anObject) {
        add(aX.doubleValue(), aY.doubleValue(), anObject);
    }
    
    /** 
     * This method returns the object at the given index.
     */
    public Object getObject (int aIndex) {
        return this.objects.get(String.valueOf(aIndex));
    }
    
}

