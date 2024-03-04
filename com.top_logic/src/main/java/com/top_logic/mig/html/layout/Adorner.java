/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.io.Serializable;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;

/**
 * An Adorner component as helper for some other, Host component.
 * 
 * Adorners are light weight and can be shared so they should not save any state.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class Adorner implements Serializable, Cloneable, View {
            
    /** 
     * This will called by your Host(s) to write us. 
     *
     * @param aComponent  The Component that you are written by.
     * 
     * @deprecated please use {@link #write(DisplayContext, TagWriter)} as implied by {@link View}. 
     */    
    @Deprecated
	public void write(ServletContext aContext, 
                      HttpServletRequest aRequest, HttpServletResponse aResponse, 
                      TagWriter anOut, LayoutComponent aComponent) 
                      throws IOException, ServletException {
        // deprecated, no need to override this any more
    }


    /**
     * Should be called by anOwner from its componentsResolved() method. 
     * 
     * This allows the Adorner to do necessary adjustments 
     * when the layout is in place.
     * 
     *  @return false to indicate that you do not want to be shown
     */
    public boolean componentsResolved(DisplayContext aContext, LayoutComponent anOwner) {
        return true;
    }

    @Override
	public boolean isVisible() {
        return true;
    }

    /**
     * Adapt {@link View} interface to Adorner interface. 
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aOut) throws IOException {
        try {
            write(aContext.asServletContext(), aContext.asRequest(), null, (TagWriter) aOut, MainLayout.getComponent(aContext));
        }
        catch (ServletException se) {
            throw (IOException) new IOException(se.getMessage()).initCause(se);
        }
    }
    
}

