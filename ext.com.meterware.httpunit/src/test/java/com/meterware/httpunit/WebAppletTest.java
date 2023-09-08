package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: WebAppletTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002, Russell Gold
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
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebApplet;

import java.applet.Applet;
import java.util.Enumeration;
import java.net.URL;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import junit.textui.TestRunner;
import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public class WebAppletTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebAppletTest.class );
    }


    public WebAppletTest( String name ) {
        super( name );
    }

    public void testDeleteMe() {
        new WebConversation();
    }


    public void testFindApplets() throws Exception {
        defineWebPage( "start", "<applet code='FirstApplet.class' width=150 height=100></applet>" +
                                "<applet code='SecondApplet.class' width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet[] applets = response.getApplets();
        assertNotNull( "No applet found", applets );
        assertEquals( "number of applets in page", 2, applets.length );
    }


    public void testAppletProperties() throws Exception {
        defineWebPage( "start", "<applet code='FirstApplet.class' name=first codebase='/classes' width=150 height=100></applet>" +
                                "<applet code='SecondApplet.class' name=second width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet applet1 = response.getApplets()[0];
        WebApplet applet2 = response.getApplets()[1];
        assertEquals( "Applet 1 codebase", getHostPath() + "/classes/", applet1.getCodeBaseURL().toExternalForm() );
        assertEquals( "Applet 2 codebase", getHostPath() + "/", applet2.getCodeBaseURL().toExternalForm() );

        assertEquals( "Applet 1 name", "first", applet1.getName() );
        assertEquals( "Applet 1 width", 150, applet1.getWidth() );
        assertEquals( "Applet 1 height", 100, applet1.getHeight() );
    }


    public void testReadAppletParameters() throws Exception {
        defineWebPage( "start", "<applet code='DoIt'>" +
                                "  <param name='color' value='ffff00'>" +
                                "  <param name='age' value='12'>" +
                                "</applet>" );

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet applet = response.getApplets()[0];
        assertNotNull( "Parameter names return null", applet.getParameterNames() );
        assertEquals( "Number of parameters", 2, applet.getParameterNames().length );
        assertMatchingSet( "Parameter names", new String[]{"color", "age"}, applet.getParameterNames() );
    }


    public void testAppletClassName() throws Exception {
        defineWebPage( "start", "<applet code='com/something/FirstApplet.class' width=150 height=100></applet>" +
                                "<applet code='org\\nothing\\SecondApplet' width=150 height=100></applet>" +
                                "<applet code='net.ThirdApplet.class' width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        assertEquals( "Applet 1 classname", "com.something.FirstApplet", response.getApplets()[0].getMainClassName() );
        assertEquals( "Applet 2 classname", "org.nothing.SecondApplet", response.getApplets()[1].getMainClassName() );
        assertEquals( "Applet 3 classname", "net.ThirdApplet", response.getApplets()[2].getMainClassName() );
    }


    public void testAppletLoading() throws Exception {
        defineWebPage( "start", "<applet code='" + SimpleApplet.class.getName() +
                                ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath( "/classes" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet wa = response.getApplets()[0];
        Applet applet = wa.getApplet();
        assertNotNull( "Applet was not loaded", applet );
        assertEquals( "Applet class", SimpleApplet.class.getName(), applet.getClass().getName() );
    }


    public void notestAppletArchive() throws Exception {
        defineWebPage( "start", "<applet archive='/lib/xercesImpl.jar,/lib/xmlParserAPIs.jar'" +
                                " code='" + XMLApplet.class.getName() + ".class'" +
                                " codebase=/classes width=100 height=100></applet>");
        mapToClasspath( "/classes" );
        mapToClasspath( "/lib" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        Applet applet = response.getApplets()[0].getApplet();
        Method testMethod = applet.getClass().getMethod( "getDocumentBuilder", new Class[0] );
        Object result = testMethod.invoke( applet, new Object[0] );
        assertEquals( "Superclass name", DocumentBuilder.class.getName(), result.getClass().getSuperclass().getName() );
    }


    public void testAppletParameterAccess() throws Exception {
        defineWebPage( "start", "<applet code='" + SimpleApplet.class.getName() +
                                ".class' codebase=/classes width=100 height=100>" +
                                "  <param name='color' value='ffff00'>" +
                                "  <param name='age' value='12'>" +
                                "</applet>");
        mapToClasspath( "/classes" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        Applet applet = response.getApplets()[0].getApplet();
        assertNull( "Applet parameter 'hue' should be null", applet.getParameter( "hue" ) );
        assertEquals( "Applet parameter 'color'", "ffff00", applet.getParameter( "color" ) );
        assertEquals( "Applet parameter 'age'", "12", applet.getParameter( "age" ) );
    }


    public void testAppletFindFromApplet() throws Exception {
        defineWebPage( "start", "<applet name=first code='" + SimpleApplet.class.getName() +
                                ".class' codebase=/classes width=100 height=100></applet>" +
                                "<applet name=second code='" + SecondApplet.class.getName() +
                                ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath( "/classes" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        Applet applet = response.getApplets()[0].getApplet();
        Applet applet2 = applet.getAppletContext().getApplet( "second" );
        assertNotNull( "Applet was not loaded", applet2 );
        assertEquals( "Applet class", SecondApplet.class.getName(), applet2.getClass().getName() );

        Enumeration applets = applet2.getAppletContext().getApplets();
        assertNotNull( "No applet enumeration returned", applets );
        assertTrue( "No applets in enumeration", applets.hasMoreElements() );
        assertTrue( "First is not an applet", applets.nextElement() instanceof Applet );
        assertTrue( "Only one applet in enumeration", applets.hasMoreElements() );
        assertTrue( "Second is not an applet", applets.nextElement() instanceof Applet );
        assertFalse( "More than two applets enumerated", applets.hasMoreElements() );
    }


    public void testShowDocument() throws Exception {
        defineResource( "next.html", "You made it!" );
        defineWebPage( "start", "<applet code='" + SimpleApplet.class.getName() +
                                ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath( "/classes" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet wa = response.getApplets()[0];
        Applet applet = wa.getApplet();
        applet.getAppletContext().showDocument( new URL( getHostPath() + "/next.html" ) );
        assertEquals( "current page URL", getHostPath() + "/next.html", wc.getCurrentPage().getURL().toExternalForm() );
    }
    
    /**
     * test for bug report [ 1895501 ] Handling no codebase attribute in APPLET tag
     * by lacton
     * @throws Exception
     */
    public void testAppletWithinADirectory() throws Exception {
      defineWebPage( "directory/start", "<applet code='"+SimpleApplet.class.getName()+"'></applet>" );
      mapToClasspath( "/directory" );
      WebConversation wc = new WebConversation();
      WebResponse response = wc.getResponse( getHostPath() + "/directory/start.html" );
      WebApplet wa = response.getApplets()[0];
      assertEquals( "Applet codebase", getHostPath() + "/directory/", wa.getCodeBaseURL().toExternalForm() );
      Applet applet = wa.getApplet();
      assertNotNull( "Applet was not loaded", applet );
      assertEquals( "Applet class", SimpleApplet.class.getName(), applet.getClass().getName() );
    }



    public static class SimpleApplet extends Applet {
    }

    public static class SecondApplet extends SimpleApplet {
    }

    public static class XMLApplet extends Applet {
        public DocumentBuilder getDocumentBuilder() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder();
        }
    }

}
