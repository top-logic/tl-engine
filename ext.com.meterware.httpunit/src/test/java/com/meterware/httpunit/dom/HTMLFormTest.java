package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: HTMLFormTest.java 107617 2012-01-03 09:51:15Z jst $
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
import org.w3c.dom.html.*;
import org.w3c.dom.Element;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLFormTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;
    private HTMLInputElement _textField, _passwordField, _defaultField, _hiddenField;
    private HTMLInputElement _radio1[], _radio2[], _checkbox[];
    private HTMLInputElement _submitInput, _resetInput, _buttonInput;
    private HTMLTextAreaElement _textArea;
    private Element _body;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HTMLFormTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _form = (HTMLFormElement) createElement( "form", new String[][] { { "action", "go_here" } } );
        _body = _htmlDocument.createElement( "body" );
        _htmlDocument.appendChild( _body );
        _body.appendChild( _form );

        _textField     = (HTMLInputElement) createElement( "input", new String[][] { { "name", "text" }, { "type", "text" }, { "value", "initial" } } );
        _passwordField = (HTMLInputElement) createElement( "input", new String[][] { { "id", "password" }, { "type", "password" } } );
        _hiddenField   = (HTMLInputElement) createElement( "input", new String[][] { { "id", "hidden" }, { "type", "hidden" }, { "value", "saved" } } );
        _defaultField  = (HTMLInputElement) createElement( "input", new String[][] { { "name", "default" }, { "value", "zero" } } );
        _submitInput   = (HTMLInputElement) createElement( "input", new String[][] { { "name", "submit" }, { "type", "submit" }, { "value", "go" } } );
        _buttonInput   = (HTMLInputElement) createElement( "input", new String[][] { { "name", "button" }, { "type", "button" }, { "value", "go" } } );
        _resetInput    = (HTMLInputElement) createElement( "input", new String[][] { { "name", "reset" }, { "type", "reset" }, { "value", "clear" } } );
        _textArea      = (HTMLTextAreaElement) createElement( "textarea", new String[][] { { "name", "area" } } );

        _radio1 = new HTMLInputElement[3];
        for (int i = 0; i < _radio1.length; i++) {
            _radio1[i] = (HTMLInputElement) createElement( "input", new String[][] { { "name", "radio" }, { "type", "radio" }, { "value", "channel" + (i+1) } } );
        }
        _radio1[1].setAttribute( "checked", "true" );

        _radio2 = new HTMLInputElement[4];
        for (int i = 0; i < _radio2.length; i++) {
            _radio2[i] = (HTMLInputElement) createElement( "input", new String[][] { { "name", "radio3" }, { "type", "radio" }, { "value", "color" + (i+1) } } );
        }
        _radio2[3].setAttribute( "checked", "true" );

        _checkbox = new HTMLInputElement[2];
        for (int i = 0; i < _checkbox.length; i++) {
            _checkbox[i] = (HTMLInputElement) createElement( "input", new String[][] { { "name", "checkbox" }, { "type", "checkbox" }, { "value", "on" + (i+1) } } );
        }
        _checkbox[1].setAttribute( "checked", "true" );
    }


    public void testTextValues() throws Exception {
        _form.appendChild( _textField );
        _form.appendChild( _passwordField );
        _form.appendChild( _hiddenField );
        _form.appendChild( _defaultField );

        verifyTextField( _textField, "initial" );
        verifyTextField( _passwordField, null );
        verifyTextField( _defaultField, "zero" );
        verifyTextField( _hiddenField, "saved" );
    }


    public void testTextArea() throws Exception {
        _form.appendChild( _textArea );
        _textArea.appendChild( _htmlDocument.createTextNode( "something here to see" ) );
        assertEquals( "Initial value", "something here to see", _textArea.getValue() );
        assertEquals( "Initial default value", "something here to see", _textArea.getDefaultValue() );

        _textArea.setValue( "what it is now" );
        assertEquals( "value after change", "what it is now", _textArea.getValue() );
        assertEquals( "default value after change", "something here to see", _textArea.getDefaultValue() );

        ((HTMLControl) _textArea).reset();
        assertEquals( "Reset value", "something here to see", _textArea.getValue() );
        assertEquals( "Reset default value", "something here to see", _textArea.getDefaultValue() );
    }


    private void verifyTextField( HTMLInputElement textField, String initialValue ) {
        assertSame( "Form for control", _form, textField.getForm() );
        assertEquals( "Initial value", initialValue, textField.getValue() );
        assertEquals( "Initial default value", initialValue, textField.getDefaultValue() );

        textField.setValue( "changed" );
        assertEquals( "value after change", "changed", textField.getValue() );
        assertEquals( "default value after change", initialValue, textField.getDefaultValue() );

        ((HTMLControl) textField).reset();
        assertEquals( "Reset value", initialValue, textField.getValue() );
        assertEquals( "Reset default value", initialValue, textField.getDefaultValue() );
    }


    public void testDefaults() throws Exception {
        _textField.setDefaultValue( "green" );
        assertEquals( "default text value", "green", _textField.getDefaultValue() );
        _checkbox[0].setDefaultChecked( true );
        assertEquals( "default checked value", true, _checkbox[0].getDefaultChecked() );
    }


    public void testCheckboxes() throws Exception {
        String[] values = { "on1", "on2" };
        boolean[] initialChecked = { false, true };

        for (int i = 0; i < _checkbox.length; i++) {
            _form.appendChild( _checkbox[i] );
        }

        for (int i = 0; i < _checkbox.length; i++) {
            assertSame( "Form for control", _form, _checkbox[i].getForm() );
            assertEquals( "Initial value " + i, values[i], _checkbox[i].getValue() );
            assertEquals( "Initial checked " + i, initialChecked[i], _checkbox[i].getChecked() );
            assertEquals( "Initial default checked " + i, initialChecked[i], _checkbox[i].getDefaultChecked() );
        }

        for (int j = 0; j < _checkbox.length; j++) {
            _checkbox[j].setChecked( !initialChecked[j] );
            for (int i = 0; i < _checkbox.length; i++) {
                assertEquals( "value " + i + " after change " + j, values[i], _checkbox[i].getValue() );
                assertEquals( "checked " + i + " after change " + j, !initialChecked[j], _checkbox[i].getChecked() );
                assertEquals( "default checked " + i + " after change " + j, initialChecked[i], _checkbox[i].getDefaultChecked() );
            }
            ((HTMLControl) _checkbox[j]).reset();
            for (int i = 0; i < _checkbox.length; i++) {
                assertEquals( "Initial checked " + i, initialChecked[i], _checkbox[i].getChecked() );
                assertEquals( "Initial default checked " + i, initialChecked[i], _checkbox[i].getDefaultChecked() );
            }
        }

        _checkbox[0].click();
        assertEquals( "checkbox 0 after 1st click", true, _checkbox[0].getChecked() );

        _checkbox[0].click();
        assertEquals( "checkbox 0 after 2nd click", false, _checkbox[0].getChecked() );
    }


    public void testRadioButtons() throws Exception {
        for (int i = 0; i < _radio1.length; i++) _form.appendChild( _radio1[i] );
        for (int i = 0; i < _radio2.length; i++) _form.appendChild( _radio2[i] );

        verifyRadioButtons( "radio 1 initial", _radio1, new boolean[]{ false, true, false } );
        verifyRadioButtons( "radio 2 initial", _radio2, new boolean[]{ false, false, false, true } );

        _radio2[2].setChecked( true );
        verifyRadioButtons( "radio 1 after set", _radio1, new boolean[]{ false, true, false } );
        verifyRadioButtons( "radio 2 after set", _radio2, new boolean[]{ false, false, true, false } );

        _form.reset();
        verifyRadioButtons( "radio 1 after reset", _radio1, new boolean[]{ false, true, false } );
        verifyRadioButtons( "radio 2 after reset", _radio2, new boolean[]{ false, false, false, true } );

        _radio1[0].click();
        verifyRadioButtons( "radio 1 after click", _radio1, new boolean[]{ true, false, false } );
    }


    private void verifyRadioButtons( String comment, HTMLInputElement[] radioButtons, boolean[] expected ) {
        for (int i = 0; i < radioButtons.length; i++) {
            assertEquals( comment + " checked " + i , expected[i], radioButtons[i].getChecked() );
        }
    }


    public void testFormElements() throws Exception {
        HTMLElement[] elements = { _textField, _passwordField, _hiddenField, _textArea, _checkbox[0], _checkbox[1], _resetInput, _submitInput };
        _form.appendChild( _htmlDocument.createElement( "i" ) ).appendChild( _htmlDocument.createTextNode( "Some controls" ) );
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            _form.appendChild( element );
        }
        HTMLCollection collection = _form.getElements();
        assertNotNull( "No collection returned", collection );
        assertEquals( "Number of elements", elements.length, collection.getLength() );
        for (int i = 0; i < elements.length; i++) {
            assertSame( "Form element " + i, elements[i], collection.item(i) );
        }
    }


    /**
     * Verifies that controls in the document body after the form but before the next form are included in the form collection of elements.
     * @throws Exception
     */
    public void testImproperFormElements() throws Exception {
        HTMLElement[] elements = { _textField, _passwordField, _hiddenField, _textArea };
        HTMLElement[] improperElements = {  _checkbox[0], _checkbox[1], _resetInput, _submitInput };
        _form.appendChild( _htmlDocument.createElement( "i" ) ).appendChild( _htmlDocument.createTextNode( "Some controls" ) );
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            _form.appendChild( element );
        }
        for (int i = 0; i < improperElements.length; i++) {
            HTMLElement element = improperElements[i];
            _body.appendChild( element );
        }
        Element form = _htmlDocument.createElement( "form" );
        _body.appendChild( form );
        form.appendChild( _htmlDocument.createElement( "button" ) );
        
        HTMLCollection collection = _form.getElements();
        assertNotNull( "No collection returned", collection );
        assertEquals( "Number of elements", elements.length + improperElements.length, collection.getLength() );
        for (int i = 0; i < elements.length; i++) {
            assertSame( "Form element " + i, elements[i], collection.item(i) );
            assertSame( "Form for element " + i, _form, ((HTMLControl) elements[i]).getForm() );
        }
        for (int i = 0; i < improperElements.length; i++) {
            int j = elements.length + i;
            assertSame( "Form element " + j, improperElements[i], collection.item(j) );
            assertSame( "Form for element " + j, _form, ((HTMLControl) improperElements[i]).getForm() );
        }
    }


    /**
     * Verifies that we can recognize buttons without forms.
     * @throws Exception
     */
    public void testFormDetection() throws Exception {
        _body.insertBefore( _buttonInput, _form );
        _body.appendChild( _textField );
        _body.appendChild( _htmlDocument.createElement( "form" ) );

        assertNull( "button should not be part of any form", _buttonInput.getForm() );
        assertSame( "Form for text field", _form, _textField.getForm() );
    }


    public void testResetInput() throws Exception {
        _form.appendChild( _textArea );
        _textArea.setDefaultValue( "Original" );
        _form.appendChild( _resetInput );

        assertEquals( "generated default", "Original", _textArea.getValue() );
        _textArea.setValue( "Changed this" );
        _resetInput.click();
        assertEquals( "value after reset", "Original", _textArea.getValue() );
    }


    public void testGetControlByName() throws Exception {
        _form.appendChild( _textField );
        _form.appendChild( _passwordField );
        _form.appendChild( _hiddenField );
        _form.appendChild( _defaultField );

        verifyNamedControlAndValue( "text", _textField, "initial" );
        verifyNamedControlAndValue( "hidden", _hiddenField, "saved" );
      }


    private void verifyNamedControlAndValue( String name, HTMLInputElement textField, String expectedValue ) {
        Object o = ((HTMLFormElementImpl) _form).get( name, null );
        assertTrue( "Result should be scriptable, is " + o, o instanceof ScriptableObject );
        ScriptableObject control = (ScriptableObject) o;
        assertSame( "control", textField, control );
        assertEquals( "field value", expectedValue, control.get( "value", null ) );
    }

}
