package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: WebXMLTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2001-2004,2006,2008 Russell Gold
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
import com.meterware.httpunit.*;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;

import javax.servlet.*;
import javax.servlet.http.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.TestSuite;
import junit.framework.TestCase;


/**
 * Tests for Web xml access 
 *
 */
public class WebXMLTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebXMLTest.class );
    }


    public WebXMLTest( String name ) {
        super( name );
    }

    /**
     * if the dtd file is not on the CLASSPATH there will be nasty java.net.MalformedURLException problems
     * for the Eclipse environment we'll give advice what to do
     * 
     * @throws Exception
     */
    public void testDTDClassPath() throws Exception {
    	boolean isDtdOnClasspath=WebXMLString.isDtdOnClasspath();
    	String msg=WebXMLString.dtd+" should be on CLASSPATH - you might want to check that META-INF is on the CLASSPATH";
    	if (!isDtdOnClasspath) {
    		System.err.println(msg);
    		if (HttpUnitUtils.isEclipse()) {        	
      		System.err.println("You seem to be running in the Eclipse environment you might want to check the project settings build path to include the META-INF directory");
      		System.err.println("To do this select properties/Java Build Path from your project, click 'Add Class Folder' and select the META-INF directory of the httpunit project");
      	}
    		System.err.println("the other tests will work around the problem by changing the DOCTYPE to avoid lots of java.net.MalformedURLExceptions");
      }
      assertTrue(msg,isDtdOnClasspath);
    }

    public void testBasicAccess() throws Exception {

        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );
        File webXml = createWebXml( wxs );

        ServletRunner sr = new ServletRunner( webXml );
        WebRequest request   = new GetMethodWebRequest( "http://localhost/SimpleServlet" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    public void testRealPath() throws Exception {

        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );
        File webXml = createWebXml( new File( "build/base" ), wxs );

        assertRealPath( "path with no context", new ServletRunner( webXml ), new File( "something.txt" ), "/something.txt" );
        assertRealPath( "path with context", new ServletRunner( webXml, "/testing" ), new File( "build/base/something.txt" ), "/something.txt" );
        // attempt for an assertion a long the line of bug report [ 1113728 ] getRealPath throws IndexOutOfBoundsException on empty string
        // TODO check what was meant by Adrian Baker
        // assertRealPath( "empty path with context", new ServletRunner( webXml, "/testing" ), new File( "" ), "/testing" );
        assertRealPath( "path with no context, no slash", new ServletRunner( webXml ), new File( "something.txt" ), "something.txt" );
        assertRealPath( "path with context, no slash", new ServletRunner( webXml, "/testing" ), new File( "build/base/something.txt" ), "something.txt" );
    }

    private void assertRealPath( String comment, ServletRunner sr, File expectedFile, String relativePath ) {
        String realPath = sr.getSession(true).getServletContext().getRealPath( relativePath );
        assertEquals( comment, expectedFile.getAbsolutePath(), realPath );
    }


    private File createWebXml( WebXMLString wxs ) throws IOException {
        return createWebXml( new File("build"), wxs );
    }

    private File createWebXml( File parent, WebXMLString wxs ) throws IOException {
        File dir = new File( parent, "META-INF" );
        dir.mkdirs();
        File webXml = new File( dir, "web.xml" );
        FileOutputStream fos = new FileOutputStream( webXml );
        fos.write( wxs.asText().getBytes() );
        fos.close();
        return webXml;
    }


    public void testBasicAuthenticationConfig() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.requireBasicAuthentication( "SampleRealm" );

        WebApplication app = new WebApplication( newDocument( wxs.asText() ) );
        assertTrue( "Did not detect basic authentication", app.usesBasicAuthentication() );
        assertEquals( "Realm name", "SampleRealm", app.getAuthenticationRealm() );
    }


    public void testFormAuthenticationConfig() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.requireFormAuthentication( "SampleRealm", "/Login", "/Error" );

        WebApplication app = new WebApplication( newDocument( wxs.asText() ) );
        assertTrue( "Did not detect form-based authentication", app.usesFormAuthentication() );
        assertEquals( "Realm name", "SampleRealm", app.getAuthenticationRealm() );
        assertEquals( "Login path", "/Login", app.getLoginURL().getFile() );
        assertEquals( "Error path", "/Error", app.getErrorURL().getFile() );
    }


    public void testSecurityConstraint() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addSecureURL( "SecureArea1", "/SimpleServlet" );
        wxs.addAuthorizedRole( "SecureArea1", "supervisor" );

        WebApplication app = new WebApplication( newDocument( wxs.asText() ) );
        assertTrue( "Did not require authorization", app.requiresAuthorization( new URL( "http://localhost/SimpleServlet" ) ) );
        assertTrue( "Should not require authorization", !app.requiresAuthorization( new URL( "http://localhost/FreeServlet" ) ) );

        List roles = Arrays.asList( app.getPermittedRoles( new URL( "http://localhost/SimpleServlet" ) ) );
        assertTrue( "Should have access", roles.contains( "supervisor" ) );
        assertTrue( "Should not have access", !roles.contains( "peon" ) );
    }


    /**
     * Verifies that the default display name is null.
     */
    public void testDefaultContextNameConfiguration() throws Exception {
        WebXMLString wxs = new WebXMLString();
        WebApplication app = new WebApplication( newDocument( wxs.asText() ) );
        assertNull( "Context name should default to null", app.getDisplayName() );
    }


    /**
     * Verifies that a web application can read its display name from the configuration.
     * @throws Exception
     */
    public void testContextNameConfiguration() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.setDisplayName( "samples" );
        wxs.addServlet( "simple", "/SimpleServlet", SimpleGetServlet.class );
        WebApplication app = new WebApplication( newDocument( wxs.asText() ) );
        assertEquals( "Display name", "samples", app.getDisplayName() );

        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient client = sr.newClient();
        InvocationContext ic = client.newInvocation( "http://localhost/SimpleServlet" );
        ServletContext servletContext = ic.getServlet().getServletConfig().getServletContext();
        assertEquals( "Context name", "samples", servletContext.getServletContextName() );
    }


    public void testServletParameters() throws Exception {
        WebXMLString wxs = new WebXMLString();
        Properties params = new Properties();
        params.setProperty( "color", "red" );
        params.setProperty( "age", "12" );
        wxs.addServlet( "simple", "/SimpleServlet", SimpleGetServlet.class, params );

        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient client = sr.newClient();
        InvocationContext ic = client.newInvocation( "http://localhost/SimpleServlet" );
        ServletConfig servletConfig = ic.getServlet().getServletConfig();
        assertEquals( "Servlet name", "simple", servletConfig.getServletName() );
        assertNull( "init parameter 'gender' should be null", servletConfig.getInitParameter( "gender" ) );
        assertEquals( "init parameter via config", "red", ic.getServlet().getServletConfig().getInitParameter( "color" ) );
        assertEquals( "init parameter directly", "12", ((HttpServlet) ic.getServlet()).getInitParameter( "age" ) );
    }


    public void testContextParameters() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );
        wxs.addContextParam( "icecream", "vanilla" );
        wxs.addContextParam( "cone", "waffle" );
        wxs.addContextParam( "topping", "" );

        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient client = sr.newClient();
        assertEquals( "Context parameter 'icecream'", "vanilla", sr.getContextParameter( "icecream" ) );
        InvocationContext ic = client.newInvocation( "http://localhost/SimpleServlet" );

        javax.servlet.ServletContext sc = ((HttpServlet) ic.getServlet()).getServletContext();
        assertNotNull( "ServletContext should not be null", sc );
        assertEquals( "ServletContext.getInitParameter()", "vanilla", sc.getInitParameter( "icecream" ) );
        assertEquals( "init parameter: cone", "waffle", sc.getInitParameter( "cone" ) );
        assertEquals( "init parameter: topping", "", sc.getInitParameter( "topping" ) );
        assertNull( "ServletContext.getInitParameter() should be null", sc.getInitParameter( "shoesize" ) );
    }
    
    /**
     * test for Patch [ 1838699 ] setContextParameter in ServletRunner
     * @throws Exception
     */
    public void xtestSetContextParameter() throws Exception {
      WebXMLString wxs = new WebXMLString();
      wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );
      wxs.addContextParam( "icecream", "vanilla" );

      ServletRunner sr = new ServletRunner( wxs.asInputStream() );
      ServletUnitClient client = sr.newClient();
      sr.setContextParameter("icecream", "strawberry");
      assertEquals( "Context parameter 'icecream'", "strawberry", sr.getContextParameter( "icecream" ) );
      
      InvocationContext ic = client.newInvocation( "http://localhost/SimpleServlet" );

      javax.servlet.ServletContext sc = ((HttpServlet) ic.getServlet()).getServletContext();
      assertNotNull( "ServletContext should not be null", sc );
      assertEquals( "ServletContext.getInitParameter()", "strawberry", sc.getInitParameter( "icecream" ) );
      assertNull( "ServletContext.getInitParameter() should be null", sc.getInitParameter( "shoesize" ) );    	
    }


    /**
     * create a new document based on the given contents
     * @param contents
     * @return the new document
     * @throws UnsupportedEncodingException
     * @throws SAXException
     * @throws IOException
     */
    private Document newDocument( String contents ) throws UnsupportedEncodingException, SAXException, IOException {
       return HttpUnitUtils.parse( toInputStream( contents ) );
    }


    private ByteArrayInputStream toInputStream( String contents ) throws UnsupportedEncodingException {
        return new ByteArrayInputStream( contents.getBytes( "UTF-8" ) );
    }


    public void testBasicAuthorization() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );
        wxs.requireBasicAuthentication( "Sample Realm" );
        wxs.addSecureURL( "SecureArea1", "/SimpleServlet" );
        wxs.addAuthorizedRole( "SecureArea1", "supervisor" );

        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient  wc = sr.newClient();
        try {
            wc.getResponse( "http://localhost/SimpleServlet" );
            fail( "Did not insist on validation for access to servlet" );
        } catch (AuthorizationRequiredException e) {
            assertEquals( "Realm", "Sample Realm", e.getAuthenticationParameter( "realm" ) );
            assertEquals( "Method", "Basic", e.getAuthenticationScheme() );
        }

        try {
            wc.setAuthorization( "You", "peon" );
            wc.getResponse( "http://localhost/SimpleServlet" );
            fail( "Permitted wrong user to access" );
        } catch (HttpException e) {
            assertEquals( "Response code", 403, e.getResponseCode() );
        }

        wc.setAuthorization( "Me", "supervisor,agent" );
        wc.getResponse( "http://localhost/SimpleServlet" );

        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );
        assertEquals( "Authenticated user", "Me", ic.getRequest().getRemoteUser() );
        assertTrue( "User assigned to 'bogus' role", !ic.getRequest().isUserInRole( "bogus" ) );
        assertTrue( "User not assigned to 'supervisor' role", ic.getRequest().isUserInRole( "supervisor" ) );
    }


    public void testFormAuthentication() throws Exception {
        HttpUnitOptions.setLoggingHttpHeaders( true );
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/Logon", SimpleLogonServlet.class );
        wxs.addServlet( "/Error", SimpleErrorServlet.class );
        wxs.addServlet( "/Example/SimpleServlet", SimpleGetServlet.class );
        wxs.requireFormAuthentication( "Sample Realm", "/Logon", "/Error" );
        wxs.addSecureURL( "SecureArea1", "/Example/SimpleServlet" );
        wxs.addAuthorizedRole( "SecureArea1", "supervisor" );
        File webXml = createWebXml( wxs );

        ServletRunner sr = new ServletRunner( webXml, "/samples" );
        ServletUnitClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/samples/Example/SimpleServlet" );
        WebForm form = response.getFormWithID( "login" );
        assertNotNull( "did not find login form", form );

        WebRequest request = form.getRequest();
        request.setParameter( "j_username", "Me" );
        request.setParameter( "j_password", "supervisor" );
        response = wc.getResponse( request );
        assertNotNull( "No response received after authentication", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );

        InvocationContext ic = wc.newInvocation( "http://localhost/samples/Example/SimpleServlet" );
        assertEquals( "Authenticated user", "Me", ic.getRequest().getRemoteUser() );
        assertTrue( "User assigned to 'bogus' role", !ic.getRequest().isUserInRole( "bogus" ) );
        assertTrue( "User not assigned to 'supervisor' role", ic.getRequest().isUserInRole( "supervisor" ) );
    }


    public void testGetContextPath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/mount" );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/mount/SimpleServlet" );
        assertEquals("/mount", ic.getRequest().getContextPath());

        sr = new ServletRunner( wxs.asInputStream() );
        wc = sr.newClient();
        ic = wc.newInvocation( "http://localhost/SimpleServlet" );
        assertEquals("", ic.getRequest().getContextPath());
    }


    public void testMountContextPath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/SimpleServlet", SimpleGetServlet.class );

        ServletRunner sr = new ServletRunner( wxs.asInputStream(), "/mount" );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/mount/SimpleServlet" );
        assertTrue(ic.getServlet() instanceof SimpleGetServlet);
        assertEquals("/mount/SimpleServlet", ic.getRequest().getRequestURI());

        try {
            ic = wc.newInvocation( "http://localhost/SimpleServlet" );
            ic.getServlet();
            fail("Attempt to access url outside of the webapp context path should have thrown a 404");
        } catch (com.meterware.httpunit.HttpNotFoundException e) {}
    }


    public void testServletMapping() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "/foo/bar/*", Servlet1.class );
        wxs.addServlet( "/baz/*", Servlet2.class );
        wxs.addServlet( "/catalog", Servlet3.class );
        wxs.addServlet( "*.bop", Servlet4.class );
        wxs.addServlet( "/",     Servlet5.class );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();

        checkMapping( wc, "http://localhost/foo/bar/index.html",  Servlet1.class, "/foo/bar",             "/index.html" );
        checkMapping( wc, "http://localhost/foo/bar/index.bop",   Servlet1.class, "/foo/bar",             "/index.bop" );
        checkMapping( wc, "http://localhost/baz",                 Servlet2.class, "/baz",                 null );
        checkMapping( wc, "http://localhost/baz/index.html",      Servlet2.class, "/baz",                 "/index.html" );
        checkMapping( wc, "http://localhost/catalog",             Servlet3.class, "/catalog",             null );
        checkMapping( wc, "http://localhost/catalog/racecar.bop", Servlet4.class, "/catalog/racecar.bop", null );
        checkMapping( wc, "http://localhost/index.bop",           Servlet4.class, "/index.bop",           null );
        checkMapping( wc, "http://localhost/something/else",      Servlet5.class, "/something/else",      null );
    }


    private void checkMapping( ServletUnitClient wc, final String url, final Class servletClass, final String expectedPath, final String expectedInfo ) throws IOException, ServletException {
        InvocationContext ic = wc.newInvocation( url );
        assertTrue( "selected servlet is " + ic.getServlet() + " rather than " + servletClass, servletClass.isInstance( ic.getServlet() ) );
        assertEquals( "ServletPath for " + url, expectedPath, ic.getRequest().getServletPath() );
        assertEquals( "ServletInfo for " + url, expectedInfo, ic.getRequest().getPathInfo() );
    }


    /**
     * Verifies that only those servlets designated will pre-load when the application is initialized.
     * SimpleGetServlet and each of its subclasses adds its classname to the 'initialized' context attribute.
     */
    public void testLoadOnStartup() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "servlet1", "one", Servlet1.class );
        wxs.setLoadOnStartup( "servlet1" );
        wxs.addServlet( "servlet2", "two", Servlet2.class );
        wxs.addServlet( "servlet3", "three", Servlet3.class );

        ServletRunner sr = new ServletRunner( toInputStream( wxs.asText() ) );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/three" );
        assertEquals( "Initialized servlets", "Servlet1,Servlet3", ic.getServlet().getServletConfig().getServletContext().getAttribute( "initialized" ) );
    }


    /**
     * Verifies that servlets pre-load in the order specified.
     * SimpleGetServlet and each of its subclasses adds its classname to the 'initialized' context attribute.
     */
    public void testLoadOrder() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "servlet1", "one", Servlet1.class );
        wxs.setLoadOnStartup( "servlet1", 2 );
        wxs.addServlet( "servlet2", "two", Servlet2.class );
        wxs.setLoadOnStartup( "servlet2", 3 );
        wxs.addServlet( "servlet3", "three", Servlet3.class );
        wxs.setLoadOnStartup( "servlet3", 1 );

        ServletRunner sr = new ServletRunner( toInputStream( wxs.asText() ) );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/two" );
        assertEquals( "Initialized servlets", "Servlet3,Servlet1,Servlet2", ic.getServlet().getServletConfig().getServletContext().getAttribute( "initialized" ) );
    }



