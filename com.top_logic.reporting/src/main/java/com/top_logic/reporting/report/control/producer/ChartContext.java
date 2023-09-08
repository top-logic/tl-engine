/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.producer;

import java.util.Iterator;
import java.util.List;

import org.jfree.chart.JFreeChart;

import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.view.component.DefaultProducerFilterComponent;

/**
 * The ChartContext contains all necessary information for an chart producer 
 * to create a {@link JFreeChart}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface ChartContext {

    /**
	 * This method returns the business model of the chart context (e.g. a
	 * project).
	 */
    public Object getModel();

    /**
	 * This method sets the business model of the chart context (e.g. a
	 * project).
	 */
    public void setModel(Object aModel);
    
    /**
     * Addtional Objects derived {@link #getModel()}, ususally by some 
     *  {@link DefaultProducerFilterComponent} FilterComponent.
     * 
	 * This method returns the filtered objects (e.g. risks). The business model
	 * can be get by the {@link #getModel()} method. It isn't necessary that all
	 * chart contexts have filtered objects. In this case this method returns an
	 * empty list but NEVER <code>null</code>.
	 * 
	 * @return never <code>null</code>
	 */
    public List getFilteredObjects();

    /**
	 * This method sets the filtered objects. See {@link #getFilteredObjects()};
	 * 
	 * @param someObjects
	 *            The filtered objects must NOT be <code>null</code>..
	 */
    public void setFilteredObjects(List someObjects);
    
    /**
	 * This method returns the object that is stored for the given key. If no
	 * key is stored <code>null</code> is returned. The chart context contains
	 * all information to produce a chart and the parameters can be get by this
	 * method.
	 * 
	 * Some expected Keys (e.g. for Export) are found in the {@link ChartProducer}
	 * 
	 * @param aKey
	 *            A key for an object that is useful to generate the chart.
	 */
    public Object getValue(Object aKey);

    /**
	 * This method sets a object for the given key. See
	 * {@link #getValue(Object)}.
	 * 
	 * @param aKey
	 *            A key must NOT be <code>null</code>.
	 * @param aValue
	 *            A arbitrary object.
	 */
    public void setValue(Object aKey, Object aValue);
    
    /**
	 * This method returns an iterator of all possible key and NEVER
	 * <code>null</code>. This method can be used to find out what keys are
	 * available.
	 * 
	 * @return never <code>null</code>
	 */
    public Iterator getAllKeys();
    
    public ReportConfiguration getReportConfiguration();
 
    public void setReportConfiguration(ReportConfiguration aConfig);
}

