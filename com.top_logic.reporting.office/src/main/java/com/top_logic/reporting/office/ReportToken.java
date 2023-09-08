/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.util.Map;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;


/**
 * A handle to identify a currently scheduled (or already processed Report).
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportToken {    

    /** This is a unique id to identify a specific report request */
	private TLID tokenID;
    
    /** The id of the report type to generate */
    private String          reportID;
    
    /** Environmenta information needed to create the specific report */
    private Map             environment;
    
    /** the user who requested the Report */
    private UserInterface   requestUser;
    
    /**
     * Constructor is private, because we use the factory method pattern.
     */
    private ReportToken () {
        super();
        tokenID = StringID.createRandomID();
    }

    /**
     * Returns the environment.
     */
    public Map getEnvironment() {
        return this.environment;
    }
    
    /**
     * Returns the reportID.
     */
    public String getReportID() {
        return this.reportID;
    }
    
    /**
     * Returns the requestUser.
     */
    public UserInterface getRequestUser() {
        return this.requestUser;
    }
    
    /**
     * Returns the tokenID.
     */
    public TLID getTokenID() {
        return this.tokenID;
    }
    
    /**
     * Store the parameters.
     */
    private void initialize (String aReportId, Map anEnvironment, UserInterface aUser) {
        reportID = aReportId;
        environment = anEnvironment;
        requestUser = aUser;
    }
    
    /**
     * Factory method to create a new ReportToken
     * 
     * @return a ReportToken
     */
    public static ReportToken createToken (String aReportID, Map anEnvironment, UserInterface aUser) {
        ReportToken result = new ReportToken();
        result.initialize(aReportID,anEnvironment,aUser);
        return result;
    }    
}
