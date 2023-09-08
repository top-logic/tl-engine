/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import com.top_logic.basic.StringServices;

/**
 * Utilities for link tags.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class LinkTagUtil {
	/**
	 * Calculates the context path. If the given context path is <code>null</code> it will be
	 * calculated by the page context. Otherwise the context path is returned.
	 * 
	 * @param pageContext
	 *        The page context to get the {@link ServletRequest} of. Must not be <code>null</code>.
	 * @param contextPath
	 *        May be <code>null</code>.
	 * 
	 * @return The calculated context path.
	 */
	public static String getContextPath(PageContext pageContext, String contextPath) {
		if (contextPath == null) {
			ServletRequest theRequest = pageContext.getRequest();
			contextPath = ((HttpServletRequest) theRequest).getContextPath();
		}

		return contextPath;
	}

	/**
	 * Concatenates the context path and the hyper reference. If the hyper reference does not start
	 * with an separator or the context path is already part of the hyper reference, only the hyper
	 * reference is returned.
	 * 
	 * @return The (concatenated) link.
	 */
	public static String getLink(String contextPath, String href) {
		if (contextPath != null && StringServices.startsWithChar(href, '/') && !href.startsWith(contextPath)) {
			return StringServices.concatenate(contextPath, href);
		} else {
			return href;
		}
	}
}
