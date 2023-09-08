/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.StringServices;

/**
 * Structure representing a HTML link.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class HTMLLink {

    private String url;

    private String name;

    /** 
     * Creates a {@link HTMLLink}.
     */
    public HTMLLink(String anURL, String aName) {
        this.url  = anURL;
        this.name = aName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " ["+
                "URL: '" + this.url +
                "', name: '" + this.name +
                "']";
    }

    /**
     * This method returns the URL.
     * 
     * @return    Returns the URL.
     */
    public String getURL() {
        return (this.url);
    }

    /**
     * This method returns the name.
     * 
     * @return    Returns the name.
     */
    public String getName() {
        return (this.name);
    }
    
    /**
     * This method creates the default window.open script for external logins. It uses the
     * config alias %EXTERNAL_WINDOW_PROPS%. If this alias is not set, the default values
     * will be used.
     * 
     * @param anURL
     *            the url to open
     * @param aName
     *            the name
     * @return a string containing the window.open script snipplet
     */
    public static String getDefaultWindowOpen(String anURL, String aName) {
        String theProps = AliasManager.getInstance().getAlias("%EXTERNAL_WINDOW_PROPS%");
        if(StringServices.isEmpty(theProps)){
            theProps = "menubar=no,status=no,location=no,toolbar=no,resizable=yes";
        }
        return getWindowOpen(anURL, aName, theProps);
    }
    
    public static String getWindowOpen(String anURL, String aName, String windowProperties) {
        if (windowProperties == null) {
            return "window.open('" + anURL + "', '" + aName + "', '');";
        }
        return "window.open('" + anURL + "', '" + aName + "', '" + windowProperties + "');";
    }
}

