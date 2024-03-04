
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import jakarta.servlet.jsp.JspWriter;

/**
 * This TagHandler class writes a HTML link to the stylesheet file specified
 * by the JSP author.
 *
 * @author    Dr. Bettina Hofstaetter
 */
public class CSSLinkTag extends AbstractLinkTag {

    /** The main style to use for HTML pages. */
    public static final String MAIN_STYLE    = "/style/mainStyle.css";
	
    private static final String DEFAULT_PATH = "/style/";

    /** If true, a timestampe is generated a the end of the stylesheet -  url to avoid caching in proxies. False by default */
    protected boolean reload;

    /**
     * set the reference to the default stylesheet
     */
    public CSSLinkTag () {
		super ();
        setHref (MAIN_STYLE);
        setReload (false);
    }

    public void setReload (boolean isReload) {
    	reload = isReload;
    }

    @Override
	protected void writePrefix (JspWriter out) throws IOException {
        String thePath = "";

        if (!hasPathPrefix ()) { 
            thePath = this.getContextPath() + DEFAULT_PATH;
        }

        out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");

        out.write(thePath); 
        
    }
    
    /**
     * generate the end of a script
     */
    @Override
	protected void writeSuffix (JspWriter out) throws IOException {
        if (reload) {
        	out.write("?timestamp=");
        	out.print(System.currentTimeMillis ());
        }
        out.print("\" />");
    }
}

