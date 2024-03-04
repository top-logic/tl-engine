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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;

/**
 * Simple Subclass of LayoutComponent for Demonstration of Concept. 
 * <p>
 *	Very valuable as placeholder: "ToDO XYZ Gui" ;-)
 * </p>
 * 
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleComponent extends LayoutComponent {

	public interface Config extends LayoutComponent.Config {
		@Name(ATT_CONTENT)
		@StringDefault("")
		String getContent();

		/** @see #getContent() */
		void setContent(String content);

		@Name(ATT_HEADER)
		@StringDefault("")
		String getHeader();

		/** @see #getHeader() */
		void setHeader(String header);

		@Name(ATT_USE_RESOURCES)
		@BooleanDefault(false)
		boolean getUseResources();
	}

	private static final String ATT_USE_RESOURCES = "useResources";

	private static final String ATT_HEADER = "header";

	private static final String ATT_CONTENT = "content";

	/** Header that was given in Constructor */
    protected String header;
	
	/** Content that was given in Constructor */
	protected String content;
    
    protected boolean useResources;

    public SimpleComponent(InstantiationContext context, Config atts) throws ConfigurationException  {
        super(context, atts);
        content      = atts.getContent();
        header       = atts.getHeader();
        useResources = atts.getUseResources();
    }

    /** Always called to write the actual body part. */
    @Override
	public void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out)
            throws IOException, ServletException {
		HTMLUtil.beginDiv(out, FormConstants.FORM_BODY_CSS_CLASS);
		{
			String theContent =
				useResources ? Resources.getInstance().getString(ResKey.internalLayout(this.content)) : this.content;
			out.writeContent(theContent);
		}
		HTMLUtil.endDiv(out);

    }

}
