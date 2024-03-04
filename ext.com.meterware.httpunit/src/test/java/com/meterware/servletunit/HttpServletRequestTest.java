package com.meterware.servletunit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
/********************************************************************************************************************
* $Id: HttpServletRequestTest.java 307838 2022-01-21 14:17:54Z bhu $
*
* Copyright (c) 2000-2008 by Russell Gold
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
* documentation files (the "Software"), to deal in the Software without restriction, including without limitation
* the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
* to permit persons to whom the Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial portions
* of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
* THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*******************************************************************************************************************/
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the ServletUnitHttpRequest class.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HttpServletRequestTest extends ServletUnitTest {

    private ServletUnitContext _context;


    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( HttpServletRequestTest.class );
    }


    public HttpServletRequestTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _context = new ServletUnitContext( null, null, new SessionListenerDispatcher() {
            public void sendSessionCreated( HttpSession session ) {}
            public void sendSessionDestroyed( HttpSession session ) {}
            public void sendAttributeAdded( HttpSession session, String name, Object value ) {}
            public void sendAttributeReplaced( HttpSession session, String name, Object oldValue ) {}
            public void sendAttributeRemoved( HttpSession session, String name, Object oldValue ) {}
        } );

    }


    public void testHeaderAccess() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "sample", "value" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        assertEquals( "sample header value", "value", request.getHeader( "sample") );

        assertContains( "Header names", "sample", request.getHeaderNames() );
        Enumeration e = request.getHeaders( "Sample" );
        assertNotNull( "No header enumeration returned", e );
        assertTrue( "Enumeration is empty", e.hasMoreElements() );
        assertEquals( "first header", "value", e.nextElement() );
        assertFalse( "Enumeration has spurious header value", e.hasMoreElements() );
    }
    
    /**
     * test getting a date header;
     * @throws Exception
     */
    public void testDateHeaderAccess() throws Exception {
      WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
      String dateStr="Mon, 26 Jul 1997 05:00:00 GMT";
      Date testDate=new Date(dateStr);
      wr.setHeaderField( "Expires", dateStr );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
      String dateStrRequest=request.getHeader("Expires");
      assertTrue("Expires header field",dateStrRequest.equals(dateStr));
      // invalid date headers return -1 
      long requestDate=request.getDateHeader("invalid");
      assertTrue("invalid date header field",requestDate==-1);
      // valid date header field return the millisecs
      requestDate=request.getDateHeader("Expires");
      assertTrue("Expires date header field",requestDate==testDate.getTime());
    }  


    private void assertContains( String comment, String string, Enumeration headerNames ) {
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            if (name.equalsIgnoreCase( string )) return;
        }
        fail( comment + " does not contain " + string );
    }


    public void testGetDefaultProperties() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        assertNull( "Authorization incorrectly specified", request.getAuthType() );
        assertNull( "Character encoding incorrectly specified", request.getCharacterEncoding() );
        assertEquals( "Parameters unexpectedly specified", "", request.getQueryString() );
        assertNotNull( "No input stream available", request.getInputStream() );
    }


    public void testSetSingleValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "age parameter", "12", request.getParameter( "age" ) );
        assertNull( "unset parameter should be null", request.getParameter( "unset" ) );
    }


    public void testSetMultiValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertMatchingSet( "age parameter", new String[] { "12" }, request.getParameterValues( "age" ) );
        assertMatchingSet( "color parameter", new String[] { "red", "blue" }, request.getParameterValues( "color" ) );
        assertNull( "unset parameter should be null", request.getParameterValues( "unset" ) );
    }
    
    /**
     * test for bug report [ 1143757 ] encoding of Special charcters broken with 1.6
     * by Klaus Halfmann
     * test for bug report [ 1159810 ] URL encoding problem with ServletUnit
     * by Sven Helmberger 
     */
    public void testParameterWithSpacesAndAt() throws Exception {
      WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
      String pValueWithSpaces="This Input is to long";
      String pValueWithAt="@user@example.org";
      wr.setParameter("age" ,pValueWithSpaces);
      String result=wr.getParameter("age");
      // according to Klaus Halfman as of 2005-02-18 / version 1.6 results in "This+Input+is+to+long"
      // System.err.println(result);
      assertEquals("spaces should survive",pValueWithSpaces,result);
      wr.setParameter("age" ,pValueWithAt);
      result=wr.getParameter("age");
      assertEquals("@ should survive",pValueWithAt,result);
    }   

    /**
     */
    public void testParameterWithAt() throws Exception {
      WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
      String pValueWithSpaces="This Input is to long";
      wr.setParameter("age" ,pValueWithSpaces);
      String result=wr.getParameter("age");
      // according to Klaus Halfman as of 2005-02-18 / version 1.6 results in "This+Input+is+to+long"
      // System.err.println(result);
      assertEquals("spaces should survive",pValueWithSpaces,result);
    }   

    public void testParameterMap() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        Map map = request.getParameterMap();
        assertMatchingSet( "age parameter", new String[] { "12" }, (Object[]) map.get( "age" ) );
        assertMatchingSet( "color parameter", new String[] { "red", "blue" }, (Object[]) map.get( "color" ) );
        assertNull( "unset parameter should be null", map.get( "unset" ) );
    }


    public void testSetQueryString() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "query string", "color=red&color=blue&age=12", request.getQueryString() );
    }


    /**
     * test Bug report 1212204
     * by Brian Bonner
     * @throws Exception
     */
    public void testBug1212204() throws Exception {
    	WebRequest request = new
    	GetMethodWebRequest("http://localhost/pathinfo?queryString");
    	//assertEquals("queryString", request.getQueryString());
    	request = new
    	GetMethodWebRequest("http://localhost/pathinfo?queryString");
    	request.setParameter("queryString","");
    	assertEquals("queryString=", request.getQueryString());
    	request = new
    	GetMethodWebRequest("http://localhost/pathinfo?queryString");
    	request.setParameter("queryString",(String)null);
    	assertEquals("queryString", request.getQueryString());
    	WebRequest wr = new
    		GetMethodWebRequest("http://localhost?wsdl");
    			wr.setParameter("abc","def");		
    			wr.setParameter("def","");
    			wr.setParameter("test",(String)null);
    			wr.setParameter("wsdl", (String)null);		
    	
    			assertEquals("wsdl&abc=def&def=&test",wr.getQueryString());
    }	


    public void testInlineSingleValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "age parameter", "12", request.getParameter( "age" ) );
        assertNull( "unset parameter should be null", request.getParameter( "unset" ) );
    }


    public void testInlineParameterWithEmbeddedSpace() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple?color=dark+red&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "age parameter", "12", request.getParameter( "age" ) );
        assertEquals( "color parameter", "dark red", request.getParameter( "color" ) );
    }


    public void testInlineMultiValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertMatchingSet( "age parameter", new String[] { "12" }, request.getParameterValues( "age" ) );
        assertMatchingSet( "color parameter", new String[] { "red", "blue" }, request.getParameterValues( "color" ) );
        assertNull( "unset parameter should be null", request.getParameterValues( "unset" ) );
    }

    
    /**
     * test patch for
     * [ 1705925 ] Bug in URL-decoding of GET-Request-Parameters
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1705925&group_id=6550&atid=106550
     * @throws Exception
     */
    
    public void    testGetMethodRequestParametersEncodedWithDefaultCharacterSet_Hebrew()  throws Exception {
    	String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
    	String paramString = "param1=red&param2=%E0%E1%E2%E3"; // use iso-8859-8 to encode the data, then string is URL encoded
    	HttpUnitOptions.setDefaultCharacterSet("ISO-8859-8");
    	WebRequest wr = new GetMethodWebRequest( "http://localhost/simple");
    	wr.setParameter("param1", "red");
    	wr.setParameter("param2", hebrewValue);
    	ServletUnitHttpRequest request = new  ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), new byte[0]);
    	assertEquals( "param1 value", "red", request.getParameter("param1") );
    	assertEquals( "param2 value", hebrewValue, request.getParameter("param2") );
    }

    /**
     * test patch for
     * [ 1705925 ] Bug in URL-decoding of GET-Request-Parameters
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1705925&group_id=6550&atid=106550
     * @throws Exception
     */
    public void testGetMethodRequestParametersEncodedWithDefaultCharacterSet_UTF8() throws  Exception {
    	String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
    	String paramString = "param1=red&param2=%E0%E1%E2%E3"; // use utf-8 to encode the data, then string is URL encoded
    	HttpUnitOptions.setDefaultCharacterSet("UTF-8");
    	WebRequest wr = new GetMethodWebRequest( "http://localhost/simple");
    	wr.setParameter("param1", "red");
    	wr.setParameter("param2", hebrewValue);
    	ServletUnitHttpRequest request = new  ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), new byte[0]);
    	assertEquals( "param1 value", "red", request.getParameter("param1") );
    	assertEquals( "param2 value", hebrewValue, request.getParameter("param2") );
    }

    public void notestInlineQueryString() throws Exception {     // TODO make this work
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "query string", "color=red&color=blue&age=12", request.getQueryString() );
    }


    public void testRequestMessageBody() throws Exception {
        String body = "12345678901234567890";
        InputStream stream = new ByteArrayInputStream( body.getBytes( "UTF-8" ) );
        WebRequest wr = new PutMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), body.getBytes() );

        assertEquals( "Request content length", body.length(), request.getContentLength() );
        BufferedInputStream bis = new BufferedInputStream( request.getInputStream() );
        byte[] buffer = new byte[ request.getContentLength() ];
        bis.read( buffer );
        assertEquals( "Request content", body, new String( buffer ) );
    }


    public void testDefaultAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        assertNull( "attribute should not be defined yet", request.getAttribute( "unset" ) );
        assertTrue( "attribute enumeration should be empty", !request.getAttributeNames().hasMoreElements() );
    }


    public void testNonDefaultAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        Object value = Integer.valueOf(1);

        request.setAttribute( "one", value );

        assertEquals( "attribute one", value, request.getAttribute( "one" ) );

        Enumeration names = request.getAttributeNames();
        assertTrue( "attribute enumeration should not be empty", names.hasMoreElements() );
        assertEquals( "contents in enumeration", "one", names.nextElement() );
        assertTrue( "attribute enumeration should now be empty", !names.hasMoreElements() );
    }


    public void testDuplicateAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        request.setAttribute( "one", Integer.valueOf(1) );
        request.setAttribute( "one", "One" );
        assertEquals( "Revised attribute value", "One", request.getAttribute( "one" ) );
    }


    public void testNullAttributeValue() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        request.setAttribute( "one", "One" );
        assertEquals( "Initial attribute value", "One", request.getAttribute( "one" ) );
        request.setAttribute( "one", null );
        assertNull( "Attribute 'one' should have been removed", request.getAttribute( "one" ) );
    }


    public void testDefaultCookies() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        Cookie[] cookies = request.getCookies();
        assertNull( "Unexpected cookies found", cookies );
    }


    public void testSetCookieViaRequestHeader() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "Cookie", "flavor=vanilla,variety=sandwich");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );

        Cookie[] cookies = request.getCookies();
        assertNotNull( "No cookies found", cookies );
        assertEquals( "Num cookies found", 2, cookies.length );
        assertEquals( "Cookie 1 name", "flavor", cookies[0].getName() );
        assertEquals( "Cookie 1 value", "vanilla", cookies[0].getValue() );
        assertEquals( "Cookie 2 name", "variety", cookies[1].getName() );
        assertEquals( "Cookie 2 value", "sandwich", cookies[1].getValue() );
    }


    public void testGetSessionForFirstTime() throws MalformedURLException {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        ServletUnitContext context = _context;
        assertEquals( "Initial number of sessions in context", 0, context.getSessionIDs().size() );

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        assertNull( "New request should not have a request session ID", request.getRequestedSessionId() );
        assertNull( "New request should not have a session", request.getSession( /* create */ false ) );
        assertEquals( "Number of sessions in the context after request.getSession(false)", 0, context.getSessionIDs().size() );

        HttpSession session = request.getSession();
        assertNotNull( "No session created", session );
        assertTrue( "Session not marked as new", session.isNew() );
        assertEquals( "Number of sessions in context after request.getSession()", 1, context.getSessionIDs().size() );
        assertSame( "Session with ID", session, context.getSession( session.getId() ) );
        assertNull( "New request should still not have a request session ID", request.getRequestedSessionId() );
    }

    /**
     * Test recognition of cookies defined on the client.
     * is test case for [ 1151277 ] httpunit 1.6 breaks Cookie handling for ServletUnitClient
     * by Michael Corum
     */
    public void testGetUserCookies() throws Exception {
        String FIRST_COOKIE = "RANDOM_COOKIE";
        String SECOND_COOKIE = "ANOTHER_COOKIE";
        String FIRST_COOKIE_VALUE = "cookie1";
        String SECOND_COOKIE_VALUE = "cookie2";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "testServlet", "ServletName" );
        ServletUnitClient m_sc = sr.newClient();
        m_sc.putCookie( FIRST_COOKIE, FIRST_COOKIE_VALUE );
        m_sc.putCookie( SECOND_COOKIE, SECOND_COOKIE_VALUE );

        InvocationContext invocation = m_sc.newInvocation( "http://localhost/testServlet" );
        HttpServletRequest requ = invocation.getRequest();

        Cookie[] cookies = requ.getCookies();
        assertEquals( 2, cookies.length );
        Cookie firstActualCookie = cookies[0];
        Cookie secondActualCookie = cookies[1];

        assertEquals( FIRST_COOKIE, firstActualCookie.getName() );
        assertEquals( SECOND_COOKIE, secondActualCookie.getName() );

        assertEquals( FIRST_COOKIE_VALUE, firstActualCookie.getValue() );
        assertEquals( SECOND_COOKIE_VALUE, secondActualCookie.getValue() );
    }
    

    /**
     * Verifies that even when session creation is not explicitly requested, the inclusion of a session cookie
     * will cause a session to be made available.
     */
    public void testRetrieveSession() throws Exception {
        ServletUnitContext context = _context;
        final ServletUnitHttpSession session = context.newSession();
        final String sessionID = session.getId();

        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + sessionID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        assertEquals( "Requested session ID defined in request", sessionID, request.getRequestedSessionId() );

        assertSame( "Session returned when creation not requested", session, request.getSession( /* create */ false ) );
        assertSame( "Session returned when creation requested", session, request.getSession( true ) );
    }


    public void testAccessForbiddenToInvalidSession() throws Exception {
        ServletUnitContext context = _context;

        HttpSession session = context.newSession();
        session.setAttribute( "Initial", Integer.valueOf( 1 ) );
        Enumeration attributeNames = session.getAttributeNames();
        assertTrue( attributeNames.hasMoreElements() );
        assertEquals( "Initial", attributeNames.nextElement() );

        session.invalidate();

        try {
            session.getAttributeNames().hasMoreElements();
            fail( "Should not be able to access an invalid session's attributes" );
        } catch (IllegalStateException ex) {
        }

        try {
            session.getAttribute( "Initial" );
            fail( "Should not be able to access an invalid session's attributes" );
        } catch (IllegalStateException ex) {
        }
    }


    /**
     * Verifies that a request for a session when the current one is invalid will result in a new session.
     *
     * Obtains a new session, invalidates it, and verifies that
     */
    public void testSessionInvalidation() throws Exception {
        ServletUnitContext context = _context;
        HttpSession originalSession = context.newSession();
        String originalID = originalSession.getId();

        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + originalID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        originalSession.setAttribute( "Initial", Integer.valueOf( 1 ) );
        Enumeration attributeNames = originalSession.getAttributeNames();
        assertTrue( attributeNames.hasMoreElements() );
        assertEquals( "Initial", attributeNames.nextElement() );

        originalSession.invalidate();

        assertNull( "Invalidated session returned", request.getSession( false ) );

        HttpSession newSession = request.getSession( true );
        assertNotNull( "getSession(true) did not return a session", newSession );
        assertNotSame( "getSession(true) returned the original invalidated session", originalSession, newSession );
        assertSame( "session returned by getSession(false)", newSession, request.getSession( false ) );
        assertSame( "Session in context with new ID", newSession, context.getSession( newSession.getId() ) );
    }


    /**
     * Verifies that a request with a bad session ID causes a new session to be generated only when explicitly requested.
     */
    public void testGetSessionWithBadCookie() throws Exception {
        ServletUnitContext context = _context;
        HttpSession originalSession = context.newSession();
        String originalID = originalSession.getId();

        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + originalID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        request.getSession();

        wr.setHeaderField( "Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + (originalID + "BAD"));
        request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );

        assertNull( "Unexpected session returned for bad cookie", request.getSession( false ) );
        assertNotNull( "Should have returned session when asked", request.getSession( true ));
        assertNotSame( "Created session", originalSession, request.getSession( true ) );
    }


    public void testGetRequestURI() throws Exception {
        ServletUnitContext context = _context;
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        assertEquals("/simple", request.getRequestURI());
        assertEquals( "http://localhost/simple", request.getRequestURL().toString() );

        wr = new GetMethodWebRequest( "http://localhost/simple?foo=bar" );
        request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, context, new Hashtable(), NO_MESSAGE_BODY );
        assertEquals("/simple", request.getRequestURI());
        assertEquals( "http://localhost/simple", request.getRequestURL().toString() );
    }


    public void testDefaultLocale() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple");

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        Locale[] expectedLocales = new Locale[] { Locale.getDefault() };
        verifyLocales( request, expectedLocales );

    }


    /**
     * verify the secure property and scheme http/https 
     * extended test for bug report [ 1165454 ] ServletUnitHttpRequest.getScheme() returns "http" for secure
     * by Jeff Mills
     * @throws Exception
     */
    public void testSecureProperty() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        assertFalse( "Incorrectly noted request as secure", request.isSecure() );
        assertEquals("http",request.getScheme(),"http");

        WebRequest secureReq = new GetMethodWebRequest( "https://localhost/simple");
        request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, secureReq, _context, new Hashtable(), NO_MESSAGE_BODY );
        assertTrue( "Request not marked as secure", request.isSecure() );
        assertEquals("https",request.getScheme(),"https");
        
        wr = new GetMethodWebRequest( "ftp://localhost/simple");
        request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        assertFalse( "Incorrectly noted request as secure", request.isSecure() );
        assertEquals("ftp",request.getScheme(),"ftp");
        
        secureReq = new GetMethodWebRequest( "ftps://localhost/simple");
        try {
        	request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, secureReq, _context, new Hashtable(), NO_MESSAGE_BODY );
          assertTrue( "Request not marked as secure", request.isSecure() );
          assertEquals("ftps",request.getScheme(),"ftps");
        } catch (java.net.MalformedURLException mue) {
        	// as of 2008-03 this happends - I'm not sure whether that should be expected WF
        	String msg=mue.getMessage();
        	// System.err.println(msg);
          assertTrue("ftps is not a known protocol",msg.indexOf("unknown protocol: ftps")>=0);
    		} 
    }


    private void verifyLocales( ServletUnitHttpRequest request, Locale[] expectedLocales ) {
        assertNotNull( "No default locale found", request.getLocale() );
        assertEquals( "default locale", expectedLocales[0], request.getLocale() );

        final Enumeration locales = request.getLocales();
        assertNotNull( "local enumeration not returned", locales );
        for (int i = 0; i < expectedLocales.length; i++) {
            assertTrue( "Expected " + expectedLocales.length + " locales, only found " + i, locales.hasMoreElements() );
            assertEquals( "Locale #" + (i+1), expectedLocales[i], locales.nextElement() );
        }
        assertFalse( "Too many locales returned", locales.hasMoreElements() );
    }


    public void testSpecifiedLocales() throws Exception {
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple");
        wr.setHeaderField( "Accept-language", "fr, en;q=0.6, en-us;q=0.7" );

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), NO_MESSAGE_BODY );
        verifyLocales( request, new Locale[] { Locale.FRENCH, Locale.US, Locale.ENGLISH } );
    }
  
  /*
   * Test for patch [ 1246438 ] For issue 1221537; ServletUnitHttpRequest.getReader not impl
   * by Tim
   */
  public void testGetInputStreamSameObject() throws Exception {
      byte[] bytes = new byte[0];
      InputStream stream = new ByteArrayInputStream( bytes );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), bytes );

      InputStream first = request.getInputStream();
      InputStream second = request.getInputStream();
      assertSame("Different InputStreams", first, second);
  }


  public void testGetInputStreamAfterGetReader() throws Exception {
      byte[] bytes = new byte[0];
      InputStream stream = new ByteArrayInputStream( bytes );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), bytes );
      
      request.getReader();
      try {
          request.getInputStream();
          fail("Expected IllegalStateException");
      } catch (IllegalStateException e) {
          // Expected
      }
  }


  public void testGetInputStream() throws Exception {
      String body = "12345678901234567890";
      InputStream stream = new ByteArrayInputStream( body.getBytes( "UTF-8" ) );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), body.getBytes() );

      BufferedInputStream bis = new BufferedInputStream( request.getInputStream() );
      byte[] buffer = new byte[ request.getContentLength() ];
      bis.read( buffer );
      assertEquals( "Request content", body, new String( buffer, "UTF-8" ) );
  }


  public void testGetReaderSameObject() throws Exception {
      byte[] bytes = new byte[0];
      InputStream stream = new ByteArrayInputStream( bytes );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), bytes );

      BufferedReader first = request.getReader();
      BufferedReader second = request.getReader();
      assertSame("Different Readers", first, second);
  }


  public void testGetReaderAfterGetInputStream() throws Exception {
      byte[] bytes = new byte[0];
      InputStream stream = new ByteArrayInputStream( bytes );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), bytes );

      request.getInputStream();
      try {
          request.getReader();
          fail("Expected IllegalStateException");
      } catch (IllegalStateException e) {
          // Expected
      }
  }


  public void testGetReaderDefaultCharset() throws Exception {
      String body = "12345678901234567890";
      InputStream stream = new ByteArrayInputStream( body.getBytes( HttpUnitUtils.DEFAULT_CHARACTER_SET ) );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), body.getBytes() );

      char[] buffer = new char[body.length()];
      request.getReader().read(buffer);
      assertEquals( "Request content", body, new String( buffer) );
  }


  /**
   * test the reader with a Specific Character set (here UTF-8)
   * @throws Exception
   * TODO make work an switch back on
   */
  public void xtestGetReaderSpecificCharset() throws Exception {
      String body = "\u05d0\u05d1\u05d2\u05d3";
      InputStream stream = new ByteArrayInputStream( body.getBytes( "UTF-8" ) );
      WebRequest wr = new PostMethodWebRequest( "http://localhost/simple", stream, "text/plain; charset=UTF-8" );
      HttpServletRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), body.getBytes() );

      char[] buffer = new char[body.length()];
      request.getReader().read(buffer);
      assertEquals( "Request content", body, new String( buffer) );
  }


  /**
   * test the specific character encoding
   * (here hebrew)
   * @throws Exception
   */
   public void testSpecifiedCharEncoding() throws Exception {   
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param1=red&param2=%E0%E1%E2%E3";  // use iso-8859-8 to encode the data
        WebRequest wr = new PostMethodWebRequest( "http://localhost/simple" );
        wr.setHeaderField( "Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-8" );
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), paramString.getBytes( "iso-8859-1" ) );
        assertEquals( "param1 value", "red", request.getParameter( "param1") );
        assertEquals( "param2 value", hebrewValue, request.getParameter( "param2") );
    }


    public void testSpecifiedCharEncoding2() throws Exception {
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        HttpUnitOptions.setDefaultCharacterSet( "iso-8859-8" );
        WebRequest wr = new PostMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "param1", "red" );
        wr.setParameter( "param2", hebrewValue );
        wr.setHeaderField( "Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-8" );
        ServletUnitClient client = ServletUnitClient.newClient( null );
        ByteArrayOutputStream messageBody = client.getMessageBody( wr );
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), messageBody.toByteArray() );
        String parameter = request.getParameter( "param2");
        assertEquals( "param2 value", hebrewValue, parameter );
        assertEquals( "param1 value", "red", request.getParameter( "param1") );
    }


    public void testSuppliedCharEncoding() throws Exception {   // xxx turn this back on
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param1=red&param2=%E0%E1%E2%E3";  // use iso-8859-8 to encode the data, then string is URL encoded
        WebRequest wr = new PostMethodWebRequest( "http://localhost/simple" );
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( NULL_SERVLET_REQUEST, wr, _context, new Hashtable(), paramString.getBytes( "ISO-8859-1" ) );
        request.setCharacterEncoding( "ISO-8859-8" );
        assertEquals( "param1 value", "red", request.getParameter( "param1") );
        assertEquals( "param2 value", hebrewValue, request.getParameter( "param2") );
    }


    private final static byte[] NO_MESSAGE_BODY = new byte[0];

    private final static ServletMetaData NULL_SERVLET_REQUEST = new ServletMetaData() {

        public Servlet getServlet() throws ServletException {
            return null;
        }


        public String getServletPath() {
            return null;
        }


        public String getPathInfo() {
            return null;
        }


        public FilterMetaData[] getFilters() {
            return null;
        }
    };
}


