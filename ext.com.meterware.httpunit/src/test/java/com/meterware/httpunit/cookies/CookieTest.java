package com.meterware.httpunit.cookies;
/********************************************************************************************************************
 * $Id: CookieTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2004,2008 Russell Gold
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
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import junit.textui.TestRunner;
import com.meterware.pseudoserver.HttpUserAgentTest;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class CookieTest extends TestCase {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( CookieTest.class );
    }


    public CookieTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        CookieProperties.reset();
    }


    public void testSimpleCookies() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource( new URL( "http://www.meterware.com" ),
                                new String[] { "Reason=; path=/",
                                               "age=12, name= george",
                                               "type=short",
                                               "funky=ab$==",
                                               "p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X",
                                               "SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;" } ) );
        assertEquals( "cookie 'Reason' value", "", jar.getCookieValue( "Reason" ) );
        assertEquals( "cookie 'age' value", "12", jar.getCookieValue( "age" ) );
        assertEquals( "cookie 'name' value", "george", jar.getCookieValue( "name" ) );
        assertEquals( "cookie 'type' value", "short", jar.getCookieValue( "type" ) );
        assertEquals( "cookie 'funky' value", "ab$==", jar.getCookieValue( "funky" ) );
        assertEquals( "cookie 'p30waco_sso' value", "3.0,en,us,AMERICA,Drew", jar.getCookieValue( "p30waco_sso" ) );
        assertEquals( "cookie 'PORTAL30_SSO_TEST' value", "X", jar.getCookieValue( "PORTAL30_SSO_TEST" ) );
        assertEquals( "cookie 'SESSION_ID' value", "17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", jar.getCookieValue( "SESSION_ID" ) );
    }


    public void testCookieMatching() throws Exception {
        assertTrue( "Universal cookie could not be sent", new Cookie( "name", "value" ).mayBeSentTo( new URL( "http://httpunit.org/anywhere" ) ));

        checkMatching( 1, true, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets/sample" );

        checkMatching( 2, false, new URL( "http://www.meterware.com/servlets/sample" ), "meterware.com", "/" );
        checkMatching( 3, true, new URL( "http://www.meterware.com/servlets/sample" ), ".meterware.com", "/" );
        checkMatching( 4, false, new URL( "http://www.meterware.com/servlets/sample" ), ".httpunit.org", "/" );

        checkMatching( 5, true, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets" );
        checkMatching( 6, false, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets/sample/data" );
    }


    private void checkMatching( int index, boolean success, URL url, String domain, String path ) {
        HashMap attributes = new HashMap();
        attributes.put( "path", path );
        attributes.put( "domain", domain );
        Cookie cookie = new Cookie( "name", "value", attributes );
        if (success) {
            assertTrue( "Cookie " + index + " did not allow " + url, cookie.mayBeSentTo( url ) );
        } else {
            assertFalse( "Cookie " + index + " allowed " + url, cookie.mayBeSentTo( url ) );
        }
    }


    /**
     * check the CookieAcceptance
     * @throws Exception
     */
    public void testCookieAcceptance() throws Exception {
        checkAcceptance( 1, true,  "www.meterware.com/servlets/special", null, null );
        checkAcceptance( 2, true,  "www.meterware.com/servlets/special", ".meterware.com", "/servlets" );
        checkAcceptance( 3, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary" );
        checkAcceptance( 4, true,  "www.meterware.com/servlets/special", "meterware.com", null );
        checkAcceptance( 5, false, "www.meterware.com/servlets/special", "meterware", null );
        checkAcceptance( 6, false, "www.meterware.com/servlets/special", ".com", null );
        checkAcceptance( 7, false, "www.meterware.com/servlets/special", ".httpunit.org", null );
        checkAcceptance( 8, false, "www.some.meterware.com/servlets/special", ".meterware.com", null );
        // modified expected result according to [ 1476380 ] Cookies incorrectly rejected despite valid domain
        checkAcceptance( 9, true,  "www.meterware.com/servlets/special", "www.meterware.com", null );
        checkAcceptance(10, false, "www.evilyahoo.com","yahoo.com",null);
    }

    /**
     * check whether the given cookie is accepted
     * @param index
     * @param shouldAccept
     * @param urlString
     * @param specifiedDomain
     * @param specifiedPath
     * @throws MalformedURLException
     */

    private void checkAcceptance( int index, boolean shouldAccept, String urlString,
                                  String specifiedDomain, String specifiedPath ) throws MalformedURLException {
        CookieJar jar = newJar( urlString, specifiedDomain, specifiedPath );

        if (shouldAccept) {
            assertNotNull( "Rejected cookie " + index + "( " + specifiedDomain + " from " + urlString + ") should have been accepted", jar.getCookie( "name" ) );
        } else {
            assertNull( "Cookie " + index + " should have been rejected", jar.getCookie( "name" ) );
        }
    }


    public void testCookieDefaults() throws Exception {
        checkDefaults( 1, "www.meterware.com/servlets/special", ".meterware.com", "/servlets", ".meterware.com", "/servlets" );
        checkDefaults( 2, "www.meterware.com/servlets/special/myServlet", null, null, "www.meterware.com", "/servlets/special" );
    }


    private void checkDefaults( int index, String urlString, String specifiedDomain, String specifiedPath,
                                String expectedDomain, String expectedPath ) throws MalformedURLException {
        CookieJar jar = newJar( urlString, specifiedDomain, specifiedPath );
        assertNotNull( "case " + index + " domain is null", jar.getCookie( "name" ).getDomain() );
        assertEquals( "case " + index + " domain", expectedDomain, jar.getCookie( "name" ).getDomain() );
        assertNotNull( "case " + index + " path is null", jar.getCookie( "name" ).getPath() );
        assertEquals( "case " + index + " path", expectedPath, jar.getCookie( "name" ).getPath() );
    }


    private CookieJar newJar( String urlString, String specifiedDomain, String specifiedPath ) throws MalformedURLException {
        StringBuffer header = new StringBuffer( "name=value" );
        if (specifiedDomain != null) header.append( "; domain=" ).append( specifiedDomain );
        if (specifiedPath != null) header.append( "; path=" ).append( specifiedPath );

        return new CookieJar( new TestSource( new URL( "http://" + urlString ), header.toString() ) );
    }

    /**
     * test cookie age and expiration handling
     * see also Friday Fun: I Hate Cookies 
     * http://www.mnot.net/blog/2006/10/27/cookie_fun
     * @throws Exception when an unexpected error occurs
     */
    public void testCookieAge() throws Exception {
        String ages[] = {"max-age=5000",
                         "Max-Age=3000",
                         "expires=Tue, 29-Mar-2005 19:30:42 GMT; Max-Age=2592000",
                         "Max-Age=2592000;expires=Tue, 29-Mar-2005 19:30:42 GMT",
                         "expires=Tue, 29-Mar-2005 19:30:42 GMT",
                         "Expires=Wednesday, 01-Jan-1970 0:0:00 GMT"
                         };
        long expectedMilliSeconds[] = {System.currentTimeMillis() + 5000 * 1000,
                                       System.currentTimeMillis() + 3000 * 1000,
                                       1112124642000l,
                                       1112124642000l,
                                       1112124642000l,
                                       0};

        for (int i = 0; i < ages.length; i++) {
            String index = "" + i;
            String cookieName = "cookie" + index.trim();
            String header = cookieName + "=cookievalue;" + ages[i];
            TestSource source = new TestSource( new URL( "http://www.somedomain.com/somepath/" ), header );
            CookieJar jar = new CookieJar( source );
            Cookie cookie = jar.getCookie( cookieName );
            assertNotNull( cookieName + " not null", cookie );

            long expiredTime = cookie.getExpiredTime();
            assertEquals( cookieName + " expiration", expiredTime, expectedMilliSeconds[i] );
        }
    }


    public void testHeaderGeneration() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putCookie( "zero", "nil" );
        jar.updateCookies( newJar( "www.meterware.com/servlets/standard/AServlet", "first=ready, gone=expired;max-age=0" ) );
        jar.updateCookies( newJar( "www.meterware.com/servlets/AnotherServlet", "second=set;max-age=1000" ) );
        jar.updateCookies( newJar( "www.httpunit.org", "zero=go; domain=.httpunit.org" ) );
        jar.updateCookies( newJar( "meterware.com", "fourth=money" ) );

        checkHeader( 1, jar, "first=ready; second=set; zero=nil", "www.meterware.com/servlets/standard/Count" );
        checkHeader( 2, jar, "second=set; zero=nil", "www.meterware.com/servlets/special/Divide" );
        checkHeader( 3, jar, "zero=go", "fancy.httpunit.org/servlets/AskMe" );

        HttpUserAgentTest.assertMatchingSet( "Cookie names",
                                             new String[] { "zero", "zero", "first", "second", "fourth", "gone" },
                                             jar.getCookieNames() );
    }
    
    /**
     * test for [ 1488617 ] alternate patch for cookie bug #1371204
     * @throws Exception
     */
    public void testSingleUseCookie() throws Exception {
      CookieJar jar = new CookieJar();
      jar.putSingleUseCookie("zero","nil","sourceforge.net","test/me");
      Cookie cookie=jar.getCookie("zero");
      assertTrue(cookie!=null);
      assertEquals(cookie.getDomain(),"sourceforge.net");
      assertEquals(cookie.getValue(),"nil");
      assertEquals(cookie.getPath(),"test/me");            
    }

    /**
     * test for bug report [ 1672385 ] HttpOnly cookie looses all cookie info
     * @throws Exception
     */
    public void testHttpOnlyCookies() throws Exception {
      CookieJar jar = new CookieJar(
              new TestSource( new URL( "http://www.meterware.com" ),
                              new String[] { "myStuff=1234; path=/foo; HttpOnly"} ) );
      assertEquals( "cookie 'myStuff' value", "1234", jar.getCookieValue( "myStuff" ) );
    }
    
    /**
     * test for bug report [ 1533762 ] Valid cookies are rejected
     * by Alexey Bulat 
     * TODO enable when working patch is available
     * @throws Exception
     */
    public void xtestCookiesRejection1533762() throws Exception {
      checkAcceptance( 1, true, "admin.automation.testing.com.ru", ".admin.automation.testing.com.ru",null);
      checkAcceptance( 2, true, "admin.automation.testing.com.ru", ".testing.com.ru", null );
    }
    

    private void checkHeader( int index, CookieJar jar, String expectedHeader, String targetURLString ) throws MalformedURLException {
        assertEquals( "header " + index, expectedHeader, jar.getCookieHeaderField( new URL( "http://" + targetURLString ) ) );
    }


    public void testCookieReplacement() throws Exception {
        CookieJar jar = new CookieJar();
        jar.updateCookies( newJar( "www.meterware.com/servlets/standard", "first=ready" ) );
        jar.updateCookies( newJar( "meterware.com/servlets/standard", "second=more" ) );
        jar.updateCookies( newJar( "www.meterware.com/servlets", "third=day" ) );
        jar.updateCookies( newJar( "www.meterware.com/servlets", "third=tomorrow" ) );

        checkHeader( 1, jar, "first=ready; third=tomorrow", "www.meterware.com/servlets/standard" );
    }


    private CookieJar newJar( String urlString, String setCookieHeader ) throws MalformedURLException {
        return new CookieJar( new TestSource( new URL( "http://" + urlString ), setCookieHeader ) );
    }


    public void testLenientMatching() throws Exception {
        CookieProperties.setDomainMatchingStrict( false );
        checkAcceptance( 1, true, "www.some.meterware.com/servlets/special", ".meterware.com", null );
        checkAcceptance( 2, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary" );
        checkAcceptance( 3, true, "www.meterware.com/servlets/special", "www.meterware.com", null );

        CookieProperties.setPathMatchingStrict( false );
        checkAcceptance( 11, true, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary" );
        checkMatching( 12, true, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets/sample/data" );
    }


    public void testRejectionCallbacks() throws Exception {
        MockListener listener = new MockListener();
        CookieProperties.addCookieListener( listener );

        checkCallback( listener, 1, 0, "www.meterware.com/servlets/special", null, null );
        checkCallback( listener, 2, CookieListener.PATH_NOT_PREFIX, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary" );
        checkCallback( listener, 3, CookieListener.DOMAIN_ONE_DOT, "www.meterware.com/servlets/special", ".com", null );
        checkCallback( listener, 4, CookieListener.DOMAIN_NOT_SOURCE_SUFFIX, "www.meterware.com/servlets/special", ".httpunit.org", null );
        checkCallback( listener, 5, CookieListener.DOMAIN_TOO_MANY_LEVELS, "www.some.meterware.com/servlets/special", ".meterware.com", null );
    }


    /**
     * check the cookieListener call Back 
     * @param listener
     * @param index
     * @param status
     * @param urlString
     * @param specifiedDomain
     * @param specifiedPath
     * @throws MalformedURLException
     */
    private void checkCallback( MockListener listener, int index, int status, String urlString,
                                String specifiedDomain, String specifiedPath ) throws MalformedURLException {
        if (status == 0) {
            listener.expectAcceptance( index );
        } else if (status == CookieListener.PATH_NOT_PREFIX) {
            listener.expectRejection( index, "name", status, specifiedPath );
        } else {
            listener.expectRejection( index, "name", status, specifiedDomain );
        }
        newJar( urlString, specifiedDomain, specifiedPath );
        if (status != 0) listener.confirmRejection();
    }


    private class MockListener implements CookieListener {

        private int _reason;
        private String _attribute;
        private String _cookieName;
        private boolean _rejected;
        private int _cookieNum;


        void expectAcceptance( int cookieNum ) {
            _cookieNum = cookieNum;
            _reason = -1;
        }


        void expectRejection( int cookieNum, String cookieName, int reason, String attribute ) {
            _cookieNum = cookieNum;
            _reason = reason;
            _attribute = attribute;
            _cookieName = cookieName;
            _rejected = false;
        }


        void confirmRejection() {
            Assert.assertTrue( "Cookie " + _cookieNum + " was not logged as rejected", _rejected );
        }


        public void cookieRejected( String name, int reason, String attribute ) {
            _rejected = true;
            Assert.assertEquals( "Cookie " + _cookieNum + " rejection code", _reason, reason );
            if (_attribute != null) Assert.assertEquals( "Cookie " + _cookieNum + " rejected attribute", _attribute, attribute );
            if (_cookieName != null) Assert.assertEquals( "Cookie " + _cookieNum + " name", _cookieName, name );
        }
    }



    /**
     * create a TestSource for Cookies 
     *
     */
    private class TestSource implements CookieSource {

        private URL _sourceURL;
        private String[] _headers;

        /**
         * construct a TestSource form a single header string
         * @param sourceURL
         * @param header
         */

        public TestSource( URL sourceURL, String header ) {
            this( sourceURL, new String[] { header } );
        }


        public TestSource( URL sourceURL, String[] headers ) {
            _sourceURL = sourceURL;
            _headers = headers;
        }


        public URL getURL() {
            return _sourceURL;
        }


        public String[] getHeaderFields( String fieldName ) {
            return fieldName.equalsIgnoreCase( "set-cookie" ) ? _headers : new String[0];
        }
    }


}
