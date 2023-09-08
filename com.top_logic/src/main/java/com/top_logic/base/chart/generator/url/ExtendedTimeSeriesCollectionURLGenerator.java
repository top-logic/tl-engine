/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.url;

import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.dataset.ExtendedTimeSeries;

/**
 * The AbstractExtendedTimeSeriesCollectionURLGenerator works together with the {@link com.top_logic.base.chart.dataset.ExtendedTimeSeries}.
 * The ExtendedTimeSeries stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getURLFor(Object)}. This object is that from the ExtendedTimeSeries.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedTimeSeriesCollectionURLGenerator implements XYURLGenerator {

    /** 
     * This method get the url from the dataset for the given series and item.
     * 
     * @see org.jfree.chart.urls.XYURLGenerator#generateURL(org.jfree.data.xy.XYDataset, int, int)
     */
    @Override
	public String generateURL(XYDataset aDataset, int aSeries, int aItem) {
        String theUrl = "";
        
        if (aDataset instanceof TimeSeriesCollection) {
            TimeSeriesCollection theCollection = (TimeSeriesCollection) aDataset;
            TimeSeries           theSeries     = theCollection.getSeries(aSeries);
            
            if (theSeries instanceof ExtendedTimeSeries) {
                ExtendedTimeSeries   theExdended = (ExtendedTimeSeries) theSeries;
                return getURLFor(theExdended.getObject(aItem));
            }
          
        }
        return theUrl;
    }

    /** 
     * This method returns the url for the given object.
     * 
     * 
     * @param anObject A object.
     */
    public abstract String getURLFor(Object anObject);
    
}

