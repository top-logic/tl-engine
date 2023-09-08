/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * The FilterVO is a simple value object.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class FilterVO implements ChartContext {

    private Object model;
    private List   filteredObjects;
    private Map    parameters;
    
    private ReportConfiguration config;

    /** Creates a {@link FilterVO}. */
    public FilterVO() {
        this(null);
    }
    
    /** Creates a {@link FilterVO}. */
    public FilterVO(Object aModel) {
        this(aModel, Collections.EMPTY_LIST);
    }
    
    /** Creates a {@link FilterVO}. */
    public FilterVO(Object aModel, List someFilteredObjects) {
        this(aModel, someFilteredObjects, RevisedReport.class);
    }

    protected FilterVO(Object aModel, List someFilteredObjects, Class aReportImplementationClass) {
        this.model           = aModel;
        this.filteredObjects = someFilteredObjects;
        this.parameters      = new HashMap();
        try {
            this.config          = ReportFactory.getInstance().createReportConfiguration(aReportImplementationClass);
        } catch (ConfigurationException c) {
            throw new ConfigurationError("invalid report configuration", c);
        }
    }
    
    public FilterVO(Object aModel, List someFilteredObjects, ReportConfiguration aConf) {
        this.model           = aModel;
        this.filteredObjects = someFilteredObjects;
        this.parameters      = new HashMap();
        this.config          = aConf;
    }

    /** This method returns the model of this value object. */
    @Override
	public Object getModel() {
        return this.model;
    }
    
    /** See {@link #getModel()}. */
    @Override
	public void setModel(Object aModel) {
        this.model = aModel;
    }
    
    /**
     * This method returns the filtered objects. The filter objects are
     * never null but maybe an empty list.
     */
    @Override
	public List getFilteredObjects() {
        return this.filteredObjects != null ? this.filteredObjects : Collections.EMPTY_LIST;
    }

    /** See {@link #getFilteredObjects()}. */
    @Override
	public void setFilteredObjects(List someFilteredObjects) {
        this.filteredObjects = someFilteredObjects;
    }

    /**
     * This method returns the stored value for the given key. The
     * additional values can be used to store any useful information.
     * The filtered objects are not enough in many cases.
     * 
     * @param aKey
     *        A key. Must not be <code>null</code>.
     */
    @Override
	public Object getValue(Object aKey) {
        return this.parameters.get(aKey);
    }
    
    /** See {@link #getValue(Object)}. */
    @Override
	public void setValue(Object aKey, Object aParameter) {
        
        if (Logger.isDebugEnabled(FilterVO.class) && "report".equals(aKey)) {
            try {
                // just for stacktrace
                throw new RuntimeException();
            } catch (Exception e) {
                Logger.info("Caching of report on filterVO is deprecated", e, FilterVO.class);
            }
        }
        
        this.parameters.put(aKey, aParameter);
    }

    @Override
	public Iterator getAllKeys() {
        return this.parameters.keySet().iterator();
    }
 
    @Override
	public ReportConfiguration getReportConfiguration() {
        return this.config;
    }
    
    @Override
	public void setReportConfiguration(ReportConfiguration aConfig) {
        this.config = aConfig;
    }
}
    


