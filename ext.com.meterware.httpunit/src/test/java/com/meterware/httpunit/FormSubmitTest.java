package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: FormSubmitTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2008, Russell Gold
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

import java.io.IOException;

import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;
import org.xml.sax.SAXException;


/**
 * A test of the parameter validation functionality.
 **/
public class FormSubmitTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( FormSubmitTest.class );
    }


    public FormSubmitTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();
    }


    public void testEmbeddedEquals() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=\"age=x\" value=12>" +
                                  "<Input type=submit>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( getHostPath() + "/ask?age%3Dx=12", request.getURL().toExternalForm() );
    }


    public void testEmptyChoiceSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<select name=empty></select>" +
                                  "<Input type=submit>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( "Empty choice query", getHostPath() + "/ask?age=12", request.getURL().toExternalForm() );
    }


    public void testFormProperties() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<select name=empty></select>" +
                                  "<Input type=submit>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( "Form method", "GET", form.getMethod() );
        assertEquals( "Form action", "/ask", form.getAction() );

        form.getScriptableObject().setAction( "/tell" );
        assertEquals( "Form action", "/tell", form.getAction() );
    }


    public void testSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age>" +
                                  "<Input type=submit value=Go>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/ask?age=23", request.getURL().toExternalForm() );
    }


    public void testSubmitStringWithQueryOnlyRelativeURL() throws Exception {
        defineWebPage( "/blah/blah/blah", "<form method=GET action = '?recall=true'>" +
                                  "<Input type=submit value=Go>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/blah/blah/blah.html" );
        WebRequest request = page.getForms()[0].getRequest();
        assertEquals( getHostPath() + "/blah/blah/blah.html?recall=true", request.getURL().toExternalForm() );
    }


    public void testSubmitStringAfterSetAction() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age>" +
                                  "<Input type=submit value=Go>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        page.getForms()[0].getScriptableObject().setAction( "tell" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/tell?age=23", request.getURL().toExternalForm() );
    }


    public void testNoNameSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text value=dontSend>" +
                                  "<Input type=text name=age>" +
                                  "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/ask?age=23", request.getURL().toExternalForm() );
    }


    /**
     * check that submit buttons will be detected
     * @throws Exception
     */
    public void testSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=update>" +
                                  "<Input type=submit name=recalculate value=value>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
        assertMatchingSet( "selected request parameters", new String[]{"age","update"},
                           form.getRequest( "update" ).getRequestParameterNames() );
    }


    /**
     * test for bug report [ 1629836 ] Anchor only form actions are not properly handled
     * by Claude Brisson 
     * @throws Exception
     */
    public void testAnchor() throws Exception {
      defineWebPage( "page", "<form method=GET action = \"#myanchor\">" +
          "<Input type=submit id=doit>" +
          "</form>" );
      WebResponse page = _wc.getResponse( getHostPath() + "/page.html" );
      WebForm form = page.getForms()[0];
      Button button=form.getButtonWithID("doit");
      button.click();
      String url=_wc.getCurrentPage().getURL().toExternalForm();
      assertTrue(url.endsWith("page.html"));          	
    }
    /**
     * check that a fake submit button will be added and marked as such
     * test for [ 1159887 ] patch for RFE 1159884
     * by Rafal Krzewski 
     * @throws Exception
     */
    public void testFakeSubmitButtonAddition() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 1, buttons.length );
        assertTrue("the only submit button returned should be a fake",buttons[0].isFake());
    }

    public void testNonSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update>" +
                                  "<Input type=reset>" +
                                  "<Input type=button value=recalculate>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        Button[] buttons = form.getButtons();
        assertEquals( "num detected buttons", 3, buttons.length );
    }


    /**
     * test detecting the reset button
     * @throws Exception
     */
    public void testResetButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update>" +
                                  "<Input type=reset id=clear>" +
                                  "<Input type=button value=recalculate>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        form.setParameter( "age", "15" );
        Button reset = form.getButtonWithID( "clear" );
        reset.click();
        assertEquals( "Value after reset", "12", form.getParameterValue( "age" ) );
        HTMLElement element = page.getElementWithID( "clear" );
        assertSame( "Reset button", reset, element );
    }


    /**
     * test that a disabled submitButton can not be submitted
     * @throws Exception
     */
    public void testDisabledSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update>" +
                                  "<Input type=submit name=recalculate disabled>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
        SubmitButton sb = form.getSubmitButton( "recalculate" );
        assertNotNull( "Failed to find disabled button", sb );
        assertTrue( "Disabled button not marked as disabled", sb.isDisabled() );
        try {
            form.getRequest( sb );
            fail( "Allowed to create a request for a disabled button" );
        } catch (IllegalStateException e) {}
        try {
            sb.click();
            fail( "Allowed to click a disabled button" );
        } catch (IllegalStateException e) {}
    }
    
    /** 
     * test self disabling submit Buttons
     * test for bug report [ 1289151 ] Order of events in button.click() is wrong
     */
    public void testSelfDisablingSubmitButton() throws Exception {
      defineWebPage( "Default", "<form method=GET action = \"Default.html\">" +
          "<Input type=submit name='update' onclick='javascript:this.disabled=true;'></form>" +
          "</form>" );
				WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
				WebForm form = page.getForms()[0];
				SubmitButton[] buttons = form.getSubmitButtons();
				assertEquals( "num detected submit buttons", 1, buttons.length );
				SubmitButton sb = form.getSubmitButton( "update" );
				assertNotNull( "Failed to find update button", sb );
				sb.click();
				assertTrue( "Disabled button not marked as disabled", sb.isDisabled() );
				try {
					form.getRequest( sb );
					fail( "Allowed to create a request for a disabled button" );
				} catch (IllegalStateException e) {}
				try {
					sb.click();
					fail( "Allowed to click a disabled button" );
				} catch (IllegalStateException e) {}
	  }
    
    /**
     * test that a disabled Button can be detected by accessing the disabled() function
     * for bug report [ 1124024 ] Formcontrol and isDisabled should be public
     * by Wolfgang Fahl
     * @throws Exception
     */
    public void testButtonDisabledFlagAccess() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=button id=button1 name=button1 >" +
                                  "<Input type=button name=button2 disabled>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        Button[] buttons = form.getButtons();
        assertTrue( "Enabled button marked as disabled", !buttons[0].isDisabled() );
        assertTrue( "Disabled button not marked as disabled", buttons[1].isDisabled() );
        FormControl control=form.getControlWithID("button1");
        assertTrue(control.getClass().getName(),control instanceof Button);
    }

    public void testButtonIDDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit id=main name=update>" +
                                  "<Input type=submit name=recalculate>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton( "update" );
        assertEquals( "Null ID", "", form.getSubmitButton( "recalculate" ).getID() );
        assertEquals( "Button ID", "main", button.getID() );

        SubmitButton button2 = form.getSubmitButtonWithID( "main" );
        assertEquals( "Submit button", button, button2 );
    }


    public void testButtonTagDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Button type=submit name=update></button>" +
                                  "<button name=recalculate></button>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
    }


    public void testImageButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update src=\"\">" +
                                  "<Input type=image name=recalculate src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
    }


    public void testImageButtonDefaultSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( "Query", getHostPath() + "/ask?age=12&update=name&update.x=0&update.y=0", request.getURL().toExternalForm() );
    }


    public void testImageButtonNoValue() throws Exception {
        defineWebPage( "Default", "<form name='login' method='get' action='ask'>" +
                                  "<input type='text' name='email' value='bread'>" +
                                  "<input type='image' name='login' src='../../se/images/buttons/login.gif'" +
                                  "       Alt='OK' border='0'>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( "Query", getHostPath() + "/ask?email=bread&login.x=0&login.y=0", request.getURL().toExternalForm() );
    }


    /**
     * test behaviour of UnnameImageButtons
     * see also WebFormTest.testSubmitFromUnnamedImageButton
     * @throws Exception
     */
    public void testUnnamedImageButtonDefaultSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        String urlString=request.getURL().toExternalForm();
        assertEquals( getHostPath() + "/ask?age=12", urlString);
    }


    /**
     * test behavoir of positional image buttons
     * see also WebFormTest.testSubmitFromPositionalButton
     * @throws Exception
     */
    public void testImageButtonPositionalSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest( form.getSubmitButton( "update" ), 10, 15 );
        assertEquals( getHostPath() + "/ask?age=12&update=name&update.x=10&update.y=15", request.getURL().toExternalForm() );
        request.setImageButtonClickPosition( 5, 20 );
        assertEquals( getHostPath() + "/ask?age=12&update=name&update.x=5&update.y=20", request.getURL().toExternalForm() );
    }


    public void testImageButtonNoValuePositionalSubmit() throws Exception {
        defineWebPage( "Default", "<form method='GET' action='test.jsp'>" +
                                  "<input type='image' src='image.gif' name='aButton'>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest( form.getSubmitButton( "aButton" ), 20, 5 );
        assertEquals( getHostPath() + "/test.jsp?aButton.x=20&aButton.y=5", request.getURL().toExternalForm() );
    }


    public void testImageButtonNoValueUncheckedPositionalSubmit() throws Exception {
        defineWebPage( "Default", "<form method='GET' action='test.jsp'>" +
                                  "<input type='image' src='image.gif' name='aButton'>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.newUnvalidatedRequest( form.getSubmitButton( "aButton" ), 20, 5 );
        assertEqualQueries( getHostPath() + "/test.jsp?aButton.x=20&aButton.y=5", request.getURL().toExternalForm() );
    }


    public void testSubmitButtonAttributes() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 1, buttons.length );
        assertEquals( "submit button name", "update", buttons[0].getName() );
        assertEquals( "submit button value", "age", buttons[0].getValue() );
    }


    public void testSubmitButtonSelectionByName() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=recompute value=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton( "zork" );
        assertNull( "Found a non-existent button", button );
        button = form.getSubmitButton( "update" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "age", button.getValue() );
    }


    public void testSubmitButtonSelectionByNameAndValue() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton( "update" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "age", button.getValue() );
        button = form.getSubmitButton( "update", "name" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "name", button.getValue() );
    }


    public void testNamedButtonSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Button type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest( form.getSubmitButton( "update", "name" ) );
        assertEquals( getHostPath() + "/ask?age=12&update=name", request.getURL().toExternalForm() );

        request = form.getRequest( "update", "name" );
        assertEquals( getHostPath() + "/ask?age=12&update=name", request.getURL().toExternalForm() );

        request = form.getRequest( "update" );
        assertEquals( getHostPath() + "/ask?age=12&update=age", request.getURL().toExternalForm() );

        try {
            request.setImageButtonClickPosition( 1, 2 );
            fail( "Should not allow set position with non-image button" );
        } catch (IllegalRequestParameterException e) {
        }
    }


    public void testUnnamedButtonSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        try {
            form.getRequest();
            fail( "Should not allow submit with unnamed button" );
        } catch (IllegalRequestParameterException e) {
        }
    }


    public void testForeignSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        defineWebPage( "Dupl",    "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        defineWebPage( "Wrong",   "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=save value=age>" +
                                  "</form>" );
        WebResponse other  = _wc.getResponse( getHostPath() + "/Dupl.html" );
        WebResponse page   = _wc.getResponse( getHostPath() + "/Default.html" );
        WebResponse wrong  = _wc.getResponse( getHostPath() + "/Wrong.html" );

        WebForm form = page.getForms()[0];
        WebForm otherForm = other.getForms()[0];
        WebForm wrongForm = wrong.getForms()[0];

        form.getRequest( otherForm.getSubmitButtons()[0] );

        try {
            form.getRequest( wrongForm.getSubmitButtons()[0] );
            fail( "Failed to reject illegal button" );
        } catch (IllegalRequestParameterException e) {
        }

        form.newUnvalidatedRequest( wrongForm.getSubmitButtons()[0] );

        HttpUnitOptions.setParameterValuesValidated( false );
        form.getRequest( wrongForm.getSubmitButtons()[0] );
    }


    public void testNoActionSupplied() throws Exception {
        defineWebPage( "abc/form", "<form name=\"test\">" +
                               "  <input type=\"text\" name=\"aTextField\">" +
                               "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" +
                               "</form>" );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form.html" );
        WebForm form    = wr.getForms()[0];
        WebRequest req  = form.getRequest( "apply" );
        req.setParameter( "aTextField", "test" );
        assertEquals( getHostPath() + "/abc/form.html?aTextField=test&apply=Apply",
                            req.getURL().toExternalForm() );
    }


    public void testNoActionSuppliedWhenBaseHasParams() throws Exception {
        defineResource( "abc/form?param1=value&param2=value", "<form name=\"test\">" +
                               "  <input type=\"text\" name=\"aTextField\">" +
                               "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" +
                               "</form>" );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form?param1=value&param2=value" );
        WebForm form    = wr.getForms()[0];
        WebRequest req  = form.getRequest( "apply" );
        req.setParameter( "aTextField", "test" );
        assertEquals( getHostPath() + "/abc/form?param1=value&param2=value&aTextField=test&apply=Apply",
                            req.getURL().toExternalForm() );
    }


    public void testNoActionSuppliedWhenBaseHasParamsSetByTheForm() throws Exception {
        defineResource( "abc/form?param1=value&param2=value", "<form name=\"test\">" +
                               "  <input type=\"text\" name='param2'>" +
                               "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" +
                               "</form>" );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form?param1=value&param2=value" );
        WebForm form    = wr.getForms()[0];
        WebRequest req  = form.getRequest( "apply" );
        req.setParameter( "param2", "test" );
        assertEquals( getHostPath() + "/abc/form?param1=value&param2=test&apply=Apply",
                            req.getURL().toExternalForm() );
    }


    public void testPostActionParametersAfterSetAction() throws Exception {
        defineWebPage( "abc/form", "<form name=\"test\" method='POST' action='stop?ready=yes'>" +
                               "  <input type=\"text\" name=\"aTextField\">" +
                               "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" +
                               "</form>" );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form.html" );
        WebForm form    = wr.getForms()[0];
        form.getScriptableObject().setAction( "go?size=3&time=now" );
        WebRequest req  = form.getRequest( "apply" );
        req.setParameter( "aTextField", "test" );
        assertEquals( getHostPath() + "/abc/go?size=3&time=now",
                            req.getURL().toExternalForm() );
    }


    public void testPostParameterEncoding() throws Exception {
        defineWebPage( "abc/form", "<form name=\"test\" method='POST' action='/doit'>" +
                               "  <input type='text' name='text_field-name*'>" +
                               "  <input type='submit' name='apply' value='Apply'>" +
                               "</form>" );
        setResourceCharSet( "abc/form.html", "iso-8859-3", true );
        defineResource( "doit", new PseudoServlet() {
            public WebResource getPostResponse() throws IOException {
                return new WebResource( new String( getBody() ) );
            }
        } );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form.html" );
        WebForm form    = wr.getForms()[0];
        form.setParameter( "text_field-name*", "a value" );

        WebResponse response = form.submit();
        assertEquals( "posted parameters", "text_field-name*=a+value&apply=Apply", response.getText() );
    }


    public void testMailtoActionRejected() throws Exception {
        defineWebPage( "abc/form", "<form name='test' action='mailto:russgold@httpunit.org'>" +
                               "  <input type='text' name='text_field'>" +
                               "  <input type='submit' name='apply' value='Apply'>" +
                               "</form>" );

        WebResponse wr  = _wc.getResponse( getHostPath() + "/abc/form.html" );
        WebForm form    = wr.getForms()[0];
        form.setParameter( "text_field", "a value" );

        try {
            form.submit();
            fail( "Should have thrown an UnsupportedActionException" );
        } catch (UnsupportedActionException success) {
            assertTrue( "Did not include mention of bad URL type", success.getMessage().indexOf( "mailto" ) >= 0 );
        }
    }
    
    /**
     * test that the enabled/disabled state of a button is accessible
     */
    public void testEnabledDisabled() throws Exception {
    	// a web page with two checkboxes
      defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
           "<input type=\"checkbox\" id=\"checkDisabled\" name=checkDisabled>Disabled" +
           "<input type=\"checkbox\" id=\"checkEnabled\"  name=checkEnabled checked>Enabled" +
           "</form>" );
    	WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
    	String[] ids={"checkDisabled","checkEnabled"};
    	for (int i=0;i<ids.length;i++) {
    		String id=ids[i];
    		Object o=page.getElementWithID(id); 
    		if (o instanceof FormControl) {
    			FormControl box=(FormControl)o;
    			boolean result= !box.isDisabled();
    		} else {
    			throw new Exception("element with id "+id+"has invalid type "+o.getClass().getName()+" expected was FormControl");
    	 		}
     	} // for	
    }


//---------------------------------------------- private members ------------------------------------------------


    private WebConversation _wc;
}

