/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tag;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RequestUtil;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Supplement for the HTML &lt;body&gt; tag that is Layout aware.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class LayoutBody implements Tag, LayoutConstants, TryCatchFinally {

	/** Our parent must be a LayoutHtml tag */
    protected LayoutHtml parent;

    /** Needed for set/get PageContext */
    protected PageContext pageContext;

	private String _frmBodyClass = FormConstants.FORM_BODY_CSS_CLASS;

	private int tagDepth;
	private boolean bodyTagWritten;

	/**
	 * Write a "normal" body tag with some special things for the layout.
	 * 
	 * Will not write anything in case of a table-based layout.
	 * 
	 * @return always EVAL_BODY_INCLUDE since the suppression of the body must be triggerend by an
	 *         action-tag.
	 */
    @Override
	public int doStartTag() throws JspException {
        HttpServletRequest  req  = (HttpServletRequest) pageContext.getRequest();
        HttpServletResponse resp = (HttpServletResponse) pageContext.getResponse();

        try {
        	// Increasing the submit number already happens in
			// MainLayout.renderPage(). Here it's to late to
			// increase the submit number, because the old number 
        	// was already used in the page header, when composing 
        	// command URLs.
        	// 
            // parent.component.increaseSubmitNumber();
			TagWriter out = MainLayout.getTagWriter(pageContext);
			tagDepth = out.getDepth();
            if (!parent.supressHtmlFramework && !parent.contentsOnly) {
                if (parent.component != null) {
					DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(req);
					// For explanation take a look at LayoutComponent.writeBodyAttributes(...)
					boolean disableScrolling = false;
					UserAgent agent = displayContext.getUserAgent();
					if (agent.is_ie6up() && !agent.is_ie8up()) {
						disableScrolling =
							(parent.component instanceof MainLayout) || (parent.component instanceof WindowComponent);
					}
					parent.component.writeBodyTag(pageContext.getServletContext(), req, resp, out, disableScrolling);
                } else {
					out.beginTag(BODY);
                }
				bodyTagWritten = true;
			} else {
				bodyTagWritten = false;
            }
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, _frmBodyClass);
			out.endBeginTag();
			out.flushBuffer();
        } catch (Exception ex) {
            throw new JspException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
	public int doEndTag() throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

		try {
			TagWriter out = RequestUtil.lookupTagWriter(req);
			out.endTag(DIV);
			if (!parent.supressHtmlFramework && !parent.contentsOnly) {
				addInfoServiceItems(out, DefaultDisplayContext.getDisplayContext(req));
				out.endTag(BODY);
				out.endTag(HTML);
			}
			out.flushBuffer();
		} catch (Exception ex) {
			throw new JspException(ex);
		}

        return EVAL_PAGE;
    }

	@SuppressWarnings("unchecked")
	private static void addInfoServiceItems(TagWriter out, DisplayContext displayContext) throws IOException {
		if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
			List<HTMLFragment> errors = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
			out.beginScript();
			out.append(InfoServiceXMLStringConverter.getJSInvocation(displayContext, errors));
			out.endScript();
		}
	}

    /**
     * the parent, is always of type LayoutHtml
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
		_frmBodyClass = FormConstants.FORM_BODY_CSS_CLASS;
    }

	/**
	 * Sets the css class used for the {@link HTMLConstants#DIV} directly rendered below
	 * {@link HTMLConstants#BODY} tag.
	 */
	@CalledFromJSP
	public void setFormBodyClass(String formBodyClass) {
		_frmBodyClass = StringServices.nonEmpty(formBodyClass);
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
		if (bodyTagWritten) {
			out.endAll(tagDepth + 1);
			JSPErrorUtil.produceErrorOutput(pageContext, getErrorMessage(), throwable, this);
			addInfoServiceItems(out, DefaultDisplayContext.getDisplayContext(pageContext.getRequest()));
			out.endAll(tagDepth);
		} else {
			out.endAll(tagDepth);
			JSPErrorUtil.produceErrorOutput(pageContext, getErrorMessage(), throwable, this);
		}
	}

	private String getErrorMessage() {
		return "Error ocurred during rendering of html body structure of component "
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
		// Do nothing
	}

}
