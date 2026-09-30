/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledFromJSP;

/**
 * This class holds information about the User Agent (read: Web Browser)
 * used by the owner of the current session.
 *
 * BrowserDetector from the Apache/Turbine project is used as backend.
 *
 * @author  Mathias Maul
 */
public class UserAgent extends BrowserDetector {

	/**
	 * CSS class marking an IE browser.
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
    public static final String IE_CSS_CLASS = "IE";
    
	/**
	 * CSS class marking an IE6 browser.
	 * 
	 * <p>
	 * Always combined with {@link #IE_CSS_CLASS}
	 * </p>
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
    public static final String IE6_CSS_CLASS = "IE6";

	/**
	 * CSS class marking an IE7 browser.
	 * 
	 * <p>
	 * Always combined with {@link #IE_CSS_CLASS}
	 * </p>
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
    public static final String IE7_CSS_CLASS = "IE7";

	/**
	 * CSS class marking an IE8 browser.
	 * 
	 * <p>
	 * Always combined with {@link #IE_CSS_CLASS}
	 * </p>
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
    public static final String IE8_CSS_CLASS = "IE8";

	/**
	 * CSS class marking an {@value} browser.
	 * 
	 * <p>
	 * Always combined with {@link #IE_CSS_CLASS}
	 * </p>
	 * 
	 * @since 5.7.3
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
	public static final String IE9_CSS_CLASS = "IE9";

	/**
	 * CSS class marking an {@value} browser.
	 * 
	 * <p>
	 * Always combined with {@link #IE_CSS_CLASS}
	 * </p>
	 * 
	 * @since 5.7.3
	 * 
	 * @see #writeBrowserClass(Appendable)
	 */
	public static final String IE10_CSS_CLASS = "IE10";

	/** The user agent (navigator.userAgent) of the current session. */ 
	public static final String USERAGENT = "user_agent";

    private boolean is_feature_detection_browser;
    
	private boolean is_firefox;

    private boolean is_ie;
    private boolean is_ie6up;
    private boolean is_ie7up;
    private boolean is_ie8up;

	private boolean is_ie9up;

	private boolean is_ie10up;
    
    private boolean is_unknown;
    
    private boolean is_httpunit;

    /**
     * Default CTor for serialization.
     */
    public UserAgent() {
        // for serialization.
    }

    /**
     * @param userAgentString A String with the user agent field.
     */
    public UserAgent(String userAgentString) {
        super (userAgentString);
        String bName    = getBrowserName();
        float bVersion  = getBrowserVersion();

        is_feature_detection_browser= bName.equals(BrowserDetector.FEATURE_DETECTION_BROWSER);
        
		is_firefox = bName.equals(BrowserDetector.FIREFOX);

        is_ie     = bName.equals(BrowserDetector.MSIE);
        if (is_ie) {
            is_ie6up  = bVersion >= 6;
            
            
            // IE 8 in compatibility mode cannot be distinguished from native IE 7, unfortunately their rendering behavior differs.
            // IE 7 native:
            //   Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)
            // IE 8 native:
            //   Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)
            // IE 8 in compatibility mode:
            //   Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)
            
            is_ie7up  = bVersion >= 7;
            is_ie8up  = bVersion >= 8;
			is_ie9up = bVersion >= 9;
			is_ie10up = bVersion >= 10;
        }

        is_unknown = "unknown".equals(bName);
        
        is_httpunit = bName.equals("httpunit");
    }
 
    /**
     * initialize a UserAgent from a request, the most common way.
     *
     * @param req uset to extract the "User-Agent" from.
     */
    public UserAgent(HttpServletRequest req) {
        this(req.getHeader("User-Agent"));
    }
    
    /**
	 * Returns true if the User Agent will be supported by feature detection
	 */
    public boolean is_feature_detection_browser() {
        return is_feature_detection_browser;
    }
    
    /** 
     * Returns true if the User Agent is any version of Firefox.
     */
    public boolean is_firefox() {
        return is_firefox;
    }
    
    /**
     * Returns true if the User Agent is MSIE 6 or later
     * 
     * @since TL_5_7
     */
    public boolean is_ie6up() {
        return is_ie6up;
    }
    
    /**
     * Returns true if the User Agent is MSIE 7 or later
     * 
     * @since TL_5_7
     */
    public boolean is_ie7up() {
        return is_ie7up;
    }
    
    /**
     * Returns true if the User Agent is MSIE 8 or later
     * 
     * @since TL_5_7
     */
    public boolean is_ie8up() {
        return is_ie8up;
    }

	/**
	 * Returns true if the User Agent is MSIE 9 or later
	 * 
	 * @since 5.7.3
	 */
	public boolean is_ie9up() {
		return is_ie9up;
	}

	/**
	 * Returns true if the User Agent is MSIE 10 or later
	 * 
	 * @since 5.7.3
	 */
	public boolean is_ie10up() {
		return is_ie10up;
	}

