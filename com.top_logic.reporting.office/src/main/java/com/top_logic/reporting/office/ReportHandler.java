/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import com.top_logic.base.user.UserInterface;


/**
 * The report handler actually knows about the task to perform for a certain report.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public interface ReportHandler {
    
    /**
     * <code>true</code> if the report processing is complete and the
     *         request for the report result makes sense.
     */
    public boolean reportFinished();
    
    /**
     * Prepare the report to be processed. Here some preprocessing may take place
     */
    public void prepare();
    
    /**
     * process the report by interpreting the symbols contained in the expansion objects and
     * replacing the expanded objects.
     */
    public void processReport();
    
    /**
     * Finish the processing of the report. Here some special postprocessing may take place
     */
    public void finishReport();
    
    /**
     * a ReportResult with the successfully generated report or an error
     *         message when something went wrong
     */
    public ReportResult getReportResult ();
    
    /**
     * set the user which requested the report and thus define the user context.
     * @param aUser a User interface which represents the user who requested the report.
     */
    public void setContextUser(UserInterface aUser);

    /**
     * Initializes the expansion context based on information stored in the
     * given report token. Especially the business object(s) relevant for the
     * report are resolved here.
     * 
     * @param aToken the information needed for initialization of the context
     * @return the initialized context
     */
    public ExpansionContext initializeExpansionContext(ReportToken aToken);
    
    /**
     * Initialize the expansion engine based on the given ReportToken and the
     * type of report. In most cases this will be the same engine.
     * 
     * @param aToken the information usefull for initialization of the engine
     * @return the initialized engine
     */
    public ExpansionEngine initializeExpansionEngine(ReportToken aToken);
}
