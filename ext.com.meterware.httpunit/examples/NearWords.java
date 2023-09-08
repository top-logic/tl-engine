import com.meterware.httpunit.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * This class demonstrates using httpunit to use the functionality of a web set from a command
 * line. To use it, specify a single word with one or more characters replaced by '?'. The
 * program will use the Merriam-Webster web site to find all words which match the pattern.
 *
 * Note: this program is not robust, but should work is used properly.
 **/
public class NearWords {


    public static void main( String[] params ) {
        try {
            if (params.length < 1) {
                System.out.println( "Usage: java NearWords [pattern]" );
                System.out.println( "where [pattern] may contain '?' to match any character" );
                System.out.println( "");
                System.out.println( "will demonstrate usage with the pattern 'test' now ...");
                String[] defaultParams={"test"};
                params=defaultParams;
            }
            WordSeeker seeker = new WordSeeker();
            
            PrintStream err = new PrintStream( new FileOutputStream( "null.txt" ) );
            System.setErr( err );

            String[] words = seeker.getWordsMatching( params[0] );
            for (int i=0; i < words.length; i++) {
                System.out.println( (i+1) + ". " + words[i] ); 
            }
        } catch (Exception e) {
            System.err.println( "Exception: " + e );
        }
    }

}

/**
 * subclass to seek words from the Merriam-Webster Online search
 * as of 2008-05-02
 *
 */
class WordSeeker {

    public WordSeeker() {
        try {
        	HttpUnitOptions.setScriptingEnabled(false);
        	String url="http://www.m-w.com/";
        	System.out.println("visiting "+url);
            WebRequest request = new GetMethodWebRequest( url );
            response = conversation.getResponse( request );
        } catch (Exception e) {
            throw new RuntimeException( "Error retrieving form: " + e );
        }
    }


    public String[] getWordsMatching( String pattern ) throws SAXException, IOException, java.net.MalformedURLException {
    	System.out.println("getting Words matching '"+pattern+"'");
      WebForm lookupForm = response.getFormWithID("search_form"); 
      WebRequest request = lookupForm.getRequest();
      request.setParameter( "va", pattern );
      request.setParameter( "book", "Dictionary" );
      response = conversation.getResponse( request );
      return getOptionsFromResponse();
    }

    private WebConversation conversation = new WebConversation();

    private WebResponse     response;


    private String[] getOptionsFromResponse() throws SAXException {
        String[] words;
        WebForm[] forms = response.getForms();
        for (int i=0; i < forms.length; i++) {
            Element form = (Element) forms[i].getDOMSubtree();
            NodeList nl = form.getElementsByTagName( "option" );
            if (nl.getLength() == 0) continue;

            words = new String[ nl.getLength() ];
            for (int j = 0; j < nl.getLength(); j++) {
                words[j] = nl.item(j).getFirstChild().getNodeValue(); 
            }
            return words;
        }
        return new String[0];
    }

}

