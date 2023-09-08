/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.Serializable;

import com.top_logic.reporting.remote.ReportStatus;
import com.top_logic.reporting.remote.ReportTicket;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReportTicketImpl implements ReportTicket, Serializable {

	private static final long serialVersionUID = 6704710678822589641L;
    
	private String reportID=null;
	private ReportStatus status=null;

	public ReportTicketImpl(String aReportID, ReportStatus aStatus) {
		reportID = aReportID;
		status   = aStatus;
	}	

	@Override
	public String getReportID() {
		return reportID;
	}

	@Override
	public ReportStatus getStatus() {
		return status;
	}
	
}
