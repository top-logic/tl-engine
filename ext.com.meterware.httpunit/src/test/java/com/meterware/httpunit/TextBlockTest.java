package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: TextBlockTest.java 107617 2012-01-03 09:51:15Z jst $
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

import java.io.StringReader;
import java.io.BufferedReader;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class TextBlockTest extends HttpUnitTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( TextBlockTest.class );
    }


    public TextBlockTest( String name ) {
        super( name );
    }


    public void testParagraphDetection() throws Exception {
        defineWebPage( "SimplePage",
                        "<p>This has no forms or links since we don't care " +
                        "about them</p>" +
                        "<p class='comment'>But it does have three paragraphs</p>\n" +
                        "<p>Which is what we want to find</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        assertEquals( "Number of paragraphs", 3, response.getTextBlocks().length );
        assertEquals( "First paragraph", "This has no forms or links since we don't care about them", response.getTextBlocks()[0].getText() );
        BlockElement comment = response.getFirstMatchingTextBlock( TextBlock.MATCH_CLASS, "comment" );
        assertNotNull( "Did not find a comment paragraph", comment );
        assertEquals( "Comment paragraph", "But it does have three paragraphs", comment.getText() );
    }


    public void testTextConversion() throws Exception {
        defineWebPage( "SimplePage",
                        "<p>Here is a line<br>followed by another</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        BufferedReader br = new BufferedReader( new StringReader( response.getTextBlocks()[0].getText() ) );
        assertEquals( "First line", "Here is a line", br.readLine() );
        assertEquals( "Second line", "followed by another", br.readLine() );
        br.readLine();
    }


    public void testHeaderDetection() throws Exception {
        defineWebPage( "SimplePage",
                        "<h1>Here is a section</h1>\n" +
                        "with some text" +
                        "<h2>A subsection</h2>" +
                        "<p>Some more text</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        TextBlock header1 = response.getFirstMatchingTextBlock( TextBlock.MATCH_TAG, "H1" );
        assertNotNull( "Did not find the H1 header", header1 );
        assertEquals( "H1 header", "Here is a section", header1.getText() );
        TextBlock header2 = response.getFirstMatchingTextBlock( TextBlock.MATCH_TAG, "h2" );
        assertNotNull( "Did not find the h2 header", header2 );
        assertEquals( "H2 header", "A subsection", header2.getText() );
        assertEquals( "Text under header 1", "with some text", response.getNextTextBlock( header1 ).getText() );
    }


    public void testEmbeddedLinks() throws Exception {
        defineWebPage( "SimplePage",
                        "<h1>Here is a section</h1>\n" +
                        "<p>with a <a id='httpunit' href='http://httpunit.org'>link to the home page</a></p>"  );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        BlockElement paragraph = response.getTextBlocks()[1];
        assertNotNull( "Did not retrieve any links", paragraph.getLinks() );
        assertNotNull( "Did not find the httpunit link", paragraph.getLinkWithID( "httpunit" ) );
        assertNull( "Should not have found the httpunit link in the header", response.getTextBlocks()[0].getLinkWithID( "httpunit" ) );
        assertNotNull( "Did not find the home page link", paragraph.getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, "home page" ) );
        assertEquals( "embedded link url", "http://httpunit.org", paragraph.getLinkWithID( "httpunit" ).getRequest().getURL().toExternalForm() );
    }


    public void testEmbeddedLists() throws Exception {
        defineWebPage( "SimplePage",
                        "<h1>Here is a section</h1>\n" +
                        "<p id='ordered'><ol><li>One<li>Two<li>Three</ol></p>" +
                        "<p id='unordered'><ul><li>Red<li>Green<li>Blue</ul></p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        TextBlock paragraph1 = (TextBlock) response.getElementWithID( "ordered" );
        WebList[] lists = paragraph1.getLists();
        assertEquals( "Number of lists found", 1, lists.length);
        WebList orderedList = lists[0];
        assertEquals( "ordered list type", WebList.ORDERED_LIST, orderedList.getListType() );
        assertEquals( "ordered list size", 3, orderedList.getItems().length );
        assertEquals( "Second ordered list item", "Two", orderedList.getItems()[1].getText() );

        TextBlock paragraph2 = (TextBlock) response.getElementWithID( "unordered" );
        lists = paragraph2.getLists();
        assertEquals( "Number of lists found", 1, lists.length);
        WebList unorderedList = lists[0];
        assertEquals( "bullet list type", WebList.BULLET_LIST, unorderedList.getListType() );
        assertEquals( "bullet list size", 3, unorderedList.getItems().length );
        assertEquals( "First bullet list item", "Red", unorderedList.getItems()[0].getText() );
    }


    public void ntestFormattingDetection() throws Exception {
        String expectedText = "Here is some bold text and some bold italic text";
        defineWebPage( "FormattedPage", "<p>Here is some <b>bold</b> text and some <b><i>bold italic</i></b> text</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/FormattedPage.html" );
        TextBlock paragraph = response.getTextBlocks()[0];
        assertMatchingSet( "Attributes for word 'bold'", new String[] { "b" }, paragraph.getFormats( expectedText.indexOf( "bold" )));
    }

}
