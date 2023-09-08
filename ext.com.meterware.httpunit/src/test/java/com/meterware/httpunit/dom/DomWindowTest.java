package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: DomWindowTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2007, Russell Gold
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
import org.w3c.dom.html.HTMLDocument;

import java.net.URL;

import junit.framework.TestSuite;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class DomWindowTest extends AbstractHTMLElementTest {

    private TestWindowProxy _proxy;


    public static TestSuite suite() {
        return new TestSuite( DomWindowTest.class );
    }

    /**
     * Verifies that we can obtain a window for a document and then retrieve document for that window.
     */
    public void testDocumentWindowAccess() throws Exception {
        DomWindow window = _htmlDocument.getWindow();
        assertSame( "The original document", _htmlDocument, window.getDocument() );
        assertSame( "The window upon subsequence request", window, _htmlDocument.getWindow() );
        assertSame( "The window accessed from itself", window, window.getWindow() );
        assertSame( "The window's 'self' object", window, window.getSelf() );
    }

    /**
     * Verifies that the open method returns a window with an appropriate document.
     */
    public void testWindowOpen() throws Exception {
        DomWindow window1 = createMainWindow();
        DomWindow window2 = window1.open( "next.html", "broken", "", false );
        HTMLDocument document = window2.getDocument();
        assertNotNull( "Window has no associated document", document );
        assertEquals( "Title of document in new window", "broken (next.html)", document.getTitle() );

        window2.close();
        TestWindowProxy.assertLastProxyMethod( "close" );
    }


    /**
     * Writes to a document should appear in the window's document write buffer.
     */
    public void testDocumentWrite() throws Exception {
        DomWindow window1 = createMainWindow();
        window1.getDocument().write( "A simple string" );
        assertEquals( "Contents of write buffer", "A simple string", window1.getDocumentWriteBuffer() );
        window1.discardDocumentWriteBuffer();
        assertEquals( "Contents of cleared write buffer", "", window1.getDocumentWriteBuffer() );

    }


    private DomWindow createMainWindow() {
        DomWindow window = _htmlDocument.getWindow();
        _proxy = new TestWindowProxy( _htmlDocument );
        window.setProxy( _proxy );
        TestWindowProxy.clearProxyCalls();
        return window;
    }


    /**
     * Verifies that an alert request is sent to the proxy appropriately.
     */
    public void testAlert() throws Exception {
        DomWindow window = createMainWindow();
        window.alert( "A little message" );
        TestWindowProxy.assertLastProxyMethod( "alert( A little message )" );
    }

    /**
     * Verifies that a confirmation request is sent to the proxy and the appropriate answer is returned.
     */
    public void testConfirm() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setAnswer( "no" );
        assertFalse( "Should have said no", window.confirm( "Time to quit?" ) );
        TestWindowProxy.assertLastProxyMethod( "confirm( Time to quit? )" );
        _proxy.setAnswer( "yes" );
        assertTrue( "Should have said yes", window.confirm( "Want to stay?" ) );
        TestWindowProxy.assertLastProxyMethod( "confirm( Want to stay? )" );
    }


    /**
     * Verifies that a prompt is sent to the proxy and the appropriate answer is returned.
     */
    public void testPrompt() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setAnswer( null );
        assertEquals( "User default choice", "0", window.prompt( "How many choices?", "0" ) );
        TestWindowProxy.assertLastProxyMethod( "prompt( How many choices? )" );
        _proxy.setAnswer( "blue" );
        assertEquals( "Explicit user choice", "blue", window.prompt( "What is your favorite color?", "yellow" ) );
        TestWindowProxy.assertLastProxyMethod( "prompt( What is your favorite color? )" );
    }

    /**
     * Verifies that writing to and closing a document triggers a replaceText request.
     */
    public void testTextReplacement() throws Exception {
        DomWindow window = createMainWindow();
        window.getDocument().write( "A bit of text" );
        window.getDocument().close();
        assertNotNull( "No text replacement occurred", _proxy.getReplacementText() );
        assertEquals( "Replacement text", "A bit of text", _proxy.getReplacementText() );
    }

    /**
     * Verifies that the window can report its URL, which it obtains via its prozy.
     */
    public void testWindowUrl() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setUrl( new URL( "http://localhost") );
        assertEquals( "Window url", new URL( "http://localhost" ), window.getUrl() );
    }

    // todo test getNavigator
    // todo test getScreen
    // todo test getLocation, setLocation
    // todo test getFrames
    // todo test clearCaches, getDocumentWriteBuffer, clearDocumentWriteBuffer


    /**
     * Verifies simply the existence of some methods not currently implemented. Todo make them do something useful.
     */
    public void testMethodExistences() throws Exception {
        DomWindow window =  _htmlDocument.getWindow();
        window.setTimeout( 40 );
        window.focus();
        window.moveTo( 10, 20 );
    }


}
