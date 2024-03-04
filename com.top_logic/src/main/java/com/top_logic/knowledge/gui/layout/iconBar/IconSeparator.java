/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
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

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.mig.html.layout.Adorner;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provides a separator for tool bars
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class IconSeparator extends Adorner {
	
	/** This class is stateless, so a single instance will do */
	public static final IconSeparator INSTANCE = new IconSeparator();

	public static final String SEPARATOR_IMAGE = "/icons/iconBarDevider.png";
	
	/** 
	 * Just write out a spacer image
	 */
	@Override
	public void write(ServletContext aContext, HttpServletRequest aRequest,
			HttpServletResponse aResponse, TagWriter anOut,
			LayoutComponent aComponent) throws IOException,
			ServletException {
		Writer theWriter = anOut.contentWriter();
		theWriter.write("<td class=\"cmdButtonCell\" valign=\"top\">");
		theWriter.write("<img class=\"cmdImg\" src=\"");
		theWriter.write(aRequest.getContextPath());
		theWriter.write(ThemeFactory.getTheme().getFileLink(SEPARATOR_IMAGE));
		theWriter.write("\" />");
		theWriter.write("</td>");
	}

}
