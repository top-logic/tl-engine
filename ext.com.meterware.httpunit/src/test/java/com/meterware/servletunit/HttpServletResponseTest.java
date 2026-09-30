package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: HttpServletResponseTest.java 107617 2012-01-03 09:51:15Z jst $
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

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests the ServletUnitHttpResponse class.
 **/
public class HttpServletResponseTest extends ServletUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( HttpServletResponseTest.class );
    }


    public HttpServletResponseTest( String name ) {
        super( name );
    }


    public void testDefaultResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        WebResponse response = new ServletUnitWebResponse( null, FrameSelector.TOP_FRAME, null, servletResponse );
        assertEquals( "Contents", "", response.getText() );
    }


    public void testSimpleResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        servletResponse.setContentLength( 65 );
        PrintWriter pw = servletResponse.getWriter();
        pw.println( "<html><head><title>Sample Page</title></head><body></body></html>" );

        WebResponse response = new ServletUnitWebResponse( null, FrameSelector.TOP_FRAME, null, servletResponse );
        assertEquals( "Status code", HttpServletResponse.SC_OK, response.getResponseCode() );
        assertEquals( "Content type", "text/html", response.getContentType() );
        assertEquals( "Title", "Sample Page", response.getTitle() );
        assertEquals( "Content length", 65, response.getContentLength() );
        assertEquals( "Content encoding", "iso-8859-1", response.getCharacterSet() );
        assertEquals( "Content header", "text/html; charset=iso-8859-1", response.getHeaderField( "Content-type" ) );
    }


    public void testEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html; charset=iso-8859-8" );
        PrintWriter pw = servletResponse.getWriter();
        pw.print( page );
        pw.close();

        WebResponse response = new ServletUnitWebResponse( null, FrameSelector.TOP_FRAME, null, servletResponse );
        assertEquals( "Character set", "iso-8859-8", response.getCharacterSet() );
        assertEquals( "Title", hebrewTitle, response.getTitle() );
    }


    public void testLocale() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        assertEquals( "Default locale", Locale.getDefault(), servletResponse.getLocale() );

        servletResponse.setContentType( "text/html" );
        servletResponse.setLocale( new Locale( "he", "IL" ) );
        assertEquals( "Specified locale", new Locale( "he", "IL" ), servletResponse.getLocale() );
        assertEquals( "Content type", "text/html; charset=iso-8859-8", servletResponse.getHeaderField( "Content-type" ) );

    }


    public void testStreamResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        ServletOutputStream sos = servletResponse.getOutputStream();
        sos.println( "<html><head><title>Sample Page</title></head><body></body></html>" );

        WebResponse response = new ServletUnitWebResponse( null, FrameSelector.TOP_FRAME, null, servletResponse );
        assertEquals( "Status code", HttpServletResponse.SC_OK, response.getResponseCode() );
        assertEquals( "Content type", "text/html", response.getContentType() );
        assertEquals( "Title", "Sample Page", response.getTitle() );
    }


    public void testStreamWriterAfterOutputStream() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        servletResponse.getOutputStream();
        try {
            servletResponse.getWriter();
            fail( "Should have thrown IllegalStateException" );
        } catch (IllegalStateException e) {
        }
    }


    public void testStreamOutputStreamAfterWriter() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getWriter();
        try {
            servletResponse.getOutputStream();
            fail( "Should have thrown IllegalStateException" );
        } catch (IllegalStateException e) {
        }
    }


    public void testSetBufferSizeAfterWrite() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setBufferSize( 120 );
        servletResponse.getWriter();
        servletResponse.setBufferSize( 100 );
        servletResponse.getWriter().print( "something" );
        try {
            servletResponse.setBufferSize( 80 );
            fail( "Should not have permitted setBufferSize after write" );
        } catch (IllegalStateException e) {}
    }


    public void testSetBufferSizeAfterStreamOutput() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setBufferSize( 120 );
        servletResponse.getOutputStream();
        servletResponse.setBufferSize( 100 );
        servletResponse.getOutputStream().print( "something" );
        try {
            servletResponse.setBufferSize( 80 );
            fail( "Should not have permitted setBufferSize after output" );
        } catch (IllegalStateException e) {}
    }


    public void testResetBuffer() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getOutputStream().print( "something" );
        assertEquals( "buffer size", 9, servletResponse.getContents().length );
        servletResponse.resetBuffer();
        assertEquals( "buffer size", 0, servletResponse.getContents().length );

        servletResponse.flushBuffer();
        try {
            servletResponse.resetBuffer();
            fail( "Should not have permitted resetBuffer after flush" );
        } catch (IllegalStateException e) {}
    }


    /**
     * test isComitted flag after flushing buffer
     * @throws Exception
     */
    public void testUpdateAfterFlushBuffer() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getWriter();
        assertFalse( "Should not be committed yet", servletResponse.isCommitted() );
        servletResponse.flushBuffer();
        assertTrue( "Should be committed now", servletResponse.isCommitted() );
    }

    public void testSingleHeaders() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );

        assertFalse( "header foo wrongly detected", servletResponse.containsHeader( "foo" ) );
        servletResponse.setHeader( "foo", "bar" );
        String headerValue = servletResponse.getHeaderField( "foo" );
        assertEquals( "header is wrong", "bar", headerValue );
        assertTrue( "header foo not detected", servletResponse.containsHeader( "foo" ) );

        servletResponse.setHeader( "foo", "baz" );
        headerValue = servletResponse.getHeaderField( "foo" );
        assertEquals( "header is wrong", "baz", headerValue );

        servletResponse.setIntHeader( "three", 3 );
        headerValue = servletResponse.getHeaderField( "three" );
        assertEquals( "int header is wrong", "3", headerValue );

        SimpleDateFormat df = new SimpleDateFormat( "MM/dd/yyyy z" );
        Date d = df.parse( "12/9/1969 GMT" );
        servletResponse.setDateHeader( "date", d.getTime() );
        headerValue = servletResponse.getHeaderField( "date" );
        assertEquals( "date header is wrong", "Tue, 09 Dec 1969 12:00:00 GMT", headerValue );
    }


    public void testMultipleHeaders() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );

        SimpleDateFormat df = new SimpleDateFormat( "MM/dd/yyyy z" );
        Date date = df.parse( "12/9/1969 GMT" );

        servletResponse.addHeader( "list", "over-rideme" );
        servletResponse.setHeader( "list", "foo" );
        servletResponse.addIntHeader( "list", 3 );
        servletResponse.addDateHeader( "list", date.getTime() );
        String[] headerList = servletResponse.getHeaderFields( "list" );
        assertEquals( "header is wrong", "foo", headerList[ 0 ] );
        assertEquals( "header is wrong", "3", headerList[ 1 ] );
        assertEquals( "header is wrong", "Tue, 09 Dec 1969 12:00:00 GMT", headerList[ 2 ] );

        servletResponse.setHeader( "list", "monkeyboy" );
        headerList = servletResponse.getHeaderFields( "list" );
        assertEquals( "setHeader did not replace the list header", headerList.length, 1 );
        assertEquals( "header is wrong", "monkeyboy", headerList[ 0 ] );
    }


    public void testSendRedirect() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        final String location = "http://localhost/newLocation";
        servletResponse.sendRedirect( location );
        assertEquals( "Redirected Location", location, servletResponse.getHeaderField( "Location" ) );
        assertEquals( "Status", 302, servletResponse.getStatus() );
    }

}


