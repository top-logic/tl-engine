/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.generator.tooltip;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import com.top_logic.reporting.report.dataset.ExtendedTask;

/**
 * This class works together with the {@link TaskSeries}.
 * The {@link ExtendedTask} stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getTooltipFor(Object)}. This object is that from the {@link ExtendedTask}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedTaskSeriesCollectionToolTipGenerator implements CategoryToolTipGenerator {

    /** 
     * This method get the tooltip from the dataset for the given series and item.
     * 
     * @see org.jfree.chart.labels.XYToolTipGenerator#generateToolTip(org.jfree.data.xy.XYDataset, int, int)
     */
    @Override
	public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
        String theTooltip = "";
        
        if (aDataset instanceof TaskSeriesCollection) {
            TaskSeriesCollection theCollection = (TaskSeriesCollection) aDataset;
            TaskSeries           theSeries     = theCollection.getSeries(aRow);
            Task                 theTask       = theSeries.get(aColumn);
            
            if (theTask instanceof ExtendedTask) {
                return getTooltipFor(((ExtendedTask)theTask).getAdditionalObject());
            }
        }
        
        return theTooltip;
    }

    /** 
     * This method returns the tooltip for the given object.
     * 
     * @param anObject A object.
     */
    public abstract String getTooltipFor(Object anObject);

}

