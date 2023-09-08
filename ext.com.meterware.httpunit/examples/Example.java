/** everything you need to start is in the com.meterware.httpunit package **/
import com.meterware.httpunit.*;

/** This is a simple example of using HttpUnit to read and understand web pages. **/
public class Example {


  public static void main( String[] params ) {
    try {
      // create the conversation object which will maintain state for us
      WebConversation wc = new WebConversation();

      // Obtain the main page on the meterware web site
      String url="http://www.meterware.com";
      WebRequest request = new GetMethodWebRequest( url );
      WebResponse response = wc.getResponse( request );

      // find the link which contains the string "HttpUnit" and click it
      WebLink httpunitLink = response.getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, "HttpUnit" );
      response = httpunitLink.click();

      // print out the number of links on the HttpUnit main page
      System.out.println( "The HttpUnit main page '"+url+"' contains " + response.getLinks().length + " links" );

    } catch (Exception e) {
       System.err.println( "Exception: " + e );
    }
  }
}

