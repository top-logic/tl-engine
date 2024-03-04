/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
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

import com.top_logic.basic.Configuration;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;

/**
 * This is the super class for all JSPs that should be protected,
 * i.e. only be accessible by authenticated users.
 *
 * @author  Klaus Halfmann
 * @author  Karsten Buch
 */
public abstract class TopLogicJspBase extends TopLogicServlet 
                                      implements HttpJspPage  {

	/**
	 * Configuration for {@link TopLogicJspBase}.
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

    static long jspMaxServiceTime = Long.MAX_VALUE;
    
    // Please do no use a static block here, See #initMaxServiceTime()

    /** Default, empty implementation of jspInit */
    @Override
	public void jspInit () { /* empty */ }
    
    /** Default, empty implementation of jspDestroy */
    @Override
	public void jspDestroy () { /* empty */  }

    /** Re-dispatch to {@link #jspInit()} */
    @Override
    public final void init (ServletConfig config) throws ServletException {
        super.init (config);
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

    /**
     * Overridden to use the same for all JSPs, Configuration is to expensive otherwise.
     */
    @Override
    public void initMaxServiceTime() {
        // Configure jspMaxServiceTime here to avoid probles with JSP-Precompiling (e.g. with WebLogic) 
        if (jspMaxServiceTime == Long.MAX_VALUE) {
            synchronized (TopLogicJspBase.class) {
                jspMaxServiceTime = MAX_SERVICE_TIME;
                Configuration theConf = Configuration.getConfiguration(TopLogicJspBase.class);
				String maxServiceTimeS = getMaxServiceTime();
                if (!StringServices.isEmpty(maxServiceTimeS)) {
                    jspMaxServiceTime = Long.parseLong(maxServiceTimeS);
                }
            }
        }
        maxServiceTime = jspMaxServiceTime;
    }

    /** Re-dispatch  to jspDestroy() */
    @Override
    public final void destroy( ) {
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
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (DefaultDisplayContext.hasDisplayContext(request)) {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

			if (PerformanceMonitor.isEnabled()) {
				ProcessingInfo processingInfo = displayContext.getProcessingInfo();
				processingInfo.setJSPName(this.getClass().getSimpleName().replace('_',
						'.'));
				processingInfo.setCommandName(I18NConstants.RENDERING_COMMAND);
				processingInfo.setProcessingKind(ProcessingKind.JSP_RENDERING);
				ControlScope controlScope = displayContext.getExecutionScope();
				if (controlScope instanceof ControlSupport) {
					processingInfo.setComponentName(((ControlSupport)
							controlScope).getComponent().getName());
				}
			}
		}

		// Note: The character encoding must be set explicitly (not in combination with the content
		// type), because the JSP will set the content type later on (without a character encoding).
		// Only an explicitly set character encoding survives this update of the content type.
		//
		// response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		response.setCharacterEncoding(StringServices.UTF8);

    	_jspService(request, response);
    }
}
