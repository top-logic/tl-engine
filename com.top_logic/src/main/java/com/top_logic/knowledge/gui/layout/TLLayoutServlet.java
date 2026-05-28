/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
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
	 * Init-parameter switch to disable XSS protection checking.
	 */
	public static final String DISABLE_SECURE_HEADER_CHECK = "disableSecureHeaderCheck";

	/**
	 * Configuration for {@link TLLayoutServlet}.
	 */
	public interface Config extends TopLogicServlet.Config {
		// same configuration as parent
	}

	private boolean _enableChecks;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String value = config.getInitParameter(DISABLE_SECURE_HEADER_CHECK);
		_enableChecks = !"true".equals(value);

	}

	/**
	 * Getter for the configuration.
	 */
	@Override
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (_enableChecks) {
			String protection = response.getHeader(HttpSecureHeaderFilter.X_XSS_PROTECTION);
			if (protection == null || protection.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/jsp/display/error/NoXssProtection.jsp");
				return;
			}
			// Check only once.
			_enableChecks = false;
		}

		super.doService(request, response);
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

	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		super.handleNoSession(request, response);

		ThreadContextManager.inSystemInteraction(TopLogicServlet.class, () -> {
			Person anonymous = PersonManager.getManager().getAnonymous();
			SessionService.getInstance().loginUser(request, response, anonymous);
		});

		redirectToStartPage(request, response);
	}
}
