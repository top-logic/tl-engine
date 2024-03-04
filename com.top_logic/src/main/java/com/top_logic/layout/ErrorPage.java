/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link ErrorPage} can be used to forward a
 * {@link HttpServletRequest} to a jsp in case an error occur. The jsp to which
 * the request can be dispatched are configured in the section "ErrorPage".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ErrorPage {

	/** Attribute of the request containing {@link HttpServletRequest#getRequestURI()} */
	public static final String JAVAX_SERVLET_ERROR_REQUEST_URI = RequestDispatcher.ERROR_REQUEST_URI;

	/**
	 * Configuration of error pages.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {
		
		/**
		 * configured error pages.
		 */
		@Key(NamedResource.NAME_ATTRIBUTE)
		Map<String, NamedResource> getPages();

	}

	/**
	 * Forwards the request (coded by the given {@link DisplayContext}) to the
	 * jsp configured under the given <code>errorKey</code>.
	 * 
	 * The jsp must be configured in the "ErrorPage"-section under the given
	 * key.
	 * 
	 * @param context
	 *        the {@link DisplayContext}wrapping the request and response.
	 * @param errorKey
	 *        the key under which the requested jsp is configured.
	 * @throws IOException
	 *         iff failures on the requested error page occur.
	 */
	public static void showPage(DisplayContext context, String errorKey) throws IOException {
		final HttpServletRequest request = context.asRequest();
		final HttpServletResponse response = context.asResponse();
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);

		String errorPage = getPage(errorKey);
		if (errorPage != null) {
			request.setAttribute(JAVAX_SERVLET_ERROR_REQUEST_URI, request.getRequestURI());
			/* This attribute is read by the Glassfish container in compiled JSP. */
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_NOT_FOUND);
			try {
				context.asServletContext().getRequestDispatcher(errorPage).forward(request, response);
			} catch (ServletException ex) {
				Logger.error("Failure on '" + errorPage + "'", ex.getRootCause(), ErrorPage.class);
				throw (IOException) new IOException().initCause(ex);
			}
		} else {
			Logger.error("No error page configured under the key '" + errorKey + "'", ErrorPage.class);
		}
	}

	private static String getPage(String errorKey) {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		NamedResource resource = config.getPages().get(errorKey);
		if (resource == null) {
			// no resource for key
			return null;
		}
		return StringServices.nonEmpty(resource.getResource());
	}

}
