package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: NavigationTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2003, Russell Gold
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
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests support for navigating among servlets.
 **/
public class NavigationTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( NavigationTest.class );
    }


    public NavigationTest( String name ) {
        super( name );
    }


    public void testRedirect() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "target", TargetServlet.class.getName() );
        sr.registerServlet( "origin", OriginServlet.class.getName() );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/origin?color=green" );
        assertNotNull( "No response received", response );
        assertEquals( "Expected response", "color=null: path=/target", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testForward() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/target", TargetServlet.class );
        wxs.addServlet( "/origin", FowarderServlet.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/context" );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/context/origin?color=green" );
        assertNotNull( "No response received", response );
        assertEquals( "Expected response", "color=green: path=/context/target", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testInclude() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/target", TargetServlet.class );
        wxs.addServlet( "/origin", IncluderServlet.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/context" );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/context/origin?color=green" );
        assertNotNull( "No response received", response );
        assertEquals( "Expected response", "expecting: color=blue: path=/context/origin", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testForwardViaHttpServletRequest() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/target", TargetServlet.class );
        wxs.addServlet( "/origin", FowarderServlet2.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/context" );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/context/origin?color=green" );
        assertNotNull( "No response received", response );
        assertEquals( "Expected response", "color=green: path=/context/target", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testForwardViaRelativePath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/some/target", TargetServlet.class );
        wxs.addServlet( "/some/origin", FowarderServlet3.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/context" );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/context/some/origin?color=green" );
        assertNotNull( "No response received", response );
        assertEquals( "Expected response", "color=green: path=/context/some/target", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    static class OriginServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            resp.sendRedirect( "http://localhost/target" );
        }

    }


    static class FowarderServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            getServletContext().getRequestDispatcher( "/target" ).forward( req, resp );
        }

    }


    static class FowarderServlet2 extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            req.getRequestDispatcher( "/target" ).forward( req, resp );
        }

    }


    static class FowarderServlet3 extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            req.getRequestDispatcher( "target" ).forward( req, resp );
        }

    }


    static class IncluderServlet extends HttpServlet {
        static final String PREFIX = "expecting: ";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.getWriter().print( PREFIX );
            getServletContext().getRequestDispatcher( "/target?color=blue" ).include( req, resp );
        }
    }


    static class TargetServlet extends HttpServlet {
        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( "color=" + req.getParameter( "color" ) );
            pw.print( ": path=" + req.getRequestURI() );
            pw.close();
        }

    }
}


