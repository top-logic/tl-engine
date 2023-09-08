package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: WebClientTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2008, Russell Gold
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
import com.meterware.httpunit.cookies.Cookie;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import junit.framework.TestSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

//import HTTPClient.HTTPConnection;
//import HTTPClient.HTTPResponse;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class WebClientTest extends HttpUnitTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebClientTest.class );
    }


    public WebClientTest( String name ) {
        super( name );
    }

    public void ntestNoSuchServer() throws Exception {
        WebConversation wc = new WebConversation();

        try {
            wc.getResponse( "http://no.such.host" );
            fail( "Should have rejected the request" );
        } catch (UnknownHostException e) {
        } catch (IOException e) {
//            if (!(e.getCause() instanceof UnknownHostException)) throw e;
        }
    }


    public void testNotFound() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/nothing.htm" );
        try {
            wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpNotFoundException e) {
            assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode() );
            assertEquals( "Response message", "unable to find /nothing.htm", e.getResponseMessage() );
        }
    }


    public void testNotModifiedResponse() throws Exception {
        defineResource( "error.htm", "Not Modified", 304 );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Response code", 304, response.getResponseCode() );
        response.getText();
        response.getInputStream().read();
    }


    public void testInternalErrorException() throws Exception {
        defineResource( "error.htm", "Internal error", 501 );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        try {
            wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpException e) {
            assertEquals( "Response code", 501, e.getResponseCode() );
        }
    }


    public void testInternalErrorDisplay() throws Exception {
        defineResource( "error.htm", "Internal error", 501 );

        WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus( false );
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/error.htm" );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Response code", 501, response.getResponseCode() );
        assertEquals( "Message contents", "Internal error", response.getText().trim() );
    }


    public void testSimpleGet() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
    }


    public void testFunkyGet() throws Exception {
        String resourceName = "ID=03.019c010101010001.00000001.a202000000000019. 0d09/login/";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
    }

    /**
     * test cookies
     * @throws Exception
     */
    public void testCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );
        addResourceHeader( resourceName, "Set-Cookie: HSBCLoginFailReason=; path=/" );
        addResourceHeader( resourceName, "Set-Cookie: age=12, name= george" );
        addResourceHeader( resourceName, "Set-Cookie: type=short" );
        addResourceHeader( resourceName, "Set-Cookie: funky=ab$==" );
        addResourceHeader( resourceName, "Set-Cookie: p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X" );
        addResourceHeader( resourceName, "Set-Cookie: SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;" );

        WebConversation wc   = new WebConversation();
        WebRequest request   = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
        String[] names=wc.getCookieNames();
        // for (int i=0;i<names.length;i++)   	System.err.println(names[i]);
        
        assertEquals( "number of cookies", 8, names.length );
        assertEquals( "cookie 'HSBCLoginFailReason' value", "", wc.getCookieValue( "HSBCLoginFailReason" ) );
        assertEquals( "cookie 'age' value", "12", wc.getCookieValue( "age" ) );
        assertEquals( "cookie 'name' value", "george", wc.getCookieValue( "name" ) );
        assertEquals( "cookie 'type' value", "short", wc.getCookieValue( "type" ) );
        assertEquals( "cookie 'funky' value", "ab$==", wc.getCookieValue( "funky" ) );
        assertEquals( "cookie 'p30waco_sso' value", "3.0,en,us,AMERICA,Drew", wc.getCookieValue( "p30waco_sso" ) );
        assertEquals( "cookie 'PORTAL30_SSO_TEST' value", "X", wc.getCookieValue( "PORTAL30_SSO_TEST" ) );
        assertEquals( "cookie 'SESSION_ID' value", "17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", wc.getCookieValue( "SESSION_ID" ) );
        // addition for [ 1488617 ] alternate patch for cookie bug #1371204
        Cookie cookie=wc.getCookieDetails("age");
        assertTrue(cookie!=null);
        assertEquals(cookie.getDomain(),"localhost");
        assertEquals(cookie.getValue(),"12");
        assertEquals(cookie.getPath(),"/something");            
    }


    public void testCookiesDisabled() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );
        addResourceHeader( resourceName, "Set-Cookie: age=12" );

        WebConversation wc   = new WebConversation();
        wc.getClientProperties().setAcceptCookies( false );
        WebRequest request   = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "number of cookies", 0, wc.getCookieNames().length );
    }


    public void testOldCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource( resourceName, resourceValue );
        addResourceHeader( resourceName, "Set-Cookie: CUSTOMER=WILE_E_COYOTE; path=/; expires=Wednesday, 09-Nov-99 23:12:40 GMT" );

        WebConversation wc   = new WebConversation();
        WebRequest request   = new GetMethodWebRequest( getHostPath() + '/' + resourceName );
        WebResponse response = wc.getResponse( request );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "number of cookies", 1, wc.getCookieNames().length );
        assertEquals( "cookie 'CUSTOMER' value", "WILE_E_COYOTE", wc.getCookieValue( "CUSTOMER" ) );
    }
    
    /**
     * test setting a cookie manually
     * @throws Exception
     */
    public void testManualCookies() throws Exception {
        defineResource( "bounce", new CookieEcho() );
        WebConversation wc   = new WebConversation();
        wc.putCookie( "CUSTOMER", "WILE_E_COYOTE" );
        WebResponse response = wc.getResponse( getHostPath() + "/bounce" );
        assertEquals( "Cookies sent", "CUSTOMER=WILE_E_COYOTE", response.getText() );
        wc.putCookie( "CUSTOMER", "ROAD RUNNER" );
        response = wc.getResponse( getHostPath() + "/bounce" );
        assertEquals( "Cookies sent", "CUSTOMER=ROAD RUNNER", response.getText() );
    }
    
    
    /**
     * test for  1799532 ] Patched CookieJar for dealing with empty cookies
     * I had a problem testing a web app that sent empty cookie values to the client.
     * 
     * Within the same http response, a cookie was set by the web app twice, once
     * with some non-empty value and the second time with an empty value. The
     * intention of the web app obviously was to delete the cookie, wich actually
     * occurs in IE and Firefox.
     *
     * In HttpUnit though the cookie was stored with the empty value and sent back
     * to the server in the next request. That confused the web app and caused
     * application errors, because it didn't expect that cookie back with the
     * empty value.
     *
     * I cannot really judge if this is actually a bug in HttpUnit or just more
     * strict appliance of protocols than real Browsers do. Of course it is at
     * least an ugly behaviour of the web app, but that wasn't my focus. It also
     * seems from forums that others might have had the same problem.
     * 
     * To over come that problem, I had to change the CookieJar class in HttpUnit
     *      
     * to not store cookies with empty values, but remove them completely instead.
     * The modified source file is attached (based on HttpUnit 1.6.2).
     *
     * Russel, please check if you want to integrate this modification in a future
     * HttpUnit Release.
     * 2007-12-28: wf@bitplan.com  - looked at
     * 		http://www.ietf.org/rfc/rfc2109.txt
     * there is no statement about empty value handling -the standard asks for 
     * opaque handling ... no mocking about with values by the server ...
     * @throws Exception
     */
    public void testEmptyCookie() throws Exception {
      defineResource( "bounce", new CookieEcho() );
      WebConversation wc   = new WebConversation();
      wc.putCookie( "EMPTYVALUE", "non-empty" );
      WebResponse response = wc.getResponse( getHostPath() + "/bounce" );
      assertEquals( "Cookies sent", "EMPTYVALUE=non-empty", response.getText() );
      wc.putCookie( "SOMECOOKIE","some value");
      wc.putCookie( "EMPTYVALUE", null );
      response = wc.getResponse( getHostPath() + "/bounce" );
      // System.err.println(response.getText());
      String[] names=wc.getCookieNames();
      // should we expect a 1 or a two here?
      // see also testCookies where 8 is currently the correct value and 7 would
      // be if we handle empty strings as cookie deletions
      // as long as 1799532 is rejected we'll go for a 2 here ...
      // [ 1371208 ] Patch for Cookie bug #1371204 has a solution to use 1 ...
      assertEquals( "number of cookies", 1, names.length );
      //  for (int i=0;i<names.length;i++)   	System.err.println(names[i]);
    }
    

    class CookieEcho extends PseudoServlet {

        public WebResource getGetResponse() throws IOException {
            return new WebResource( getHeader( "Cookie" ) );
        }
    }


    public void testHeaderFields() throws Exception {
        defineResource( "getHeaders", new PseudoServlet() {
            public WebResource getGetResponse() {
                StringBuffer sb = new StringBuffer();
                sb.append( getHeader( "Junky" ) ).append( "<-->" ).append( getHeader( "User-Agent" ) );
                return new WebResource( sb.toString(), "text/plain" );
            }
        } );

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setUserAgent( "me alone" );
        wc.setHeaderField( "junky", "Mozilla 6" );
        WebResponse wr = wc.getResponse( getHostPath() + "/getHeaders" );
        assertEquals( "headers found", "Mozilla 6<-->me alone", wr.getText() );
    }


    public void testBasicAuthentication() throws Exception {
        defineResource( "getAuthorization", new PseudoServlet() {
            public WebResource getGetResponse() {
                return new WebResource( getHeader( "Authorization" ), "text/plain" );
            }
        } );

        WebConversation wc = new WebConversation();
        wc.setAuthorization( "user", "password" );
        WebResponse wr = wc.getResponse( getHostPath() + "/getAuthorization" );
        assertEquals( "authorization", "Basic dXNlcjpwYXNzd29yZA==", wr.getText() );
    }


    public void testOnDemandBasicAuthentication() throws Exception {
        defineResource( "getAuthorization", new PseudoServlet() {
            public WebResource getGetResponse() {
                String header = getHeader( "Authorization" );
                if (header == null) {
                    WebResource webResource = new WebResource( "unauthorized" );
                    webResource.addHeader( "WWW-Authenticate: Basic realm=\"testrealm\"");
                    return webResource;
                } else {
                    return new WebResource( header, "text/plain" );
                }
            }
        } );

        WebConversation wc = new WebConversation();
        wc.setAuthentication( "testrealm", "user", "password" );
        WebResponse wr = wc.getResponse( getHostPath() + "/getAuthorization" );
        assertEquals( "authorization", "Basic dXNlcjpwYXNzd29yZA==", wr.getText() );
    }


    /**
     * Verifies that even though we have specified username and password for a realm,
     * a request for a different realm will still result in an exception.
     * @throws Exception if an unexpected exception is thrown.
     */
    public void testBasicAuthenticationRequestedForUnknownRealm() throws Exception {
        defineResource( "getAuthorization", new PseudoServlet() {
            public WebResource getGetResponse() {
                String header = getHeader( "Authorization" );
                if (header == null) {
                    WebResource webResource = new WebResource( "unauthorized" );
                    webResource.addHeader( "WWW-Authenticate: Basic realm=\"bogusrealm\"");
                    return webResource;
                } else {
                    return new WebResource( header, "text/plain" );
                }
            }
        } );

        WebConversation wc = new WebConversation();
        wc.setAuthentication( "testrealm", "user", "password" );
        try {
            wc.getResponse( getHostPath() + "/getAuthorization" );
            fail( "Should have rejected authentication" );
        } catch (AuthorizationRequiredException e) {
            assertEquals( "authorization scheme", "Basic", e.getAuthenticationScheme() );
            assertEquals( "bogusrealm", e.getAuthenticationParameter( "realm" ) );
        }
    }


    public void suspendtestProxyServerAccessWithAuthentication() throws Exception {
        defineResource( "http://someserver.com/sample", new PseudoServlet() {
            public WebResource getGetResponse() {
                return new WebResource( getHeader( "Proxy-Authorization" ), "text/plain" );
            }
        } );
        WebConversation wc = new WebConversation();
        wc.setProxyServer( "localhost", getHostPort(), "user", "password" );
        WebResponse wr = wc.getResponse( "http://someserver.com/sample" );
        assertEquals( "authorization", "Basic dXNlcjpwYXNzd29yZA==", wr.getText() );
    }


    /**
     * Verifies one-time digest authentication with no Quality of Protection (qop).
     * @throws Exception if an unexpected exception is thrown during the test.
     */
    public void testRfc2069DigestAuthentication() throws Exception {
        defineResource( "/dir/index.html", new PseudoServlet() {
            public WebResource getGetResponse() {
                String header = getHeader( "Authorization" );
                if (header == null) {
                    WebResource resource = new WebResource( "not authorized", HttpURLConnection.HTTP_UNAUTHORIZED );
                    resource.addHeader( "WWW-Authenticate: Digest realm=\"testrealm@host.com\"," +
                                        " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                                        " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"" );
                    return resource;
                } else {
                    return new WebResource( getHeader( "Authorization" ), "text/plain" );
                }
            }
        });
        WebConversation wc = new WebConversation();
        wc.setAuthentication( "testrealm@host.com", "Mufasa", "CircleOfLife" );
        WebResponse wr = wc.getResponse( getHostPath() + "/dir/index.html" );
        HttpHeader expectedHeader
                = new HttpHeader( "Digest username=\"Mufasa\"," +
                                  "       realm=\"testrealm@host.com\"," +
                                  "       nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                                  "       uri=\"/dir/index.html\"," +
                                  "       response=\"1949323746fe6a43ef61f9606e7febea\"," +
                                  "       opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"" );
        HttpHeader actualHeader = new HttpHeader( wr.getText() );
        assertHeadersEquals( expectedHeader, actualHeader );
    }


    /**
      * Verifies one-time digest authentication with Quality of Protection (qop).
      * @throws Exception
      */
    public void ntestQopDigestAuthenticationhttp_client() throws Exception {     //  todo enable this
        defineResource( "/dir/index.html", new PseudoServlet() {
            public WebResource getGetResponse() {
                String header = getHeader( "Authorization" );
                if (header == null) {
                    WebResource resource = new WebResource( "not authorized", HttpURLConnection.HTTP_UNAUTHORIZED );
                    resource.addHeader( "WWW-Authenticate: Digest realm=\"testrealm@host.com\"," +
                                        " qop=\"auth\"," +
                                        " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                                        " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"" );
                    return resource;
                } else {
                    return new WebResource( getHeader( "Authorization" ), "text/plain" );
                }
            }
        } );
        String text = getPageContents( getHostPath() + "/dir/index.html", "testrealm@host.com", "Mufasa", "Circle Of Life" );
        HttpHeader actualHeader = new HttpHeader( text );
        HttpHeader expectedHeader
                = new HttpHeader( "Digest username=\"Mufasa\"," +
                                  "       realm=\"testrealm@host.com\"," +
                                  "       nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                                  "       uri=\"/dir/index.html\"," +
                                  "       qop=auth," +
                                  "       nc=00000001," +
                                  "       cnonce=\"19530e1f777250e9d7ad02a93b187b9d\"," +
                                  "       response=\"943fad0655736f7a2342daef67186ce6\"," +
                                  "       opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"" );
        String cnonce = actualHeader.getProperty( "cnonce" );
        assertHeadersEquals( expectedHeader, actualHeader );
    }

    private static String getPageContents( String pageAddress, String protectionDomain, String userName, String password ) throws Exception {
        WebConversation wc = new WebConversation();
        wc.setAuthentication( protectionDomain, userName, password );
        WebResponse wr = wc.getResponse( pageAddress );
        return wr.getText();
    }


    private void assertHeadersEquals( HttpHeader expectedHeader, HttpHeader actualHeader ) {
        assertEquals( "Authentication type", expectedHeader.getLabel(), actualHeader.getLabel() );
        if (!expectedHeader.equals( actualHeader )) {
            Deltas deltas = new Deltas();
            Set actualKeys = new HashSet( actualHeader.getProperties().keySet() );
            for (Iterator eachKey = expectedHeader.getProperties().keySet().iterator(); eachKey.hasNext();) {
                String key = (String) eachKey.next();
                if (!actualKeys.contains( key )) {
                    deltas.addMissingValue( key, expectedHeader.getProperty( key ));
                } else {
                    actualKeys.remove( key );
                    deltas.compareValues( key, expectedHeader.getProperty( key ), actualHeader.getProperty( key ) );
                }
            }
            for (Iterator eachKey = actualKeys.iterator(); eachKey.hasNext();) {
                String key = (String) eachKey.next();
                deltas.addExtraValue( key, actualHeader.getProperty( key ) );
            }
            fail( "Header not as expected: " + deltas );
        }
    }


    static class Deltas {
        private ArrayList _missingValues = new ArrayList();
        private ArrayList _extraValues = new ArrayList();


        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (!_missingValues.isEmpty()) sb.append( "missing: ").append( _missingValues );
            if (!_extraValues.isEmpty()) sb.append( "extra: " ).append( _extraValues );
            return sb.toString();
        }


        void addMissingValue( Object key, Object value ) {
            _missingValues.add( key + "=" + value );
        }

        void addExtraValue( Object key, Object value ) {
            _extraValues.add( key + "=" + value );
        }

        void compareValues( Object key, Object expected, Object actual ) {
            if (!expected.equals( actual )) {
                addMissingValue( key, expected );
                addExtraValue( key, actual );
            }
        }
    }


    /**
     * test the Referer Header
     * @param refererEnabled - true if it should not be stripped
     * @throws Exception
     */
    public void dotestRefererHeader(boolean refererEnabled) throws Exception {
        String resourceName = "tellMe";
        String linkSource = "fromLink";
        String formSource = "fromForm";

        String page0 = getHostPath() + '/' + resourceName;
        String page1 = getHostPath() + '/' + linkSource;
        String page2 = getHostPath() + '/' + formSource;

        defineResource( linkSource, "<html><head></head><body><a href=\"tellMe\">Go</a></body></html>" );
        defineResource( formSource, "<html><body><form action=\"tellMe\"><input type=submit></form></body></html>" );
        defineResource( resourceName, new PseudoServlet() {
            public WebResource getGetResponse() {
                String referer = getHeader( "Referer" );
                return new WebResource( referer == null ? "null" : referer, "text/plain" );
            }
        } );

        WebConversation wc   = new WebConversation();
        wc.getClientProperties().setSendReferer(refererEnabled);        
        WebResponse response = wc.getResponse( page0 );
        assertEquals( "Content type", "text/plain", response.getContentType() );
        assertEquals( "Default Referer header", "null", response.getText().trim() );

        response = wc.getResponse( page1 );
        response = wc.getResponse( response.getLinks()[0].getRequest() );
        String expected=page1;
        if (!refererEnabled)	expected="null";
        assertEquals( "Link Referer header", expected, response.getText().trim() );
        response = wc.getResponse( page2 );
        response = wc.getResponse( response.getForms()[0].getRequest() );
        expected=page2;
        if (!refererEnabled)	expected="null";
        assertEquals( "Form Referer header", expected, response.getText().trim() );
    }

    public void testRefererHeader() throws Exception {
    	dotestRefererHeader(true);
    }	

    /**
     * test the referer Header twice - with and without stripping it according to
     *  [ 844084 ] Block HTTP referer
     * @throws Exception
     */
    public void testRefererHeaderWithStrippingReferer() throws Exception {
    	dotestRefererHeader(false);
    }



    public void testRedirectedRefererHeader() throws Exception {
        String linkSource = "fromLink";
        String linkTarget = "anOldOne";
        String resourceName = "tellMe";

        defineResource( linkSource, "<html><head></head><body><a href='" + linkTarget + "'>Go</a></body></html>" );

        defineResource( linkTarget, "ignored content", HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( linkTarget, "Location: " + getHostPath() + '/' + resourceName );

        defineResource( resourceName, new PseudoServlet() {
            public WebResource getGetResponse() {
                String referer = getHeader( "Referer" );
                return new WebResource( referer == null ? "null" : referer, "text/plain" );
            }
        } );

        WebConversation wc   = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + '/' + linkSource );
        response = wc.getResponse( response.getLinks()[0].getRequest() );
        assertEquals( "Link Referer header", getHostPath() + '/' + linkSource, response.getText().trim() );
    }
    
  /**
   * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
   * @author james abley
   * @throws Exception
   */
  public void testSelfReferentialRedirect() throws Exception {
    String resourceName = "something/redirected";
    
    defineResource(resourceName, "ignored content",
            HttpURLConnection.HTTP_MOVED_PERM);
    addResourceHeader(resourceName, "Location: " + getHostPath() + '/'
            + resourceName);

    WebConversation wc = new WebConversation();
    try {
        wc.getResponse(getHostPath() + '/' + resourceName);
        fail("Should have thrown a RecursiveRedirectionException");
    } catch (RecursiveRedirectionException expected) {
    }
  }

  /**
   * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
   * @author james abley
   * @throws Exception
   */
  public void testLoopingMalformedRedirect() throws Exception {
      String resourceAName = "something/redirected";
      String resourceBName = "something/else/redirected";
      String resourceCName = "another/redirect";

      // Define a linked list of 'A points to B points to C points to A...'
      defineResource(resourceAName, "ignored content",
              HttpURLConnection.HTTP_MOVED_PERM);
      addResourceHeader(resourceAName, "Location: " + getHostPath() + '/'
              + resourceBName);

      defineResource(resourceBName, "ignored content",
              HttpURLConnection.HTTP_MOVED_TEMP);
      addResourceHeader(resourceBName, "Location: " + getHostPath() + "/"
              + resourceCName);
      
      defineResource(resourceCName, "ignored content", 
              HttpURLConnection.HTTP_MOVED_PERM);
      addResourceHeader(resourceCName, "Location: " + getHostPath() + "/" 
              + resourceAName);

      WebConversation wc = new WebConversation();
      try {
          wc.getResponse(getHostPath() + '/' + resourceAName);
          fail("Should have thrown a RecursiveRedirectionException");
      } catch (RecursiveRedirectionException expected) {
      }
  }

  /**
   * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
   * @author james abley
   * @throws Exception
   */
  public void testRedirectHistoryIsClearedOut() throws Exception {
      String resourceName = "something/interesting";
      String resourceValue = "something interesting";
      
      defineResource(resourceName, resourceValue);
      
      String redirectName = "something/redirected";
      
      defineResource(redirectName, "ignored content",
              HttpURLConnection.HTTP_MOVED_PERM);
      addResourceHeader(redirectName, "Location: " + getHostPath() + '/'
              + resourceName);

      // Normal behaviour first time through - redirects to resource
      WebConversation wc = new WebConversation();
      WebResponse response = wc.getResponse(getHostPath() + "/" + redirectName);
      assertEquals("OK response", 200, response.getResponseCode());
      assertEquals("Expected response string", resourceValue, response.getText().trim());
      assertEquals("Content type", "text/html", response.getContentType());
      
      // Can we get the resource again, or is the redirect urls not being cleared out?
      try {
          wc.getResponse(getHostPath() + "/" + redirectName);
          assertEquals("OK response", 200, response.getResponseCode());
          assertEquals("Expected response string", resourceValue, response.getText().trim());
          assertEquals("Content type", "text/html", response.getContentType());
      } catch (RecursiveRedirectionException e) {
          fail("Not expecting RecursiveRedirectionException - " 
                  + "list of redirection urls should be new for each " 
                  + "client-initiated request");
      }
  }

  /**
   * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
   * @author james abley
   * @throws Exception
   */
  public void testRedirectionLeadingToMalformedURLStillClearsOutRedirectionList() throws Exception {
      String resourceAName = "something/redirected";
      String resourceBName = "something/else/redirected";
      String resourceCName = "another/redirect";

      // Define a linked list of 'A points to B points to C points to A...'
      defineResource(resourceAName, "ignored content",
              HttpURLConnection.HTTP_MOVED_PERM);
      addResourceHeader(resourceAName, "Location: " + getHostPath() + '/'
              + resourceBName);

      defineResource(resourceBName, "ignored content",
              HttpURLConnection.HTTP_MOVED_TEMP);
      addResourceHeader(resourceBName, "Location: " + getHostPath() + "/"
              + resourceCName);
      
      defineResource(resourceCName, "ignored content", 
              HttpURLConnection.HTTP_MOVED_PERM);
      addResourceHeader(resourceCName, "Location: NotAProtocolThatIKnowOf://ThisReallyShouldThrowAnException");

      WebConversation wc = new WebConversation();
      try {
          wc.getResponse(getHostPath() + '/' + resourceAName);
          fail("Should have thrown a MalformedURLException");
      } catch (MalformedURLException expected) {
          try {
              wc.getResponse(getHostPath() + "/" + resourceAName);
          } catch (RecursiveRedirectionException e) {
              fail("Not expecting RecursiveRedirectionException");
          } catch (MalformedURLException expected2) {
              
          }
      }
  }

	/**
	 * test for bug report [ 1283878 ] FileNotFoundException using Sun JDK 1.5 on empty error pages
	 * by Roger Lindsjö  
	 * @throws Exception
	 */
	public void testEmptyErrorPage() throws Exception {
    boolean originalState =
  		HttpUnitOptions.getExceptionsThrownOnErrorStatus();
    HttpUnitOptions.setExceptionsThrownOnErrorStatus( false );       
  		 
    try {
      WebConversation wc = new WebConversation();
      defineResource( "emptyError", "", 404);
      WebRequest request = new GetMethodWebRequest( getHostPath() +"/emptyError" );
      WebResponse response = wc.getResponse( request );
      assertEquals( 404, response.getResponseCode() );
      assertEquals( 0, response.getContentLength() );
    } catch (java.io.FileNotFoundException fnfe) {
    	fnfe.printStackTrace();
    	assertTrue("There should be not file not found exception '"+fnfe.getMessage()+"'",false);
    } finally {
      // Restore exceptions state
    	HttpUnitOptions.setExceptionsThrownOnErrorStatus(originalState );
  	}
  }

  /**
   * test GZIP begin disabled
   * @throws Exception
   */
    public void testGZIPDisabled() throws Exception {
      String expectedResponse = "Here is my answer";
      defineResource( "Compressed.html", new CompressedPseudoServlet( expectedResponse ) );

      WebConversation wc = new WebConversation();
      wc.getClientProperties().setAcceptGzip( false );
      WebResponse wr = wc.getResponse( getHostPath() + "/Compressed.html" );
      assertNull( "Should not have received a Content-Encoding header", wr.getHeaderField( "Content-encoding" ) );
      assertEquals( "Content-Type", "text/plain", wr.getContentType() );
      assertEquals( "Content", expectedResponse, wr.getText().trim() );
    }


    public void testGZIPHandling() throws Exception {
        String expectedResponse = "Here is my answer. It needs to be reasonably long to make compression smaller " +
                                  "than the raw message. It should be obvious when you reach that point. " +
                                  "Of course it is more than that - it needs to be long enough to cause a problem.";
        defineResource( "Compressed.html", new CompressedPseudoServlet( expectedResponse ) );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/Compressed.html" );
        assertEquals( "Content-Encoding header", "gzip", wr.getHeaderField( "Content-encoding" ) );
        assertEquals( "Content-Type", "text/plain", wr.getContentType() );
        assertEquals( "Content", expectedResponse, wr.getText().trim() );
    }
    
    /**
     * try to validate support request
     * [ 885326 ] In CONTENT-ENCODING: gzip, EOFException happens.
     * -- disabled by wf 2007-12-30 - does lead to a javascript problem and
     * not fit for a reqular test since it depends on an outsided website not under control of the project
     * @throws Exception
     */
    public void xtestGZIPHandling2() throws Exception {
    	String 	url = "http://sourceforge.net/project/showfiles.php?group_id=6550";
    	WebConversation	conversation = new WebConversation();
    	WebRequest 	request = new GetMethodWebRequest(url);
    	WebResponse	response = conversation.getResponse(request);
    }


    private class CompressedPseudoServlet extends PseudoServlet {

        private String _responseText;
        private boolean _suppressLengthHeader;


        public CompressedPseudoServlet( String responseText ) {
            _responseText = responseText;
        }


        public CompressedPseudoServlet( String responseText, boolean suppressLengthHeader ) {
            this( responseText );
            _suppressLengthHeader = suppressLengthHeader;
        }


        public WebResource getGetResponse() throws IOException {
            if (!userAcceptsGZIP()) {
                return new WebResource( _responseText.getBytes(), "text/plain" );
            } else {
                WebResource result = new WebResource( getCompressedContents(), "text/plain" );
                if (_suppressLengthHeader) result.suppressAutomaticLengthHeader();
                result.addHeader( "Content-Encoding: gzip" );
                return result;
            }
        }


        private boolean userAcceptsGZIP() {
            String header = getHeader( "Accept-Encoding" );
            if (header == null) return false;
            return header.toLowerCase().indexOf( "gzip" ) >= 0;
        }


        private byte[] getCompressedContents() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream( baos );
            OutputStreamWriter out = new OutputStreamWriter( gzip );
            out.write( _responseText );
            out.flush();
            out.close();
            return baos.toByteArray();
        }
    }


    public void testGZIPUndefinedLengthHandling() throws Exception {
        String expectedResponse = "Here is my answer. It needs to be reasonably long to make compression smaller " +
                                  "than the raw message. It should be obvious when you reach that point. " +
                                  "Of course it is more than that - it needs to be long enough to cause a problem.";
        defineResource( "Compressed.html", new CompressedPseudoServlet( expectedResponse, /* suppress length */ true ) );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/Compressed.html" );
        assertEquals( "Content-Encoding header", "gzip", wr.getHeaderField( "Content-encoding" ) );
        assertEquals( "Content-Type", "text/plain", wr.getContentType() );
        assertEquals( "Content", expectedResponse, wr.getText().trim() );
    }


    public void testClientListener() throws Exception {
        defineWebPage( "Target", "This is another page with <a href=Form.html target='_top'>one link</a>" );
        defineWebPage( "Form", "This is a page with a simple form: " +
                               "<form action=submit><input name=name><input type=submit></form>" +
                               "<a href=Target.html target=red>a link</a>" );
        defineResource( "Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                "<FRAMESET cols='20%,80%'>" +
                "    <FRAME src='Target.html' name='red'>" +
                "    <FRAME src=Form.html name=blue>" +
                "</FRAMESET></HTML>" );

        WebConversation wc = new WebConversation();
        ArrayList messageLog = new ArrayList();
        wc.addClientListener( new ListenerExample( messageLog ) );

        wc.getResponse( getHostPath() + "/Frames.html" );
        assertEquals( "Num logged items", 6, messageLog.size() );
        for (int i = 0; i < 3; i++) {
            verifyRequestResponsePair( messageLog, 2 * i );
        }
    }


    private void verifyRequestResponsePair( ArrayList messageLog, int i ) throws MalformedURLException {
        assertTrue( "Logged item " + i + " is not a web request, but " + messageLog.get( i ).getClass(),
                messageLog.get( i ) instanceof WebRequest );
        assertTrue( "Logged item " + (i + 1) + " is not a web response, but " + messageLog.get( i + 1 ).getClass(),
                messageLog.get( i + 1 ) instanceof WebResponse );
        assertEquals( "Response target", ((WebRequest) messageLog.get( i )).getTarget(), ((WebResponse) messageLog.get( i + 1 )).getFrameName() );
        assertEquals( "Response URL", ((WebRequest) messageLog.get( i )).getURL(), ((WebResponse) messageLog.get( i + 1 )).getURL() );
    }


    private static class ListenerExample implements WebClientListener {

        private List _messageLog;


        public ListenerExample( List messageLog ) {
            _messageLog = messageLog;
        }


        public void requestSent( WebClient src, WebRequest req ) {
            _messageLog.add( req );
        }


        public void responseReceived( WebClient src, WebResponse resp ) {
            _messageLog.add( resp );
        }
    }


    public void testRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";

        defineResource( resourceName, resourceValue );
        defineResource( redirectName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( redirectName, "Location: " + getHostPath() + '/' + resourceName );

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + '/' + redirectName );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "status", HttpURLConnection.HTTP_OK, response.getResponseCode() );
    }


    public void testDuplicateHeaderRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";

        defineResource( resourceName, resourceValue );
        defineResource( redirectName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( redirectName, "Location: " + getHostPath() + '/' + resourceName );
        addResourceHeader( redirectName, "Location: " + getHostPath() + '/' + resourceName );

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + '/' + redirectName );
        assertEquals( "requested resource", resourceValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
    }


    public void testDisabledRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";
        String redirectValue = "old content";

        defineResource( resourceName, resourceValue );
        defineResource( redirectName, redirectValue, HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( redirectName, "Location: " + getHostPath() + '/' + resourceName );

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAutoRedirect( false );
        WebResponse response = wc.getResponse( getHostPath() + '/' + redirectName );
        assertEquals( "requested resource", redirectValue, response.getText().trim() );
        assertEquals( "content type", "text/html", response.getContentType() );
    }


    public void testDNSOverride() throws Exception {
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setDnsListener( new DNSListener() {
            public String getIpAddress( String hostName ) { return "127.0.0.1"; }
        });

        defineResource( "whereAmI", new PseudoServlet() {
            public WebResource getGetResponse() {
                WebResource webResource = new WebResource( "found host header: " + getHeader( "Host" ) );
                webResource.addHeader( "Set-Cookie: type=short" );
                return webResource;
            }
        } );

        defineResource( "checkCookies", new PseudoServlet() {
            public WebResource getGetResponse() {
                return new WebResource( "found cookies: " + getHeader( "Cookie" ) );
            }
        } );


        WebResponse wr = wc.getResponse( "http://meterware.com:" + getHostPort() + "/whereAmI" );
        assertEquals( "Submitted host header", "found host header: meterware.com:" + getHostPort(), wr.getText() );
        assertEquals( "Returned cookie 'type'", "short", wc.getCookieValue( "type" ) );

        wr = wc.getResponse( "http://meterware.com:" + getHostPort() + "/checkCookies" );
        assertEquals( "Submitted cookie header", "found cookies: type=short", wr.getText() );
    }

 }
