/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Tag to include JS (Javascript) files.
 * 
 * Adds an <code>var appContext="..."</code> snippet so js-files
 * can refer to the application context.
 * 
 * This class replaces JCO's JSLinkTag class, which has been marked 
 * as deprecated.
 *
 * @author <a href=mailto:mma@top-logic.com>Mathias Maul</a>
 */
public class JSIncludeTag extends TagSupport {

	/** The system path where JavaScript files live */
	private static final String PATH_JAVASCRIPT = "/script/";

	/** The name of the resources key mapping to the JavaScript resources file to be used. */
	private static final ResKey RESOURCEKEY_JS_RESOURCES = I18NConstants.SCRIPT_RESOURCE_FILE;
	
	/** util.js */
	private static final String FILE_UTIL = "tl/util";


    /** The default name of the JavaScript file to be referenced. Empty by default. */
    private String name;

    /** If true, also generate a script import statement with I18N'ed resources. True by default.*/
	private boolean i18n;

	/** If true, a timestamp is generated a the end of the javascript url to avoid caching in proxies. False by default */
    private boolean reload;

    /** If true, the global "appContext" var will NOT be set. Example usage of appContext in menuutil.js. */
    private boolean noAppContext;

    /** Application context path. */
    private String appPath;


    /** These attributes are set in the request once the corresponding element has
     *  been written to the JSP in order to avoid repetetions.
     */
    private static final String ATTR_APP_CONTEXT = "JSIncludeTag_AppContext";
    private static final String ATTR_I18N  = "JSIncludeTag_I18N";

    
    public JSIncludeTag () {
		setName ("");
		setI18n (true);
        setReload (false);
        setNoAppContext (false);
    }

    /**
     * Sets the name attribute.
	 *
     * @param aName   Part of the file name of the JS file to include, without
	 *                path and ".js" extension.
     */
    public void setName (String aName) {
        this.name = aName;
    }

    /**
     * Returns the name attribute.
     */
    public String getName() {
    	return name;
    }

	/**
	 * Sets the "i18n" attribute.
	 */
	public void setI18n (boolean aValue) {
		this.i18n = aValue;
	}
	

	/**
	 * Returns the i18n attribute.
	 */
	public boolean getI18n() {
		return this.i18n;
	}
	
	/**
	 * Sets the "reload" attribute.
	 */
	public void setReload (boolean isReload) {
		this.reload = isReload;
	}
	
    
    /**
     * Sets the "noAppContext" attribute.
     */
    public void setNoAppContext (boolean _noAppContext) {
        this.noAppContext = _noAppContext;
    }
    
    /**
     * Returns the "noAppContext" attribute.
     */
    public boolean getNoAppContext() {
        return this.noAppContext;
    }

    /**
     * Retrieves the standard PrintWriter object to print the stylesheet link
     * to the JSP page head.
     */
    @Override
	public int doStartTag() throws JspException {
        try {
            this.appPath = ((HttpServletRequest) pageContext.getRequest()).
                            getContextPath();
            writeHTML(this.appPath, pageContext.getOut());
        }
        catch (Exception ex) {
            Logger.error ("IO problems", ex, this);

            throw new JspException ("IO problems (" + ex + ')', ex);
        }

        return SKIP_BODY;
    }

    /**
     * Returns an HTML string containing a &lt;script&gt; tag which includes
     * the JavaScript file specified by the <code>name</code> attribute. If
     * <code>i18n</code> is set to true, an additional script containing
     * i18n'ed resources will be included as well.
     */
    protected void writeHTML (String context, JspWriter out) throws IOException {
        boolean withI18N = this.getI18n() && !hasBeenWritten(ATTR_I18N);

        if (!this.getNoAppContext() && !this.hasBeenWritten(ATTR_APP_CONTEXT)) {
            out.write("<script language=\"JavaScript\" type=\"text/javascript\">var appContext=\"");
            out.write(this.appPath);
            out.write("\";</script>\n");

            this.markWritten(ATTR_APP_CONTEXT);
        }

        JSIncludeTag.writeHTML(context, out, this.getName(), withI18N, this.reload);

		if (withI18N) {
            this.markWritten(ATTR_I18N);
        }
    }

    /** Returns true if the element specified by the anAttributeName has
     *  already been written to the JSP.
     */
    private boolean hasBeenWritten(String anAttrName) {
        HttpServletRequest theRequest = (HttpServletRequest)pageContext.getRequest();
        
        return (theRequest.getAttribute(anAttrName) != null);
    } 

    /** Stores information about whether the element specified by anAttributeName
     * has already been written to the JSP.
     */
    private void markWritten(String anAttrName) {
        HttpServletRequest theRequest = (HttpServletRequest)pageContext.getRequest();
        theRequest.setAttribute(anAttrName, Boolean.TRUE); 
            // actual value is irrelevant, existance matters
    }

    /** 
     * Generate a correct &lt;script&gt; Tag.
     */
    public static void writeScriptHTMLFor(String aContext, Writer anOut, String aName, boolean withReload) throws IOException {
        anOut.write("<script language=\"JavaScript1.2\" type=\"text/javascript\" src=\"");
        anOut.write(aContext);
        anOut.write(PATH_JAVASCRIPT);
        anOut.write(aName);
        anOut.write(".js");

        if (withReload) { // fake a different url to force relaod for some proxies
            anOut.write("?timestamp=");
            anOut.write(Long.toString(System.currentTimeMillis ()));
        }

        anOut.write("\"></script>\n");
    }    

    /**
     * Returns an HTML string containing a &lt;script&gt; tag which includes
     * the JavaScript file specified by the <code>name</code> attribute. If
     * <code>i18n</code> is set to true, an additional script containing
     * i18n'ed resources will be included as well.
     */
    public static void writeHTML(String aContext, Writer anOut, String aName, boolean withI18N, boolean withReload) throws IOException {
        if (withI18N) {
            JSIncludeTag.writeScriptHTMLFor(aContext, anOut, 
                                            Resources.getInstance().getString(RESOURCEKEY_JS_RESOURCES), 
                                            withReload);
        }

        if (StringServices.isEmpty(aName)) {
            JSIncludeTag.writeScriptHTMLFor(aContext, anOut, FILE_UTIL, withReload);
        } 
        else {
            JSIncludeTag.writeScriptHTMLFor(aContext, anOut, aName, withReload);
        }
    }
}
