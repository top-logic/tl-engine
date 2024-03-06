package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: StatefulTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2004, Russell Gold
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
import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests support for state-management behavior.
 **/
public class StatefulTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( StatefulTest.class );
    }


    public StatefulTest( String name ) {
        super( name );
    }


    public void testNoInitialState() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, StatefulServlet.class.getName() );

        WebRequest request   = new GetMethodWebRequest( "http://localhost/" + resourceName );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "No session found", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testStateCookies() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, StatefulServlet.class.getName() );

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "Returned cookie count", 1, response.getNewCookieNames().length );
    }


    public void testStatePreservation() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName1, StatefulServlet.class.getName() );
        sr.registerServlet( resourceName2, StatefulServlet.class.getName() );
        WebClient wc = sr.newClient();

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName1 );
        request.setParameter( "color", "red" );
        WebResponse response = wc.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You selected red", response.getText() );

        request = new GetMethodWebRequest( "http://localhost/" + resourceName2 );
        response = wc.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You posted red", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testSessionPreloading() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName1, StatefulServlet.class.getName() );
        sr.registerServlet( resourceName2, StatefulServlet.class.getName() );
        ServletUnitClient wc = sr.newClient();

        wc.getSession( true ).setAttribute( "color", "green" );
        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName2 );
        WebResponse response = wc.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You posted green", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    public void testSessionAccess() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName1, StatefulServlet.class.getName() );
        sr.registerServlet( resourceName2, StatefulServlet.class.getName() );

        WebRequest request = new PostMethodWebRequest( "http://localhost/" + resourceName1 );
        request.setParameter( "color", "yellow" );
        sr.getResponse( request );

        assertNotNull( "No session was created", sr.getSession( false ) );
        assertEquals( "Color attribute in session", "yellow", sr.getSession( false ).getAttribute( "color" ) );
    }


    public void testInvocationContext() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, StatefulServlet.class.getName() );
        ServletUnitClient suc = sr.newClient();

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );

        InvocationContext ic = suc.newInvocation( request );
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        assertNull( "A session already exists", ss.getColor( ic.getRequest() ) );

        ss.setColor( ic.getRequest(), "blue" );
        assertEquals( "Color in session", "blue", ss.getColor( ic.getRequest() ) );

        Enumeration e = ic.getRequest().getSession().getAttributeNames();
        assertNotNull( "No attribute list returned", e );
        assertTrue( "No attribute names in list", e.hasMoreElements() );
        assertEquals( "First attribute name", "color", e.nextElement() );
        assertTrue( "List did not end after one name", !e.hasMoreElements() );
    }


    public void testInvocationCompletion() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, StatefulServlet.class.getName() );
        ServletUnitClient suc = sr.newClient();

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );

        InvocationContext ic = suc.newInvocation( request );
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        ss.setColor( ic.getRequest(), "blue" );
        ss.writeSelectMessage( "blue", ic.getResponse().getWriter() );

        WebResponse response = ic.getServletResponse();
        assertEquals( "requested resource", "You selected blue", response.getText() );
        assertEquals( "Returned cookie count", 1, response.getNewCookieNames().length );
    }


    public void testInvocationContextUpdate() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, StatefulServlet.class.getName() );
        ServletUnitClient suc = sr.newClient();

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );

        InvocationContext ic = suc.newInvocation( request );
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        ss.setColor( ic.getRequest(), "blue" );
        suc.getResponse( ic );

        WebResponse response = suc.getResponse( "http://localhost/" + resourceName );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You posted blue", response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }


    static class StatefulServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            writeSelectMessage( req.getParameter( "color" ), resp.getWriter() );
            setColor( req, req.getParameter( "color" ) );
        }
                                                             
        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            String color = getColor( req );
            if (color == null) {
                pw.print( "No session found" );
            } else {
                pw.print( "You posted " + color );
            }
            pw.close();
        }

        protected void writeSelectMessage( String color, PrintWriter pw ) throws IOException {
            pw.print( "You selected " + color );
            pw.close();
        }

        protected void setColor( HttpServletRequest req, String color ) throws ServletException {
            req.getSession().setAttribute( "color", color );
        }


        protected String getColor( HttpServletRequest req ) throws ServletException {
            HttpSession session = req.getSession( /* create */ false );
            if (session == null) return null;

            return (String) session.getAttribute( "color" );
        }

    }
}


