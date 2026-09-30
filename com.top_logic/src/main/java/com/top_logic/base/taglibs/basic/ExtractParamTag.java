
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.util.Enumeration;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;


/**
 * This TagHandler classextracts the parameters from the current request and
 * writes them into hidden fields; to be passed on with the response.
 *
 * @author    Dr. Bettina Hofstaetter
 */
public class ExtractParamTag extends TagSupport {

    
    public ExtractParamTag () {

    }

    /**
     * Retrieves the standard PrintWriter object to print the hidden parameters into
     * the page body.
     */
    @Override
	public int doStartTag () throws JspException {
        this.extractParam ();

        return SKIP_BODY;

    }

    /**
     * Gets the current request from the page context. Extracts the parameters from it
     * and writes them into hidden fields.
     */
    protected void extractParam () throws JspException {
        ServletRequest request = pageContext.getRequest ();
        JspWriter      out;
        try {
            out = pageContext.getOut ();

            Enumeration theEnum = request.getParameterNames ();

            Object      theAttribute;
            String      attributeName = null;
            while (theEnum.hasMoreElements ()) {
                attributeName = ( String ) theEnum.nextElement ();
                theAttribute  = request.getParameter (attributeName);

                out.print ("<input type=\"Hidden\" name=\"" + attributeName
                           + "\" value=\"" + theAttribute + "\" ><br />");
            }
        }
        catch (Exception ex) {
            throw new JspException ("IO problems");
        }
    }

}
