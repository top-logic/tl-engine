package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: HTMLElementTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004-2007, Russell Gold
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

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.html.*;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLElementTest extends AbstractHTMLElementTest {


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HTMLElementTest.class );
    }


    public void testCaseInsensitiveTagSearch() throws Exception {
        Element element = createElement( "body" );
        Node form = element.appendChild( createElement( "form" ) );
        NodeList nl = element.getElementsByTagName( "form" );
        assertEquals( "# form nodes to find", 1, nl.getLength() );
        assertSame( "Found form node", form, nl.item(0) );
    }


    public void testBaseElementDefaults() throws Exception {
        Element element = createElement( "b", new String[0][] );
        assertTrue( "node should be an HTMLElement but is " + element.getClass(), element instanceof HTMLElement );
        assertEquals( "Tag name", "B", element.getNodeName() );

        HTMLElement htmlElement = (HTMLElement) element;
        assertNull( "class name should not be specified by default", htmlElement.getClassName() );
        assertNull( "direction should not be specified by default", htmlElement.getDir() );
        assertNull( "id should not be specified by default", htmlElement.getId() );
        assertNull( "lang should not be specified by default", htmlElement.getLang() );
        assertNull( "title should not be specified by default", htmlElement.getTitle() );
    }


    /**
     * test base element attributes
     * @throws Exception
     */
    public void testBaseElementAttributes() throws Exception {
        Element element = createElement( "code", new String[][] { { "class", "special" }, { "dir", "rtl" }, { "id", "sample" }, { "lang", "hb" }, { "title", "psalm 83"} } );
        assertTrue( "node should be an HTMLElement but is " + element.getClass(), element instanceof HTMLElement );
        assertEquals( "Tag name", "CODE", element.getNodeName() );

        HTMLElement htmlElement = (HTMLElement) element;
        assertEquals( "class name", "special",  htmlElement.getClassName() );
        assertEquals( "direction",  "rtl",      htmlElement.getDir() );
        assertEquals( "id",         "sample",   htmlElement.getId() );
        assertEquals( "lang",       "hb",       htmlElement.getLang() );
        assertEquals( "title",      "psalm 83", htmlElement.getTitle() );
    }


    public void testWriteableElementAttributes() throws Exception {
        Element element = createElement( "cite", new String[0][] );
        assertTrue( "node should be an HTMLElement but is " + element.getClass(), element instanceof HTMLElement );
        assertEquals( "Tag name", "CITE", element.getNodeName() );

        HTMLElement htmlElement = (HTMLElement) element;
        htmlElement.setClassName( "special" );
        htmlElement.setDir( "rtl" );
        htmlElement.setId( "sample" );
        htmlElement.setLang( "hb" );
        htmlElement.setTitle( "psalm 83" );

        assertEquals( "class name", "special",  htmlElement.getClassName() );
        assertEquals( "direction",  "rtl",      htmlElement.getDir() );
        assertEquals( "id",         "sample",   htmlElement.getId() );
        assertEquals( "lang",       "hb",       htmlElement.getLang() );
        assertEquals( "title",      "psalm 83", htmlElement.getTitle() );
    }


    public void testEmptyFormDefaults() throws Exception {
        Element element = createElement( "form", new String[][] { { "action", "go_here" } } );
        assertTrue( "node should be an HTMLFormElement but is " + element.getClass(), element instanceof HTMLFormElement );
        assertEquals( "Tag name", "FORM", element.getNodeName() );

        HTMLFormElement form = (HTMLFormElement) element;
        assertEquals( "default character set", "UNKNOWN", form.getAcceptCharset() );
        assertEquals( "specified action", "go_here", form.getAction() );
        assertEquals( "number of controls in collection", 0, form.getElements().getLength() );
        assertEquals( "default form encoding", "application/x-www-form-urlencoded", form.getEnctype() );
        assertEquals( "number of controls in form", 0, form.getLength() );
        assertEquals( "default method", "GET", form.getMethod().toUpperCase() );
        assertNull( "form name should not be specified by default", form.getName() );
        assertNull( "default target is not null", form.getTarget() );
    }


    public void testFormAttributes() throws Exception {
        Element element = createElement( "form", new String[][] { { "accept-charset", "latin-1" },
                                                                  { "enctype", "multipart/form-data" },
                                                                  { "method", "post" },
                                                                  { "name", "aform" },
                                                                  { "target", "green" } } );
        HTMLFormElement form = (HTMLFormElement) element;
        assertEquals( "character set", "latin-1", form.getAcceptCharset() );
        assertEquals( "form encoding", "multipart/form-data", form.getEnctype() );
        assertEquals( "method", "post", form.getMethod() );
        assertEquals( "form name", "aform", form.getName() );
        assertEquals( "target", "green", form.getTarget() );
    }


    public void testWriteableFormAttributes() throws Exception {
        Element element = createElement( "form", new String[][] { { "action", "go_here" } } );
        HTMLFormElement form = (HTMLFormElement) element;

        form.setAction( "go_there" );
        form.setAcceptCharset( "latin-1" );
        form.setEnctype( "multipart/form-data" );
        form.setMethod( "post" );
        form.setName( "aform" );
        form.setTarget( "green" );

        assertEquals( "specified action", "go_there", form.getAction() );
        assertEquals( "character set", "latin-1", form.getAcceptCharset() );
        assertEquals( "form encoding", "multipart/form-data", form.getEnctype() );
        assertEquals( "method", "post", form.getMethod() );
        assertEquals( "form name", "aform", form.getName() );
        assertEquals( "target", "green", form.getTarget() );
    }


    public void testTitleElement() throws Exception {
        Element element = createElement( "title" );
        Text text = _htmlDocument.createTextNode( "something here" );
        element.appendChild( text );

        assertTrue( "node should be an HTMLTitleElement but is " + element.getClass(), element instanceof HTMLTitleElement );
        assertEquals( "Tag name", "TITLE", element.getNodeName() );

        HTMLTitleElement title = (HTMLTitleElement) element;
        assertEquals( "initial title", "something here", title.getText() );

        title.setText( "what it says now" );
        NodeList childNodes = element.getChildNodes();
        assertEquals( "Number of child nodes", 1, childNodes.getLength() );
        assertTrue( "Sole child node is not text", childNodes.item(0) instanceof Text );
        assertEquals( "Revised title text", "what it says now", ((Text) childNodes.item(0)).getData() );
        assertEquals( "revised title", "what it says now", title.getText() );
    }


    public void testEmptyTitleElement() throws Exception {
        Element element = createElement( "title" );

        assertTrue( "node should be an HTMLTitleElement but is " + element.getClass(), element instanceof HTMLTitleElement );
        assertEquals( "Tag name", "TITLE", element.getNodeName() );

        HTMLTitleElement title = (HTMLTitleElement) element;
        assertEquals( "initial title", "", title.getText() );
        title.setText( "what it says now" );
        NodeList childNodes = element.getChildNodes();
        assertEquals( "Number of child nodes", 1, childNodes.getLength() );
        assertTrue( "Sole child node is not text", childNodes.item(0) instanceof Text );
        assertEquals( "Revised title text", "what it says now", ((Text) childNodes.item(0)).getData() );
        assertEquals( "revised title", "what it says now", title.getText() );
    }


    public void testHtmlElement() throws Exception {
        doElementTest( "html", HTMLHtmlElement.class, new String[][] { { "version", "4.0" } } );
    }


    public void testHeadElement() throws Exception {
        doElementTest( "head", HTMLHeadElement.class, new String[][] { { "profile", "http://www.acme.com/profiles/core" } } );
    }


    public void testLinkElement() throws Exception {
        doElementTest( "link", HTMLLinkElement.class, new Object[][] { { "charset", "utf-8" }, { "href", "site.css" },
                                                                       { "hreflang", "en" }, { "disabled", Boolean.TRUE, Boolean.FALSE },
                                                                       { "rel", "ccc.html" }, { "rev", "aaa.html" }, { "target", "green" },
                                                                       { "type", "text/html" }, { "media", "paper", "screen" } } );
    }


    public void testMetaElement() throws Exception {
        doElementTest( "meta", HTMLMetaElement.class, new Object[][] { { "content", "Something" }, { "http-equiv", "Refresh"},
                                                                       { "name", "author"}, { "scheme", "ISBN" } } );
    }


    public void testBaseElement() throws Exception {
        doElementTest( "base", HTMLBaseElement.class, new Object[][] { { "href", "somewhere.html" }, { "target", "blue" } } );
    }


    public void testStyleElment() throws Exception {
        doElementTest( "style", HTMLStyleElement.class, new Object[][] {{"disabled", Boolean.TRUE, Boolean.FALSE },
                                                                        {"media", "paper", "screen" }, { "type", "text/css" } } );
    }


    public void testBodyElement() throws Exception {
        doElementTest( "body", HTMLBodyElement.class, new Object[][] { { "aLink", "red" }, { "background", "blue" }, { "link", "azure" },
                                                                        {"bgColor", "white" }, { "text", "maroon" }, { "vLink", "crimson" } } );
    }


    public void testOptionElementAttributes() throws Exception {
        doElementTest( "option", HTMLOptionElement.class, new Object[][] { { "disabled", Boolean.TRUE, Boolean.FALSE }, { "label", "Vert" },
                                                                           { "value", "green" } } );
    }


    public void testSelectElement() throws Exception {
        doElementTest( "select", HTMLSelectElement.class, new Object[][] { { "multiple", Boolean.TRUE, Boolean.FALSE },
                                                                           { "name", "here" }, { "tabindex", Integer.valueOf(1), Integer.valueOf(0) },
                                                                           { "size", Integer.valueOf(12), Integer.valueOf(0) }, { "disabled", Boolean.TRUE, Boolean.FALSE } } );
    }


    public void testInputElement() throws Exception {
        doElementTest( "input", HTMLInputElement.class, new Object[][] { { "accept", "text/html" }, { "accessKey", "C" }, { "align", "middle", "bottom" },
                                                                         { "alt", "check"},
                                                                         { "disabled", Boolean.TRUE, Boolean.FALSE }, { "maxlength", Integer.valueOf(5), Integer.valueOf(0) },
                                                                         { "name", "here" }, { "readonly", Boolean.TRUE, Boolean.FALSE },
                                                                         { "size", "12" }, { "src", "arrow.jpg" }, { "tabindex", Integer.valueOf(1), Integer.valueOf(0) },
                                                                         { "type", "radio", "text", "ro" }, { "useMap", "myMap" }, { "value", "230" } } );
        // XXX blur, focus, select, click
    }


    public void testButtonElement() throws Exception {
        doElementTest( "button", HTMLButtonElement.class, new Object[][] { { "accesskey", "C" }, { "disabled", Boolean.TRUE, Boolean.FALSE },
                                                                         { "name", "here" }, { "tabindex", Integer.valueOf(1), Integer.valueOf(0) },
                                                                         { "type", "button", "submit", "ro" }, { "value", "230" } } );
        // XXX blur, focus, select, click
    }


    public void testTextAreaElement() throws Exception {
        doElementTest( "textarea", HTMLTextAreaElement.class, new Object[][] { { "accesskey", "C" }, { "cols", Integer.valueOf(1), Integer.valueOf(0) },
                                                                               { "disabled", Boolean.TRUE, Boolean.FALSE }, { "name", "here" },
                                                                               { "readonly", Boolean.TRUE, Boolean.FALSE }, { "rows", Integer.valueOf(8), Integer.valueOf(0) },
                                                                               { "tabindex", Integer.valueOf(1), Integer.valueOf(0) },
                                                                               { "type", "radio", "text", "ro" } } );
        // XXX blur, focus, select
    }


    public void testAnchorElement() throws Exception {
        doElementTest( "a", HTMLAnchorElement.class, new Object[][] { { "accesskey", "U" }, { "charset", "utf-8" }, { "hreflang", "en" },
                                                                      { "name", "here" }, { "rel", "link" }, { "rev", "index" }, { "target", "green" },
                                                                      { "type", "text/html" } } );
    }


    public void testAreaElement() throws Exception {
        doElementTest( "area", HTMLAreaElement.class, new Object[][] { { "accesskey", "U" }, { "alt", "[draw]" }, { "coords", "30,40,20" },
                                                                       { "nohref", Boolean.TRUE, Boolean.FALSE }, { "shape", "circle" },  { "tabindex", Integer.valueOf(4), Integer.valueOf(0) },
                                                                       { "target", "green" } } );
    }


    public void testImageElement() throws Exception {
        doElementTest( "img", HTMLImageElement.class,
                       new Object[][] { { "name", "here" }, { "align", "top" }, { "alt", "big show" },
                                        { "border", "3" }, { "height", "7" },
                                        { "hspace", "1" }, { "ismap", Boolean.TRUE, Boolean.FALSE },
                                        { "longdesc", "not too very" }, { "src", "circle.jpg" },
                                        { "usemap", "mapname" }, { "vspace", "4" }, { "width", "15" } } );
    }


    public void testParagraphElement() throws Exception {
        doElementTest( "p", HTMLParagraphElement.class,
                       new Object[][] { { "title", "here" }, { "id", "aaa"}, {"align", "top"} } );
    }


    public void testIFrameElement() throws Exception {
        doElementTest( "iframe", HTMLIFrameElement.class,
                       new Object[][] { { "align", "center" }, { "src", "aaa"} } );
    }


    /**
     * test the Applet Element
     * changed default codebase from "/" to "." according to bug report [ 1895501 ] Handling no codebase attribute in APPLET tag
     * @throws Exception
     */
    public void testAppletElement() throws Exception {
        doElementTest( "applet", HTMLAppletElement.class,
                       new Object[][] { { "align", "center" },/* { "alt", "an applet" }, { "archive", "my.jar" },*/
                                        { "code", "here.There" }, { "codebase", "there", "." }, /*{ "height", "17" },
                                        { "hspace", "2" }, { "name", "applet" }, { "object", "a file here" },
                                        { "vspace", "3" }, { "width", "80"}*/ } );
    }


    // XXX form.getLength, form.submit
    // XXX input.blur, input.focus, input.select, input.click
    // XXX a.blur, a.focus
    // XXX iframe.longDescm iframe.name, iframe.width, iframe.height, iframe.scrolling
    // XXX iframe.marginheight, iframe.marginwidth, iframe.frameborder


}
