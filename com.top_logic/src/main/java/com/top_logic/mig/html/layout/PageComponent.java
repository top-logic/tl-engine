/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.MediaQueryControl;

/**
 * A LayoutComponent wrapper for "intelligent" HTML Pages (JSP, Servlet ...) .
 * 
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class PageComponent extends ControlComponent implements LayoutConstants {

	public interface Config extends ControlComponent.Config {
		@Name(ATT_PAGE)
		@Mandatory
		String getPage();

		/** @see #getPage() */
		void setPage(String value);

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		@Override
		@InstanceDefault(MediaQueryControl.Layout.class)
		Layouting getContentLayouting();
	}

	/** Page to call finally */
    private String page;
    
    /** XML layout definition attribute for component page. */
    public static final String ATT_PAGE = "page";

    /**
     * CTor used by frameworks
     */
    public PageComponent(InstantiationContext context, Config atts) throws ConfigurationException  {
        super(context, atts);
        this.page     = atts.getPage();
    }
    
    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" 
                + "name: '" + this.getName()
                + "', page: '" + this.page
                + "', invalid :"   + this.isInvalid()
                + "]");
    }


    /**
     * The page to call finally. 
     * 
     * @return the page
     */
    public String getPage() {
        return page;
    }

    /**
     * The page to call finally. 
     * 
     * @param aPage the page
     */
    public void setPage(String aPage) {
        page = aPage;
    }
    
    @Override
	public void writeBody(ServletContext context,
                          HttpServletRequest req, 
                          HttpServletResponse resp,
                          TagWriter out)
        throws IOException, ServletException {
		out.flushBuffer();
        context.getRequestDispatcher(page).include(req, resp);
    }
    
    @Override
	public boolean isCompleteRenderer() {
        return true;
    }
    
    @Override
	protected boolean supportsInternalModel(Object object) {
		return true;
    }
}
