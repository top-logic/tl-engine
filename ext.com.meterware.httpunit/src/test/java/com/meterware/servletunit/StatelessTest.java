package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: StatelessTest.java 107617 2012-01-03 09:51:15Z jst $
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
import com.meterware.httpunit.*;
import com.meterware.pseudoserver.HttpUserAgentTest;

import java.io.*;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests support for stateless HttpServlets.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class StatelessTest extends TestCase {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( StatelessTest.class );
    }


    public StatelessTest( String name ) {
        super( name );
    }


    public void testNotFound() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request = new GetMethodWebRequest( "http://localhost/nothing" );
        try {
            sr.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpNotFoundException e) {
            assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode() );
        }
    }


    public void testServletCaching() throws Exception {
        final String resourceName = "something/interesting";

        assertEquals( "Initial instances of servlet class", 0, AccessCountServlet.getNumInstances() );
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, AccessCountServlet.class.getName() );

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName );
        assertEquals( "First reply", "1", sr.getResponse( request ).getText().trim() );
        assertEquals( "Instances of servlet class after first call", 1, AccessCountServlet.getNumInstances() );
        assertEquals( "Second reply", "2", sr.getResponse( request ).getText().trim() );
        assertEquals( "Instances of servlet class after first call", 1, AccessCountServlet.getNumInstances() );
        sr.shutDown();
        assertEquals( "Instances of servlet class after shutdown", 0, AccessCountServlet.getNumInstances() );
    }


    public void testServletAccessByClassName() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request = new GetMethodWebRequest( "http://localhost/servlet/" + SimpleGetServlet.class.getName() );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    public void testSimpleGet() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, SimpleGetServlet.class.getName() );

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    public void testGetWithSetParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You selected red", response.getText() );
        String[] headers = response.getHeaderFields( "MyHeader" );
        assertEquals( "Number of MyHeaders returned", 2, headers.length );
        assertEquals( "MyHeader #1", "value1", headers[ 0 ] );
        assertEquals( "MyHeader #2", "value2", headers[ 1 ] );
    }


    public void testGetWithInlineParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName + "?color=dark+red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You selected dark red", response.getText() );
    }


    public void testHeaderRetrieval() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "/Parameters", ParameterServlet.class.getName() );

        ServletUnitClient client = sr.newClient();
        client.setHeaderField( "Sample", "Value" );
        client.setHeaderField( "Request", "Client" );
        WebRequest request = new GetMethodWebRequest( "http://localhost/Parameters?color=dark+red" );
        request.setHeaderField( "request", "Caller" );
        InvocationContext ic = client.newInvocation( request );
        assertEquals( "Sample header", "Value", ic.getRequest().getHeader( "sample" ) );
        assertEquals( "Request header", "Caller", ic.getRequest().getHeader( "Request" ) );
    }


    public void testParameterHandling() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "/testForm", FormSubmissionServlet.class.getName() );

        ServletUnitClient client = sr.newClient();
        WebResponse wr = client.getResponse( "http://localhost/testForm" );
        WebForm form = wr.getForms()[0];
        form.setParameter( "login", "me" );
        form.setParameter( "password", "haha" );
        form.submit();
        assertEquals( "Resultant response", "You posted me,haha", client.getCurrentPage().getText() );
    }


    public void testSimplePost() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You posted red", response.getText() );
    }


    public void testStreamBasedPost() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "ReportData", BodyEcho.class.getName() );

        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream( sourceData.getBytes( "iso-8859-1" ) );

        WebClient wc = sr.newClient();
        WebRequest wr = new PostMethodWebRequest( "http://localhost/ReportData", source, "text/sample" );
        WebResponse response = wc.getResponse( wr );
        assertEquals( "Body response", sourceData.length() + "\n" + sourceData, response.getText() );
        assertEquals( "Content-type", "text/sample", response.getContentType() );
    }


    public void testRequestInputStream() throws Exception {
        ServletRunner sr = new ServletRunner();
        WebRequest request = new PostMethodWebRequest( "http://localhost/servlet/" + ParameterServlet.class.getName() );
        request.setParameter( "color", "green" );
        final String expectedBody = "color=green";
        InvocationContext ic = sr.newClient().newInvocation( request );
        assertEquals( "Message body type", "application/x-www-form-urlencoded", ic.getRequest().getContentType() );
        InputStream is = ic.getRequest().getInputStream();
        byte[] buffer = new byte[ expectedBody.length() ];
        assertEquals( "Input stream length", buffer.length, is.read( buffer ) );
        assertEquals( "Message body", expectedBody, new String( buffer ) );
    }


    public void testFrameAccess() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "Frames", FrameTopServlet.class.getName() );
        sr.registerServlet( "RedFrame", SimpleGetServlet.class.getName() );
        sr.registerServlet( "BlueFrame", AccessCountServlet.class.getName() );

        WebClient client = sr.newClient();
        WebRequest request = new GetMethodWebRequest( "http://host/Frames" );
        WebResponse page = client.getResponse( request );
        HttpUserAgentTest.assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, client.getFrameNames() );
        WebResponse response = client.getFrameContents( "red" );
        assertEquals( "Frame contents", SimpleGetServlet.RESPONSE_TEXT, response.getText() );

        page.getSubframeContents( page.getFrameNames()[0] );
    }


    static class SimpleGetServlet extends HttpServlet {

        static String RESPONSE_TEXT = "the desired content\r\n";


        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }


    static class AccessCountServlet extends HttpServlet {

        private int _numAccesses;

        private static int _numInstances = 0;


        public void init() throws ServletException {
            super.init();
            _numInstances++;
        }


        public void destroy() {
            super.destroy();
            _numInstances--;
        }


        public static int getNumInstances() {
            return _numInstances;
        }


        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( String.valueOf( ++_numAccesses ) );
            pw.close();
        }
    }


    static class ParameterServlet extends HttpServlet {

        static String RESPONSE_TEXT = "the desired content\r\n";


        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/plain" );
            resp.addHeader( "MyHeader", "value1" );
            resp.addHeader( "MyHeader", "value2" );

            PrintWriter pw = resp.getWriter();
            pw.print( "You selected " + req.getParameter( "color" ) );
            pw.close();
        }


        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( "You posted " + req.getParameter( "color" ) );
            pw.close();
        }

    }


    static class BodyEcho extends HttpServlet {
        /**
         * Returns a resource object as a result of a get request.
         **/
        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            int length = req.getIntHeader( "Content-length" );
            String contentType = req.getHeader( "Content-type" );
            resp.setContentType( contentType );

            InputStreamReader isr = new InputStreamReader( req.getInputStream() );
            BufferedReader br = new BufferedReader( isr );
            resp.getWriter().print( length );

            String line = br.readLine();
            while (line != null) {
                resp.getWriter().print( "\n" );
                resp.getWriter().print( line );
                line = br.readLine();
            }
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }

    static class FormSubmissionServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );

            PrintWriter pw = resp.getWriter();
            pw.println( "<html><head></head><body>" );
            pw.println( "<FORM ACTION='/testForm?submission=act' METHOD='POST'>" );
            pw.println( "<INPUT NAME='login' TYPE='TEXT'>" );
            pw.println( "<INPUT NAME='password' TYPE='PASSWORD'>" );
            pw.println( "<INPUT TYPE='SUBMIT'>" );
            pw.println( "</FORM></body></html>" );
            pw.close();
        }


        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( "You posted " + req.getParameter( "login" ) + "," + req.getParameter( "password" ) );
            pw.close();
        }

    }


    static class FrameTopServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );

            PrintWriter pw = resp.getWriter();
            pw.println( "<html><head></head><frameset cols='20%,80&'>" );
            pw.println( "<frame src='RedFrame' name='red'>" );
            pw.println( "<frame src='BlueFrame' name='blue'>" );
            pw.println( "</frameset></html>" );
            pw.close();
        }

    }
}


