/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.KBXMLConstants;

/**
 * This class allows importing of Knowledgebases via an XML-File.
 *
 * It can be used to import MetaObjects only. Or to import data into
 * an Existing (DB)Knowledgebase.
 *
 * @author  Klaus Halfmann
 */
public class MORepositoryImporter extends DispatchingHandler implements KBXMLConstants {

	/** MORepository extracted from the Knowledgebase */
	/*package protected*/ final MORepository  typeRepository;

	protected String currentResourceName;
	
	protected final Protocol report;
	
	protected MORepositoryImporter(MORepository typeRepository, Protocol report) {
    	this.typeRepository = typeRepository;
		this.report = report;

    }

	@Override
	protected ContentHandler registerHandler(String name, ContentHandler handler) {
		return super.registerHandler(name, handler);
	}

	/**
	 * Actual importing function.
	 * 
	 * @param is
	 *        Stream used to import the Data from.
	 * @param resourceName
	 *        The name of the loaded resource (for error handling).
	 * 
	 */
    public void doImport(InputStream is, String resourceName) {
        this.currentResourceName = resourceName;
        
		// run in super user context
        ThreadContext.pushSuperUser();
        try {
            SAXParser parser;
			parser = SAXUtil.newSAXParser();
            parser.parse(is, this);
        } catch (ParserConfigurationException ex) {
            throw new AssertionError(ex);
        } catch (SAXException ex) {
			report.error("Data loaded from '" + resourceName + "' is invalid.", ex);
		} catch (IOException ex) {
			report.error("Error while reading data from '" + resourceName + "'.", ex);
		} finally {
        	// return to normal user context
        	ThreadContext.popSuperUser();
        	
        	this.currentResourceName = null;
        }
    }

    /** Actual importing function 
     *
     * @param aFile File used to import the Data from.
     *
     * @return true in case import was successful
     */
    public boolean doImport(File aFile) {
        boolean result = false;
        
        // run in super user context
        ThreadContext.pushSuperUser();

        try {
			SAXParser parser = SAXUtil.newSAXParser();
            parser.parse(aFile, this);
            result = true;
        } catch (SAXException ex) {
            throw toRuntimeException(ex);
        } catch (ParserConfigurationException ex) {
            throw new AssertionError(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {         
            ThreadContext.popSuperUser();
        }
        return result;
    }

	private RuntimeException toRuntimeException(SAXException ex) {
		Exception cause = ex.getException();
		if (cause instanceof RuntimeException) {
			return (RuntimeException) cause;
		} else {
			return new RuntimeException(cause != null ? cause : ex);
		}
	}
    

    
}
