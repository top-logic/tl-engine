package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: DomScriptingTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2006-2007,2008 Russell Gold
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLAnchorElement;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Context;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import junit.framework.TestSuite;

import java.net.URL;

/**
 * Tests basic scripting via the DOM.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class DomScriptingTest extends AbstractHTMLElementTest {


    public static TestSuite suite() {
        return new TestSuite( DomScriptingTest.class );
    }


    public void testGetDocument() throws Exception {
        Element element = createElement( "body" );
        assertEquals( "Returned document", _htmlDocument, ((Scriptable) element).get( "document", null ) );
    }


    public void testDocumentGetTitle() throws Exception {
        _htmlDocument.setTitle( "something" );
        assertEquals( "title", "something", _htmlDocument.get( "title", null ) );

        Node body = createElement( "body" );
        assertEquals( "title", "something", evaluateExpression( body, "document.title" ) );
    }


    public void testDocumentPutTitle() throws Exception {
        _htmlDocument.put( "title", _htmlDocument, "right here" );
        assertEquals( "title after put", "right here", _htmlDocument.getTitle() );

        Node body = createElement( "body" );
        evaluateExpression( body, "document.title='new value'" );
        assertEquals( "title after script", "new value", _htmlDocument.getTitle() );
    }

    // todo test document.write, document.writeln - window must override getDocumentWriteBuffer, discardDocumentWriteBuffer(?)

    public void testElementPutTitle() throws Exception {
        HTMLBodyElement body = (HTMLBodyElement) createElement( "body" );
        Scriptable scriptableBody = (Scriptable) body;

        scriptableBody.put( "title", scriptableBody, "right here" );
        assertEquals( "title after put", "right here", body.getTitle() );

        evaluateExpression( body, "title='new value'" );
        assertEquals( "title after script", "new value", body.getTitle() );
    }


    public void testBodyAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        body.setBgColor( "red" );

        assertEquals( "initial background color", "red", evaluateExpression( _htmlDocument, "body.bgcolor" ) );

        evaluateExpression( _htmlDocument, "body.id='blue'" );
        assertEquals( "revised foreground color", "blue", body.getId() );
    }


    public void testNumericAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        HTMLAnchorElementImpl anchor = (HTMLAnchorElementImpl) createElement( "a" );
        body.appendChild( anchor );
        anchor.setTabIndex( 4 );

        assertEquals( "initial tab index", Integer.valueOf(4), evaluateExpression( anchor, "tabindex" ) );

        evaluateExpression( anchor, "tabindex=6" );
        assertEquals( "revised tab index", 6, anchor.getTabIndex() );
    }


    private HTMLBodyElement addBodyElement() {
        HTMLBodyElement body = (HTMLBodyElement) createElement( "body" );
        _htmlDocument.setBody( body );
        return body;
    }


    public void testCreateElement() throws Exception {
        Object node = evaluateExpression( _htmlDocument, "createElement( 'a' )" );
        assertNotNull( "No node returned", node );
        assertTrue( "Node is not an anchor element", node instanceof HTMLAnchorElement );
    }


    public void testDocumentLinksCollection() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy( _htmlDocument );
        proxy.setUrl( new URL( "http://localhost" ) );
        _htmlDocument.getWindow().setProxy( proxy );
        HTMLBodyElement body = addBodyElement();
        appendLink( body, "red", "red.html" );
        appendLink( body, "blue", "blue.html" );

        assertEquals( "number of links", Integer.valueOf(2), evaluateExpression( _htmlDocument, "links.length" ) );
        Object second = evaluateExpression( _htmlDocument, "links[1]" );
        assertNotNull( "Did not obtain any link object", second );
        assertTrue( "Object is not a link element", second instanceof HTMLAnchorElement );
        assertEquals( "Link ID", "blue", ((HTMLAnchorElement) second).getId() );

        assertEquals( "red link href", "http://localhost/red.html", evaluateExpression( _htmlDocument, "links.red.href" ) );
    }



    private void appendLink( HTMLBodyElement body, String id, String href ) {
        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement( "a" );
        anchor1.setId( id );
        anchor1.setHref( href );
        body.appendChild( anchor1 );
    }


    public void testConvertable() throws Exception {
        assertConvertable( String.class, String.class );
        assertConvertable( Integer.class, String.class );
        assertConvertable( String.class, Integer.class );
        assertConvertable( Short.class, Integer.class );
        assertConvertable( String.class, Boolean.class );
        assertConvertable( Byte.class, int.class );
    }

    private void assertConvertable( Class valueType, Class parameterType ) {
        assertTrue( valueType.getName() + " should be convertable to " + parameterType.getName(), ScriptingSupport.isConvertableTo( valueType, parameterType ) );
    }


    private Object evaluateExpression( Node node, String expression ) {
        try {
            Context context = Context.enter();
            context.initStandardObjects( null );
            return ((NodeImpl) node).evaluateExpression( expression );
        } finally {
            Context.exit();
        }
    }
}
