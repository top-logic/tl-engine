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
import com.top_logic.mig.html.layout.Adorner;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provides a space between buttons
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class IconSpacer extends Adorner {
	
	/** This class is atateless so a single instance will do */
	public static final IconSpacer INSTANCE = new IconSpacer(); 
	
	/** 
	 * Just write out a spacer image
	 */
	@Override
	public void write(ServletContext aContext, HttpServletRequest aRequest,
			HttpServletResponse aResponse, TagWriter anOut,
			LayoutComponent aComponent) throws IOException,
			ServletException {
		Writer theWriter = anOut.contentWriter();
		theWriter.write("<td class=\"cmdButtonCell\" width=\"100%\" >");
		theWriter.write("&nbsp");
		theWriter.write("</td>");
	}

}
