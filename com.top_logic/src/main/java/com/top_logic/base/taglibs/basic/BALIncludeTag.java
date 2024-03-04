/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Include all JavaScripts required for the Browser Abstraction Layer depending
 * on the current user agent.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BALIncludeTag extends TagSupport {

	@Override
	public int doStartTag() throws JspException {
		TagWriter writer = MainLayout.getTagWriter(pageContext);
    	HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		UserAgent userAgent = UserAgent.getUserAgent(request);
		String contextPath = request.getContextPath();

		try {
			AJAXCommandHandler.writeBALInclude(writer, contextPath, userAgent);

			writer.flushBuffer();
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		
        return SKIP_BODY;
	}
	
}