//===============================================================================================================


//===============================================================================================================


    static class SimpleLogonServlet extends HttpServlet {
        static String RESPONSE_TEXT = "<html><body>\r\n" +
                                      "<form id='login' action='j_security_check' method='POST'>\r\n" +
                                      "  <input name='j_username' />\r\n" +
                                      "  <input type='password' name='j_password' />\r\n" +
                                      "</form></body></html>";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }

//===============================================================================================================


    static class SimpleErrorServlet extends HttpServlet {
        static String RESPONSE_TEXT = "<html><body>Sorry could not login</body></html>";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }

//===============================================================================================================


    static class SimpleGetServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        public void init() throws ServletException {
            ServletConfig servletConfig = getServletConfig();
            String initialized = (String) servletConfig.getServletContext().getAttribute( "initialized" );
            if (initialized == null) initialized = getLocalName();
            else initialized = initialized + "," + getLocalName();
            servletConfig.getServletContext().setAttribute( "initialized", initialized );
        }

        private String getLocalName() {
            String className = getClass().getName();
            int dollarIndex = className.indexOf( '$' );
            if (dollarIndex < 0) return className;
            return className.substring( dollarIndex+1 );
        }

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }

    static class Servlet1 extends SimpleGetServlet {}
    static class Servlet2 extends SimpleGetServlet {}
    static class Servlet3 extends SimpleGetServlet {}
    static class Servlet4 extends SimpleGetServlet {}
    static class Servlet5 extends SimpleGetServlet {}


}






