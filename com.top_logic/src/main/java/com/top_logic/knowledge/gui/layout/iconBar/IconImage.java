/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.iconBar;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.ImageInfo;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * TODO TSA add some comment
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class IconImage extends ImageInfo {
    
    public interface Config extends ImageInfo.Config {
		@Name(WIRTE_PLAIN_XML_ATTRIBUTE)
		@BooleanDefault(false)
		boolean getPlain();
	}

	private static final String WIRTE_PLAIN_XML_ATTRIBUTE = "plain";
    
    private boolean writePlain;
    
    public IconImage(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        this.writePlain = atts.getPlain();
    }
    
	/** 
     * Just write out a spacer image
     */
    @Override
	public void write(ServletContext aContext, HttpServletRequest aRequest,
            HttpServletResponse aResponse, TagWriter anOut,
            LayoutComponent aComponent) throws IOException,
            ServletException {
		Writer theWriter = anOut.contentWriter();
        if (!writePlain) {
            theWriter.write("<td class=\"cmdButtonCell\" valign=\"top\">");
        }
        super.write(aContext, aRequest, aResponse, anOut, aComponent);
        if (!writePlain) {
            theWriter.write("</td>"); // TRI -> TSA: why isnt the calle responsible for drawing the cell?
        }
    }

}
