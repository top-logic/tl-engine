/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReportStatus {

	/**
	 * true, if there was an error during creation of the report
	 */
    boolean isError();

    /**
	 * an approximated time in seconds for the report to be finished
	 */
    int getExpectedDuration();

    /**
     * true, if the report is finished.
     */
	boolean isDone();

	/**
	 * the Exception, may be null
	 */
	Exception getException();
}
