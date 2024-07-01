/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;

// package org.apache.turbine.util;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and 
 *    "Apache Turbine" must not be used to endorse or promote products 
 *    derived from this software without prior written permission. For 
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without 
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/**
 * This class parses the user agent string and sets javasciptOK and
 * cssOK following the rules described below.  If you want to check
 * for specific browsers/versions then use this class to parse the
 * user agent string and use the accessor methods in this class.
 *
 * JavaScriptOK means that the browser understands JavaScript on the
 * same level the Navigator 3 does.  Specifically, it can use named
 * images.  This allows easier rollovers.  If a browser doesn't do
 * this (Nav 2 or MSIE 3), then we just assume it can't do any
 * JavaScript.  Referencing images by load order is too hard to
 * maintain.
 *
 * CSSOK is kind of sketchy in that Nav 4 and MSIE work differently,
 * but they do seem to have most of the functionality.  MSIE 4 for the
 * Mac has buggy CSS support, so we let it do JavaScript, but no CSS.
 *
 * Ported from Leon's PHP code at
 * http://www.working-dogs.com/freetrade by Frank.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:leon@clearink.com">Leon Atkisnon</a>
 * @author <a href="mailto:mospaw@polk-county.com">Chris Mospaw</a>
 * @author <a href="mailto:bgriffin@cddb.com">Benjamin Elijah Griffin</a>
 */

/* slight changes to make parsing (hopefully) more robust by mma/kha */
public class BrowserDetector implements Serializable {
    
    /** Constants used by this class. */
	public static final String MSIE = "MSIE";
	public static final String MSIE_ENGINE = "Trident";

	public static final String FEATURE_DETECTION_BROWSER = "Feature_Detection_Browser";
    public static final String FIREFOX   = "Firefox";
    
    public static final int WINDOWS      = 0x0010;
    public static final int UNIX         = 0x0020;
    public static final int MACINTOSH    = 0x0030;
    public static final int OS_X         = 0x0040;
    
    public static final int UNKNOWN      = -1;
    
    /** The user agent string. */
    private String userAgentString = "";

    /** The browser name specified in the user agent string. */
	private String browserName;
    
    /**
     * The browser version specified in the user agent string.  If we
     * can't parse the version just assume an old browser.
     */
    private float browserVersion = 1.0f;

    /**
     * The browser platform specified in the user agent string.
     */
    private int browserPlatform = UNKNOWN;

    /**
     * Default CTor for serialization.
     */
    public BrowserDetector() {
        // for serialization.
    }

    /**
     * Constructor used to initialize this class.
     *
     * @param userAgent A String with the user agent field.
     */
    public BrowserDetector(String userAgent) {
    	this.userAgentString = getAgentNonNull(userAgent);
        parse();
    }

	private String getAgentNonNull(String agentString) {
		if (agentString != null) {
			return agentString;
    	} else {
    		return "";
    	}
	}
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '[' + userAgentString + ']';
    }

    /**
     * Constructor used to initialize this class.
     *
     * @param req uset to extract the "User-Agent" from.
     */
    public BrowserDetector(HttpServletRequest req) {
		this(req.getHeader("User-Agent"));
    }

    /**
     * The browser name specified in the user agent string.
     *
     * @return A String with the browser name.
     */
    public String getBrowserName() {
        return browserName;
    }

    /**
     * The browser platform specified in the user agent string.
     *
     * @return A String with the browser platform.
     */
    public int getBrowserPlatform() {
        return browserPlatform;
    }

    /**
     * The browser version specified in the user agent string.
     *
     * @return A String with the browser version.
     */
    public float getBrowserVersion() {
        return browserVersion;
    }

    /**
     * The user agent string for this class.
     *
     * @return A String with the user agent.
     */
    public String getUserAgentString() {
        return userAgentString;
    }

    /**
     * Helper method to initialize this class parsing the user-agent
     * string.
     */
    private void parse() {
		browserName = FEATURE_DETECTION_BROWSER;

        // MSIE lies about its name.  Of course...
        // Ex: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)
        // Note: the IE check should be done BEFORE the Opera check because there
        // are some versions of Opera that also use the string "MSIE"
		checkForIE10Below(userAgentString);
        
        // Firefox is a Mozilla-based browser
        // Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.6pre) Gecko/2009011606 Firefox/3.1
        // Note: the Firefox check should also be done BEFORE the Opera - see
        // MSIE
        checkRealBrowser(userAgentString, FIREFOX, ' ');

        // Try to figure out what platform.
        if ((userAgentString.indexOf("Windows") != -1)
            || (userAgentString.indexOf("WinNT") != -1)
            || (userAgentString.indexOf("Win98") != -1)
            || (userAgentString.indexOf("Win95") != -1)) {
            browserPlatform = WINDOWS;
        } else if (userAgentString.indexOf("Mac OS X") != -1) {
            browserPlatform = OS_X;
        } else if (userAgentString.indexOf("Macintosh") != -1) {
            browserPlatform = MACINTOSH;
        } else if (userAgentString.indexOf("X11") != -1) {
            browserPlatform = UNIX;
        }
    }

    /**
     * Helper method to conver String to a float.
     *
     * @param s A String.
     * @return The String converted to float.
     */
    private float toFloat(String s) {
        return Float.parseFloat(s);
    }
    
    private void checkForIE10Below(String userAgentString) {
		int renderEnginePosition = userAgentString.indexOf(MSIE_ENGINE);
		if (renderEnginePosition != -1) {
			int renderEngineIeVersionDelta = 4;
			int engineVersionIndex = userAgentString.indexOf("/", renderEnginePosition) + 1;
			float browserVersion =
				toFloat(String.valueOf(userAgentString.charAt(engineVersionIndex))) + renderEngineIeVersionDelta;
			if (browserVersion < 11) {
				browserName = MSIE;
				this.browserVersion = browserVersion;
			}
			return;
		}
		checkRealBrowser(userAgentString, MSIE, ';');
    }
    
    /** 
     * This method sets the browserName and browserVersion fields for some browsers
     * that lie about their identity.
     * 
     * @param aUserAgentString The full user agent string.
     * @param aBrowserName     The browser name to check this string for.
     * @param aSeparator       The separator that will mark the end of the version substring.
     */
    private void checkRealBrowser(String aUserAgentString, String aBrowserName, char aSeparator) {
        if (aUserAgentString.indexOf(aBrowserName) != -1) {
            //E.g.:
            // Mozilla/4.0 (Windows NT 4.0;US) Opera 3.61  [en]
            // Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.6pre) Gecko/2009011606 Firefox/3.1
            int versionStartIndex =
                (aUserAgentString.indexOf(aBrowserName) + aBrowserName.length() + 1);
            int versionEndIndex = aUserAgentString.indexOf(aSeparator, versionStartIndex);

            browserName = aBrowserName;
            try {
                if (versionEndIndex >= 0) {
                    browserVersion =
                        toFloat(
                            aUserAgentString.substring(
                                versionStartIndex,
                                versionEndIndex));
                }
                else {
                    browserVersion =
                        toFloat(
                            aUserAgentString.substring(
                                versionStartIndex));
                }
            }
            catch (NumberFormatException e) {
                // Just use the default value.
            }
        }
    }
}
