/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.Serializable;

import com.top_logic.reporting.remote.ReportResult;
import com.top_logic.reporting.remote.ReportStatus;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReportResultImpl implements ReportResult, Serializable {

    private static final long serialVersionUID = -7033011787941940993L;

    private Object       report;
    private String       mode;
    private ReportStatus reportStatus;
    
    public ReportResultImpl(Object aReport, ReportStatus aStatus, String aMode) {
        report       = aReport;
        reportStatus = aStatus;
        mode         = aMode;
    }
    

    /**
     * @see com.top_logic.reporting.remote.ReportResult#getReportStatus()
     */
    @Override
	public ReportStatus getReportStatus() {
        return reportStatus;
    }


    /**
     * @see com.top_logic.reporting.remote.ReportResult#getReport()
     */
    @Override
	public Object getReport() {
        return report;
    }


	/**
	 * @see com.top_logic.reporting.remote.ReportResult#getMode()
	 */
	@Override
	public String getMode() {
		return mode;
	}

}
