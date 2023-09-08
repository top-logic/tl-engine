/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Base class for {@link Tag}s writing HTML <code>link</code>s.
 * 
 * @author <a href=mailto:jco@top-logic.com>Jörg Connotte</a>
 */
public abstract class AbstractLinkTag extends TagSupport {

    /** The default URL of the .css file to be referenced. */
    private String href;

    /** The context path of this application. */
    private String contextPath;

    /**
     * Sets the tag attribute to the value specified in the corresponding
     * JSP page.
     *
     * @param aHref The String URL of the stylesheet the JSP author has specified.
     */
    public void setHref (String aHref) {
        this.href = aHref;
    }

    /**
     * Retrieves the standard PrintWriter object to print the stylesheet link
     * to the JSP page head.
     */
    @Override
	public int doStartTag () throws JspException {
        try {
            writeLink (pageContext.getOut ());
        }
        catch (IOException ex) {
            throw new JspException ("IO problems",ex);
        }
        return SKIP_BODY;

    }
    
    /**
     * returns the link to the requested file.
     */
    protected String getHref () {
    	return href;
    }
    
    /**
     * Creates a HTML statement. The only changing part of this is the
     * javascript file to be referenced. .
     */
    protected void writeLink(JspWriter out) throws IOException {
        String theContext = this.getContextPath();
        
        writePrefix (out);

		out.print(LinkTagUtil.getLink(theContext, href));

        writeSuffix (out); 
    }
   
	/**
	 * Calculates the context path.
	 * 
	 * @see LinkTagUtil#getContextPath(PageContext, String)
	 * 
	 * @return The calculated context path.
	 */
	protected String getContextPath() {
		this.contextPath = LinkTagUtil.getContextPath(this.pageContext, this.contextPath);

		return this.contextPath;
    }
    
    protected boolean hasPathPrefix () {
        return (getHref ().indexOf ("style") > -1);
    }
    
    
    protected abstract void writePrefix (JspWriter out) throws IOException;
    
    
    protected abstract void writeSuffix (JspWriter out)throws IOException ;

}

