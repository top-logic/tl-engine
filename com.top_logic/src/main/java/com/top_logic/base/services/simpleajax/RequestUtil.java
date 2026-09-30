/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.jsp.PageContext;

import com.top_logic.basic.xml.TagWriter;

/**
 * Utilities for accessing common request attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RequestUtil {

	/**
	 * Name of the request attribute holding the {@link TagWriter} used for creating the response
	 * for the current request.
	 */
	private static final String ATTRIBUTE_HTML_WRITER =
		"com.top_logic.mig.html.layout.LayoutConstants.ATTRIBUTE_HTML_WRITER";

	/**
	 * Lookup the {@link TagWriter} used for serving the given request.
	 * 
	 * @see #installTagWriter(ServletRequest, TagWriter)
	 */
	public static TagWriter lookupTagWriter(ServletRequest request) {
		return (TagWriter) request.getAttribute(RequestUtil.ATTRIBUTE_HTML_WRITER);
	}

	/**
	 * Lookup the {@link TagWriter} used for serving the given {@link PageContext}'s request.
	 * 
	 * @see #lookupTagWriter(ServletRequest)
	 * @see #installTagWriter(PageContext, TagWriter)
	 */
	public static TagWriter lookupTagWriter(PageContext pageContext) {
		return lookupTagWriter(pageContext.getRequest());
	}

	/**
	 * Install the {@link TagWriter} used for serving the given request.
	 * 
	 * @see #lookupTagWriter(ServletRequest)
	 */
	public static void installTagWriter(ServletRequest request, TagWriter out) {
		request.setAttribute(RequestUtil.ATTRIBUTE_HTML_WRITER, out);
	}

	/**
	 * Install the {@link TagWriter} used for serving the given {@link PageContext}'s request.
	 * 
	 * @see #lookupTagWriter(PageContext)
	 */
	public static void installTagWriter(PageContext pageContext, TagWriter out) {
		installTagWriter(pageContext.getRequest(), out);
	}

}