    /**
     * Returns true if the User Agent is any version of MSIE
     */
    public boolean is_ie() {
        return is_ie;
    }


    /**
     * Returns true if the User Agent is unknown
     */
    public boolean is_unknown() {
        return is_unknown;
    }
    
    /** 
     * Returns true if the platform is Windows-based.
     */
    public boolean is_MsWindows() {
        return getBrowserPlatform() == WINDOWS;
    }
    
    /** 
     * Returns true if the platform is Unix-based.
     */
    public boolean is_Unix() {
        return getBrowserPlatform() == UNIX;
    }
    
    /** 
     * Returns true if the platform is Mac-based.
     */
    public boolean is_OS_X() {
        return getBrowserPlatform() == OS_X;
    }

    /**
     * Whether the browser is a <code>HttpUnit</code> client.
     */
    public boolean is_httpunit() {
		return is_httpunit;
	}

    /**
     * Pseudo Constructor that will create or fetch a UserAgent from the Session.
     * 
     * @param request The request.
     */
    public static UserAgent getUserAgent(HttpServletRequest request) {
		// Make sure not to establish a session, if there is none before. This
		// happens, if the UserAgent is used e.g. on login pages.
        HttpSession session = request.getSession(false);
        if (session == null) {
            UserAgent result = (UserAgent) request.getAttribute(USERAGENT);
            if (result == null) {
            	result = new UserAgent(request);
            	request.setAttribute(USERAGENT, result);
            }
        	return result;
        }
        
        UserAgent result = (UserAgent) session.getAttribute(USERAGENT);
        if (result == null) {
            result = new UserAgent(request);
            session.setAttribute(USERAGENT, result);
        }
        return result;
    }

	/**
	 * Append CSS classes that allow creating browser switches in CSS to the given buffer.
	 * 
	 * @param out
	 *        The buffer to add the CSS classes identifying the user agent in CSS.
	 * 
	 * @see #IE_CSS_CLASS
	 * @see #IE6_CSS_CLASS
	 * @see #IE7_CSS_CLASS
	 * @see #IE8_CSS_CLASS
	 * @see #IE9_CSS_CLASS
	 * @see #IE10_CSS_CLASS
	 * 
	 * @since TL_5_7
	 */
	public void writeBrowserClass(Appendable out) throws IOException {
        if (is_ie()) {
			HTMLUtil.appendCSSClass(out, IE_CSS_CLASS);
        	
			if (is_ie10up()) {
				out.append(IE10_CSS_CLASS);
			} else if (is_ie9up()) {
				out.append(IE9_CSS_CLASS);
			} else if (is_ie8up()) {
				out.append(IE8_CSS_CLASS);
            } else if (is_ie7up()) {
				out.append(IE7_CSS_CLASS);
            } else if (is_ie6up()) {
				out.append(IE6_CSS_CLASS);
            }
        }
	}

	/**
	 * Add browser-specific classes to the given set of custom CSS classes.
	 * 
	 * <p>
	 * Note: This method is intended to be called from JSP pages.
	 * </p>
	 * 
	 * @param customClasses
	 *        The custom CSS classes.
	 * @return The given CSS classes appended with browser-specific classes.
	 * 
	 * @since TL 5.7
	 */
	@CalledFromJSP
	public CharSequence getCSSClasses(String customClasses) {
		try {
			StringWriter result = new StringWriter();

			// Buffer simulating the behavior of a TagWriter in CSS class mode.
			Appendable buffer = new Appendable() {
				@Override
				public Appendable append(CharSequence csq, int start, int end) throws IOException {
					if (end > start) {
						if (result.getBuffer().length() > 0) {
							result.append(' ');
						}
						result.append(csq, start, end);
					}
					return this;
				}

				@Override
				public Appendable append(char c) throws IOException {
					return append(String.valueOf(c));
				}

				@Override
				public Appendable append(CharSequence csq) throws IOException {
					return append(csq, 0, csq.length());
				}
			};

			buffer.append(customClasses);
			writeBrowserClass(buffer);
			return result.toString();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Checks whether it is an InternetExplorer in Version 6 or older.
	 * 
	 * @since TL 5.7.2
	 */
	public static boolean is_ie6down(UserAgent agent) {
		return agent.is_ie() && !agent.is_ie7up();
	}

	/**
	 * Checks whether it is an InternetExplorer in Version 8 or older.
	 * 
	 * @since TL 5.7.2
	 */
	public static boolean is_ie8down(UserAgent agent) {
		return agent.is_ie() && !agent.is_ie9up();
	}

	/**
	 * Checks whether it is an InternetExplorer in Version 9 or older.
	 * 
	 * @since TL 5.7.3
	 */
	public static boolean is_ie9down(UserAgent agent) {
		return agent.is_ie() && !agent.is_ie10up();
	}
}
