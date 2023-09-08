package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: FormParametersTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2004, 2007, Russell Gold
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
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

import com.meterware.httpunit.FormParameter.UnusedParameterValueException;
import com.meterware.httpunit.FormParameter.UnusedUploadFileException;
import com.meterware.httpunit.WebForm.InvalidFileParameterException;
import com.meterware.httpunit.WebForm.NoSuchParameterException;
import com.meterware.httpunit.controls.IllegalParameterValueException;
import com.meterware.httpunit.protocol.UploadFileSpec;


/**
 * A test of the parameter validation functionality.
 **/
public class FormParametersTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( FormParametersTest.class );
    }


    public FormParametersTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();
    }


    public void testChoiceParameterValidationBypassDeprecated() throws Exception {
        HttpUnitOptions.setParameterValuesValidated( false );
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Select name=colors><Option>blue<Option>red</Select>" +
                                       "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                       "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                       "<Input type=submit name=submit value=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "noSuchControl", "green" );
        request.setParameter( "colors", "green" );
        request.setParameter( "fish", "purple" );
        request.setParameter( "media", "CDRom" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
    }


    public void testChoiceParameterValidationBypass() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Select name=colors><Option>blue<Option>red</Select>" +
                                       "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                       "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                       "<Input type=submit name=submit value=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].newUnvalidatedRequest();
        request.setParameter( "noSuchControl", "green" );
        request.setParameter( "colors", "green" );
        request.setParameter( "fish", "purple" );
        request.setParameter( "media", "CDRom" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
    }


    public void testChoiceParameterValidation() throws Exception {
      defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
          "<Select name=colors><Option>blue<Option>red</Select>" +
          "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
          "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
          "<Input type=submit></form>" );
			WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
			WebRequest request = page.getForms()[0].getRequest();
			validateSetParameterRejected( request, "noSuchControl", "green", "setting of non-existent control" );
			validateSetParameterRejected( request, "colors", "green", "setting of undefined value" );
			validateSetParameterRejected( request, "fish", "snapper", "setting of display value" );
			validateSetParameterRejected( request, "media", "CDRom", "setting list to illegal value" );
			validateSetParameterRejected( request, "colors", new String[] { "blue", "red" }, "setting multiple values on choice" );
			validateSetParameterRejected( request, "media", new String[] { "TV", "CDRom" }, "setting one bad value in a group" );
			
			request.setParameter( "colors", "blue" );
			request.setParameter( "fish", "red" );
			request.setParameter( "media", "TV" );
			request.setParameter( "colors", new String[] { "blue" } );
			request.setParameter( "fish", new String[] { "red" } );
			request.setParameter( "media", new String[] { "TV", "Radio" } );   
    }
    
    /**
     * test for bug [ 1215734 ] another <select> problem
     * by alex
     * @throws Exception
     */
    public void testChoiceParameterBug1215734() throws Exception {
     	String formFromBugReport=
    		"<form id='form1' class='form' method='post'\n"+
    		"action='/SanityJSFWebApp/faces/Page1.jsp;jsessionid=5210f4ac722ad98199c2498\n"+
    		"69de0'\n"+
    		"enctype='application/x-www-form-urlencoded'>\n"+
    		"<select id='form1:dropdown1' name='form1:dropdown1'\n"+
    		"size='1' style='left: 264px; top: 168px; position:\n"+
    		"absolute'> <option value='Able, Tony'>Able, Tony</option>\n"+
    		"<option value='Black, John'>Black, John</option>\n"+
    		"<option value='Kent, Richard'>Kent, Richard</option>\n"+
    		"<option value='Chen, Larry'>Chen, Larry</option>\n"+
    		"<option value='Donaldson, Sue'>Donaldson, Sue</option>\n"+
    		"</select><input id='form1:button1' type='submit'\n"+
    		"name='form1:button1' value='Go' style='left: 168px;\n"+
    		"top: 168px; position: absolute' />\n"+
    		"<input id='form1_hidden' name='form1_hidden'\n"+
    		"value='form1_hidden' type='hidden' />\n"+
    		"</form>";
        defineWebPage( "Default", formFromBugReport);
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        try {
        	request.setParameter("form1:dropdown1","Kent,\tRichard");
        	fail("a tab is not a space ... or?");
        } catch (IllegalParameterValueException ipve) {
        	// fine
        }
        request.setParameter("form1:dropdown1","Kent, Richard");
    }

    /**
     * test for bug Report [ 1122186 ] Duplicate select with same name cause error
     * by Serge Knystautas
     */
    public void testDuplicateSelect() throws Exception {
    	String html="  </head>\n"+
    	"  <body>\n"+
    	"  <form name='form1' action='test.php' onsubmit='showpost()'>\n"+
    	"		<select id='select1' name='selects'>\n"+
    	"		<option value=''>Pick one</option>\n"+
    	"		<option value='123'>Foobar</option>\n"+
    	"		<option value='345'>bar</option>\n"+
    	"		</select>\n"+
    	"		\n"+
    	"		<select id='select2' name='selects'>\n"+
    	"		<option value=''>Pick one</option>\n"+
    	"		<option value='123'>Foobar</option>\n"+
    	"		<option value='345'>bar</option>\n"+
    	"		</select>\n"+
    	"		\n"+
    	"		<select id='select3' name='selects'>\n"+
    	"		<option value=''>Pick one</option>\n"+
    	"		<option value='123'>Foobar</option>\n"+
    	"		<option value='345'>bar</option>\n"+
    	"		</select>\n"+
    	"		<br/>\n"+
    	"		<input type='submit' name='go' value='go'>\n"+
    	"  </form>\n"+
    	"  </body>\n"+
    	"</html>\n";
      defineWebPage( "Default", html);
      WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
      WebForm webform=page.getForms()[0];
      try {
      	webform.setParameter("selects", "123");
      } catch (IllegalParameterValueException ipve) {
      	//com.meterware.httpunit.controls.IllegalParameterValueException: May not set parameter 'selects' to 'unknown bad value'. Value must be one of: { '', '123', '345' }
      	assertTrue("unknown bad value expected",ipve.getMessage().indexOf("unknown bad value")>=0);
      }
    }

    public void testTextParameterValidationBypass() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=secret>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].newUnvalidatedRequest();
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "secret", "value" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
        request.setParameter( "secret", new String[] { "red", "pink" } );
    }


    public void testTextParameterValidationBypassDeprecated() throws Exception {
        HttpUnitOptions.setParameterValuesValidated( false );
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=secret>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "secret", "value" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
        request.setParameter( "secret", new String[] { "red", "pink" } );
    }


    public void testTextParameterValidation() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=secret value=value>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "secret", "value" );
        validateSetParameterRejected( request, "colors", new String[] { "blue", "red" }, "setting input to multiple values" );
        validateSetParameterRejected( request, "password", new String[] { "red", "pink" }, "setting password to multiple values" );
        validateSetParameterRejected( request, "secret", new String[] { "red", "pink" }, "setting hidden field to multiple values" );
    }


    public void testHiddenParameters() throws Exception {
        defineWebPage( "Default", "<form method=GET action = '/ask'>" +
                                  "<Input type=text name=open value=value>" +
                                  "<Input type=hidden name=secret value=value>" +
                                  "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        final WebForm form = page.getForms()[0];
        assertFalse( "Should not call 'open' hidden", form.isHiddenParameter( "open") );
        assertTrue( "Should have called 'secret' hidden", form.isHiddenParameter( "secret") );

        WebRequest request = form.getRequest();
        validateSetParameterRejected( request, "secret", new String[] { "red" }, "setting hidden field to wrong value" );

        form.getScriptableObject().setParameterValue( "secret", "new" );
        assertEquals( "New hidden value", "new", form.getParameterValue( "secret" ) );
    }


    public void testUnknownParameter() throws Exception {
        defineWebPage( "Default", "<form method=GET action = '/ask'>" +
                                  "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        try {
            request.setParameter( "secret", "zork" );
            fail( "Should have rejected set of unknown parameter" );
        } catch (WebForm.NoSuchParameterException e) {
        }
    }
    
    /**
     * check that an UnusedParameterValueException is thrown if a parameter value is not supplied
     * @throws Exception
     */
    public void testUnusedParameterValue() throws Exception {
      defineWebPage( "Default", "<form method=GET action = '/ask'>" +
      													"<Select name=colors><Option>blue<Option>red</Select>" +
      													"<Input type=submit></form>" );
      WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
      WebRequest request = page.getForms()[0].getRequest();
      try {
        request.setParameter( "colors", new String[] { "blue", "red" } );
      	fail( "Should have rejected set of unused parameter value" );
      } catch (FormParameter.UnusedParameterValueException e) {
      	// System.err.println(e.getMessage());
      }   	
    }

    /**
     * check that an UnusedParameterValueException is not
     * thrown if a parameter value is not supplied
     * See BR 1843978
     *   also BR 1449658 
     * @throws Exception
     */
    public void testBug1843978() throws Exception {
      defineWebPage( "http://www.w3.org/1999/xhtml", "Default", "<form method=GET action = '/ask'>" +
      													"<Select name=colors><Option>blue<Option>red</Select>" +
      													"<Input type=submit></form>" );
      WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
      // System.err.println(page.getText());
      WebRequest request = page.getForms()[0].getRequest();
      try {
        request.setParameter( "colors", "blue"  );
      } catch (FormParameter.UnusedParameterValueException e) {
      	fail( "Should not have rejected set of unused parameter value" );
      	// System.err.println(e.getMessage());
      }   	
    }

     public void testMultipleTextParameterValidation() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=color value='green'>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();

        assertEquals( "Number of parameters named 'password'", 1, form.getNumTextParameters( "password" ) );
        assertEquals( "Number of parameters named 'color'", 2, form.getNumTextParameters( "color" ) );
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "color", new String[] { "red", "green" } );
        validateSetParameterRejected( request, "colors", new String[] { "blue", "red", "green" }, "setting input to multiple values" );
        validateSetParameterRejected( request, "password", new String[] { "red", "pink" }, "setting password to multiple values" );
    }


    public void testRadioButtonValidationBypass() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=radio name=color value=red>" +
                                       "<Input type=radio name=color value=blue>" +
                                       "<Input type=radio name=color value=green>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].newUnvalidatedRequest();
        request.setParameter( "color", "black" );
        request.setParameter( "color", new String[] { "blue", "red" } );
    }


    public void testRadioButtonValidationBypassDeprecated() throws Exception {
        HttpUnitOptions.setParameterValuesValidated( false );
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=radio name=color value=red>" +
                                       "<Input type=radio name=color value=blue>" +
                                       "<Input type=radio name=color value=green>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "color", "black" );
        request.setParameter( "color", new String[] { "blue", "red" } );
    }


    public void testRadioButtonValidation() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=radio name=color value=red>Crimson" +
                                       "<Input type=radio name=color value=blue>Aquamarine" +
                                       "<Input type=radio name=color value=green>Chartreuse" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        assertEquals( "color options", new String[] { "red", "blue", "green" }, page.getForms()[0].getOptionValues( "color" ) );
        assertEquals( "color names", new String[] { "Crimson", "Aquamarine", "Chartreuse" }, page.getForms()[0].getOptions( "color" ) );
        request.setParameter( "color", "red" );
        request.setParameter( "color", "blue" );
        validateSetParameterRejected( request, "color", "black", "setting radio buttons to unknown value" );
        validateSetParameterRejected( request, "color", new String[] { "blue", "red" }, "setting radio buttons to multiple values" );
    }


    public void testCheckboxValidationBypassDeprecated() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=checkbox name=use_color>" +
                                       "<Input type=checkbox name=color value=red>" +
                                       "<Input type=checkbox name=color value=blue>" +
                                       "<Input type=submit></form>" );
        HttpUnitOptions.setParameterValuesValidated( false );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "use_color", "red" );
        request.setParameter( "color", "green" );
    }


    public void testCheckboxValidationBypass() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input type=checkbox name=use_color>" +
                                       "<Input type=checkbox name=color value=red>" +
                                       "<Input type=checkbox name=color value=blue>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].newUnvalidatedRequest();
        request.setParameter( "use_color", "red" );
        request.setParameter( "color", "green" );
    }


    public void testCheckboxValidation() throws Exception {
        defineWebPage( "Default", "<form method=GET action = 'ask?color='>" +
                                       "<Input type=checkbox name=use_color>" +
                                       "<Input type=checkbox name=color value=red>Scarlet" +
                                       "<Input type=checkbox name=color value=blue>Turquoise" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertEquals( "Color values", new String[] { "Scarlet", "Turquoise" }, page.getForms()[0].getOptions( "color" ) );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "use_color", "on" );
        request.removeParameter( "use_color" );
        validateSetParameterRejected( request, "use_color", "red", "setting checkbox to a string value" );
        request.setParameter( "color", "red" );
        request.setParameter( "color", new String[] { "red", "blue" } );
        validateSetParameterRejected( request, "color", "on", "setting checkbox to an incorrect value" );
        validateSetParameterRejected( request, "color", new String[] { "green", "red" }, "setting checkbox to an incorrect value" );
    }


    public void testCheckboxShortcuts() throws Exception {
        defineWebPage( "Default", "<form method=GET id='boxes'>" +
                                       "<Input type=checkbox name=use_color>" +
                                       "<Input type=checkbox name=running value=fast>" +
                                       "<Input type=checkbox name=color value=red>Scarlet" +
                                       "<Input type=checkbox name=color value=blue>Turquoise" +
                                       "<Input type=text name=fish>" +
                                       "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getFormWithID( "boxes" );
        form.toggleCheckbox( "use_color" );
        assertEquals( "'use_color' checkbox after toggle", "on", form.getParameterValue( "use_color" ) );
        form.setCheckbox( "use_color", false );
        assertEquals( "'use_color' checkbox after set-false", null, form.getParameterValue( "use_color" ) );
        form.toggleCheckbox( "running" );
        assertEquals( "'running' checkbox after toggle", "fast", form.getParameterValue( "running" ) );

        try {
            form.setCheckbox( "color", true );
            fail( "Did not forbid setting checkbox with multiple values" );
        } catch (IllegalRequestParameterException e) {
        }

        try {
            form.toggleCheckbox( "fish" );
            fail( "Did not forbid toggling non-checkbox parameter" );
        } catch (IllegalRequestParameterException e) {
        }

        form.toggleCheckbox( "color", "red" );
        assertMatchingSet( "color checkboxes", new String[] { "red" }, form.getParameterValues( "color" ) );
        form.setCheckbox( "color", "blue", true );
        assertMatchingSet( "color checkboxes", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );

        try {
            form.setCheckbox( "color", "green", true );
            fail( "Did not forbid setting checkbox with unknown value" );
        } catch (IllegalRequestParameterException e) {
        }

    }


    public void testReadOnlyControls() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                       "<Input readonly type=checkbox name=color value=red checked>" +
                                       "<Input type=checkbox name=color value=blue>" +
                                       "<Input type=radio name=species value=hippo readonly>" +
                                       "<Input type=radio name=species value=kangaroo checked>" +
                                       "<Input type=radio name=species value=lemur>" +
                                       "<textarea name='big' readonly rows=2 cols=40>stop me</textarea>" +
                                       "<Input type=text name=age value=12 readonly value='12'></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = page.getForms()[0].getRequest();

        assertFalse( "'color' incorrectly reported as read-only", form.isReadOnlyParameter( "color" ) );
        assertFalse( "'species' incorrectly reported as read-only", form.isReadOnlyParameter( "species" ) );
        assertTrue( "'big' should be reported as read-only", form.isReadOnlyParameter( "big" ) );
        assertTrue( "'age' should be reported as read-only", form.isReadOnlyParameter( "age" ) );

        assertMatchingSet( "selected color", new String[] { "red" }, form.getParameterValues( "color" ) );
        assertEquals( "selected animal", "kangaroo", form.getParameterValue( "species" ) );
        assertEquals( "age", "12", form.getParameterValue( "age" ) );

        assertMatchingSet( "color choices", new String[] { "red", "blue" }, form.getOptionValues( "color" ) );
        assertMatchingSet( "species choices", new String[] { "kangaroo", "lemur" }, form.getOptionValues( "species" ) );

        validateSetParameterRejected( request, "color", "blue", "unchecking 'red'" );
        validateSetParameterRejected( request, "color", new String[] { "blue" }, "unchecking 'red'" );
        validateSetParameterRejected( request, "species", "hippo", "selecting 'hippo'" );
        validateSetParameterRejected( request, "age", "15", "changing a read-only text parameter value" );
        validateSetParameterRejected( request, "big", "go-go", "changing a read-only textarea parameter value" );

        request.setParameter( "color", "red" );
        request.setParameter( "color", new String[] { "red", "blue" } );
        request.setParameter( "species", "lemur" );
        request.setParameter( "age", "12" );
        request.setParameter( "big", "stop me" );
    }


    /**
     * test disabled controls
     * @throws Exception
     */
    public void testDisabledControls() throws Exception {
        defineWebPage( "Default", "<form method=GET action = '/ask'>" +
                                       "<Input disabled type=checkbox name=color value=red checked>" +
                                       "<Input type=checkbox name=color value=blue>" +
                                       "<Input type=radio name=species value=hippo disabled>" +
                                       "<Input type=radio name=species value=kangaroo checked>" +
                                       "<Input type=radio name=species value=lemur>" +
                                       "<textarea name='big' disabled rows=2 cols=40>stop me</textarea>" +
                                       "<Input type=text name=age value=12 disabled value='12'></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = page.getForms()[0].getRequest();
        assertEquals( "Expected request URL", getHostPath() + "/ask?species=kangaroo", request.getURL().toExternalForm() );

        assertFalse( "'color' incorrectly reported as disabled", form.isDisabledParameter( "color" ) );
        assertFalse( "'species' incorrectly reported as disabled", form.isDisabledParameter( "species" ) );
        assertTrue( "'big' should be reported as disabled", form.isDisabledParameter( "big" ) );
        assertTrue( "'age' should be reported as disabled", form.isDisabledParameter( "age" ) );

        assertMatchingSet( "selected color", new String[] { "red" }, form.getParameterValues( "color" ) );
        assertEquals( "selected animal", "kangaroo", form.getParameterValue( "species" ) );
        assertEquals( "age", "12", form.getParameterValue( "age" ) );

        assertMatchingSet( "color choices", new String[] { "red", "blue" }, form.getOptionValues( "color" ) );
        assertMatchingSet( "species choices", new String[] { "kangaroo", "lemur" }, form.getOptionValues( "species" ) );

        validateSetParameterRejected( request, "color", "blue", "unchecking 'red'" );
        validateSetParameterRejected( request, "color", new String[] { "blue" }, "unchecking 'red'" );
        validateSetParameterRejected( request, "species", "hippo", "selecting 'hippo'" );
        validateSetParameterRejected( request, "age", "15", "changing a read-only text parameter value" );
        validateSetParameterRejected( request, "big", "go-go", "changing a read-only textarea parameter value" );

        request.setParameter( "color", "red" );
        request.setParameter( "color", new String[] { "red", "blue" } );
        request.setParameter( "species", "lemur" );
        request.setParameter( "age", "12" );
        request.setParameter( "big", "stop me" );
    }


    public void testFileParameterValue() throws Exception {
        defineWebPage( "Default", "<form method=POST action='/ask'>" +
                                  "<Input type=file name=File>" +
                                  "<Input type=submit value=Upload></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        String[] values = form.getParameterValues( "File" );
        assertEquals( "Number of file parameter values", 1, values.length );
        assertEquals( "Default selected filename", "", values[0] );

        final File file = new File( "dummy.txt" );
        form.setParameter( "File", new UploadFileSpec[] { new UploadFileSpec( file ) } );
        assertEquals( "Selected filename", file.getAbsolutePath(), form.getParameterValue( "File" ) );

        form.setParameter( "File", file );

        WebRequest wr = form.getRequest();
        assertEquals( "File from validated request", file.getAbsolutePath(), wr.getParameterValues( "File" )[0] );

        wr = form.newUnvalidatedRequest();
        assertEquals( "File from unvalidated request", file.getAbsolutePath(), wr.getParameterValues( "File" )[0] );
    }
    
    /**
     * test for bug report [ 1510495 ] getParameterValue on a submit button fails
     * by Julien HENRY
     * @throws Exception
     */
    public void testSubmitButtonParameterValue() throws Exception {
    	defineResource("/someaction?submitButton=buttonLabel","submitted");
    	String html="<form name='checkit' method=GET action='someaction'>"+
    							"<input type='submit' name='submitButton' value='buttonLabel' />"+
    	            "</form>";
    	defineWebPage("checkit",html);
   	 	WebResponse resp= _wc.getResponse( getHostPath() + "/checkit.html" );    
   	  WebForm form = resp.getFormWithName("checkit");
   	  form.submit();
   	  String paramValue=form.getParameterValue("submitButton");
   	  assertTrue("the parameter value should be buttonLabel",paramValue.equals("buttonLabel"));
    }

    
    /**
     * test for bug report
     * [ 1510582 ] setParameter fails with <input type="file">
     * by Julien HENRY
     */
    public void testUnusedParameterExceptionForFileParamWithStringValue() throws Exception {
   	 	defineWebPage( "Default", "<form method=POST action='/ask'>" +
         "<Input type=file name=\"file1\">" +
         "<Input type=file name=\"file2\">" +
         "<Input type=submit value=Upload></form>" );
  	 	WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );    	 	
  	 	WebForm form = page.getForms()[0];
  	 	try { 
  	 		// this works with no exception
  	 		form.setParameter("file1", new File("/tmp/test.txt"));
  	 		// this doesn't 
  	 		form.setParameter("file2", "/tmp/test.txt");
  	 		fail("There should have been an exception");
  	 	} catch (UnusedParameterValueException upe) {
  	 		String msg=upe.getMessage();
  	 		// this is the pre 1.7 behaviour
  	 		String expected="Attempted to assign to parameter 'file2' the extraneous value '/tmp/test.txt'.";
  	 		System.err.println(msg);
  	 		assertTrue("exception message is not as expected",msg.equals(expected));
  	 		fail("in 1.7 bug 1510582 should be fixed");
  	 	} catch (InvalidFileParameterException ipe) {
  	 		String msg=ipe.getMessage();
  	 		String expected="The file parameter with the name 'file2' must have type File but the string values '/tmp/test.txt' where supplied";
  	 		// System.err.println(msg);
  	 		assertTrue("InvalidFileParameterException message is not as expected",msg.equals(expected));
  	 	}
    	
    }
    /**
     * test for bug report [ 1390695 ] bad error message
     * by Martin Olsson 
     */
    public void testUnusedUploadFileException() throws Exception {
  	 defineWebPage( "Default", "<form method=POST action='/ask'>" +
         "<Input type=file name=correct_field_name>" +
         "<Input type=submit value=Upload></form>" );
  	 	WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );    	 	
  	 	WebForm form = page.getForms()[0];
  	 	try { 
  	 	  // purposely try to set a non existing file name
  	 	 form.setParameter("wrong_field_name", new File("exists.txt"));
  	 	 fail("There should have been an exception");
  	 	} catch (NoSuchParameterException npe) {
  	 		String msg=npe.getMessage();
  	 		String expected="No parameter named 'wrong_field_name' is defined in the form";
  	 		assertTrue(msg.equals(expected));
  	 	} catch (UnusedUploadFileException ufe) {
  	 		String msg=ufe.getMessage();
  	 		// this is the pre 1.7 behaviour
  	 		String expected="Attempted to upload 1 files using parameter 'null' which is not a file parameter.";
  	 		assertTrue(msg.equals(expected));
  	 		System.err.println(msg);
  	 		fail("in 1.7 bug 1390695 should be fixed");
  	 	}
    }
    	

    /**
     * test for BugReport 1937946 (different result on Mac than on other platforms
     * @throws Exception
     * to activate test download 
     * https://sourceforge.net/tracker/download.php?group_id=6550&atid=106550&file_id=274135&aid=1937946
     * and copy as index.html (or whatever - change url if necessary) to local host
     * tested with real browser so deactivated for HttpUnitSuite
     */
  	public void xtestBugReport1937946Mac() throws Exception {
    	String url = "http://localhost/index.html";
  		WebConversation conversation = new WebConversation();
  		WebRequest      request = new GetMethodWebRequest( url );
  		WebResponse 	response = conversation.getResponse( request );
  		
  		HttpUnitOptions.setExceptionsThrownOnScriptError( false );
  		
  		assertNotNull( "Kein Response von URL '" + url + "'.", response );
  	        System.out.println( "\nResponse von URL '" + url + "'." );
  	    
  	        WebForm form = response.getFormWithID( "suchen" );
  	        String param[] = form.getParameterNames();
  	  
  	  for (int i = 0; i < param.length; i++) {
  			System.err.println(param[i]);
  		}
      assertTrue("expecting 5 params but found "+param.length,param.length==5);
  	}
  

//---------------------------------------------- private members ------------------------------------------------


    private WebConversation _wc;


    private void validateSetParameterRejected( WebRequest request, String parameterName, String value, String comment ) throws Exception {
        try {
            request.setParameter( parameterName, value );
            fail( "Did not forbid " + comment );
        } catch (IllegalRequestParameterException e) {
        }
    }


    private void validateSetParameterRejected( WebRequest request, String parameterName, String[] values, String comment ) throws Exception {
        try {
            request.setParameter( parameterName, values );
            fail( "Did not forbid " + comment );
        } catch (IllegalRequestParameterException e) {
        }
    }
}
