/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLPathParser;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * This Servlet dispatches LayoutComponents not based on JSP pages or servlets.
 * 
 * Needs to be reimplemented for top-Logic to extend the TopLogicServlet
 * and this way provide security. 
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author Navid Vahdat
 */
public class TLLayoutServlet extends TopLogicServlet implements LayoutConstants {

	/**
	 * Configuration for {@link TLLayoutServlet}.
	 */
	public interface Config extends TopLogicServlet.Config {
		// same configuration as parent
	}

	/**
	 * Getter for the configuration.
	 */
	@Override
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}
    
    /**
     * Dispatch the request via a MainLayout.
     */
    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

//		// Un-comment to simulate a slow connection
//		try {
//			Thread.sleep(1600);   
//		} catch(InterruptedException e) {}
    	
//    	// Un-comment to simulate a missing frame (by selectively reloading).
//    	if (System.currentTimeMillis() > 0) {
//    		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//    		if (System.currentTimeMillis() > 0) return;
//    	}
    	
		try {
			TLSessionContext sessionContext = TLContextManager.getSession();
			if (sessionContext == null) {
				return;
			}

			if (null == req.getCharacterEncoding()) {
				// Use UTF-8 for Non-Ajax forms, too
				req.setCharacterEncoding(LayoutConstants.UTF_8);
			}
			ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
			URLPathParser requestedUri = URLPathParser.createURLPathParser(req.getPathInfo());
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(req);
			handlersRegistry.handleContent(displayContext, null, requestedUri);
		} catch (RuntimeException ex) {
			Logger.error("Problem handling request: " + ex.getMessage(), ex, TLLayoutServlet.class);
			throw ex;
		} catch (Error ex) {
			Logger.error("Error handling request: " + ex.getMessage(), ex, TLLayoutServlet.class);
			throw ex;
		}
	}

	/**
     * Calls {@link #doGet(HttpServletRequest, HttpServletResponse)}.
     */
    @Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        doGet(req, resp);
    }

}
