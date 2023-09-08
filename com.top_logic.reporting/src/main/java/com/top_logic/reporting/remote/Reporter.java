/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface Reporter {

	/**
	 * Initiates the creation of a report for the template and map given in the
	 * ReportDescriptor
	 * 
	 * @param description : contains information about the report to create
	 * @return ReportTicket : contains initial information about how long it will
	 * approximately take for the report to be finished and a unique ID to fetch,
	 * cancel or poll information about the progress of the report
	 */
    public ReportTicket createReport(ReportDescriptor description); 
	
    /**
     * Returns information about how long it will approximately take for the
     * report to be finished
     * @param reportID : the unique ID gotten with the ReportTicket when initiated
     * the report
     * @return ReportStatus : contains information about state of the progress 
     * (e.g. time till finish or error)
     */
	public ReportStatus getStatus(String reportID);
	
	/**
	 * Returns the report for aReportID if the creation is finished successfully.
	 *  
	 * @return ReportResult : A result containing information about how to get
	 * the finished report, a Result with an Error status in case of an invalid reportID.
	 */
	public ReportResult getReport(String aReportID);

	/**
	 * Tries to stop the creation of the report. If the creation has not yet started
	 * or is already finished the according file will be deleted. 
	 * The file can't be deleted if the report is currently handled. 
	 */
	public void cancelReport(String reportID);
	
	/**
	 * Return a ReporterState describing the state of this reporter
	 */
	public ReporterState getReporterState();
	
	/**
	 * If this is some kind of remote-Reporter, isValid checks if its settings are valid.
	 * Even if the settings are valid, it might happen that no connection can be established! 
	 * @return true if configuration is valid, false if not (e.g. a given port is not a number)
	 */
	public boolean isValid();
	
	/**
	 * Use this to initialize the connection if this is a remote-Reporter.
	 * @return true if the connection can be established, false if not
	 */
	public boolean init();

	/**
	 * Method is called when the reporter is not longer needed. Frees any
	 * resources used by this reporter.
	 */
	public void shutDown();
	
}
