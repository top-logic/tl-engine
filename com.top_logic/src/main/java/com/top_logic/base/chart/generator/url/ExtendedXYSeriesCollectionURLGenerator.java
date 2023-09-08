/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.url;

import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.top_logic.base.chart.dataset.ExtendedXYSeries;

/**
 * The AbstractExtendedXYSeriesCollectionURLGenerator works together with the {@link com.top_logic.base.chart.dataset.ExtendedXYSeries}.
 * The ExtendedXYSeries stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getURLFor(Object)}. This object is that from the ExtendedXYSeries.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedXYSeriesCollectionURLGenerator implements XYURLGenerator {

    /** 
     * This method get the url from the dataset for the given series and item.
     * 
     * @see org.jfree.chart.urls.XYURLGenerator#generateURL(org.jfree.data.xy.XYDataset, int, int)
     */
    @Override
	public String generateURL(XYDataset aDataset, int aSeries, int aItem) {
        String url = "";
        
        if (aDataset instanceof XYSeriesCollection) {
            XYSeriesCollection theCollection = (XYSeriesCollection) aDataset;
            XYSeries           theSeries     = theCollection.getSeries(aSeries);
            
            if (theSeries instanceof ExtendedXYSeries) {
                ExtendedXYSeries   theExdended = (ExtendedXYSeries) theSeries;
                return getURLFor(theExdended.getObject(aItem));
            }
        }

        return url;
    }

    /** 
     * This method returns the url for the given object.
     * 
     * 
     * @param anObject A object.
     */
    public abstract String getURLFor(Object anObject);
    
}

