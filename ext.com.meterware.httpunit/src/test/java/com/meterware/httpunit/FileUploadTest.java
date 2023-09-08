package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: FileUploadTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2002, 2007, Russell Gold
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
import com.meterware.httpunit.protocol.UploadFileSpec;

import java.io.*;
import java.util.StringTokenizer;
import java.net.URLEncoder;

import javax.activation.DataSource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A unit test of the file upload simulation capability.
 **/
public class FileUploadTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
	
	
    public static Test suite() {
        return new TestSuite( FileUploadTest.class );
    }


    public FileUploadTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }
	
        
    public void testParametersMultipartEncoding() throws Exception {
        defineResource( "ListParams", new MimeEcho() );
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=text name=age value=12>" +
                                  "<Textarea name=comment>first\nsecond</textarea>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();
        WebResponse encoding = wc.getResponse( formSubmit );
        assertEquals( "http://dummy?age=12&comment=first%0D%0Asecond&update=age", "http://dummy?" + encoding.getText().trim() );
    }


    public void testFileParameterValidation() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();

        try {
            formSubmit.setParameter( "message", "text/plain" );
            fail( "Should not allow setting of a file parameter to a text value" );
        } catch (IllegalRequestParameterException e) {
        }
    }


    public void testNonFileParameterValidation() throws Exception {
        File file = new File( "temp.html" );

        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=text name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();

        try {
            formSubmit.selectFile( "message", file );
            fail( "Should not allow setting of a text parameter to a file value" );
        } catch (IllegalRequestParameterException e) {
        }
    }


    public void testURLEncodingFileParameterValidation() throws Exception {
        File file = new File( "temp.html" );

        defineWebPage( "Default", "<form method=POST action = \"ListParams\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();

        try {
            formSubmit.selectFile( "message", file );
            fail( "Should not allow setting of a file parameter in a form which specifies url-encoding" );
        } catch (IllegalRequestParameterException e) {
        }
    }


    public void testFileMultipartEncoding() throws Exception {
        File file = new File( "temp.txt" );
        FileWriter fw = new FileWriter( file );
        PrintWriter pw = new PrintWriter( fw );
        pw.println( "Not much text" );
        pw.println( "But two lines" );
        pw.close();

        defineResource( "ListParams", new MimeEcho() );
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();
        formSubmit.selectFile( "message", file );
        WebResponse encoding = wc.getResponse( formSubmit );
        assertEquals( "text/plain:message.name=temp.txt&message.lines=2&update=age", encoding.getText().trim() );

        file.delete();
    }


    public void testMultiFileSubmit() throws Exception {
        File file = new File( "temp.txt" );
        FileWriter fw = new FileWriter( file );
        PrintWriter pw = new PrintWriter( fw );
        pw.println( "Not much text" );
        pw.println( "But two lines" );
        pw.close();

        File file2 = new File( "temp2.txt" );
        fw = new FileWriter( file2 );
        pw = new PrintWriter( fw );
        pw.println( "Even less text on one line" );
        pw.close();

        defineResource( "ListParams", new MimeEcho() );
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();
        formSubmit.setParameter( "message", new UploadFileSpec[] { new UploadFileSpec( file ), new UploadFileSpec( file2, "text/more" ) } );
        WebResponse encoding = wc.getResponse( formSubmit );
        assertEquals( "text/plain:message.name=temp.txt&message.lines=2&text/more:message.name=temp2.txt&message.lines=1&update=age", encoding.getText().trim() );

        file.delete();
        file2.delete();
    }


    public void testIllegalMultiFileSubmit() throws Exception {
        File file = new File( "temp.txt" );
        FileWriter fw = new FileWriter( file );
        PrintWriter pw = new PrintWriter( fw );
        pw.println( "Not much text" );
        pw.println( "But two lines" );
        pw.close();

        File file2 = new File( "temp2.txt" );
        fw = new FileWriter( file2 );
        pw = new PrintWriter( fw );
        pw.println( "Even less text on one line" );
        pw.close();

        defineResource( "ListParams", new MimeEcho() );
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();
        try {
            formSubmit.setParameter( "message", new UploadFileSpec[] { new UploadFileSpec( file ), new UploadFileSpec( file2, "text/more" ) } );
            fail( "Permitted two files on a single file parameter" );
        } catch (IllegalRequestParameterException e) {
        }

        file.delete();
        file2.delete();
    }


    public void testInputStreamAsFile() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream( "Not much text\nBut two lines\n".getBytes() );

        defineResource( "ListParams", new MimeEcho() );
        defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                  "<Input type=file name=message>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest formSubmit = simplePage.getForms()[0].getRequest();
        formSubmit.selectFile( "message", "temp.txt", bais, "text/plain" );
        WebResponse encoding = wc.getResponse( formSubmit );
        assertEquals( "text/plain:message.name=temp.txt&message.lines=2&update=age", encoding.getText().trim() );
    }


    public void testFileUploadWithoutForm() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream( "Not much text\nBut two lines\n".getBytes() );

        defineResource( "ListParams", new MimeEcho() );
        WebConversation wc = new WebConversation();
        PostMethodWebRequest formSubmit = new PostMethodWebRequest( getHostPath() + "/ListParams", /* mime-encoded */ true );
        formSubmit.selectFile( "message", "temp.txt", bais, "text/plain" );
        WebResponse encoding = wc.getResponse( formSubmit );
        assertEquals( "text/plain:message.name=temp.txt&message.lines=2", encoding.getText().trim() );
    }

    /**
     * test the file content type for a given file
     * @param file
     * @param expected
     */
    protected void doTestFileContentType(File file, String contentType,String expected) throws Exception {
      defineResource( "ListParams", new MimeEcho() );
      defineWebPage( "Default", "<form method=POST action = \"ListParams\" enctype=\"multipart/form-data\"> " +
                                "<Input type=file name=message>" +
                                "</form>" );
      WebConversation wc = new WebConversation();
      WebRequest request = new GetMethodWebRequest( getHostPath() + "/Default.html" );
      WebResponse simplePage = wc.getResponse( request );
      WebRequest formSubmit = simplePage.getForms()[0].getRequest();
      if (contentType==null) {
      	formSubmit.selectFile( "message", file );
      }	else {
      	formSubmit.selectFile( "message", file ,contentType);
      }
      WebResponse encoding = wc.getResponse( formSubmit );
      assertEquals( expected,expected, encoding.getText().trim() );

      file.delete();    	
    }
    
    /**
     * create a file from the given byte contents
     * @param name - the name of the file
     * @param content - the bytes to use as content
     * @return
     * @throws Exception
     */
    private File createFile(String name,byte[] content) throws Exception {
    	File file = new File(name);
    	FileOutputStream fos = new FileOutputStream( file );
    	fos.write( content, 0, content.length );
    	fos.close();
    	return file;
    }	
    	
    /**
     * test the file content type for several file types e.g. a gif image
     * modified for patch 1415415
     * @throws Exception
     */
    public void testFileContentType() throws Exception {
      File file = createFile("temp.gif", new byte[] { 1, 2, 3, 4, 0x7f, 0x23 } );
      doTestFileContentType(file,null,"image/gif:message.name=temp.gif&message.lines=1");
      file = createFile("1x1.tif", new byte[] { 
      		(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,
      		(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x8A,(byte)0x00,
      		(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x01,
      		(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x11,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x01,(byte)0x00,
      		(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x15,(byte)0x01,(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x00,
      		(byte)0x00,(byte)0x00,(byte)0x16,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x17,(byte)0x01,
      		(byte)0x04,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x1C,(byte)0x01,(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x00,
      		(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x08});
      doTestFileContentType(file,null,"image/tiff:message.name=1x1.tif&message.lines=1");
    }


    /**
     * test the file content type for some ".new" file
     * @throws Exception
     */
    public void testSpecifiedFileContentType() throws Exception {
      File file = createFile( "temp.new" , new byte[] { 1, 2, 3, 4, 0x7f, 0x23 });
      doTestFileContentType(file,"x-application/new","x-application/new:message.name=temp.new&message.lines=1");
    }
}


class ByteArrayDataSource implements DataSource {
    ByteArrayDataSource( String contentType, byte[] body ) {
        _contentType = contentType;
        _inputStream = new ByteArrayInputStream( body );
    }


    public java.io.InputStream getInputStream() {
        return _inputStream;
    }


    public java.io.OutputStream getOutputStream() throws IOException {
        throw new IOException();
    }


    public java.lang.String getContentType() {
        return _contentType;
    }


    public java.lang.String getName() {
        return "test";
    }


    private String      _contentType;
    private InputStream _inputStream;

}


class MimeEcho extends PseudoServlet {
    public WebResource getPostResponse() {
        StringBuffer sb = new StringBuffer();
        try {
            String contentType = getHeader( "Content-Type" );
            DataSource ds = new ByteArrayDataSource( contentType, getBody() );
            MimeMultipart mm = new MimeMultipart( ds );
            int numParts = mm.getCount();
            for (int i = 0; i < numParts; i++) {
                appendPart( sb, (MimeBodyPart) mm.getBodyPart(i) );
                if (i < numParts-1) sb.append( '&' );
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            sb.append( "Oops: " ).append( e );
        } catch (IOException e) {
            e.printStackTrace();
            sb.append( "Oops: " ).append( e );
        }

        return new WebResource( sb.toString(), "text/plain" );
    }


    private void appendPart( StringBuffer sb, MimeBodyPart mbp ) throws IOException, MessagingException {
        String[] disposition = mbp.getHeader( "Content-Disposition" );
        String name = getHeaderAttribute( disposition[0], "name" );
        if (mbp.getFileName() == null) {
            appendFieldValue( name, sb, mbp );
        } else {
            sb.append( mbp.getContentType() ).append( ':' );
            appendFileSpecs( name, sb, mbp );
        }
    }


    private void appendFieldValue( String parameterName, StringBuffer sb, MimeBodyPart mbp ) throws IOException, MessagingException {
        sb.append( parameterName ).append( "=" ).append( URLEncoder.encode( mbp.getContent().toString() ) );
    }


    private void appendFileSpecs( String parameterName, StringBuffer sb, MimeBodyPart mbp ) throws IOException, MessagingException {
        String filename = mbp.getFileName();
        filename = filename.substring( filename.lastIndexOf( File.separator )+1 );
        BufferedReader br = new BufferedReader( new StringReader( mbp.getContent().toString() ) );
        int numLines = 0;
        while (br.readLine() != null) numLines++;

        sb.append( parameterName ).append( ".name=" ).append( filename ).append( "&" );
        sb.append( parameterName ).append( ".lines=" ).append( numLines );
    }


    private String getHeaderAttribute( String headerValue, String attributeName ) {
        StringTokenizer st = new StringTokenizer( headerValue, ";=", /* returnTokens */ true );

        int state = 0;
        String name = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals( ";" )) {
                state = 1;   // next token is attribute name
            } else if (token.equals( "=" )) {
                if (state == 1) {
                    state = 2;   // next token is attribute value
                } else {
                    state = 0;   // reset and keep looking
                }
            } else if (state == 1) {
                name = token.trim();
            } else if (state == 2) {
                if (name.equalsIgnoreCase( attributeName )) {
                    return stripQuotes( token.trim() );
                }
            }
        }
        return "";
    }


    private String stripQuotes( String value ) {
        if (value.startsWith( "\"" ) && value.endsWith( "\"" )) {
            return value.substring( 1, value.length()-1 );
        } else {
            return value;
        }
    }
}
