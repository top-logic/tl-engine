package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: WebWindowTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2004, 2007, Russell Gold
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
import java.util.ArrayList;
import java.io.IOException;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class WebWindowTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebWindowTest.class );
    }


    public WebWindowTest( String name ) {
        super( name );
    }


    /**
     * Verifies that clicking on a link that specifies the _blank target creates a new window, populated with
     * the contents of the referenced page.
     * @throws Exception on any unexpected problem.
     */
    public void testNewTarget() throws Exception {
        defineResource( "goHere", "You made it!" );
        defineWebPage( "start", "<a href='goHere' id='go' target='_blank'>here</a>" );

        WebClient wc = new WebConversation();
        assertEquals( "Number of initial windows", 1, wc.getOpenWindows().length );
        WebWindow main = wc.getMainWindow();
        WebResponse initialPage = main.getResponse( getHostPath() + "/start.html" );
        initialPage.getLinkWithID( "go" ).click();
        assertEquals( "Number of windows after following link", 2, wc.getOpenWindows().length );
        assertEquals( "Main page in original window", initialPage, main.getCurrentPage() );
        WebWindow other = wc.getOpenWindows()[1];
        assertEquals( "New window contents", "You made it!", other.getCurrentPage().getText() );

        main.close();
        assertTrue( "Original main window is not closed", main.isClosed() );
        assertFalse( "New window has been closed", other.isClosed() );

        assertEquals( "Num open windows", 1, wc.getOpenWindows().length );
        assertEquals( "Main window", other, wc.getMainWindow() );
    }


    public void testUnknownTarget() throws Exception {
        defineResource( "goThere", "You came back!" );
        defineResource( "goHere", "You made it!" );
        defineWebPage( "start", "<a href='goHere' id='go' target='somewhere'>here</a>" +
                                "<a href='goThere' id='return' target='somewhere'>there</a>");

        WebClient wc = new WebConversation();
        WebResponse initialPage = wc.getResponse( getHostPath() + "/start.html" );
        initialPage.getLinkWithID( "go" ).click();
        assertEquals( "Number of windows after following link", 2, wc.getOpenWindows().length );
        WebWindow other = wc.getOpenWindows()[1];
        assertEquals( "New window contents", "You made it!", other.getCurrentPage().getText() );
        initialPage.getLinkWithID( "return" ).click();
        assertEquals( "Number of windows after following link", 2, wc.getOpenWindows().length );
        assertEquals( "Updated window contents", "You came back!", other.getCurrentPage().getText() );
    }


    public void testTargetInAnotherWindow() throws Exception {
        defineWebPage( "linker", "<a href='start.html' target='_blank'>start</a>" );
        defineResource( "Frames.html", "<html>" +
                        "<frameset cols=\"20%,80%\">" +
                        "    <frame src=\"linker.html\" name=\"here\">" +
                        "    <frame name=\"somewhere\">" +
                        "</frameset></html>" );
        defineResource( "goHere", "You made it!" );
        defineWebPage( "start", "<a href='goHere' id='go' target='somewhere'>here</a>" );

        WebClient wc = new WebConversation();
        WebResponse initialPage = wc.getResponse( getHostPath() + "/Frames.html" );
        initialPage.getSubframeContents( "here" ).getLinkWith( "start" ).click();
        assertEquals( "# Open windows", 2, wc.getOpenWindows().length );
        WebWindow other = wc.getOpenWindows()[1];
        WebResponse result = other.getCurrentPage().getLinkWithID( "go" ).click();
        assertEquals( "New frame contents", "You made it!", initialPage.getSubframeContents( "somewhere").getText() );
    }


    public void testCloseOnlyWindow() throws Exception {
        defineResource( "goHere", "You made it!" );
        WebConversation wc = new WebConversation();
        WebWindow original = wc.getMainWindow();
        wc.getMainWindow().close();
        assertTrue( "Main window did not close", original.isClosed() );
        assertNotNull( "No main window was created", wc.getMainWindow() );
    }


    public void testListeners() throws Exception {
        defineResource( "goHere", "You made it!" );
        defineWebPage( "start", "<a href='goHere' id='go' target='_blank'>here</a>" );

        final ArrayList newWindowContents = new ArrayList();
        final ArrayList closedWindows = new ArrayList();
        WebClient wc = new WebConversation();
        wc.addWindowListener( new WebWindowListener() {
            public void windowOpened( WebClient client, WebWindow window ) {
                try {
                    newWindowContents.add( window.getCurrentPage().getText() );
                } catch (IOException e) {
                    fail( "Error trying to read page" );
                }
            }
            public void windowClosed( WebClient client, WebWindow window ) {
                closedWindows.add( window );
            }
        });
        WebResponse initialPage = wc.getResponse( getHostPath() + "/start.html" );
        initialPage.getLinkWithID( "go" ).click();
        assertFalse( "No window opened", newWindowContents.isEmpty() );
        assertEquals( "New window contents", "You made it!", newWindowContents.get(0) );
        assertTrue( "Window already reported closed", closedWindows.isEmpty() );

        WebWindow main = wc.getMainWindow();
        WebWindow other = wc.getOpenWindows()[1];
        other.close();
        assertEquals( "Main window", main, wc.getMainWindow() );
        assertFalse( "No windows reported closed", closedWindows.isEmpty() );
        assertEquals( "Window reported closed", other, closedWindows.get(0) );
    }


    public void testWindowIndependence() throws Exception {
        defineResource( "next", "You made it!", "text/plain" );
        defineWebPage( "goHere", "<a href='next' id=proceed>more</a>" );
        defineWebPage( "start", "<a href='goHere.html' id='go' target='_blank'>here</a>" );

        WebClient wc = new WebConversation();
        WebWindow main = wc.getMainWindow();
        WebResponse initialPage = wc.getResponse( getHostPath() + "/start.html" );
        initialPage.getLinkWithID( "go" ).click();
        WebWindow other = wc.getOpenWindows()[1];
        other.getResponse( other.getCurrentPage().getLinkWithID( "proceed" ).getRequest() );
        assertEquals( "Main page URL", getHostPath() + "/start.html", main.getCurrentPage().getURL().toExternalForm() );
        assertEquals( "New window contents", "You made it!", other.getCurrentPage().getText() );
    }


    public void testWindowContext() throws Exception {
        defineResource( "next", "You made it!" );
        defineWebPage( "goHere", "<a href='next' id=proceed>more</a>" );
        defineWebPage( "start", "<a href='goHere.html' id='go' target='_blank'>here</a>" );

        WebClient wc = new WebConversation();
        wc.getMainWindow();
        WebResponse initialPage = wc.getResponse( getHostPath() + "/start.html" );
        initialPage.getLinkWithID( "go" ).click();
        WebWindow other = wc.getOpenWindows()[1];
        other.getCurrentPage().getLinkWithID( "proceed" ).click();
        assertEquals( "New window contents", "You made it!", other.getCurrentPage().getText() );
    }


}
