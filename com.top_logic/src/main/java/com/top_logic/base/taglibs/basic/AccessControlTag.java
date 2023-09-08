
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Check access according to given roles or symbols.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AccessControlTag extends TagSupport {

	private static final String REFUSED_PAGE_DEFAULT = "/jsp/administration/UnallowedAccess.jsp";

	private String _refusedMessage;

	private String _refusedPage = REFUSED_PAGE_DEFAULT;

	private BoundCommandGroup _commandGroup = SimpleBoundCommandGroup.WRITE;

    /**
     * Retrieves the standard JspWriter object to print an HTML comment
	 * with the result of the access control.
	 * Skips the body if access is refused.
     *
     * @throws JspException	if something goes wrong.
     */
    @Override
	public int doStartTag () throws JspException {
        try {
			LayoutComponent component = MainLayout.getComponent(pageContext);
			TagWriter out = MainLayout.getTagWriter(pageContext);

			if (!(component instanceof BoundComponent)) {
				return accessRefused(out);
			}

			BoundComponent check = (BoundComponent) component;
			if (!check.allow(_commandGroup)) {
				return accessRefused(out);
			}

			int result = accessGranted(out);
			out.flushBuffer();
			return result;
		} catch (Exception ex) {
			ex.printStackTrace ();
            throw new JspException ("Problems evaluating AccessControlTag: " + ex);
        }
    }

	private int accessRefused(TagWriter out) throws IOException, ServletException {
		out.writeComment("Access denied!");
		printRefused(out);
		return SKIP_BODY;
	}

	private int accessGranted(TagWriter out) {
		out.beginComment();
		out.writeCommentContent("Access granted by ");
		out.writeCommentContent(this.getClass().getName());
		out.endComment();
		return EVAL_BODY_INCLUDE;
	}

    /**
     * Retrieves the standard JspWriter object to print an HTML comment
     * that the access restricted part ends.
     * Skips the body if access is refused.
     *
     * @throws JspException	if something goes wrong.
     */
    @Override
	public int doEndTag () throws JspException {
        try {
			TagWriter out = MainLayout.getTagWriter(pageContext);
			out.writeComment("End of access restricted section");
			out.flushBuffer();
		}
        catch (Exception ex) {
			ex.printStackTrace ();
            throw new JspException ("Problems evaluating AccessControlTag: " + ex);
        }
		
		return EVAL_PAGE;
    }

	/**
	 * Sets the command group on which the user must have rights to see content of this tag.
	 */
	public void setCommandGroup(BoundCommandGroup group) {
		_commandGroup = group;
	}

    /**
     * Set the message to be printed if access is refused.
     *
     * @param aMessage 	the message.
     */
    public void setRefusedMessage (String aMessage) {
		this._refusedMessage = aMessage;
    }

    /**
	 * Get the message to be printed if access is refused.
	 *
     * @return	the message.
     */
    public String getRefusedMessage () {
		return this._refusedMessage;
    }

    /**
     * Set the URL of the page to be included if access is refused.
     *
     * @param aPageURL 	the page URL.
     */
    public void setRefusedPage (String aPageURL) {
		this._refusedPage = aPageURL;
    }

    /**
     * Get the URL of the page to be included if access is refused.
     *
     * @return	the page URL.
     */
    public String getRefusedPage () {
		return this._refusedPage;
    }

	/**
	 * Print the text specified if the access is refused, i.e.
	 * the given refused text and/or page.
	 *
	 * @exception IOException and ServletException is something goes wrong.
	 */
	private void printRefused(TagWriter out) throws IOException, ServletException {

		String theRefused = this.getRefusedMessage ();
		String thePage    = this.getRefusedPage ();
		if (!StringServices.isEmpty (theRefused)) {
			out.writeContent(theRefused);
			HTMLUtil.writeBr(out);
		}
		if (!StringServices.isEmpty (thePage)) {
			this.pageContext.include (thePage);
		}
	}
}

