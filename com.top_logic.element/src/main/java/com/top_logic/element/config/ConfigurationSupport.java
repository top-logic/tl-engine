/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.io.InputStream;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.UnknownDBException;

/**
 * A Thin Layer around a DataSource to store some structural XML-Files.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ConfigurationSupport {

    /** DataSource name where additional structure info is stored */
    public static final String CONF_DS = "conf://";

    /** Root DSA based on {@link #CONF_DS} */
    public static DataAccessProxy baseDSA;

    private ConfigurationSupport() {
        super();
    }

    /**
     * Lazy accessor to DSA based on {@link #CONF_DS}
     */
    public static DataAccessProxy getBaseDSA() throws UnknownDBException {
        if (baseDSA == null) {
            baseDSA = new DataAccessProxy(CONF_DS);
        }
        return baseDSA;
    }

    public static DataAccessProxy getData(String anEntryName) throws DatabaseAccessException {
        DataAccessProxy theDAP = getBaseDSA().getChildProxy(anEntryName);
        return theDAP.isEntry() ? theDAP : null;
    }

    public static void putData(String anEntryName, InputStream someData) throws DatabaseAccessException {
        DataAccessProxy theDAP = getData(anEntryName);
        if (theDAP == null) {
            getBaseDSA().createEntryProxy(anEntryName, someData);
        } else {
            theDAP.putEntry(someData);
        }
    }

}
