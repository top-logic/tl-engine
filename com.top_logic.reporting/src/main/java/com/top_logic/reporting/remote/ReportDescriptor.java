/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

import java.util.Map;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReportDescriptor {
    
    /** type-indicator for PowerPoint-Reports */
    public static final String   PPT = ".ppt";
    /** type-indicator for Word-Reports */
    public static final String  WORD = ".doc";
    /** type-indicator for Excel-Reports */
    public static final String EXCEL = ".xls";

    /** mode-indicator for reading values from a file */
    public static final String MODE_GETVALUES = "GET";
    
    /** mode-indicator for creating a report from a template */
    public static final String MODE_SETVALUES = "SET";
    
    /**
     * returns the value-Map used for generating the report
     */
    public Map getValues();
    
    /**
     * returns the template for the report
     */
    public byte[] getTemplate();
    
    /** 
     * returns the name of the template-file
     */
    public String getTemplateName();
    
    /**
     * returns the type of the report. E.g.: PowerPoint, Word or Excel
     */
    public String getType();
    
    /**
     * returns the mode of the report. currently the modes {@link #MODE_GETVALUES} and {@link #MODE_SETVALUES} (default) are supported
     */
    public String getMode();

}
