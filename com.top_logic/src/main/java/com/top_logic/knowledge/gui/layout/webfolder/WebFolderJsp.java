/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.xml.sax.SAXException;

import com.top_logic.basic.StringServices;
import com.top_logic.common.webfolder.model.DocumentContext;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.TopLogicJspBase;

/**
 * This is the superclass of JSPs handling WebFolder issues.
 *  
 * Ensure this class is threadsafe!
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class WebFolderJsp extends TopLogicJspBase {

    /**
     * The component opening the jsp is fetched here.
     */
    protected BoundComponent getComponent(ServletContext aContext, HttpServletRequest aRequest) throws IOException, SAXException {
        return (BoundComponent) MainLayout.getComponent(DefaultDisplayContext.getDisplayContext(aRequest));
    }

    protected Object getModel(ServletContext aContext, HttpServletRequest aRequest) throws IOException, SAXException {
        LayoutComponent  theComponent = this.getComponent(aContext, aRequest);
        return theComponent.getModel();
    }
    
    /**
     * look if our component performs an upload or an update.
     */
	protected boolean doingFileUpdate(ServletContext aContext, HttpServletRequest aRequest)
			throws IOException, SAXException {
    	return !StringServices.isEmpty(this.getUpdateFileName(aContext, aRequest));
    }

    /**
     * when component performs a update, here we can get the name of file to be updated.
     */
	protected String getUpdateFileName(ServletContext aContext, HttpServletRequest aRequest)
			throws IOException, SAXException {
        LayoutComponent theComponent = this.getComponent(aContext, aRequest);
        Object theDocument = theComponent.getModel();
        if(theDocument instanceof DocumentContext) {
        	return ((DocumentContext) theDocument).getDocumentName();
        }
        if(!(theDocument instanceof Document)) {
            return "";
        }
        else {
            Document doc = (Document) theDocument;
            return doc.getName();
        }
    }

    /**
     * build up a string, which can be used, to instance a new javascriptArray.
     */
    protected String initJsArray(List aList) {

        StringBuffer theBuffer = null;
        
        Iterator theIt = aList.iterator();
        while (theIt.hasNext()) {   
            String theName = (String) theIt.next();
            theName  = theName.replaceAll("'","\\'").toLowerCase().trim();
            if (theBuffer == null) {
                theBuffer = new StringBuffer("\"");
            }
            else {
                theBuffer.append("\",\"");
            }
            theBuffer.append(theName);
        }
        
        if (theBuffer == null) { 
            theBuffer = new StringBuffer(); 
        }
        else { 
            theBuffer.append('\"'); 
        }
        return theBuffer.toString();
    }

}
