/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

/**
 * ResultStatus returned by reports (TODO FMA)
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReportResult {
    
    /**
     * the {@link ReportStatus} with additional information for this result
     */
    public ReportStatus getReportStatus();
    
    /**
     * the String for the report if the creation was finished successfully
     * The type of the result depends on the report mode.
     *   {@link ReportDescriptor#MODE_GETVALUES}: Map
     *   {@link ReportDescriptor#MODE_SETVALUES}: byte[]
     * To get more information about the status of this report use getReportStatus()
     */
    public Object getReport();

    /**
     * the report mode, currently {@link ReportDescriptor#MODE_GETVALUES} or {@link ReportDescriptor#MODE_SETVALUES}
     */
    public String getMode();
    
}
