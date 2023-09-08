/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.iconBar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Resources;

/**
 * {@link View} that writes the "Set homepage" button.
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class HomepageQuestion extends IconImage {

    private static final String SET_HOMEPAGE = "setHomepage";

    public HomepageQuestion(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }
    
    /** 
     * Overridden to register the setHomepage Command in my Owner.
     * 
     * @return false when no command handler for "setHomepage" was found
     */
    @Override
	public boolean componentsResolved(DisplayContext aContext, LayoutComponent anOwner) {
        super.componentsResolved(aContext, anOwner);
        CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
        CommandHandler theHandler = theFac.getHandler(SET_HOMEPAGE);
        if (theHandler != null) {
            anOwner.registerCommand(theHandler);
            return true;
        }
        return false;
    }
    
    /**
     * Write the link to set the homepage.
     */
    @Override
	public void write(ServletContext aContext, HttpServletRequest aRequest, HttpServletResponse aResponse, TagWriter anOut, LayoutComponent aComponent) throws IOException, ServletException {
        String contextPath = aRequest.getContextPath();
        anOut.beginBeginTag("a");
        anOut.writeContent("href=\"javascript:confirmHomepage()\"");                            
        anOut.endBeginTag();
        this.writeInner(contextPath, anOut);
        HTMLUtil.endA(anOut);
    }
    
    /**
	 * Write a piece of Javascript needed by this adorner
	 * 
	 * @param aSource
	 *            the {@link LayoutComponent} which renders the question
	 * @param aTarget
	 *            the {@link LayoutComponent} where the command is registered
	 */
	public static void writeConfirmHomepage(TagWriter anOut, LayoutComponent aSource, LayoutComponent aTarget) {
//		String theReference = LayoutUtils.getFrameReference(aSource, aTarget);
//
		String theQuestion = Resources.getInstance().getString(I18NConstants.CONFIRM_SET_HOMEPAGE);
		theQuestion = HTMLUtil.encodeJS(theQuestion);
		PrintWriter pOut = anOut.getPrinter();
		pOut.println("function confirmHomepage() {");
		pOut.write('\t');
		pOut.println("if (confirm('" + theQuestion + "')) {");
		//        pOut.println("\t\t" + theReference + ".setHomepage();");
		pOut.println("\t\t invokeCommand('" + SET_HOMEPAGE + "', null, " + aTarget.getJSContextInformation() + ");");
		pOut.println("} }");
	}
}
