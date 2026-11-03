/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.util.Map;
import java.util.Properties;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.XMLProperties;
import com.top_logic.util.error.TopLogicException;


/**
 * The central class responsible for creating office based reports.
 * 
 * The normal usage workflow for this {@link ReportGenerator} is the following:
 * <ol>
 *   <li>order a Report and receive a {@link ReportToken}</li>
 *   <li>ask if the report generation is complete</li>
 *   <li>get the ReportResult for the {@link ReportToken}</li>
 * </ol>
 * 
 * TODO JCO activate the queueing mechanism for reports.
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportGenerator implements Reloadable {
    
    /** Report type of PowerPoint reports. */
    protected static final String PPT_REPORT     = "ppt_report";

    /** Report type of Excel reports. */
    protected static final String EXCEL_REPORT   = "excel_report";

    /** Report type of Word reports. */
    protected static final String WORD_REPORT    = "word_report";

    /** the one and only instance of the generator */
    private static ReportGenerator singleton;
    
    /** some queues for different kinds of reports */
    protected ReportQueue pptQueue;

    /** The queue of {@link #EXCEL_REPORT Excel reports}. */
    protected ReportQueue excelQueue;

    /** The queue of {@link #WORD_REPORT Word reports}. */
    protected ReportQueue wordQueue;

    /**
     * the sole instance of this {@link ReportGenerator}.
     */
    public static ReportGenerator getInstance () {
        if (singleton == null) {
            singleton = new ReportGenerator();
        }
        return singleton;
    }
    
    /**
     * Default constructor, only accessible via singleton pattern.
     *
     */
    protected ReportGenerator () {
        super();
        pptQueue    = new ReportQueue("Powerpoint Queue");
        excelQueue  = new ReportQueue("Excel Queue");
        wordQueue   = new ReportQueue("Word Queue");
    }
       
    /**
     * If a report is generated asynchronous we provide here a handle to
     * identify the report. This handle is used in the generation queue to
     * schedule the reporting and attache results to this.
     * 
     * @param aReportType an id to specify which report to generate
     * @param anEnivronment additional information to support the generation of
     *            the report. This info contains e.g. the object(s) subject to
     *            the report, the i18n to use for the reporting language etc.
     * @param aUser the user requesting the report. This can be later used to
     *            send messages about the status of the report.
     * @return a token to identify a report, or to be more specific a report
     *         request.
     * @throws TopLogicException if the requested report is not of a type that is valid.
     */
    public ReportToken requestReport (String aReportType, Map anEnivronment, UserInterface aUser) {
        try {
            if (!isValidReportType(aReportType)) {
                throw new IllegalArgumentException (aReportType + " is not a known reportType");
            }
            ReportToken result =ReportToken.createToken(aReportType,anEnivronment,aUser);
            queueReportRequest (result);
            return result;
        }
        catch (Exception exp) {
            throw new TopLogicException (this.getClass(), "requestReport", null, exp);
        }
    }
      
    /**
     * This method returns the completion state of the specified report.
     * 
     * @param aToken the {@link ReportToken} to find the report
     * @return <code>true</code> if the report is available and the processing is complete.
     */
    public boolean reportReady (ReportToken aToken){
        
        ReportQueue theQueue = getReportQueue(aToken);
        boolean result = theQueue.jobExecuted(aToken);
        return result;
    }
    
    /**
     * The report results in a {@link ReportResult} which is returned here.
     * If no report for the token is found
     * @param aToken the handle to identify the report which is generated
     * @return an result object containing the created report or an error message if something went wrong
     */
    public ReportResult getReportResult (ReportToken aToken) {
        try {
            ReportQueue theQueue = getReportQueue(aToken); 
            ReportHandler theReport = null;
            theReport = theQueue.getExecutedJob(aToken);
            if (theReport != null) {
                if (theReport.reportFinished()) {
                    return theReport.getReportResult();                
                } else {
                    throw new IllegalStateException ("report is not (yet) finished");
                }
            } else throw new NullPointerException ("No executed report found for this token");
        }
        catch (RuntimeException exc) {
            Logger.error ("Unable to get the ReportHandler",exc,this);
            return new ReportResult (false,null,"Unable to return a usefull result due to exception",exc);
        }        
    }
    
    /**
     * evaluate the type of report to generate to decide in which queue the
     * request is to be stored then store it in the queue.
     * 
     * @param aToken the token to use as handle for the queue
     */
    protected void queueReportRequest (ReportToken aToken) {
        ReportQueue theQueue = getReportQueue(aToken);
        if (theQueue.jobExecuted(aToken) || theQueue.jobQueued (aToken)) {
            throw new IllegalStateException ("there is already a report executed for this token");
        }
        theQueue.addJob(aToken);
    }
    
    /**
     * Determines the right queue for the report.
     * At the moment there is only a limited number of queues explicitly for ppt, xls or doc!
     * 
     * @param aToken the token we use for idenfying the reports.
     * @return the {@link ReportQueue} this token refers to, is either powerpoint or excel or word!
     * @throws IllegalArgumentException when an unknown kind of report is requested!
     */
    protected ReportQueue getReportQueue (ReportToken aToken) {
        String reportKind = getReportTypes().getProperty(aToken.getReportID() + ".kind");
        if (PPT_REPORT.equals (reportKind)) {
            return pptQueue;
        }
        if (EXCEL_REPORT.equals (reportKind)) {
            return excelQueue;
        }
        if (WORD_REPORT.equals (reportKind)) {
            return wordQueue;
        }
        throw new IllegalArgumentException ("Unable to determine the kind of report or kind is unsupported: " + reportKind);
    }
        
    /**
     * Depending on the token we create the right report handler instance and
     * provide this handler with the right {@link ExpansionContext},
     * {@link ExpansionEngine} and {@link ReportReaderWriter} to perform its
     * duty.
     * If something with the configuration or report type is amiss a standard (error)
     * report handler will be instanciated!
     * 
     * @param aToken the token to identify the report handler to create and
     *            provide the environment.
     * @return an instance to represent the report to be processed.
     */
    protected ReportHandler createReportHandler (ReportToken aToken) {
        ReportHandler theReportHandler = null;
        String theReportID = aToken.getReportID();
        if (!isValidReportType(theReportID)) {
            Logger.error ("the type " + theReportID + " is not a valid report type, creating noop handler",this);
            return new FallbackNoopReportHandler("invalid report type " + theReportID);
        }
  
        // now create the reportHandler class:
        String reportClassName = null;
        try {
            reportClassName = getReportTypes().getProperty(theReportID);
            Class reportClass = Class.forName(reportClassName);
            theReportHandler = (ReportHandler)reportClass.newInstance();
            initializeReportHandler (theReportHandler, aToken);
        }
        catch (Exception exp) {
            Logger.error ("Unable to initialize the reportHandler, creating noop handler",exp,this);
            return new FallbackNoopReportHandler("initialization failed for " + theReportID + " with class " + reportClassName);
        }        
        return theReportHandler;
    }
    
    /**
     * initialize the handler by initializing the expansion context and the expansion engine and 
     * defining the user under whos context the report will be done.
     */
    protected void initializeReportHandler(ReportHandler aHandler, ReportToken aToken) {
        aHandler.setContextUser (aToken.getRequestUser());
        aHandler.initializeExpansionContext(aToken);
        aHandler.initializeExpansionEngine(aToken);
    }
    
    /**
     * <code>true</code> if the given type is declared in the properties
     */
    protected boolean isValidReportType (String aReportType) {
        return getReportTypes().containsKey(aReportType);
    }

    
    /**
     * Cache of {@link #getReportTypes()}.
     */
    protected Properties reportTypes;

    /**
     * The configured report types, mapping report type names to the classes of their
     * {@link ReportHandler}s.
     */
    protected Properties getReportTypes () {
        if (reportTypes == null) {
            XMLProperties xmlProp = XMLProperties.getInstance();
            reportTypes = xmlProp.getProperties(ReportGenerator.class);
        }
        return reportTypes;
    }

    /** 
     * @see com.top_logic.basic.Reloadable#reload()
     */
    @Override
	public boolean reload() {
        reportTypes = null;
        return true;
    }

    /**
     * @see com.top_logic.basic.Reloadable#getName()
     */
    @Override
	public String getName() {
        return "ReportGenerator";
    }

    /**
     * @see com.top_logic.basic.Reloadable#getDescription()
     */
    @Override
	public String getDescription() {
        return "The central class for handling reporting, i.e. office reports.";
    }

    /**
     * @see com.top_logic.basic.Reloadable#usesXMLProperties()
     */
    @Override
	public boolean usesXMLProperties() {
        return true;
    }
    
    
}
