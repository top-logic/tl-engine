/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.dataset;

import java.util.Iterator;
import java.util.List;

import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import com.top_logic.reporting.chart.axis.TreeTaskAxis;

/**
 * Extend the TaskSeriesCollectio to use the TreeTask as keys.
 * 
 * This in turn is needen by the {@link TreeTaskAxis} later.
 */
public class TreeTaskSeriesCollection extends TaskSeriesCollection {
    
    /**
     * Overriden to add the TreeTasks as keys
     */
    @Override
	public void add(TaskSeries series) {
        
        if (series == null) {
            throw new IllegalArgumentException("Null 'series' argument.");
        }
        List keys = getColumnKeys();
        List data = this.getRowKeys();
        data.add(series);
        series.addChangeListener(this);

        // TODO KHA better just use addAll?
        // -> this would violate JFreecharts policies.
        
        // look for any keys that we don't already know about...
        Iterator iterator = series.getTasks().iterator();
        while (iterator.hasNext()) {
            Comparable task =  (Comparable) iterator.next();
            int index = keys.indexOf(task);
            if (index < 0) {
                keys.add(task);
            }
        }
        fireDatasetChanged();
    }

}