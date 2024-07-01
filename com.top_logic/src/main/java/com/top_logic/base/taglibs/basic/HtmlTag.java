/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;
import jakarta.servlet.jsp.tagext.TryCatchFinally;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tag.JSPErrorUtil;

/**
 * Tag that writes a the XML header, XHTML doctype and the HTML element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HtmlTag extends TagSupport implements TryCatchFinally {

	private TagWriter out;
	
	private int tagDepth;

	/**
	 * Old writer of out. During {@link #doStartTag() the start tag} the {@link TagWriter} gets the
	 * {@link PageContext#getOut() output writer} of the pageContext to ensure that the order of
	 * content on the jsp and stuff written to the writer is correct.
	 */
	private Writer oldOut;
	
	@Override
	public int doStartTag() throws JspException {
		try {
			DisplayContext context = DefaultDisplayContext.getDisplayContext(pageContext);
			out = MainLayout.getTagWriter(pageContext);
			tagDepth = out.getDepth();
			oldOut = out.setOut(pageContext.getOut());
			MainLayout.writeHTMLStructureStart(context, out);

			out.flushBuffer();

			return EVAL_BODY_INCLUDE;
		} catch (Throwable ex) {
			try {
				doCatch(ex);
			} catch (Throwable innerException) {
				// Ignore;
			}
			return SKIP_BODY;
		}
	}
	
	
	@Override
	public int doEndTag() throws JspException {
		try {
			if (out != null) {
				out.endTag(HTMLConstants.HTML);
				out.flushBuffer();
			}
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		
		// Note: There may be code on a JSP after the end tag (e.g. in the
		// LogoutPage.jsp) that needs to run. Therefore, no SKIP_PAGE must 
		// be returned.
		return EVAL_PAGE;
	}

	@SuppressWarnings("hiding")
	@Override
	public void doCatch(Throwable throwable) throws Throwable {
		String errorMessage = "Error on JSP page.";
		if (out != null) {
			out.endAll(tagDepth);
			JSPErrorUtil.produceErrorOutput(pageContext, errorMessage, throwable, HtmlTag.class);
			out.flushBuffer();
		} else {
			TagWriter out = new TagWriter(pageContext.getOut());
			JSPErrorUtil.produceFailSafeErrorOutput(out, errorMessage, throwable, HtmlTag.class);
			out.flushBuffer();
		}
	}

	@Override
	public void doFinally() {
		if (out != null) {
			out.setOut(oldOut);
			out = null;
			oldOut = null;
		}
	}
	
}
