import com.meterware.httpunit.*;

import junit.framework.*;


/**
 * An example of testing servlets using httpunit and JUnit.
 **/
public class ExampleTest extends TestCase {


	/**
	 * run this testcase as a suite from the command line
	 * @param args - ignored
	 */
	public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
	}
	
	
	/**
	 * supply this test cases as part of a suite
	 * @return
	 */
	public static Test suite() {
		return new TestSuite( ExampleTest.class );
	}


	/**
	 * constructor with name parameter needed for suite creation
	 * @param name
	 */
	public ExampleTest( String name ) {
	    super( name );
	}

	/**
	 * try getting a response for the given Conversation and Request
	 * show an error message if a 404 error appears
	 * @param conversation - the conversation to use
	 * @param request
	 * @return the response
	 * @throws an Exception if getting the response fails
	 */
	public WebResponse tryGetResponse(WebConversation conversation,WebRequest request) throws Exception {
		WebResponse response=null;
		try {
			response = conversation.getResponse( request );
		} catch (HttpNotFoundException nfe) {
			System.err.println("The URL '"+request.getURL()+"' is not active any more");
			throw nfe;
		}
		return response;
	}

  /**
   * Verifies that the welcome page has exactly one form, with the single parameter, "name"
   **/
  public void testWelcomePage() throws Exception {
      WebConversation     conversation = new WebConversation();
      WebRequest request = new GetMethodWebRequest( "http://www.meterware.com/servlet/TopSecret" );
     	WebResponse response = tryGetResponse(conversation, request );
      
      WebForm forms[] = response.getForms();
      assertEquals( 1, forms.length );
      assertEquals( 1, forms[0].getParameterNames().length );
      assertEquals( "name", forms[0].getParameterNames()[0] );
  }


  /**
   * Verifies that submitting the login form without entering a name results in a page
   * containing the text "Login failed"
   **/
  public void testBadLogin() throws Exception {
      WebConversation     conversation = new WebConversation();
      WebRequest  request = new GetMethodWebRequest( "http://www.meterware.com/servlet/TopSecret" );

     	WebResponse response = tryGetResponse(conversation, request );
      WebForm loginForm = response.getForms()[0];
      request = loginForm.getRequest();
      response = conversation.getResponse( request );
      assertTrue( "Login not rejected", response.getText().indexOf( "Login failed" ) != -1 );
  }


  /**
   * Verifies that submitting the login form with the name "master" results
   * in a page containing the text "Top Secret"
   **/
  public void testGoodLogin() throws Exception {
      WebConversation     conversation = new WebConversation();
      WebRequest  request = new GetMethodWebRequest( "http://www.meterware.com/servlet/TopSecret" );

     	WebResponse response = tryGetResponse(conversation, request );
      WebForm loginForm = response.getForms()[0];
      request = loginForm.getRequest();
      request.setParameter( "name", "master" );
      response = conversation.getResponse( request );
      assertTrue( "Login not accepted", response.getText().indexOf( "You made it!" ) != -1 );

      assertEquals( "Page title", "Top Secret", response.getTitle() );
  }
}


