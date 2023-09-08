/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.generator.url;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import com.top_logic.reporting.report.dataset.ExtendedTask;

/**
 * The ExtendedTaskSeriesCollectionURLGenerator works together with the {@link TaskSeriesCollection}.
 * The {@link ExtendedTask} stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getURLFor(Object)}. This object is that from the ExtendedTask.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedTaskSeriesCollectionURLGenerator implements
        CategoryURLGenerator {

    /** 
     * @see org.jfree.chart.urls.CategoryURLGenerator#generateURL(org.jfree.data.category.CategoryDataset, int, int)
     */
    @Override
	public String generateURL(CategoryDataset aDataset, int aRow, int aCategory) {
        String theUrl = "";
        
        if (aDataset instanceof TaskSeriesCollection) {
            TaskSeriesCollection theDataset = (TaskSeriesCollection) aDataset;
            TaskSeries           theSeries  = theDataset.getSeries(aRow);
            Task                 theTask    = theSeries.get(aCategory);
            
            if (theTask instanceof ExtendedTask) {
                theUrl = getURLFor(((ExtendedTask) theTask).getAdditionalObject());
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

