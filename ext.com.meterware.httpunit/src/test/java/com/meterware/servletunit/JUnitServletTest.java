package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: JUnitServletTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2001-2004, Russell Gold
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Dictionary;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class JUnitServletTest extends TestCase {
    private ServletRunner _runner;


    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( JUnitServletTest.class );
    }


    public JUnitServletTest( String name ) {
        super( name );
    }


    public void testNoTestClassSpecified() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit" );
        assertTrue( "Did not find error message", wr.getText().indexOf( "Cannot run" ) >= 0 );
    }


    public void testBadTestClassSpecified() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?test=gobbledygook" );
        assertTrue( "Did not find error message", wr.getText().indexOf( "Cannot run" ) >= 0 );
    }


    public void testAllTestsPass() throws Exception {
        ServletUnitClient client = newClient();
        WebResponse wr = client.getResponse( "http://localhost/JUnit?test=" + PassingTests.class.getName() );
        final WebTable resultsTable = wr.getTableWithID( "results" );
        assertNotNull( "Did not find results table", resultsTable );
        final String[][] results = resultsTable.asText();
        assertEquals( "Num rows", 1, results.length );
        assertEquals( "Num columns", 3, results[0].length );
        assertEquals( "Time header", "1 test", results[0][0] );
        assertEquals( "Status", "OK", results[0][2] );
    }


    public void testAllTestsPassTextFormat() throws Exception {
        ServletUnitClient client = newClient();
        WebResponse wr = client.getResponse( "http://localhost/JUnit?format=text&test=" + PassingTests.class.getName() );
        String expectedStart = PassingTests.class.getName() + " (1 test): OK";
        assertTrue( "Results (" + wr.getText() + ") should start with '" + expectedStart, wr.getText().startsWith( expectedStart ) );
    }


    public void testSomeFailures() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?test=" + FailingTests.class.getName() );
        final WebTable resultsTable = wr.getTableWithID( "results" );
        assertNotNull( "Did not find results table", resultsTable );
        final String[][] results = resultsTable.asText();
        assertEquals( "Num rows", 4, results.length );
        assertEquals( "Num columns", 3, results[0].length );
        assertEquals( "First header", "3 tests", results[0][0] );
        assertEquals( "Status", "Problems Occurred", results[0][2] );
        assertEquals( "Failure header", "2 failures", results[1][1] );
        assertEquals( "Failure index 1", "1", results[2][0] );
        assertEquals( "Failure index 2", "2", results[3][0] );
        assertTrue( "Test class not found", results[2][1].indexOf( '(' + FailingTests.class.getName() + ')' ) >= 0 );
    }


    public void testSomeFailuresTextFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?format=text&test=" + FailingTests.class.getName() );
        String expectedStart = FailingTests.class.getName() + " (3 tests): Problems Occurred";
        assertTrue( "Results (" + wr.getText() + ") should start with: " + expectedStart, wr.getText().startsWith( expectedStart ) );
        assertTrue( "Results (" + wr.getText() + ") should contain: 2 failures", wr.getText().indexOf( "2 failures" ) >= 0 );
    }


    public void testSomeErrors() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?test=" + ErrorTests.class.getName() );
        final WebTable resultsTable = wr.getTableWithID( "results" );
        assertNotNull( "Did not find results table", resultsTable );
        final String[][] results = resultsTable.asText();
        assertEquals( "Num rows", 3, results.length );
        assertEquals( "Num columns", 3, results[0].length );
        assertEquals( "First header", "2 tests", results[0][0] );
        assertEquals( "Status", "Problems Occurred", results[0][2] );
        assertEquals( "Failure header", "1 error", results[1][1] );
        assertEquals( "Failure index 1", "1", results[2][0] );
        assertTrue( "Test class not found", results[2][1].indexOf( '(' + ErrorTests.class.getName() + ')' ) >= 0 );
    }


    public void testSomeFailuresXMLFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?format=xml&test=" + FailingTests.class.getName() );
        assertEquals( "Content type", "text/xml", wr.getContentType() );
        DocumentBuilder builder = HttpUnitUtils.newParser();
        Document document = builder.parse( wr.getInputStream() );
        Element element = document.getDocumentElement();
        assertEquals( "document element name", "testsuite", element.getNodeName() );
        assertEquals( "number of tests", "3", element.getAttribute( "tests" ) );
        assertEquals( "number of failures", "2", element.getAttribute( "failures" ) );
        assertEquals( "number of errors", "0", element.getAttribute( "errors" ) );
        NodeList nl = element.getElementsByTagName( "testcase" );
        verifyElementWithNameHasFailureNode( "testAddition", nl, /* failed */ "failure", true );
        verifyElementWithNameHasFailureNode( "testSubtraction", nl, /* failed */ "failure", true );
        verifyElementWithNameHasFailureNode( "testMultiplication", nl, /* failed */ "failure", false );
    }


    public void testSomeErrorsXMLFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse( "http://localhost/JUnit?format=xml&test=" + ErrorTests.class.getName() );
        assertEquals( "Content type", "text/xml", wr.getContentType() );
        DocumentBuilder builder = HttpUnitUtils.newParser();
        Document document = builder.parse( wr.getInputStream() );
        Element element = document.getDocumentElement();
        assertEquals( "document element name", "testsuite", element.getNodeName() );
        assertEquals( "number of tests", "2", element.getAttribute( "tests" ) );
        assertEquals( "number of failures", "0", element.getAttribute( "failures" ) );
        assertEquals( "number of errors", "1", element.getAttribute( "errors" ) );
        NodeList nl = element.getElementsByTagName( "testcase" );
        verifyElementWithNameHasFailureNode( "testAddition", nl, /* failed */ "error", true );
        verifyElementWithNameHasFailureNode( "testMultiplication", nl, /* failed */ "error", false );
    }


    private void verifyElementWithNameHasFailureNode( String name, NodeList nl, String nodeName, boolean failed ) {
        for (int i = 0; i < nl.getLength(); i++) {
            Element element = (Element) nl.item(i);
            if (element.getAttribute( "name" ).indexOf( name ) >= 0) {
                if (failed) {
                    assertEquals( "no " + nodeName + " element found for test '" + name + "'", 1, element.getElementsByTagName( nodeName ).getLength() );
                } else {
                    assertEquals( "unexpected " + nodeName + " element found for test '" + name + "'", 0, element.getElementsByTagName( nodeName ).getLength() );
                }
                return;
            }
        }
        if (failed) fail( "No test result found for '" + name + "'" );
    }


    public void testScriptedServletAccess() throws Exception {
        WebXMLString wxs = new WebXMLString();
        Properties params = new Properties();
        params.setProperty( "color", "red" );
        params.setProperty( "age", "12" );
        wxs.addServlet( "simple", "/SimpleServlet", SimpleGetServlet.class, params );
        wxs.addServlet( "/JUnit", TestRunnerServlet.class );

        MyFactory._runner = _runner = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient client = _runner.newClient();
        WebResponse wr = client.getResponse( "http://localhost/JUnit?test=" + ServletAccessTest.class.getName() );

        final WebTable resultsTable = wr.getTableWithID( "results" );
        assertNotNull( "Did not find results table", resultsTable );
        final String[][] results = resultsTable.asText();
        assertEquals( "Status", "OK", results[0][2] );
    }


    private ServletUnitClient newClient() {
        _runner = new ServletRunner();
        MyFactory._runner = _runner;
        _runner.registerServlet( "/JUnit", TestRunnerServlet.class.getName() );
        ServletUnitClient client = _runner.newClient();
        return client;
    }



//===============================================================================================================


    static class SimpleGetServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }


    static class TestRunnerServlet extends JUnitServlet {

        public TestRunnerServlet() {
            super( new MyFactory() );
        }
    }


    static class MyFactory implements InvocationContextFactory {
        private static ServletRunner _runner;

        public InvocationContext newInvocation( ServletUnitClient client, FrameSelector targetFrame, WebRequest request, Dictionary clientHeaders, byte[] messageBody ) throws IOException, MalformedURLException {
            return new InvocationContextImpl( client, _runner, targetFrame, request, clientHeaders, messageBody );
        }


        public HttpSession getSession( String sessionId, boolean create ) {
            return null;
        }
    }
}
