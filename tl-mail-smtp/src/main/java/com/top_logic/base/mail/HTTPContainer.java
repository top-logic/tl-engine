
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.StringServices;
import com.top_logic.util.Constants;


/**
 * A container providing the capability to get information out of the HTTP
 * request and the HTTP response.
 *
 * @author  Michael Gänsler
 */
public class HTTPContainer {

    /** The postfix of the mail address (is "@top-logic.com"). */
    protected final static String DEFAULT_MAIL_ADDRESS = "@top-logic.com";

    /** The parameter defining the working user. */
    protected final static String USER_PARAMETER       = "username";

    /** The parameter defining the password of the user. */
    protected final static String PASSWORD_PARAMETER   = "password";

    /** The encapsulated request. */
    private HttpServletRequest    httpRequest;

    /** The encapsulated response. */
    private HttpServletResponse   httpResponse;

    /**
     * Creates an instance of this class. When one of the given parameters
     * is <code>null</code>, this constructor throws an <code>
     * IllegalArgumentException</code>.
     *
     * @param       aRequest                    The request to be encapsulated.
     * @param       aResponse                   The response to be encapsulated.
     * @exception   IllegalArgumentException    If one parameter is
     *                                          <code>null</code>.
     */
    public HTTPContainer (
            HttpServletRequest aRequest, HttpServletResponse aResponse)
                throws IllegalArgumentException {
        if (aRequest == null) {
            throw new IllegalArgumentException ("HTTP request parameter "
                                                + "is null");
        }
        if (aResponse == null) {
            throw new IllegalArgumentException ("HTTP response parameter "
                                                + "is null");
        }

        this.httpRequest  = aRequest;
        this.httpResponse = aResponse;
    }

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return getClass ().getName () + " [" + "httpRequest: "
               + this.httpRequest + ", httpResponse: " + this.httpResponse
               + "]";
    }

    /**
     * Returns the writer to send a response to the client browser.
     *
     * @return      The writer to send the response.
     * @exception   IOException    If an error occured.
     */
    public PrintWriter getWriter () throws IOException {
        return this.getResponse ().getWriter ();
    }

    /**
     * Returns the requested parameter as <code>int</code>. If that parameter
     * is not defined, <code>-1</code> will be returned. This information will
     * be read out of the request.
     *
     * @param    aName    The name of the parameter.
     * @return   The value or <code>-1</code> if parameter is not defined.
     */
    public int getIntParameter (String aName) {
        String theMessage = this.getParameter (aName);

        if (!StringServices.isEmpty(theMessage)) {
            return (Integer.parseInt (theMessage));
        } else {
            return (-1);
        }
    }

    /**
     * Returns the list of all values for the given parameter. If the given
     * parameter is unknown (not defined), <code>null</code> will be returned.
     * This information will be read out of the request.
     *
     * @param    aName    The name of the parameter.
     * @return   The array of values or <code>null</code> if parameter unknown.
     */
    public String[] getParameterValues (String aName) {
        return this.getRequest ().getParameterValues (aName);
    }

    /**
     * Returns the value for the given parameter as string. If the given
     * parameter is unknown (not defined), <code>null</code> will be returned.
     * This information will be read out of the request.
     *
     * @param    aName    The name of the parameter.
     * @return   The requested value or <code>null</code> if parameter unknown.
     */
    public String getParameter (String aName) {
        return this.getRequest ().getParameter (aName);
    }

    /**
     * Returns the name of the user working with this instance.
     *
     * @return      The name of the user.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
    public String getUsername () throws NoSessionFoundException {
        Object theObject = this.getAttribute (USER_PARAMETER);

        if (theObject == null) {
            return null;
        } else {
            return theObject.toString ();
        }
    }

    /**
     * Returns the mail address of the user working with this instance. At the
     * moment this address will be build in this method with the {@link
     * #DEFAULT_MAIL_ADDRESS standard mail address}.
     *
     * @return      The mail address of the user.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
    public String getUserAddress () throws NoSessionFoundException {
        return this.getUsername () + DEFAULT_MAIL_ADDRESS;
    }

    /**
     * Returns the password of the user working with this instance.
     *
     * @return      The password of the user.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
    public String getPassword () throws NoSessionFoundException {
        Object theObject = this.getAttribute (PASSWORD_PARAMETER);

        if (theObject == null) {
            return null;
        } else {
            return theObject.toString ();
        }
    }

    /**
     * Returns the requested value out of the current session. In this
     * implementation the method uses {@link
     * jakarta.servlet.http.HttpSession#getAttribute(java.lang.String)} which
     * is defined in Java Servlet 2.2 specification. If you wanna use the
     * specification 2.1, you have to overwrite this method.
     *
     * @param       aName    The name of the requested value.
     * @return      The requested value or <code>null</code> if value unknown.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
    public Object getAttribute (String aName) throws NoSessionFoundException {
        // // Version for Servlet 2.1
        // return this.getSession ().getValue (aName);
        // 
        // Version for Servlet 2.2
        // System.out.println(aName + " : " + this.getSession ().getAttribute (aName));
        return this.getSession ().getAttribute (aName);
    }

    /**
     * Store the given value in the current session. In this  implementation the
     * method uses {@link jakarta.servlet.http.HttpSession#setAttribute(java.lang.String,java.lang.Object)}
     * which is defined in Java Servlet 2.2 specification. If you wanna use the
     * specification 2.1, you have to overwrite this method.
     *
     * @param       aName       The name of the value to be set.
     * @param       anObject    The value to be set.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
    public void setAttribute (String aName, Object anObject)
            throws NoSessionFoundException {
        // // Version for Servlet 2.1
        // this.getSession ().putValue (aName, anObject);
        // 
        // Version for Servlet 2.2
        this.getSession ().setAttribute (aName, anObject);
    }

    /**
     * Returns the currently used session.
     *
     * @return      The currently used session.
     * @exception   NoSessionFoundException    If there is no opened session.
     */
     public HttpSession getSession () throws NoSessionFoundException {
        HttpServletRequest theRequest = this.getRequest ();
        HttpSession        theSession = SessionService.getInstance().getSession(theRequest);

        if (theSession == null) {
            throw new NoSessionFoundException ("No session for "
                                               + theRequest);
        }

        return theSession;
     }

    

    /**
     * Returns the encapsulated request.
     *
     * @return     The held request.
     */
    public HttpServletRequest getRequest () {
        return this.httpRequest;
    }

    /**
     * Returns the encapsulated response.
     *
     * @return     The held response.
     */
    public HttpServletResponse getResponse () {
        return this.httpResponse;
    }

    /**
     * Returns the URL to the server page providing this request. This can be
     * used to generate a new URL pointing to this application.
     *
     * @return     The URL as <code>StringBuffer</code>.
     */
    public static StringBuffer getRequestURL () {
        return new StringBuffer (HTTPContainer.getRequestURL (null));
    }

    /**
     * Returns the URL to the server page providing this request. This can be
     * used to generate a new URL pointing to this application.
     *
     * @return     The URL as <code>StringBuffer</code>.
     */
    public static String getRequestURL (Object anObject) {
        return System.getProperty (Constants.SERVLET_HOSTNAME);
    }
}
