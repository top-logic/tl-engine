/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReportTicket {

	/**
	 * the initial status of the report. 
	 */
    ReportStatus getStatus();

    /**
     * the unique ID to identify the report. Used to check the state,
     * cancel or fetch the report
     */
	String getReportID();

}
