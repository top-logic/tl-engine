package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: WebFormTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2002, 2008 Russell Gold
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
import com.meterware.httpunit.controls.SelectionFormControl;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.xml.sax.SAXException;


/**
 * A test of the web form functionality.
 **/
public class WebFormTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( WebFormTest.class );
    }


    public WebFormTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "OneForm", "<h2>Login required</h2>" +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<input type=\"checkbox\" name=second checked>Enabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
    }


    public void testSubmitFromForm() throws Exception {
        defineWebPage( "Form", "<form method=GET id=main action = 'tryMe'>" +
                              "<Input type=text Name=name>" +
                              "<input type=\"checkbox\" name=second checked>Enabled" +
                              "</form>" );
        defineResource( "/tryMe?name=master&second=on", "You made it!" );
        WebResponse wr = _wc.getResponse( getHostPath() + "/Form.html" );
        WebForm form = wr.getFormWithID( "main" );
        form.setParameter( "name", "master" );
        form.submit();
        assertEquals( "Expected response", "You made it!", _wc.getCurrentPage().getText() );
    }


    public void testAmbiguousSubmitFromForm() throws Exception {
        defineWebPage( "Form", "<form method=GET id=main action = 'tryMe'>" +
                              "<Input type=text Name=name>" +
                              "<input type=\"checkbox\" name=second checked>Enabled" +
                              "<input type='submit' name='left'><input type='submit' name='right'>" +
                              "</form>" );
        defineResource( "/tryMe?name=master&second=on", "You made it!" );
        WebResponse wr = _wc.getResponse( getHostPath() + "/Form.html" );
        WebForm form = wr.getFormWithID( "main" );
        form.setParameter( "name", "master" );
        try {
            form.submit();
            fail( "Should have rejected request as ambiguous" );
        } catch (IllegalRequestParameterException e) {
        }
        WebResponse noButton = form.submitNoButton();
        assertEquals( "Expected response", "You made it!", noButton.getText() );
    }


    public void testSubmitFromButton() throws Exception {
        defineWebPage( "Form", "<form method=GET id=main action = 'tryMe'>" +
                              "<Input type=text Name=name>" +
                              "<input type=\"checkbox\" name=second checked>Enabled" +
                              "<input type=submit name=save value=none>" +
                              "<input type=submit name=save value=all>" +
                              "</form>" );
        defineResource( "/tryMe?name=master&second=on&save=all", "You made it!" );
        WebResponse wr = _wc.getResponse( getHostPath() + "/Form.html" );
        WebForm form = wr.getFormWithID( "main" );
        form.setParameter( "name", "master" );
        SubmitButton button = form.getSubmitButton( "save", "all" );
        button.click();
        assertEquals( "Expected response", "You made it!", _wc.getCurrentPage().getText() );
    }

    
    /**
     * test clicking on a Positional Button with a given name "update"
     * @throws Exception
     */
    public void testSubmitFromPositionalButton() throws Exception {    	
        defineResource( "ask?age=12&update=name&update.x=5&update.y=15", "You made it!", "text/plain" );
        defineWebPage( "Default", "<form id='form' method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        SubmitButton button = page.getFormWithID( "form" ).getSubmitButton( "update" );
        assertEquals("x param name",button.positionParameterName("x"),"update.x");
        assertEquals("y param name",button.positionParameterName("y"),"update.y");
        button.click( 5, 15 );
        assertEquals( "Result of click", "You made it!", _wc.getCurrentPage().getText() );
    }


    /**
     * test clicking on a unnamed Image Button
     * see also FormSubmitTest.testUnnamedImageButtonDefaultSubmit
     * @throws Exception
     */
    public void testSubmitFromUnnamedImageButton() throws Exception {
    	boolean oldAllowUnnamedImageButton=SubmitButton.isAllowUnnamedImageButton();
    	SubmitButton.setAllowUnnamedImageButton(true);
      defineResource( "ask?age=12", "Unnamed Image Button ignored!", "text/plain" );
      defineResource( "ask?age=12&x=5&y=15", "You made it!", "text/plain" );
      defineWebPage( "Default", "<form id='form' method=GET action = \"/ask\">" +
                                "<Input type=text name=age value=12>" +
                                "<Input type=image id=imageid value=name src=\"\">" +
                                "</form>" );    
      WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
      SubmitButton button = page.getFormWithID( "form" ).getSubmitButtonWithID( "imageid" );
      assertEquals("empty button name",button.getName(),"");
      assertEquals("x param name",button.positionParameterName("x"),"x");
      assertEquals("y param name",button.positionParameterName("y"),"y");
      button.click( 5, 15 );
      WebResponse response=_wc.getCurrentPage();
      URL url=response.getURL();
      // reset for other test
    	SubmitButton.setAllowUnnamedImageButton(oldAllowUnnamedImageButton);
      // System.err.println(url.getPath());      
      assertEquals( "Result of click", "You made it!", response.getText() );
    }


    public void testFindNoForm() throws Exception {
        defineWebPage( "NoForms", "This has no forms but it does" +
                                  "have <a href=\"/other.html\">an active link</A>" +
                                  " and <a name=here>an anchor</a>" );

        WebForm[] forms = _wc.getResponse( getHostPath() + "/NoForms.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 0, forms.length );
    }


    public void testFindOneForm() throws Exception {
        WebForm[] forms = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 1, forms.length );
    }


    public void testFindFormByName() throws Exception {
        defineWebPage( "Default", "<form name=oneForm method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNull( "Found nonexistent form", page.getFormWithName( "nobody" ) );
        assertNotNull( "Did not find named form", page.getFormWithName( "oneform" ) );
    }


    public void testFindFormByID() throws Exception {
        defineWebPage( "Default", "<form id=oneForm method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNull( "Found nonexistent form", page.getFormWithID( "nobody" ) );
        assertNotNull( "Did not find specified form", page.getFormWithID( "oneForm" ) );
    }


    public void testFormParameters() throws Exception {
        defineWebPage( "AForm", "<h2>Login required</h2>" +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "Enter the name 'master': <textarea Name=name>Something</textarea></B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<input type=\"checkbox\" name=second checked>Enabled" +
                                  "<input type=textbox name=third value=something>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/AForm.html" ).getForms()[0];
        String[] parameters = form.getParameterNames();
        assertNotNull( parameters );
        assertMatchingSet( "form parameter names", new String[] { "first", "name", "second", "third" }, parameters );

        assertNull( "First checkbox has a non-null value",  form.getParameterValue( "first" ) );
        assertEquals( "Second checkbox", "on", form.getParameterValue( "second" ) );
        assertNull( "Found extraneous value for unknown parameter 'magic'", form.getParameterValue( "magic" ) );
        assertTrue( "Did not find parameter 'first'", form.hasParameterNamed( "first" ) );
        assertTrue( "Did not find parameter with prefix 'sec'", form.hasParameterStartingWithPrefix( "sec" ) );
        assertTrue( "Did not find parameter with prefix 'nam'", form.hasParameterStartingWithPrefix( "nam" ) );

        assertTrue( "Did not find parameter named 'third'", form.hasParameterNamed( "third" ) );
        assertEquals( "Value of parameter with unknown type", "something", form.getParameterValue( "third" ) );

        assertEquals( "Original text area value", "Something", form.getParameterValue( "name" ) );
        form.setParameter( "name", "Something Else" );
        assertEquals( "Changed text area value", "Something Else", form.getParameterValue( "name" ) );

        form.reset();
        assertEquals( "Reset text area value", "Something", form.getParameterValue( "name" ) );
    }
    

    public void testFormRequest() throws Exception {
        WebForm form = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "master" );
        assertTrue( "Should be a post request", !(request instanceof GetMethodWebRequest) );
        assertEquals( getHostPath() + "/servlet/Login", request.getURL().toExternalForm() );
    }


    public void testHiddenParameters() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "surprise", request.getParameter( "secret" ) );
        assertEquals( "nothing", request.getParameter( "typeless" ) );
        form.setParameter( "secret", "surprise" );
        assertEquals( "surprise", request.getParameter( "secret" ) );

        try {
            form.setParameter( "secret", "illegal" );
            fail( "Should have rejected change to hidden parameter 'secret'" );
        } catch (IllegalRequestParameterException e) {
        }

        assertEquals( "surprise", request.getParameter( "secret" ) );
    }


    /**
     * test Null textValues
     */ 
    public void testNullTextValues() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=>" +
                                  "<br><Input name=typeless value=>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "", request.getParameter( "secret" ) );
        assertEquals( "", request.getParameter( "typeless" ) );
    }

    /**
     * [ httpunit-Bugs-1954311 ] Set the value of a text area. Currently fails
     * if the textarea is empty to begin with.  
     * by m0smith
     * 
     * @throws Exception on failure
     */
    public void testTextArea() throws Exception {
        String fieldName = "comments";
        String comment = "My what a lovely dress that is";
        // Setting defaultValue to something other than an empty string makes
        // this test case pass.
        String defaultValue = "";

        defineWebPage("Default", "<form method=POST action = \"/servlet/Login\">" + "<textarea name='" + fieldName
                + "' row='10' cols='20'>" + defaultValue + "</textarea>" +
                	"<br><Input type=submit value = \"Submit\">"
                + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() +"/Default.html");
        WebForm form = page.getForms()[0];
        form.setParameter(fieldName, comment);  // THIS LINE FAILS

        assertEquals(1, form.getParameterNames().length);

        WebRequest request = form.getRequest();
        assertEquals(comment, request.getParameter(fieldName));

    }

    public void testTableForm() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<table summary=\"\"><tr><td>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "</td><td><Input type=Radio name=sex value=male>Masculine" +
                                  "</td><td><Input type=Radio name=sex value=female checked>Feminine" +
                                  "</td><td><Input type=Radio name=sex value=neuter>Neither" +
                                  "<Input type=submit value = \"Log in\"></tr></table>" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertMatchingSet( "parameter names", new String[] { "name","sex" }, parameterNames );
        assertEquals( "Default name", "", form.getParameterValue( "name" ) );
        assertEquals( "Default sex", "female", form.getParameterValue( "sex" ) );

        form.setParameter( "sex", "neuter" );
        assertEquals( "New value for sex", "neuter", form.getParameterValue( "sex" ) );

        try {
            form.setParameter( "sex", "illegal" );
            fail( "Should have rejected change to radio parameter 'sex'" );
        } catch (IllegalRequestParameterException e) {
        }
        assertEquals( "Preserved value for sex", "neuter", form.getParameterValue( "sex" ) );

        form.reset();
        assertEquals( "Reverted value", "female", form.getParameterValue( "sex" ) );
    }

    /**
     * test Select HTML Element 
     * @throws Exception
     */
    public void testSelect() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Select id='select1' name=color><Option>blue<Option selected>red \n" +
                                  "<Option>green</select>" +
                                  "<TextArea name=\"text\">Sample text</TextArea>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "Default color", "red", form.getParameterValue( "color" ) );
        assertEquals( "Default text",  "Sample text", form.getParameterValue( "text" ) );
        WebRequest request = form.getRequest();
        assertEquals( "Submitted color", "red", request.getParameter( "color" ) );
        assertEquals( "Submitted text",  "Sample text", request.getParameter( "text" ) );

        form.setParameter( "color", "green" );
        assertEquals( "New select value", "green", form.getParameterValue( "color" ) );

        try {
            form.setParameter( "color", new String[] { "green", "red" } );
            fail( "Should have rejected set with multiple values" );
        } catch (IllegalRequestParameterException e) {
        	assertEquals("exception should read ","Attempted to assign to parameter 'color' the extraneous value 'red'.",e.getMessage());
        }

        form.setParameter( "color", "green" );
        assertEquals( "Pre-reset color", "green", form.getParameterValue( "color" ) );
        form.reset();
        assertEquals( "Reverted color", "red", form.getParameterValue( "color" ) );
    }
    


    public void testSizedSelect() throws Exception {
        defineWebPage( "Default", "<form method=POST action = '/servlet/Login'>" +
                                  "<Select name=poems><Option>limerick<Option>haiku</select>" +
                                  "<Select name=songs size=2><Option>aria<Option>folk</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        assertEquals( "Default poem", "limerick", form.getParameterValue( "poems" ) );
        assertNull( "Default song should be null",  form.getParameterValue( "songs" ) );
    }


    public void testSingleSelectParameterOrdering() throws Exception {
        StringBuffer sb = new StringBuffer( "<form action='sendIt' id='theform'>" );
        for (int i= 0; i < 4; i++) {
            sb.append( "<select name='enabled'><option value='true'>Enabled<option value='false' selected>Disabled</select>" );
        }
        sb.append( "</form>" );

        defineWebPage( "OnCommand", sb.toString() );

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithID( "theform" );
        form.setParameter( "enabled", new String[] { "true", "false", "false", "true" } );
        WebRequest request = form.getRequest();
        assertEquals( "request", getHostPath() + "/sendIt?enabled=true&enabled=false&enabled=false&enabled=true", request.getURL().toExternalForm() );
    }


    /**
     * testMultiSelect
     * should fit to bug report [ 1060291 ] setting multiple values in selection list
     * by Vladimir
     * @throws Exception
     */
    public void testMultiSelect() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select multiple size=4 name=colors>" +
                                  "<Option>blue<Option selected>red \n" +
                                  "<Option>green<Option value=\"pink\" selected>salmon</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "num parameters", 1, parameterNames.length );
        assertEquals( "parameter name", "colors", parameterNames[0] );
        assertTrue( "Found extraneous values for unknown parameter 'magic'", form.getParameterValues( "magic" ).length == 0 );
        assertMatchingSet( "Select defaults", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
        assertMatchingSet( "Select options", new String[] { "blue", "red", "green", "salmon" }, form.getOptions( "colors" ) );
        assertEquals( "Select values", new String[] { "blue", "red", "green", "pink" }, form.getOptionValues( "colors" ) );
        WebRequest request = form.getRequest();
        assertMatchingSet( "Request defaults", new String[] { "red", "pink" }, request.getParameterValues( "colors" ) );
        assertEquals( "URL", getHostPath() + "/ask?colors=red&colors=pink", request.getURL().toExternalForm() );


        form.setParameter( "colors", "green" );
        assertEquals( "New select value", new String[] { "green" }, form.getParameterValues( "colors" ) );
        form.setParameter( "colors", new String[] { "blue", "pink" } );
        assertEquals( "New select value", new String[] { "blue", "pink" }, form.getParameterValues( "colors" ) );

        try {
            form.setParameter( "colors", new String[] { "red", "colors" } );
            fail( "Should have rejected set with bad values" );
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet( "Reverted colors", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
    }


    public void testUnspecifiedDefaults() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select name=colors><Option>blue<Option>red</Select>" +
                                  "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                  "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
         assertEquals( "inferred color default", "blue", form.getParameterValue( "colors" ) );
        assertEquals( "inferred fish default", "red", form.getParameterValue( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], form.getParameterValues( "media" ) );

        WebRequest request = form.getRequest();
        assertEquals( "inferred color request", "blue", request.getParameter( "colors" ) );
        assertEquals( "inferred fish request",  "red", request.getParameter( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], request.getParameterValues( "media" ) );
    }


    public void testCheckboxControls() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=checkbox name=ready value=yes checked>" +
                                  "<Input type=checkbox name=color value=red checked>" +
                                  "<Input type=checkbox name=color value=blue checked>" +
                                  "<Input type=checkbox name=gender value=male checked>" +
                                  "<Input type=checkbox name=gender value=female>" +
                                  "<Input type=submit></form>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNotNull( response.getForms() );
        assertEquals( "Num forms in page", 1, response.getForms().length );
        WebForm form = response.getForms()[0];
        assertEquals( "ready state", "yes", form.getParameterValue( "ready" ) );
        assertMatchingSet( "default genders allowed", new String[] { "male" }, form.getParameterValues( "gender" ) );
        assertMatchingSet( "default colors", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );

        form.setParameter( "color", "red" );
        assertMatchingSet( "modified colors", new String[] { "red" }, form.getParameterValues( "color" ) );
        try {
            form.setParameter( "color", new String[] { "red", "purple" } );
            fail( "Should have rejected set with bad values" );
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet( "reverted colors", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );
    }


    public void testGetWithQueryString() throws Exception {
        defineResource( "QueryForm.html",
                        "<html><head></head>" +
                        "<form method=GET action=\"SayHello?speed=fast\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        defineResource( "SayHello?speed=fast&name=me", new PseudoServlet() {
            public WebResource getGetResponse() {
                WebResource result = new WebResource( "<html><body><table><tr><td>Hello, there" +
                                                      "</td></tr></table></body></html>" );
                return result;
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/QueryForm.html" );
        WebForm form = formPage.getForms()[0];
        form.setParameter( "name", "me" );
        WebRequest request = form.getRequest();
        assertEquals( "Request URL", getHostPath() + "/SayHello?speed=fast&name=me", request.getURL().toExternalForm() );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, there", cells[0][0] );
    }


    public void testPostWithQueryString() throws Exception {
        defineResource( "QueryForm.html",
                        "<html><head></head>" +
                        "<form method=POST action=\"SayHello?speed=fast\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        defineResource( "SayHello?speed=fast", new PseudoServlet() {
            public WebResource getPostResponse() {
                WebResource result = new WebResource( "<html><body><table><tr><td>Hello, there" +
                                                      "</td></tr></table></body></html>" );
                return result;
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/QueryForm.html" );
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "Charlie" );
        assertEquals( "Request URL", getHostPath() + "/SayHello?speed=fast", request.getURL().toExternalForm() );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, there", cells[0][0] );
    }


    public void testPostWithEmbeddedSpace() throws Exception {
        String sessionID = "/ID=03.019c010101010001.00000001.a202000000000019. 0d09";
        defineResource( "login", "redirectoring", HttpURLConnection.HTTP_MOVED_PERM );
        super.addResourceHeader( "login", "Location: " + getHostPath() + sessionID + "/login" );
        defineResource( sessionID + "/login",
                        "<html><head></head>" +
                        "<form method=POST action='SayHello'>" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        defineResource( sessionID + "/SayHello", new PseudoServlet() {
            public WebResource getPostResponse() {
                return new WebResource( "<html><body><table><tr><td>Hello, there</td></tr></table></body></html>" );
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/login" );
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "Charlie" );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, there", cells[0][0] );
    }


    private WebConversation _wc;
}
