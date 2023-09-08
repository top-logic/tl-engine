import com.meterware.httpunit.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;


/**
 * This example is from http://m0smith.freeshell.org/blog-portletunit/2008/05/bare-bones-browser-launch-for-java-use
 * @author Matthew O. Smith 
 *
 */
public class BrowserDisplayer {
	/**
   * Show the response in a browser.
   *
   * @param response
   *            the response
   * @throws Exception
   *             on error
   */
  public static void showResponseInBrowser(WebResponse response) throws Exception {
      String text = response.getText();
      File f = File.createTempFile("httpUnit", ".html");
      f.deleteOnExit();
      PrintWriter fod = new PrintWriter(new FileOutputStream(f));
      fod.print("<head><base href=\"'http://localhost'/\"> </head>");
      fod.print(text);
      fod.close();
      URL url = f.toURL();
      openURL(url);
  }

  /**
   * Bare Bones Browser Launch Version 1.5 (December 10, 2005) By Dem
   * Pilafian. Supports: Mac OS X, GNU/Linux, Unix, Windows XP
   *
   * Example Usage: String url = "http://www.centerkey.com/";
   * BareBonesBrowserLaunch.openURL(url); Public Domain Software -- Free to
   * Use as You Like
   *
   * @param url
   *            the url to open
   * @throws ClassNotFoundException
   *             getting class
   * @throws NoSuchMethodException
   *             yes
   * @throws SecurityException
   *             well
   * @throws InvocationTargetException
   *             trying to invloke
   * @throws IllegalAccessException
   *             trying to access
   * @throws IllegalArgumentException
   *             bad arguement
   * @throws IOException
   *             opening window
   * @throws InterruptedException waiting
   */
  public static void openURL(URL url) throws ClassNotFoundException,  NoSuchMethodException,
       IllegalAccessException, InvocationTargetException, IOException, InterruptedException {
      String osName = System.getProperty("os.name");

      if (osName.startsWith("Mac OS")) {
          Class fileMgr = Class.forName("com.apple.eio.FileManager");
          Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class });
          openURL.invoke(null, new Object[] {url.toString() });
      } else if (osName.startsWith("Windows")) {
          String cmdLine = "rundll32 url.dll,FileProtocolHandler " + url.toString();
          Process exec = Runtime.getRuntime().exec(cmdLine);
          exec.waitFor();
      } else { // assume Unix or Linux
          String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
          String browser = null;
          for (int count = 0; count < browsers.length && browser == null; count++) {                 if (Runtime.getRuntime().exec(new String[] {"which", browsers[count] }).waitFor() == 0) {                     browser = browsers[count];                 }             }             if (browser == null) {                 throw new IllegalStateException("Could not find web browser");              } else {                 Runtime.getRuntime().exec(new String[] {browser, url.toString() });
            }
        }
     }
  
  /**
   * open a given url indirectly 
   * @param params
   */
  public static void main( String[] params ) {
    try {
        if (params.length < 1) {
            System.out.println( "Usage: java BrowserDisplay [url]" );
            System.out.println( "");
            System.out.println( "will demonstrate usage with the url 'http://www.meterware.com' now ...");
            String[] defaultParams={"http://www.meterware.com"};
            params=defaultParams;
        }
        // direct call first
        String url=params[0];
        openURL(new URL(url));
        // and now indirectly
        // create the conversation object which will maintain state for us
        WebConversation wc = new WebConversation();

        // Obtain the main page on the meterware web site
        WebRequest request = new GetMethodWebRequest( url );
        WebResponse response = wc.getResponse( request );
        showResponseInBrowser(response);
        
        // find the link which contains the string "HttpUnit" and click it
        WebLink httpunitLink = response.getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, "HttpUnit" );
        response = httpunitLink.click();
        showResponseInBrowser(response);
        System.out.println("Your browser should show three pages now:");
        System.out.println("1. a direct invocation of "+url);
        System.out.println("2. an indirect invocation of "+url+" via httpunit");
        System.out.println("3. the result httpunit clicking the httpunit link on 2.");


    } catch (Exception e) {
      System.err.println( "Exception: " + e );
    }
}        
}
