package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: MessageBodyRequestTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2001, Russell Gold
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
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A unit test to verify miscellaneous requests with message bodies.
 **/
public class MessageBodyRequestTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
	
	
    public static Test suite() {
        return new TestSuite( MessageBodyRequestTest.class );
    }


    public MessageBodyRequestTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }
	
        
    public void testGenericPostRequest() throws Exception {
        defineResource( "ReportData", new BodyEcho() );
        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream( sourceData.getBytes( "iso-8859-1" ) );

        WebConversation wc = new WebConversation();
        WebRequest wr = new PostMethodWebRequest( getHostPath() + "/ReportData", source, "text/sample" );
        WebResponse response = wc.getResponse( wr );
        assertEquals( "Body response", "\nPOST\n" + sourceData, response.getText() );
        assertEquals( "Content-type", "text/sample", response.getContentType() ); 
    }


    public void testPutRequest() throws Exception {
        defineResource( "ReportData", new BodyEcho() );
        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream( sourceData.getBytes( "iso-8859-1" ) );

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest( getHostPath() + "/ReportData", source, "text/plain" );
        WebResponse response = wc.getResponse( wr );
        assertEquals( "Body response", "\nPUT\n" + sourceData, response.getText() );
    }


    public void testDownloadRequest() throws Exception {
        defineResource( "ReportData", new BodyEcho() );
        byte[] binaryData = new byte[] { 0x01, 0x05, 0x0d, 0x0a, 0x02 };

        InputStream source = new ByteArrayInputStream( binaryData );

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest( getHostPath() + "/ReportData", source, "application/random" );
        WebResponse response = wc.getResponse( wr );

        byte[] download = getDownload( response );
        assertEquals( "Body response", binaryData, download );
    }


    private byte[] getDownload( WebResponse response ) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getInputStream();

        byte[] buffer = new byte[8 * 1024];
        int count = 0;
        do {
            outputStream.write( buffer, 0, count );
            count = inputStream.read( buffer, 0, buffer.length );
        } while (count != -1);

        inputStream.close();
        return outputStream.toByteArray();
    }


}


class BodyEcho extends PseudoServlet {
    /**
     * Returns a resource object as a result of a get request.
     **/
    public WebResource getResponse( String method ) {
        String contentType = getHeader( "Content-type" );
        if (contentType.startsWith( "text" )) {
            return new WebResource( "\n" + method + "\n" + new String( getBody() ), contentType );
        } else {
            return new WebResource( getBody(), contentType );
        }
    }
}

