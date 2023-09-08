package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: EncodingTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004, Russell Gold
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
import junit.framework.TestSuite;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;


/**
 * Tests handling of non-Latin scripts.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:matsuhashi@quick.co.jp">Kazuaki Matsuhashi</a>
 **/
public class EncodingTest extends HttpUnitTest {


    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( EncodingTest.class );
    }


    public EncodingTest( String name ) {
        super( name );
    }


    public void testDecodeWithCharacterSetAsArg() throws Exception {
        String expected = "newpage\u30b5\u30f3\u30d7\u30eb";  // "\u30b5\u30f3\u30d7\u30eb" means "SAMPLE" in Japanese EUC-JP characterSet

        String encodedString = "newpage%A5%B5%A5%F3%A5%D7%A5%EB";
        String actual = HttpUnitUtils.decode( encodedString , "EUC-JP" );
        assertEquals( "decoded string" , expected , actual);
    }
    
    /**
     * test parseContentHeader
     * @throws Exception
     */
    public void testParseContentHeader() throws Exception {
    	String headers[]={
    		"text/plain",
    		"text/html; charset=Cp1252",
    		"text/html; charset=iso-8859-8",
    		"text/html; charset=EUC-JP"
    	};
    	String expected[][]={
    			{"text/plain",null},
    			{"text/html","Cp1252"},
    			{"text/html","iso-8859-8"},
    			{"text/html","EUC-JP"}
    	};
    	for (int i=0;i<headers.length;i++) {
    		String result[]=HttpUnitUtils.parseContentTypeHeader(headers[i]);
        assertEquals( "header "+i , expected[i][0] , result[0]);
        assertEquals( "header "+i , expected[i][1] , result[1]);
    	} // for	
    }


    public void testSpecifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        setResourceCharSet( "SimplePage.html", "iso-8859-8", true );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
        assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
    }


    public void testQuotedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        setResourceCharSet( "SimplePage.html", "\"iso-8859-8\"", true );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
        assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
    }


    public void testUnspecifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        setResourceCharSet( "SimplePage.html", "iso-8859-8", false );

        HttpUnitOptions.setDefaultCharacterSet( "iso-8859-8" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
        assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
    }


   public void testMetaEncoding() throws Exception {
       String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
       String page = "<html><head><title>" + hebrewTitle + "</title>" +
                     "<meta Http_equiv=content-type content=\"text/html; charset=iso-8859-8\"></head>\n" +
                     "<body>This has no data\n" +
                     "</body></html>\n";
       defineResource( "SimplePage.html", page );
       setResourceCharSet( "SimplePage.html", "iso-8859-8", false );

       WebConversation wc = new WebConversation();
       WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
       WebResponse simplePage = wc.getResponse( request );

       assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
       assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
    }


    public void testHebrewForm() throws Exception {
        String hebrewName = "\u05d0\u05d1\u05d2\u05d3";
        defineResource( "HebrewForm.html",
                        "<html><head></head>" +
                        "<form method=POST action=\"SayHello\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        setResourceCharSet( "HebrewForm.html", "iso-8859-8", true );
        defineResource( "SayHello", new PseudoServlet() {
            public WebResource getPostResponse() {
                try {
                    String name = getParameter( "name" )[0];
                    WebResource result = new WebResource( "<html><body><table><tr><td>Hello, " +
                                                          new String( name.getBytes( "iso-8859-1" ), "iso-8859-8" ) +
                                                          "</td></tr></table></body></html>" );
                    result.setCharacterSet( "iso-8859-8" );
                    result.setSendCharacterSet( true );
                    return result;
                } catch (java.io.UnsupportedEncodingException e) {
                    return null;
                }
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/HebrewForm.html" );
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", hebrewName );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, " + hebrewName, cells[0][0] );
        assertEquals( "Character set", "iso-8859-8", answer.getCharacterSet() );
    }


    public void testEncodedRequestWithoutForm() throws Exception {
        String hebrewName = "\u05d0\u05d1\u05d2\u05d3";
        defineResource( "SayHello", new PseudoServlet() {
            public WebResource getPostResponse() {
                try {
                    String name = getParameter( "name" )[0];
                    WebResource result = new WebResource( "<html><body><table><tr><td>Hello, " +
                                                          new String( name.getBytes( "iso-8859-1" ), "iso-8859-8" ) +
                                                          "</td></tr></table></body></html>" );
                    result.setCharacterSet( "iso-8859-8" );
                    result.setSendCharacterSet( true );
                    return result;
                } catch (java.io.UnsupportedEncodingException e) {
                    return null;
                }
            }
        } );

        WebConversation wc = new WebConversation();
        HttpUnitOptions.setDefaultCharacterSet( "iso-8859-8" );
        WebRequest request = new PostMethodWebRequest( getHostPath() + "/SayHello" );
        request.setParameter( "name", hebrewName );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, " + hebrewName, cells[0][0] );
        assertEquals( "Character set", "iso-8859-8", answer.getCharacterSet() );
    }


    public void testUnsupportedEncoding() throws Exception {
        defineResource( "SimplePage.html", "not much here" );
        addResourceHeader( "SimplePage.html", "Content-type: text/plain; charset=BOGUS");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Text", "not much here", simplePage.getText() );
        assertEquals( "Character set", WebResponse.getDefaultEncoding(), simplePage.getCharacterSet() );
    }


    public void testJapaneseLinkParamNameWithValue() throws Exception {
        String japaneseUrl = "request?%A5%D8%A5%EB%A5%D7=2";
        defineWebPage( "Linker", "<a id='link' href='" + japaneseUrl + "'>goThere</a>" );
        setResourceCharSet( "Linker.html", "EUC-JP", true );
        defineResource( japaneseUrl, "You made it!" );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/Linker.html" );
        WebResponse target = formPage.getLinkWithID( "link" ).click();
        assertEquals( "Resultant page", "You made it!", target.getText() );
    }


    public void testJapaneseLinkParamNameWithoutValue() throws Exception {
        String japaneseUrl = "request?%A5%D8%A5%EB%A5%D7";
        defineWebPage( "Linker", "<a id='link' href='" + japaneseUrl + "'>goThere</a>" );
        setResourceCharSet( "Linker.html", "EUC-JP", true );
        defineResource( japaneseUrl, "You made it!" );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/Linker.html" );
        WebResponse target = formPage.getLinkWithID( "link" ).click();
        assertEquals( "Resultant page", "You made it!", target.getText() );
    }


    public void testSimpleEntityReplacement() throws Exception {
        String rawString = "Cox&amp;&amp;Forkum";
        assertEquals( "After substitution", "Cox&&Forkum", HttpUnitUtils.replaceEntities( rawString ) );
    }


    public void testSkipEntityReplacementOnBadString() throws Exception {
        String rawString = "Cox&Forkum";
        assertEquals( "After substitution", "Cox&Forkum", HttpUnitUtils.replaceEntities( rawString ) );
    }


    public void testSkipEntityReplacementOnUnhandledEntity() throws Exception {
        String rawString = "&lt;something&gt;";
        assertEquals( "After substitution", "&lt;something&gt;", HttpUnitUtils.replaceEntities( rawString ) );
    }


}
