/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A {@link ContentHandler} is a resource which can handle some
 * {@link HttpServletRequest}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ContentHandler {

	/**
	 * Handles the {@link DisplayContext#asRequest() request} of the given {@link DisplayContext}
	 * and writes content to its {@link DisplayContext#asResponse() response}.
	 * 
	 * @param context
	 *        to get the {@link HttpServletRequest request} to handle and the
	 *        {@link HttpServletResponse response} to write to
	 * @param id
	 *        The prefix of the given url that triggered the invocation of this handler.
	 *        <code>null</code>, if there is no parent handler.
	 * @param url
	 *        the parser which was used to identify this resource in a resource hierarchy. The
	 *        resource which identifies this resource is already removed from that parser.
	 * @throws IOException
	 *         if some errors occurred
	 * @throws ServletException
	 *         If some servlet-specific error occurs.
	 */
	void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException;

}
