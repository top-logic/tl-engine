/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Write an image located in the theme area to the JSP.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ImageTag extends TagSupport {

    /** Relative path to the image in the theme directory. */
    private String name;

    /** Optional flag, if path name has to be I18N (needed for some beacons). */
    private boolean i18n;

    /**
     * Generates the tag for displaying an image loacated in the theme area.
     *
     * @return    SKIP_BODY
     */
    @Override
	public int doStartTag() throws JspException {
        try {
            String    thePath  = ((HttpServletRequest) this.pageContext.getRequest()).getContextPath();
            JspWriter theOut   = this.pageContext.getOut();
            Theme     theTheme = ThemeFactory.getTheme();
			String theFile = this.i18n ? Resources.getInstance().getString(ResKey.internalJsp(this.name)) : this.name;

            theFile = theTheme.getFileLink(theFile);

            theOut.write("<img vspace=\"0\" border=\"0\" src=\"" + thePath + theFile +"\" />");
        }
        catch (Exception ex) {
            throw new JspException(ex);
        }

        return (SKIP_BODY);
    }

    /**
     * This method sets the name.
     *
     * @param    aName    The name to set.
     */
    public void setName(String aName) {
        this.name = aName;
    }

    /**
     * This method sets the I18N flag.
     *
     * @param    aFlag    The I18N flag to set.
     */
    public void setI18n(boolean aFlag) {
        this.i18n = aFlag;
    }
}

