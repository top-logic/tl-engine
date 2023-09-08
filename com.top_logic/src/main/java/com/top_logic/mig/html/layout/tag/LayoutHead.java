/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import com.top_logic.base.services.simpleajax.RequestUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Supplement for the HTML &lt;head&gt; tag that is Layout aware.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class LayoutHead implements Tag, LayoutConstants, TryCatchFinally {

    /** Our parent must be a LayoutHtml tag. */
    protected LayoutHtml parent;

    /** Needed for set/get PageContext. */
    protected PageContext pageContext;

	private int tagDepth;

    /**
     * Start the rendering of the layout aware page.
     * 
     * Will create a TagWriter and save it at the request using
     * the {@link com.top_logic.base.services.simpleajax.RequestUtil#ATTRIBUTE_HTML_WRITER}.
     * 
     * Will call back into its component to write an XHTML-Header and whatever
     * defaults parts the component wishes to write. 
     */
    @Override
	public int doStartTag() throws JspException {
        
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		TagWriter out = MainLayout.getTagWriter(pageContext);
		tagDepth = out.getDepth();
        LayoutComponent    lc  = parent.component;
        if (parent.supressHtmlFramework) {
            return Tag.SKIP_BODY; // no need to execute the header.
        }
        if (! parent.contentsOnly) {
        	try {
				MainLayout.writeHTMLStructureStart(DefaultDisplayContext.getDisplayContext(pageContext), out);
				tagDepth = out.getDepth();
				out.beginTag("head");
        		if (lc != null) {
					lc.writeHeader(req.getContextPath(), out, req);
					lc.getMainLayout().writeDefaultHeader(pageContext.getServletContext(), out, req, false);
        		}

				out.flushBuffer();
        	} catch (Exception e) {
        		throw new JspException(e);
        	}
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Eventually end the head tag.
     * 
     * @return always EVAL_PAGE.
     */
    @Override
	public int doEndTag() throws JspException {
        if (! parent.contentsOnly) {
        	HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			TagWriter out = RequestUtil.lookupTagWriter(req);
        	if (!parent.supressHtmlFramework) {
        		try {
        			out.endTag("head");
					out.flushBuffer();
        		} catch (Exception e) {
        			throw new JspException(e);
        		}
        	}
        }
        return EVAL_PAGE;
    }

    /**
     * the parent, is always of type LayoutHtml.
     */
    @Override
	public Tag getParent() {
        return parent;
    }

    /**
     * Forget about everything.
     */
    @Override
	public void release() {
        parent      = null;
        pageContext = null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#setPageContext(PageContext)
     */
    @Override
	public void setPageContext(PageContext context) {
        pageContext = context;
    }

    /**
     * Will die with a <code>ClassCastException</code> in case parent is no LayoutHtml.
     */
    @Override
	public void setParent(Tag aParent) {
        this.parent = (LayoutHtml) aParent;
    }

	@Override
	public void doCatch(Throwable throwable) throws Throwable {
		TagWriter out = MainLayout.getTagWriter(pageContext);
		out.endAll(tagDepth);
		JSPErrorUtil.produceErrorOutput(pageContext, getErrorMessage(), throwable, this);
	}

	private String getErrorMessage() {
		return "Error ocurred during rendering of html head structure of component "
			+ JSPErrorUtil.getComponentInformation(getComponent(parent)) + ".";
	}

	@SuppressWarnings("hiding")
	private LayoutComponent getComponent(LayoutHtml parent) {
		if (parent != null) {
			return parent.component;
		} else {
			return null;
		}
	}

	@Override
	public void doFinally() {
		// Nothing to do
	}
    

}
