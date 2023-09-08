/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.top_logic.basic.Logger;
import com.top_logic.importer.logger.BufferLogger;
import com.top_logic.importer.logger.FileLogger;
import com.top_logic.importer.logger.FirstLastLogger;
import com.top_logic.importer.logger.HasAnyErrorLogger;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.importer.logger.MultiLogger;
import com.top_logic.importer.logger.PlainMessageLogger;

/**
 * Utility methods for import handling (e.g. getting a correct {@link ImportLogger}).
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ImportUtils {

    public static final ImportUtils INSTANCE = new ImportUtils();

    /**
	 * Get the name of the actual Server Node.
	 * 
	 * @return	the name of the server node
	 */
	public String getServerNodeName() {
		String theHost = "Unknown host";

		try {
            InetAddress localHost = InetAddress.getLocalHost();
            theHost   = localHost.getHostName();

            int thePos = theHost.indexOf('.');

            if (thePos > 0) {
                theHost = theHost.substring(0, thePos);
            }
        }
        catch (UnknownHostException uhx) {
            Logger.warn("Unable to evaluate host", uhx, ImportUtils.class);
        }
		return theHost;
	}

	public MultiLogger merge(ImportLogger aLogger1, ImportLogger aLogger2) {
	    if ((aLogger1 == null) || (aLogger2 == null)) {
	        throw new IllegalArgumentException("Given logger must not be null"
	                + " (logger 1: " + aLogger1 
	                + ", logger 2: " + aLogger2 + ")!");
	    }
	    else {
	        MultiLogger theMulti = (MultiLogger) ((aLogger1 instanceof MultiLogger) ? aLogger1 : null);

    	    if (theMulti == null) {
    	        theMulti = (MultiLogger) ((aLogger2 instanceof MultiLogger) ? aLogger2 : null);
    	    }
    	    else {
    	        theMulti.add(aLogger2);
    	    }

    	    if (theMulti == null) {
    	        theMulti = new MultiLogger(aLogger1, aLogger2);
    	    }
    	    else {
    	        theMulti.add(aLogger1);
    	    }

    	    return theMulti;
	    }
	}

	public FirstLastLogger getFirstLastLogger(ImportLogger logger) {
        return getLogger(logger, FirstLastLogger.class);
    }

	public BufferLogger getBufferLogger(ImportLogger logger) {
	    return getLogger(logger, BufferLogger.class);
	}
    
    public HasAnyErrorLogger getHasAnyErrorLogger(ImportLogger logger) {
        return getLogger(logger, HasAnyErrorLogger.class);
    }

    public MultiLogger getMultiLogger(ImportLogger logger) {
        return getLogger(logger, MultiLogger.class);
    }

    public FileLogger getFileLogger(ImportLogger logger) {
        return getLogger(logger, FileLogger.class);
    }

    public ImportMessageLogger getImportMessageLogger(ImportLogger logger) {
        return getLogger(logger, ImportMessageLogger.class);
    }

    public PlainMessageLogger getPlainMessageLogger(ImportLogger logger) {
        return getLogger(logger, PlainMessageLogger.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends ImportLogger> T getLogger(ImportLogger aLogger, Class<T> aClass) {
		if (aLogger instanceof MultiLogger) {
			return ((MultiLogger) aLogger).getLogger(aClass);
		}
		else if (aClass.isAssignableFrom(aLogger.getClass())) {
			return (T) aLogger;
		}
		else { 
		    return null;
		}
	}
}
