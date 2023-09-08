package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: HTMLSelectTest.java 107617 2012-01-03 09:51:15Z jst $
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
import junit.textui.TestRunner;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;
import org.w3c.dom.html.HTMLFormElement;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLSelectTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;
    private HTMLSelectElement _select;
    private HTMLOptionElement[] _options;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HTMLSelectTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _form = (HTMLFormElement) createElement( "form", new String[][] { { "action", "go_here" } } );
        _select = (HTMLSelectElement) createElement( "select" );
        _htmlDocument.appendChild( _form );
        _form.appendChild( _select );

        _options = new HTMLOptionElement[] { createOption( "red", "Vermillion", false ), createOption( "blue", "Azure", true ), createOption( "green", "Chartreuse", false ) };
        for (int i = 0; i < _options.length; i++) {
            HTMLOptionElement option = _options[i];
            _select.appendChild( option );
        }
    }


    public void testSingleSelect() throws Exception {
        assertSame( "Form for select", _form, _select.getForm() );
        assertEquals( "type with no size", HTMLSelectElementImpl.TYPE_SELECT_ONE, _select.getType() );
        assertEquals( "select index", 1, _select.getSelectedIndex() );
        assertEquals( "initial value", "blue", _select.getValue() );

        _select.setSelectedIndex( 0 );
        assertEquals( "modified select index", 0, _select.getSelectedIndex() );
        assertProperties( "changed default selected", "defaultSelected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
        assertProperties( "changed selected", "selected", _options, new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE } );

        ((HTMLOptionElementImpl) _options[2]).setSelected( true );
        assertEquals( "remodified select index", 2, _select.getSelectedIndex() );
        assertProperties( "rechanged selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.TRUE } );

        ((HTMLControl) _select).reset();
        assertEquals( "reset value", "blue", _select.getValue() );
        assertEquals( "reset index", 1, _select.getSelectedIndex() );
        assertProperties( "reset selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
    }


    public void testMultiSelect() throws Exception {
        _select.setMultiple( true );
        _select.setSize( 3 );

        assertEquals( "type with size", HTMLSelectElementImpl.TYPE_SELECT_MULTIPLE, _select.getType() );
        assertEquals( "select index", 1, _select.getSelectedIndex() );
        assertEquals( "initial value", "blue", _select.getValue() );

        ((HTMLOptionElementImpl) _options[0]).setSelected( true );
        assertEquals( "modified select index", 0, _select.getSelectedIndex() );
        assertProperties( "changed default selected", "defaultSelected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
        assertProperties( "changed selected", "selected", _options, new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.FALSE } );

        ((HTMLControl) _select).reset();
        assertEquals( "reset value", "blue", _select.getValue() );
        assertProperties( "reset selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
    }


    public void testSingleLineSelect() throws Exception {
        _select.setMultiple( true );
        _select.setSize( 1 );
        assertEquals( "type with size 1", HTMLSelectElementImpl.TYPE_SELECT_ONE, _select.getType() );

        assertEquals( "select index", 1, _select.getSelectedIndex() );
        assertEquals( "initial value", "blue", _select.getValue() );

        ((HTMLOptionElementImpl) _options[0]).setSelected( true );
        assertEquals( "modified select index", 0, _select.getSelectedIndex() );
        assertProperties( "changed default selected", "defaultSelected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
        assertProperties( "changed selected", "selected", _options, new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE } );
    }


    public void testElements() throws Exception {
        assertEquals( "number of options", _options.length, _select.getOptions().getLength() );
        assertSame( "first option", _options[0], _select.getOptions().item(0) );
        assertProperties( "default selected", "defaultSelected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
        assertProperties( "initial selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE } );
        assertProperties( "index", "index", _options, new Integer[] { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) } );
        assertProperties( "text", "text", _options, new String[] { "Vermillion", "Azure", "Chartreuse" } );
        assertProperties( "value", "value", _options, new String[] { "red", "blue", "green" } );
        assertEquals( "select length", _options.length, _select.getLength() );
    }


    public void testSingleWithNothingSelected() throws Exception {
        ((HTMLOptionElementImpl) _options[1]).setSelected( false );
        assertEquals( "select index", 0, _select.getSelectedIndex() );
        assertEquals( "initial value", "red", _select.getValue() );

        assertProperties( "initial selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE } );
    }


    public void testMultipleWithNothingSelected() throws Exception {
        _select.setMultiple( true );
        _select.setSize( 3 );
        ((HTMLOptionElementImpl) _options[1]).setSelected( false );
        assertEquals( "select index", -1, _select.getSelectedIndex() );
        assertEquals( "initial value", null, _select.getValue() );

        assertProperties( "initial selected", "selected", _options, new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE } );
    }


    // XXX value (write), length (write)
    // XXX add, remove, blur, focus

}
