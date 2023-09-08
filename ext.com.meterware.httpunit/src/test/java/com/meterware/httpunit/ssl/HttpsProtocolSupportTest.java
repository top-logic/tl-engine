package com.meterware.httpunit.ssl;
/********************************************************************************************************************
* $Id: HttpsProtocolSupportTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2007, Russell Gold
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
import java.security.Provider;
import java.security.Security;

import com.meterware.httpunit.HttpsProtocolSupport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the HttpsProtocolSupport
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:wf@bitplan.com">Wolfgang Fahl</a> 
 **/
public class HttpsProtocolSupportTest extends TestCase {

    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( HttpsProtocolSupportTest.class );
    }


    public HttpsProtocolSupportTest( String name ) {
        super( name );
    }


    /**
     * test the available HttpsProtocolProviders
     * are available
     */
    public void testProvider() throws Exception {
    	Class provider= HttpsProtocolSupport.getHttpsProviderClass();
			String expected=HttpsProtocolSupport.SunJSSE_PROVIDER_CLASS;
			Provider[] sslProviders = Security.getProviders("SSLContext.SSLv3");
			if (sslProviders.length>0)
				expected= sslProviders[0].getClass().getName();
			assertEquals( "provider",expected, provider.getName() );				
    }

    /**
     * test the available HttpsProtocolProviders
     */
    public void testProviderIBM() throws Exception {
    	HttpsProtocolSupport.useIBM();
    	Class provider= HttpsProtocolSupport.getHttpsProviderClass();
			String expected=HttpsProtocolSupport.IBMJSSE_PROVIDER_CLASS;
			Provider[] sslProviders = Security.getProviders("SSLContext.SSLv3");
			if (sslProviders.length>0)
				expected= sslProviders[0].getClass().getName();
			assertEquals( "provider",expected, provider.getName() );				
    }
  }
