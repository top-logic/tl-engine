/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tag;

import java.io.Writer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TryCatchFinally;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.tag.LayoutTag;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Supplement for the HTML &lt;html&gt; tag that is Layout aware.
 *
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class LayoutHtml implements LayoutConstants, LayoutTag, Tag, TryCatchFinally /* Need redundant implements for BEA :-(*/
{

    /** Needed for set/get PageContext */
    protected PageContext pageContext;

    /** The Component that defines this page */
    protected LayoutComponent component;

    /** 
     * true, when just the plain content, but no html framework should be written. 
     * This is the case when in table layout or called from the LayoutServlet. 
     */
    protected boolean supressHtmlFramework;
    
    protected boolean contentsOnly;

	private TagWriter out;

	private Writer oldOut;

	private int tagDepth;

    /** 
     * Extract the LayoutComponent from the request.
     */
    @Override
	public int doStartTag() throws JspException {
    	try {
			HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			out = MainLayout.getTagWriter(pageContext);
			tagDepth = out.getDepth();
			oldOut = out.setOut(pageContext.getOut());

			component = MainLayout.getComponent(pageContext);
			if (component != null) {
				supressHtmlFramework =
					(req.getAttribute(ATTRIBUTE_SUPRESS_HTML_FRAMEWORK) != null);

				contentsOnly = DefaultDisplayContext.getDisplayContext(pageContext).get(ATTRIBUTE_CONTENTS_ONLY);
			}
			return EVAL_BODY_INCLUDE;
		} catch (Throwable ex) {
			try {
				doCatch(ex);
			} catch (Throwable innerException) {
				// Ignore
			}
			return SKIP_BODY;
		}
    }

    /**
     * Forget about the current component.
     */
    @Override
	public int doEndTag() throws JspException {
		// Note: There may be code on a JSP after the end tag that needs to run. 
        // Therefore, no SKIP_PAGE must be returned.
		return EVAL_PAGE;
    }

    /**
     * always null html is the outer tag
     */
    @Override
	public Tag getParent() {
        return null;
    }

    /**
     * @see jakarta.servlet.jsp.tagext.Tag#release()
     */
    @Override
	public void release() {
        pageContext = null;
    }

    /**
     * @see jakarta.servlet.jsp.tagext.Tag#setPageContext(PageContext)
     */
    @Override
	public void setPageContext(PageContext context) {
        pageContext = context;
    }

    /**
     * Allow access to PageContext.
     */
    public PageContext getPageContext() {
        return pageContext;
    }

    /**
     * Alwawy throws a RumtimeException,html must not have a parent.
     */
    @Override
	public void setParent(Tag parent) {
        if (parent != null) {
            throw new RuntimeException("html must not have a parent " + parent);
        }
    }

    /**
	 * the component currently rendered (usually a page component)
	 */
    @Override
	public LayoutComponent getComponent() {
        return component;
    }

	@SuppressWarnings("hiding")
	@Override
	public void doCatch(Throwable throwable) throws Throwable {
		String logMessage =
			"Error on JSP page of component " + JSPErrorUtil.getComponentInformation(component) + ".";
		if (out != null) {
			out.endAll(tagDepth);
			JSPErrorUtil.produceErrorOutput(pageContext, logMessage, throwable, LayoutHtml.class);
			out.flushBuffer();
		} else {
			TagWriter out = new TagWriter(pageContext.getOut());
			JSPErrorUtil.produceFailSafeErrorOutput(out, logMessage, throwable, LayoutHtml.class);
			out.flushBuffer();
		}
	}

	@Override
	public void doFinally() {
		if (out != null) {
			out.setOut(oldOut);
			oldOut = null;
			component = null;
		}
	}

}
