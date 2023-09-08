/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionEngine;
import com.top_logic.reporting.office.ReportHandler;
import com.top_logic.reporting.office.ReportResult;
import com.top_logic.reporting.office.ReportToken;
import com.top_logic.reporting.office.basic.BasicExpansionContext;


/**
 * ReportHandler to use for testing purposes.
 * If the token value 'successull' is availeable and <code>true</code>
 * the ReportHandler return a successfull reportResult.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class NoopReportHandler implements ReportHandler {

    private ExpansionContext context;
    private ReportResult reportResult;
    private boolean successful;
    
    public NoopReportHandler() {
        super();
    }

    /**
     * @see com.top_logic.reporting.office.ReportHandler#reportFinished()
     */
    @Override
	public boolean reportFinished() {
        return true;
    }

    /**
     * examine the context for the successful flag
     * @see com.top_logic.reporting.office.ReportHandler#prepare()
     */
    @Override
	public void prepare() {
        Map theMap = context.getBusinessObjects();
        if (theMap.containsKey("successful")) {
            successful = "true".equalsIgnoreCase((String)theMap.get("successful"));
        }
    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#processReport()
     */
    @Override
	public void processReport() {
    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#finishReport()
     */
    @Override
	public void finishReport() {
        File theFile;
        try {
            theFile = BasicTestCase.createTestFile("simple", "txt");
            reportResult = new ReportResult (successful, theFile, "Successful flag is " + successful);
        }
        catch (IOException exp) {
			reportResult = new ReportResult(false, (BinaryContent) null, "IOException occurred creating temp file");
        }
        
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
    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionContext(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionContext initializeExpansionContext(ReportToken aToken) {
        context = new BasicExpansionContext (aToken.getEnvironment());
        return context;
    }

    /** 
     * @see com.top_logic.reporting.office.ReportHandler#initializeExpansionEngine(com.top_logic.reporting.office.ReportToken)
     */
    @Override
	public ExpansionEngine initializeExpansionEngine(ReportToken aToken) {
        return null;
    }

}
