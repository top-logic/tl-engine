/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.HttpJspPage;

import com.top_logic.basic.StringServices;

/**
 * Super class for JSP's that may not be used with a valid session. Typically usage is within a
 * login or logout jsp.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class NoContextJspBase extends NoContextServlet implements HttpJspPage {

	/** Default, empty implementation of jspInit */
	@Override
	public void jspInit() { /* empty */
	}

	/** Default, empty implementation of jspDestroy */
	@Override
	public void jspDestroy() { /* empty */
	}

	/** Re-dispatch to {@link #jspInit()} */
	@Override
	public final void init(ServletConfig config) throws ServletException {
		super.init(config);
		jspInit();
		_jspInit();
	}

	/**
	 * Tomcat 7 expects that JSP's derive from org.apache.jasper.runtime.HttpJspBase. This class
	 * defines _jspInit() and _jspDestroy() which are overridden by compiled JSP's. Therefore we
	 * must declare {@link #_jspInit()} and {@link #_jspDestroy()} to ensure that the overridden
	 * methods in the JSPs are executed.
	 * 
	 * @see #_jspDestroy()
	 */
	public void _jspInit() {
		// for JSP's compiled by Tomcat 7
	}

	/** Re-dispatch to jspDestroy() */
	@Override
	public final void destroy() {
		_jspDestroy();
		jspDestroy();
		super.destroy();
	}

	/**
	 * @see #_jspInit()
	 */
	protected void _jspDestroy() {
		// for JSP's compiled by Tomcat 7
	}

	@Override
	protected void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/* Set character encoding to ensure the content is always delivered in UTF-8, independent of
		 * the default in the container. */
		resp.setCharacterEncoding(StringServices.UTF8);

		_jspService(req, resp);
	}

}
