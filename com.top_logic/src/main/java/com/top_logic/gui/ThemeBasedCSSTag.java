/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import com.top_logic.base.taglibs.basic.CSSLinkTag;
import com.top_logic.basic.Logger;

/**
 * New link tag for usage in layout framework.
 * 
 * This tag will know by itself, which links to create.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ThemeBasedCSSTag extends CSSLinkTag {

    /** Flag,m if this tag has been used in the page before. */
    private static final String CSS_WRITTEN = "cssWritten";

    
    public ThemeBasedCSSTag() {
        super();
    }

    @Override
	protected void writeLink(JspWriter out) throws IOException {
        if (this.pageContext.getAttribute(CSS_WRITTEN) == null) {
			String css = ThemeFactory.getTheme().getStyleSheet();
            String   thePath   = this.getContextPath();

			out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			if (thePath != null) {
				out.write(thePath);
			}
			out.write(css);

			if (this.reload) {
				out.write("?timestamp=");
				out.print(System.currentTimeMillis());
			}
			out.println("\" />");

			FaviconTag.write(out, thePath);

            this.pageContext.setAttribute(CSS_WRITTEN, Boolean.TRUE);
		} else {
            Logger.debug("Called twice!", this);
        }
    }
}
