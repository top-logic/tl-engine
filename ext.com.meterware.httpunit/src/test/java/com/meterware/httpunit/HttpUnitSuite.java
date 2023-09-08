package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: HttpUnitSuite.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2008, Russell Gold
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
import com.meterware.pseudoserver.PseudoServerTest;
import com.meterware.httpunit.cookies.CookieTest;
import com.meterware.httpunit.javascript.NekoEnhancedScriptingTest;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.meterware.httpunit.parsing.ParsingTestSuite;
import com.meterware.httpunit.ssl.HttpsProtocolSupportTest;
import com.meterware.httpunit.dom.DomTestSuite;

import junit.framework.AssertionFailedError;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.framework.Test;


/**
 * Tests for the httpunit package.
 **/
public class HttpUnitSuite extends ConditionalTestSuite {
	
	  /**
	   * entry point to run suite from command line
	   * @param args - command line arguments
	   */
    public static void main( String[] args ) {
        try {
        	HttpUnitTest.warnDelim="\n";
  	  		TestResult result=junit.textui.TestRunner.run(suite());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }
	
	
    /**
     * get the suite of tests
     * @return the test suite
     */
    public static Test suite() {
        TestSuite result = new TestSuite();
        result.addTest( WebPageTest.suite() );
        result.addTest( WebLinkTest.suite() );
        result.addTest( WebImageTest.suite() );
        result.addTest( HtmlTablesTest.suite() );
        result.addTest( WebFormTest.suite() );
        result.addTest( DomTestSuite.suite() );
        result.addTest( WebFrameTest.suite() );
        result.addTest( WebWindowTest.suite() );
        result.addTest( RequestTargetTest.suite() );
        result.addTest( FormParametersTest.suite() );
        result.addTest( FormSubmitTest.suite() );
        result.addTest( Base64Test.suite() );
        result.addTest( PseudoServerTest.suite() );
        result.addTest( WebClientTest.suite() );
        result.addTest( HttpsProtocolSupportTest.suite());
        result.addTest( MessageBodyRequestTest.suite() );
        result.addTest( WebAppletTest.suite() );
        result.addTest( CookieTest.suite() );
        result.addTest( ParsingTestSuite.suite() );
        result.addTest( NormalizeURLTest.suite() );
        result.addTest( TextBlockTest.suite() );
        result.addTest( EncodingTest.suite() );
        if (HTMLParserFactory.getHTMLParser().getClass().getName().indexOf("NekoHTML") >= 0) result.addTest( NekoEnhancedScriptingTest.suite() );
        addOptionalTestCase( result, "com.meterware.httpunit.XMLPageTest" );
        addOptionalTestCase( result, "com.meterware.httpunit.FileUploadTest" );
        addOptionalTestCase( result, "com.meterware.httpunit.javascript.JavaScriptTestSuite" );
        addOptionalTestCase( result, "com.meterware.servletunit.ServletUnitSuite" );
        return result;
    }

}

