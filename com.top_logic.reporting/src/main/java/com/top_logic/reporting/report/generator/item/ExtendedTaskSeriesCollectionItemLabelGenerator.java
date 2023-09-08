/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.generator.item;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import com.top_logic.reporting.report.dataset.ExtendedTask;

/**
 * The ExtendedTimeSeriesCollectionItemLabelGenerator works together with the {@link ExtendedTask}.
 * The ExtendedTask stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getItemLabelFor(Object)}. This object is that from the ExtendedTask.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedTaskSeriesCollectionItemLabelGenerator implements CategoryItemLabelGenerator {

    @Override
	public String generateColumnLabel(CategoryDataset aDataset, int aColumn) {
        return aDataset.getColumnKey(aColumn).toString();
    }
    
    @Override
	public String generateLabel(CategoryDataset aDataset, int aRow, int aColumn) {
        String theItemLabel = "";
        
        if (aDataset instanceof TaskSeriesCollection) {
            TaskSeriesCollection theCollection = (TaskSeriesCollection) aDataset;
            TaskSeries           theSeries     = theCollection.getSeries(aRow);
            Task                 theTask       = theSeries.get(aColumn);
            
            if (theTask instanceof ExtendedTask) {
                return getItemLabelFor(((ExtendedTask) theTask).getAdditionalObject());
            }
        } 
        
        return theItemLabel;
    }

    @Override
	public String generateRowLabel(CategoryDataset aDataset, int aRow) {
        return aDataset.getRowKey(aRow).toString();
    }
    
    /**
     * This method returns the item label for the given object.
     * 
     * @param aObject
     *        A object.
     */
    public abstract String getItemLabelFor(Object aObject);

}

