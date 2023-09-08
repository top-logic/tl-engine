
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import com.top_logic.basic.Logger;


/**
 * Proxy for accessing the JNDI context.
 *
 * This implementation is needed, when the JNDI implementation closes the 
 * connection after a specific timeout. This causes the called methods to
 * throw an CommunicationException, which will be catched. The class tries
 * a reconnect now and sends the command a second time. If there is another
 * problem as the timeout, it'll be there after the reconnect and will be
 * thrown again.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TopLogicDirContext extends InitialDirContext {

    /**
     * Constructor using the properties for setup in the given parameter.
     *
     * @param    aTable    The list of parameters for setting up.
     * @throws   NamingException    If the parameters doesn't leed to a goal.
     */
    public TopLogicDirContext (Hashtable aTable) throws NamingException {
        super (aTable);
    }

    /**
     * Searches in the named context or object for entries that satisfy the 
     * given search filter. Performs the search as specified by the search 
     * controls. See DirContext.search(Name, String, SearchControls) for details.
     *
     * @return   The result of the search.
     * @throws   NamingException    If something went wrong.
     */
    @Override
	public NamingEnumeration search (String aName, 
                                     String aFilter, 
                                     SearchControls aContr) throws NamingException {
        NamingEnumeration theEnum = null;

        try {
            theEnum = super.search (aName, aFilter, aContr);
        } 
        catch (CommunicationException ex) {
            this.reconnect ();

            theEnum = super.search (aName, aFilter, aContr);
        }

        return (theEnum);
    }

    @Override
	public void modifyAttributes (String aName,
                                  int anOp,
                                  Attributes anAttr) throws NamingException {
        try {
            super.modifyAttributes (aName, anOp, anAttr);
        } 
        catch (CommunicationException ex) {
            this.reconnect ();

            super.modifyAttributes (aName, anOp, anAttr);
        }
    }

    /**
     * Tries to reconnect to the JNDI service.
     *
     * @throws    NamingException    If reconnect fails.
     */
    protected void reconnect () throws NamingException {
        this.gotDefault     = false;
        this.defaultInitCtx = null;

        if (Logger.isDebugEnabled (this)) {
            Logger.debug ("Try to reconnect to JNDI service!", this);
        }

        this.init (this.myProps);

        Logger.info ("Reconnect to JNDI service!", this);
    }
}

