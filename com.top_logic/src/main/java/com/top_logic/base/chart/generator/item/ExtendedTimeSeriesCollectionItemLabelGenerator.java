/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.item;

import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.dataset.ExtendedTimeSeries;

/**
 * The ExtendedTimeSeriesCollectionItemLabelGenerator works together with the {@link com.top_logic.base.chart.dataset.ExtendedTimeSeries}.
 * The ExtendedTimeSeries stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getItemLabelFor(Object)}. This object is that from the ExtendedTimeSeries.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedTimeSeriesCollectionItemLabelGenerator implements XYItemRenderer {

    /** 
     * @see org.jfree.chart.labels.XYItemLabelGenerator#generateLabel(org.jfree.data.xy.XYDataset, int, int)
     */
    public String generateLabel(XYDataset aDataset, int aSeries, int aItem) {
        String theItemLabel = "";
        
        if (aDataset instanceof TimeSeriesCollection) {
            TimeSeriesCollection theCollection = (TimeSeriesCollection) aDataset;
            TimeSeries           theSeries     = theCollection.getSeries(aSeries);
            
            if (theSeries instanceof ExtendedTimeSeries) {
                ExtendedTimeSeries theExdended = (ExtendedTimeSeries)theSeries;
                return getItemLabelFor(theExdended.getObject(aItem));
            }
        } 
        
        return theItemLabel;
    }

    /**
     * This method returns the item label for the given object.
     * 
     * @param aObject
     *        A object.
     */
    public abstract String getItemLabelFor(Object aObject);

}

