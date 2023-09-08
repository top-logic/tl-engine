package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: RequestDispatcherTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2003, Russell Gold
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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;


public class RequestDispatcherTest extends TestCase {

    final String outerServletName  = "something/interesting";
    final String innerServletName  = "something/more";
    final String decodeExampleName = "repository/Default%20repository";


    final static String REQUEST_URI  = "javax.servlet.include.request_uri";
    final static String CONTEXT_PATH = "javax.servlet.include.context_path";
    final static String SERVLET_PATH = "javax.servlet.include.servlet_path";
    final static String PATH_INFO    = "javax.servlet.include.path_info";
    final static String QUERY_STRING = "javax.servlet.include.query_string";

    private ServletRunner _runner;


    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( RequestDispatcherTest.class );
    }


    public RequestDispatcherTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( outerServletName, RequestDispatcherServlet.class );
        wxs.addServlet( decodeExampleName, RequestDispatcherServlet.class );
        wxs.addServlet( innerServletName, IncludedServlet.class );
        _runner = new ServletRunner( wxs.asInputStream(), "/sample");
    }


    public void testRequestDispatcherParameters() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + outerServletName + "?param=original&param1=first" );


        final HttpServletRequest request = ic.getRequest();
        final HttpServletResponse response = ic.getResponse();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );

        assertEquals( "param", "original", request.getParameter( "param" ) );
        assertEquals( "param1", "first", request.getParameter( "param1" ) );
        assertNull( "param2 should not be defined", request.getParameter( "param2" ) );

        ic.pushIncludeRequest( rd, request, response );

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals( "param in included servlet", "revised", innerRequest.getParameter( "param" ) );
        assertEquals( "param1 in included servlet", "first", innerRequest.getParameter( "param1" ) );
        assertEquals( "param2 in included servlet", "new", innerRequest.getParameter( "param2" ) );

        assertEquals( "Included servlet class", IncludedServlet.class, ic.getServlet().getClass() );

        ic.popRequest();

        final HttpServletRequest restoredRequest = ic.getRequest();
        assertEquals( "reverted param", "original", restoredRequest.getParameter( "param" ) );
        assertEquals( "reverted param1", "first", restoredRequest.getParameter( "param1" ) );
        assertNull( "reverted param2 should not be defined", restoredRequest.getParameter( "param2" ) );
        assertEquals( "Included servlet class", RequestDispatcherServlet.class, ic.getServlet().getClass() );
    }


    public void testRequestDispatcherIncludePaths() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + outerServletName + "?param=original&param1=first" );

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );

        assertEquals( "request URI", "/sample/" + outerServletName, request.getRequestURI() );
        assertEquals( "context path attribute", "/sample", request.getContextPath() );
        assertEquals( "servlet path attribute", "/" + outerServletName, request.getServletPath() );
        assertNull( "path info not null attribute", request.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() ); TODO make this work

        final HttpServletResponse response = ic.getResponse();
        ic.pushIncludeRequest( rd, request, response );

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals( "request URI", "/sample/" + outerServletName, innerRequest.getRequestURI() );
        assertEquals( "context path attribute", "/sample", innerRequest.getContextPath() );
        assertEquals( "servlet path attribute", "/" + outerServletName, innerRequest.getServletPath() );
        assertNull( "path info not null attribute", innerRequest.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", innerRequest.getQueryString() );

        assertEquals( "request URI attribute", "/sample/" + innerServletName, innerRequest.getAttribute( REQUEST_URI ) );
        assertEquals( "context path attribute", "/sample", innerRequest.getAttribute( CONTEXT_PATH ) );
        assertEquals( "servlet path attribute", "/" + innerServletName, innerRequest.getAttribute( SERVLET_PATH ) );
        assertNull( "path info attribute not null", innerRequest.getAttribute( PATH_INFO ) );
//        assertEquals( "query string attribute", "param=revised&param2=new", innerRequest.getAttribute( QUERY_STRING ) );

        ic.popRequest();
        final HttpServletRequest restoredRequest = ic.getRequest();

        assertNull( "reverted URI attribute not null", restoredRequest.getAttribute( REQUEST_URI ) );
        assertNull( "context path attribute not null", restoredRequest.getAttribute( CONTEXT_PATH ) );
        assertNull( "servlet path attribute not null", restoredRequest.getAttribute( SERVLET_PATH ) );
        assertNull( "path info attribute not null", restoredRequest.getAttribute( PATH_INFO ) );
//        assertNull( "query string attribute not null", "param=revised&param2=new", restoredRequest.getAttribute( QUERY_STRING ) );
    }

    /**
     * test for fix of bug [ 1323031 ] getPathInfo does not decode request URL
     * by Hugh Winkler -
     */
    public void testDecodeRequestURL() throws Exception {
      InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + decodeExampleName );

      final HttpServletRequest request = ic.getRequest();
      RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
      RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );
      String path=request.getPathInfo();
      String servletPath=request.getServletPath();
      // System.err.println("servletPath='"+servletPath+"'\npath='"+path+"'");
      assertEquals(servletPath,"/repository/Default repository");
    }  



    public void testRequestDispatcherForwardPaths() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + outerServletName + "?param=original&param1=first" );

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );

        assertEquals( "request URI", "/sample/" + outerServletName, request.getRequestURI() );
        assertEquals( "context path attribute", "/sample", request.getContextPath() );
        assertEquals( "servlet path attribute", "/" + outerServletName, request.getServletPath() );
        assertNull( "path info not null attribute", request.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() ); TODO make this work

        final HttpServletResponse response = ic.getResponse();
        ic.pushForwardRequest( rd, request, response );

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals( "request URI", "/sample/" + innerServletName, innerRequest.getRequestURI() );
        assertEquals( "context path attribute", "/sample", innerRequest.getContextPath() );
        assertEquals( "servlet path attribute", "/" + innerServletName, innerRequest.getServletPath() );
        assertNull( "path info not null attribute", innerRequest.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", innerRequest.getQueryString() );

        ic.popRequest();
        final HttpServletRequest restoredRequest = ic.getRequest();

        assertNull( "reverted URI attribute not null", restoredRequest.getAttribute( REQUEST_URI ) );
        assertNull( "context path attribute not null", restoredRequest.getAttribute( CONTEXT_PATH ) );
        assertNull( "servlet path attribute not null", restoredRequest.getAttribute( SERVLET_PATH ) );
        assertNull( "path info attribute not null", restoredRequest.getAttribute( PATH_INFO ) );
//        assertNull( "query string attribute not null", "param=revised&param2=new", restoredRequest.getAttribute( QUERY_STRING ) );
    }

    static class RequestDispatcherServlet extends HttpServlet {

        public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/subdir/pagename.jsp?param=value&param2=value" );
            dispatcher.forward( request, response );
        }
    }


    static class IncludedServlet extends HttpServlet {

        static String DESIRED_REQUEST_URI = "localhost/subdir/pagename.jsp";
        static String DESIRED_SERVLET_PATH = "/subdir/pagename.jsp";
        static String DESIRED_QUERY_STRING = "param=value&param2=value";
        static String DESIRED_OUTPUT = DESIRED_REQUEST_URI + DESIRED_QUERY_STRING + DESIRED_SERVLET_PATH;


        public void service( HttpServletRequest request, HttpServletResponse response ) throws IOException {
            response.setContentType( "text/plain" );
            String requestUri = (String) request.getAttribute( REQUEST_URI );
            String queryString = (String) request.getAttribute( QUERY_STRING );
            String servletPath = (String) request.getAttribute( SERVLET_PATH );
            PrintWriter pw = response.getWriter();
            pw.write( blankIfNull( requestUri ) );
            pw.write( blankIfNull( queryString ) );
            pw.write( blankIfNull( servletPath ) );
            pw.close();
        }

        private String blankIfNull( String s ) {
            return s == null ? "" : s;
        }
    }
}
