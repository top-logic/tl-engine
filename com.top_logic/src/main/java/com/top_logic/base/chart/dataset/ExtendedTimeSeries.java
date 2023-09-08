/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.dataset;

import java.util.HashMap;

import org.jfree.data.general.SeriesException;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;

/**
 * The ExtendedTimeSeries extends the {@link org.jfree.data.time.TimeSeries}. The 
 * ExtendedTimesSeries stores additionally objects.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExtendedTimeSeries extends TimeSeries {

    /** The additional objects. */
    HashMap<String, Object> objects;

    /** 
     * Creates a {@link ExtendedTimeSeries} with the
     * given parameters.
     */
    public ExtendedTimeSeries(String aName) {
        super(aName);
        
        this.objects = new HashMap<>();
    }

    /** 
     * Creates a {@link ExtendedTimeSeries} with the
     * given parameters.
     */
    public ExtendedTimeSeries(String aName, Class<?> aTimePeriodClass) {
		super(aName);
        
        this.objects = new HashMap<>();
    }
    
   // Methods
    
   /**
     * @see org.jfree.data.time.TimeSeries#add(org.jfree.data.time.RegularTimePeriod, double)
     */
    public void add(RegularTimePeriod aTime, double aValue, Object anObject) {
        super.add(aTime, aValue);
        this.objects.put(String.valueOf(getItemCount() - 1), anObject);
    }

    /** 
     * @see org.jfree.data.time.TimeSeries#add(org.jfree.data.time.RegularTimePeriod, java.lang.Number)
     */
    public void add(RegularTimePeriod aTime, Number aNumber, Object anObject) {
        add(aTime, aNumber.doubleValue(), anObject);
    }
 
    /** 
     * @see org.jfree.data.time.TimeSeries#update(org.jfree.data.time.RegularTimePeriod, java.lang.Number)
     */
    public void update(RegularTimePeriod aTime, double aValue, Object anObject) {
        super.update(aTime, Double.valueOf(aValue));
        this.objects.put(String.valueOf(getIndex(aTime)), anObject);
    }
    
    /**
     * This method is a convenience method because the
     * {@link #add(RegularTimePeriod, double, Object)} throws a exception if you
     * try to add two values add the same time period and the
     * {@link #update(RegularTimePeriod, double, Object)} method throws a
     * exception if no value exists add the update time period.
     * 
     * @param aTime
     *        A {@link RegularTimePeriod}.
     * @param aValue
     *        A double value.
     * @param anObject
     *        An object.
     */
    public void addOrUpdate(RegularTimePeriod aTime, double aValue, Object anObject) {
        try {
            add(aTime, aValue, anObject);
        } catch (SeriesException se) {
            update(aTime, aValue, anObject);
        }
    }
    
    /** 
     * This method returns the object at the given index.
     */
    public Object getObject (int aIndex) {
        return this.objects.get(String.valueOf(aIndex));
    }

}

