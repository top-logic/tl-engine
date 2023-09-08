/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.io.BinaryContent;


/**
 * This is the last fallback we can do if no valid report type is given.
 * This reportHandler does nothing and its ReportResult object is an error object.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class FallbackNoopReportHandler implements ReportHandler {

    /** this result is in fact an error here! */
    private ReportResult reportResult;
    
    /** the reason this handler was created */
    private String reason;
    
    public FallbackNoopReportHandler(String aReason) {
        super();
        reason = aReason;
    }

    /** 
     * as we have nothing to report this is inherently finished
     * @see com.top_logic.reporting.office.ReportHandler#reportFinished()
     */
    @Override
	public boolean reportFinished() {
        return true;
    }

    /**
     * @see com.top_logic.reporting.office.ReportHandler#prepare()
     */
    @Override
	public void prepare() {
        // do nothing at all
    }

    /**
     * @see com.top_logic.reporting.office.ReportHandler#processReport()
     */
    @Override
	public void processReport() {
        // do nothing at all
    }

    /**
     * create a ReportResult with an error as this handler cannot be the right one 
     * for a real report.
     * @see com.top_logic.reporting.office.ReportHandler#finishReport()
     */
    @Override
	public void finishReport() {
		reportResult = new ReportResult(false, (BinaryContent) null,
			"no result is possible for the handler due to the following reason: " + reason);

    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#getReportResult()
     */
    @Override
	public ReportResult getReportResult() {
        return reportResult;
    }
    
    /**
     * @see com.top_logic.reporting.office.ReportHandler#setContextUser(com.top_logic.base.user.UserInterface)
     */
    @Override
	public void setContextUser(UserInterface aUser) {
        // no report no responsible :)
    }
    /**
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionContext(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionContext initializeExpansionContext(ReportToken aToken) {
        // do nothing at all
        return null;
    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionEngine(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionEngine initializeExpansionEngine(ReportToken aToken) {
        // do nothing at all
        return null;
    }

}
