/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.util.monitor.AbstractMonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;

/**
 * Monitor for testing the repository datasource adaptor.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class RepositoryMonitor extends AbstractMonitorComponent {

    /** The data access proxy to be tested. */
    private DataAccessProxy dap;

    
    public RepositoryMonitor() {
        super();
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                + "dap: " + this.dap
                + "]");
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getName()
     */
    @Override
	public String getName() {
        return "Repository";
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getDescription()
     */
    @Override
	public String getDescription() {
        return "Monitor for the versioned file storage for the application";
    }

    @Override
	public void checkState(MonitorResult result) {
		Status theType;
        String theMessage;
        String theResult = this.checkDAP();

        if (theResult != null) {
            theType    = MonitorMessage.Status.ERROR;
            theMessage = "Check for existence of adaptor fails, reason is: " + theResult + '!';
        }
        else {
            theResult = this.checkRoot();

            if (theResult != null) {
                theType    = MonitorMessage.Status.ERROR;
                theMessage = "Check for existence of file storage fails, reason is: " + theResult + '!';
            }
            else {
                theType    = MonitorMessage.Status.INFO;

                try {
					theMessage = "Versioned file storage (" + this.getDAP().getProtocol() + "): " + getDAP();
                }
                catch (UnknownDBException ex) {
                    theMessage = "Getting protocol of storage failed, reason is: " + ex.getMessage() + '!';
                }
            }
        }

		result.addMessage(new MonitorMessage(theType, theMessage, this));
    }

    /**
     * TODO MGA: add documentation.
     */
    protected String checkDAP() {
        try {
            DataAccessProxy theDAP = this.getDAP();

            if (theDAP.exists()) {
                return (null);
            }
            else {
                return ("Repository '" + this.getDSN() + "' doesn't exist");
            }
		} catch (DatabaseAccessException ex) {
            return ("Unable to get proxy for repository '" + this.getDSN() + 
                    "', reason is: " + ex);
        }
    }

    /**
     * TODO MGA: add documentation.
     * TODO MGA add implementation. what is this good for?
     */
    protected String checkRoot() {
        return (null);
    }

    /**
     * Return a DataAccessProxy that should exist to make this class happy.
     */
    protected DataAccessProxy getDAP() throws UnknownDBException {
        if (this.dap == null) {
            this.dap = new DataAccessProxy(this.getDSN());
        }

        return (this.dap);
    }

    /**
     * Return the datasourcename to check for.
     */
    protected String getDSN() {
        return ("repository://");
    }
}
