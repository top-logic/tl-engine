
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.Logger;

/**
 * Initializes the overlib. See ...script/overlib.js. 
 * Use in the body section of a JSP.
 *
 * @author    Mathias Maul
 */
public class InitOverlibTag extends TagSupport {

    /** Complete JavascriptTag for overlib stuff */

    /**
     * Retrieves the standard PrintWriter object to print the script statement
     * to the JSP page head.
     */
    @Override
	public int doStartTag () {
        try {
            JspWriter out = pageContext.getOut ();
            String appPath = ((HttpServletRequest) pageContext.getRequest()).
                getContextPath();
            out.write("<script language=\"JavaScript\" type=\"text/javascript\" src=\"");
            out.write(appPath);
            out.println("/script/overlib.js\"></script>");
            out.println("<div id=\"overDiv\" style=\"position:absolute;visibility:hidden;z-index:1000;\"></div>");
        }
        catch (IOException ex) {
            Logger.fatal("Problems initializing the menu: ", ex, this);
        }
        return SKIP_BODY;

    }
}

