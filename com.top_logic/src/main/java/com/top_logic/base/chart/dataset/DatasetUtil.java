/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.dataset;

import java.util.Date;

import org.jfree.data.time.TimeSeriesCollection;

import com.top_logic.basic.StringServices;

/**
 * The DatasetUtil contains useful static methods for JFreeChart datasets.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DatasetUtil {

    /**
     * This method returns the lowest date in the given times series collection.
     * Note, the returned date is adjusted to the begin of the day.
     * 
     * @param aTimeSeriesCollection
     *        A {@link TimeSeriesCollection}.
     */
    public static Date getLowestDate (TimeSeriesCollection aTimeSeriesCollection) {
        return new Date((long)aTimeSeriesCollection.getDomainLowerBound(/* Include interval */ true));
    }
    
    /**
     * This method returns the highest date in the given times series
     * collection. Note, the returned date is adjusted to the end of the day.
     * 
     * @param aTimeSeriesCollection
     *        A {@link TimeSeriesCollection}.
     */
    public static Date getHighestDate  (TimeSeriesCollection aTimeSeriesCollection) {
        return new Date((long)aTimeSeriesCollection.getDomainUpperBound(/* Include interval */ true));
    }
    
    /**
     * This method returns the given label with the given number of spaces at
     * the beginning. Note, it is not possible to add two categories with the
     * same name (e.g. 'Category A') to a dataset. JFreeChart overwrites the
     * values in the dataset if two categroy labels are equals.
     * 
     * @param aNumberOfSpaces
     *        The number of spaces (0..n).
     * @param aLabel
     *        The label (e.g. 'Category A'). Must not be <code>null</code>.
     * @return Returns the given label with the given number of spaces at the
     *         beginning.
     */
    public static String getSpaceLabel(int aNumberOfSpaces, String aLabel) {
        if (aLabel == null) {
            throw new NullPointerException("The label must NOT be null!");
        }
        if (aNumberOfSpaces < 0) {
            throw new IllegalArgumentException("The number of spaces must be greater than 0!");
        }
        
        return StringServices.getSpaces(aNumberOfSpaces) + aLabel;
    }
    
}

