package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id: NekoEnhancedScriptingTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2004, Russell Gold
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
import junit.textui.TestRunner;

import com.meterware.httpunit.*;


/**
 * Tests that work under NekoHTML but not JTidy due to the ability to do script processing during parsing.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class NekoEnhancedScriptingTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( NekoEnhancedScriptingTest.class );
    }


    public NekoEnhancedScriptingTest( String name ) {
        super( name );
    }


    public void testEmbeddedDocumentWrite() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>something</title></head>" +
                                            "<body>" +
                                            "<script language='JavaScript'>" +
                                            "document.write( '<a id=here href=about:blank>' );" +
                                            "document.writeln( document.title );" +
                                            "document.write( '</a>' );" +
                                            "</script>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinkWithID( "here" );
        assertNotNull( "The link was not found", link );
        assertEquals( "Link contents", "something", link.getText() );
    }


    public void testEmbeddedDocumentWriteWithClose() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>something</title></head>" +
                                            "<body>" +
                                            "<script language='JavaScript'>" +
                                            "document.write( '<a id=here href=about:blank>' );" +
                                            "document.writeln( document.title );" +
                                            "document.write( '</a>' );" +
                                            "document.close();" +
                                            "</script>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinkWithID( "here" );
        assertNotNull( "The link was not found", link );
        assertEquals( "Link contents", "something", link.getText() );
    }


    public void testUnknownScript() throws Exception {
        defineWebPage( "FunkyScript",
                       "<SCRIPT>" +
                       "var stuff='<A href=\"#\">Default JavaScript Working</A><BR>';" +
                       "document.writeln(stuff);" +
                       "</SCRIPT>" +
                       "<SCRIPT Language='JavaScript'>" +
                       "var stuff='<A href=\"#\">JavaScript Working</A><BR>';" +
                       "document.writeln(stuff);" +
                       "</SCRIPT>" +
                       "<SCRIPT Language='JavaScript1.2'>" +
                       "var stuff='<A href=\"#\">JavaScript 1.2 Working</A><BR>';" +
                       "document.writeln(stuff);" +
                       "</SCRIPT>" +
                       "<SCRIPT Language='VBScript'>" +
                       "Dim stuff" +
                       "stuff = '<A href=\"#\">VBScript</A><BR>'" +
                       "document.writeln(stuff)" +
                       "</SCRIPT>" );
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/FunkyScript.html" );
        assertNotNull( "No default script link found", wr.getLinkWith( "Default JavaScript Working" ) );
        assertNotNull( "No default script link found", wr.getLinkWith( "JavaScript Working" ) );
        assertNotNull( "No default script link found", wr.getLinkWith( "JavaScript 1.2 Working" ) );
        assertNull( "VBScript link found", wr.getLinkWith( "VBScript" ) );
    }

    
    /**
     * test no script sections
     * @throws Exception
     */
    public void testNoScriptSections() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>something</title></head>" +
                                            "<body>" +
                                            "<script language='JavaScript'>" +
                                            "document.write( '<a id=here href=about:blank>' );" +
                                            "document.writeln( document.title );" +
                                            "document.write( '</a>' );" +
                                            "</script>" +
                                            "<noscript>" +
                                            "<a href='#' id='there'>anything</a>" +
                                            "</noscript>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinkWithID( "here" );
        assertNotNull( "The link was not found", link );
        assertEquals( "Link contents", "something", link.getText() );
        assertNull( "Should not have found link in noscript", response.getLinkWithID( "there" ) );

        HttpUnitOptions.setScriptingEnabled( false );
        response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        link = response.getLinkWithID( "there" );
        assertNotNull( "The link was not found", link );
        assertEquals( "Link contents", "anything", link.getText() );
        assertNull( "Should not have found scripted link", response.getLinkWithID( "here" ) );
    }


    /**
     * Verifies that nodes defined before a script section are available to that script section, even if a preceding
     * script section has caused them to be cached. Currently does not work with JTidy since there is no way to parse
     * only to a specific position in the document. It may be possible to fix this with some logic changes...
     */
    public void testFormsCaching() throws Exception {
        defineWebPage(  "OnCommand",  "<form>" +
                                      "  <input type='text' name='color' value='blue' >" +
                                      "</form>" +
                                      "<script type='JavaScript'>" +
                                      "  alert( document.forms[0].color.value );" +
                                      "</script>" +
                                      "<form>" +
                                      "  <input type='text' name='size' value='3' >" +
                                      "</form>" +
                                      "<script type='JavaScript'>" +
                                      "  alert( document.forms[1].size.value );" +
                                      "</script>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Message 1", "blue", wc.popNextAlert() );
        assertEquals( "Message 2", "3", wc.popNextAlert() );
    }


    /**
     * Verifies that a script can write part of the frameset.
     */
    public void testScriptedFrames() throws Exception {
        defineWebPage( "OneForm", "<form name='form'><input name=text value='nothing special'></form>");
        defineResource("Frames.html",
                "<html><script>" +
                "  document.write( '<frameset>' )" +
                "</script>" +
                "    <frame src='OneForm.html' name='green'>" +
                "    <frame name=blue>" +
                "</frameset></htmlL>");

        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/Frames.html" );
        assertMatchingSet( "Loaded frames", new String[] { "_top", "green", "blue" }, wc.getFrameNames() );
    }


}
