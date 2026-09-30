/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * A LayoutComponent wrapper for 
 * any external url i.e. http://www.google.de.
 * 
 * @author  <a href="mailto:jco@top-logic.com">jco</a>
 */
public class ExternalPageComponent extends BoundComponent implements LayoutConstants{

    public interface Config extends BoundComponent.Config {
		@Name("url")
		String getUrl();
	}

	/** Page to call finally */
    private String url;
        
    public ExternalPageComponent(InstantiationContext context, Config atts) throws ConfigurationException  {
        super(context, atts);

        this.url = StringServices.nonEmpty(atts.getUrl());
    }

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}
    
    /**
     *  Accessor for the url to call finally. 
     */
    public String getUrl() {
        return url;
    }

    /**
     *  Accessor for the url to call finally. 
     */
    public void setUrl(String aPage) {
        url = aPage;
    }

    @Override
	public void writeBody(ServletContext context,
                          HttpServletRequest req, 
                          HttpServletResponse resp,
                          TagWriter out)
        throws IOException, ServletException {

        String params = getURLParams();
        String theUrl = getUrl();
        if (!StringServices.isEmpty (params)) {
            resp.sendRedirect(theUrl + URLUtilities.getConcatSymbol(theUrl) + getURLParams());    
            
        } else {
            resp.sendRedirect(theUrl);    
        }
    }

    /**
     * Returns a Query string (excluding question mark) derived from the model.
     * 
     * @return in this case an empty String
     */
    protected String getURLParams() {
        return "";
    }
    
    /**
	 * <code>true</code>
	 * @see com.top_logic.mig.html.layout.LayoutComponent#isCompleteRenderer()
	 */
    @Override
	public boolean isCompleteRenderer() {
        return true;
    }
    
}
