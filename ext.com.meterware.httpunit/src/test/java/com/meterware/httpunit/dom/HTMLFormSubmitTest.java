package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: HTMLFormSubmitTest.java 107617 2012-01-03 09:51:15Z jst $
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
import junit.textui.TestRunner;
import junit.framework.TestSuite;
import org.w3c.dom.html.*;

import java.net.URL;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class HTMLFormSubmitTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HTMLFormSubmitTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        TestWindowProxy windowProxy = new TestWindowProxy( _htmlDocument );
        windowProxy.setUrl( new URL( "http://localhost/aux.html" ) );

        _htmlDocument.getWindow().setProxy( windowProxy );
        HTMLBodyElement body = (HTMLBodyElement) _htmlDocument.createElement( "body" );
        _htmlDocument.appendChild( body );
        
        _form = (HTMLFormElement) createElement( "form", new String[][] { { "action", "go_here" } } );
        body.appendChild( _form );
        _form.setMethod( "GET" );
        _form.setAction( "tryMe" );
    }


    /**
     * Verifies that submitting a simple form works.
     */
    public void testSubmitFromForm() throws Exception {
        addInput( "text", "name" ).setValue( "master" );
        addInput( "checkbox", "second" ).setChecked( true );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?name=master&second=on, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that submitting a simple form from a button selects that button only.
     */
    public void testSubmitFromButton() throws Exception {
        addInput( "text", "name", "master" );
        addInput( "checkbox", "second" ).setChecked( true );
        addInput( "submit", "save", "none" );
        HTMLInputElementImpl button = (HTMLInputElementImpl) addInput( "submit", "save", "all" );
        button.doClickAction();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?name=master&second=on&save=all, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that characters in parameter names will be appropriately encoded.
     */
    public void testEmbeddedEquals() throws Exception {
        addInput( "text", "age=x", "12" );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?age%3Dx=12, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that an empty "select" element does not transmit any parameter values.
     */
    public void testEmptyChoiceSubmit() throws Exception {
        addInput( "text", "age", "12" );
        addSelect( "empty" );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?age=12, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that a select will send a value taken from the "value" attribute.
     */
    public void testSubmitUsingSelectOptionAttributes() throws Exception {
        addInput( "text", "age", "12" );
        HTMLSelectElement select = addSelect( "color" );
        addOption( select, "red", null );
        addOption( select, "blue", "azure" ).setSelected( true );
        addOption( select, "green", null );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?age=12&color=blue, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that a select will send a value taken from the text nodes following the option tags.
     */
    public void testSubmitUsingSelectOptionLabels() throws Exception {
        addInput( "text", "age", "12" );
        HTMLSelectElement select = addSelect( "color" );
        select.setMultiple( true );
        select.setSize( 2 );
        addOption( select, null, "red" );
        addOption( select, null, "blue" ).setSelected( true );
        addOption( select, null, "green" ).setSelected( true );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?age=12&color=blue&color=green, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that a radio button will send its value on submit.
     */
    public void testSubmitRadioButtons() throws Exception {
        addInput( "radio", "color", "red" ).setChecked( true );
        addInput( "radio", "color", "blue" ).setChecked( true );
        addInput( "radio", "color", "green" );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?color=blue, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that checkboxes will send their values on submit.
     */
    public void testSubmitCheckboxes() throws Exception {
        addInput( "checkbox", "color", "red" ).setChecked( true );
        addInput( "checkbox", "color", "blue" ).setChecked( true );
        addInput( "checkbox", "color", "green" );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( GET, http://localhost/tryMe?color=red&color=blue, null, null )", TestWindowProxy.popProxyCall() );
    }


    /**
     * Verifies that forms with the POST method send their data in the message body.
     */
    public void ntestSubmitUsingPost() throws Exception {
        _form.setMethod( "POST" );
        addInput( "checkbox", "color", "red" ).setChecked( true );
        addInput( "checkbox", "color", "blue" ).setChecked( true );
        addInput( "checkbox", "color", "green" );
        _form.submit();
        assertEquals( "Expected response", "submitRequest( POST, http://localhost/tryMe, null, color=red&color=blue )", TestWindowProxy.popProxyCall() );
    }


    private HTMLSelectElement addSelect( String name ) {
        HTMLSelectElement select = (HTMLSelectElement) _htmlDocument.createElement( "select" );
        _form.appendChild( select );
        select.setName( name );
        return select;
    }


    private HTMLOptionElement addOption( HTMLSelectElement select, String value, String label ) {
        HTMLOptionElement option = (HTMLOptionElement) _htmlDocument.createElement( "option" );
        select.appendChild( option );
        if (value != null) option.setValue( value );
        if (label != null) select.appendChild( _htmlDocument.createTextNode( label ) );
        return option;
    }


    private HTMLInputElement addInput( String type, String name ) {
        HTMLInputElement element = (HTMLInputElement) _htmlDocument.createElement( "input" );
        element.setAttribute( "type", type );
        element.setAttribute( "name", name );
        _form.appendChild( element );
        return element;
    }


    private HTMLInputElement addInput( String type, String name, String value ) {
        HTMLInputElement element = addInput( type, name );
        element.setValue( value );
        return element;
    }

}
