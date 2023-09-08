/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This Servlet is needed for the Benchmark AJAX Test.
 * 
 * 
 * @author    <a href=mailto:dna@top-logic.com>dna</a>
 */
public class PingServlet extends HttpServlet {

    /**
      * This Method only delivers the actual server time in ms since 1970.
      * 
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
        resp.getWriter().print(System.currentTimeMillis());
    }

}