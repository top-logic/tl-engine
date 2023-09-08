
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import com.top_logic.basic.Logger;


/**
 * Logger for all message occuring in this package. This class is needed,
 * if the crypt functions are used in the installation process. Therefore
 * the Logger class is not available and the logging should go to somewhere
 * defined ;).
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
class CryptLogger {

    /**
     * Logs given message as debug information.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void debug (String aMessage, Object aCaller) {
        try {
            Logger.debug (aMessage, aCaller);
        } 
        catch (Throwable ex) {
            System.out.println (aCaller.toString () + ": " + aMessage);
        }
    }

    /**
     * Logs given message as information.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void info (String aMessage, Object aCaller) {
        try {
            Logger.info (aMessage, aCaller);
        } 
        catch (Throwable ex) {
            System.out.println (aCaller.toString () + ": " + aMessage);
        }
    }   

    /**
     * Logs given message as warning.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void warn (String aMessage, Object aCaller) {
        try {
            Logger.warn (aMessage, aCaller);
        } 
        catch (Throwable ex) {
            System.err.println (aCaller.toString () + ": " + aMessage);
        }
    }    

    /**
     * Logs given message as error.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void error (String aMessage, Object aCaller) {
        try {
            Logger.error (aMessage, aCaller);
        } 
        catch (Throwable ex) {
            System.err.println (aCaller.toString () + ": " + aMessage);
        }
    }  

    /**
     * Logs given message and casue as error.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void error (String aMessage, Throwable cause, Object aCaller) {
        try {
            Logger.error (aMessage, cause, aCaller);
        } 
        catch (Throwable ex) {
            System.err.println (aCaller.toString () + ": " + aMessage);
        }
    }  

    /**
     * Logs given message as fatal.
     *
     * @param    aMessage    The message to log
     * @param    aCaller     The caller of the method
     */
    public static synchronized void fatal (String aMessage, Object aCaller) {
        try {
            Logger.error (aMessage, aCaller);
        } 
        catch (Throwable ex) {
            System.err.println (aCaller.toString () + ": " + aMessage);
        }
    }   

    /**
     * Returns if debugging for the aCaller is enabled.
     *
     * @param    aCaller    The caller of the method.
     * @return   Is debugging for the caller enabled.
     */
    public static synchronized boolean isDebugEnabled (Object aCaller) {
        try {
            return (Logger.isDebugEnabled (aCaller));
        } 
        catch (Throwable ex) {
            return (true);
        }
    }    
}

