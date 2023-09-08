
/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.UserAgent;

/**
 * Test class for the {@link com.top_logic.mig.html.UserAgent}.
 *
 * @author     <a href="mailto:mvo@top-logic.com">Michael Vogt</a>
 */
public class TestUserAgent extends BasicTestCase {

    /** Constructors */
    public TestUserAgent (String aName) {
        super (aName);
    }

    /** Public methods */
    
    /** 
     *  Main method for all tests
     */    
    public void testMain () throws Exception {

		this.assertMSIE();
		this.assertFirefox();
    }
    
    /** 
     *  common assertions for all Mozilla Versions
     */    
    public UserAgent assertCommonMozilla(String anUserAgentString) throws Exception {

		UserAgent anUserAgent = new UserAgent(anUserAgentString);

		//check if anUserAgent is serializable
    	anUserAgent = (UserAgent) assertSerializable(anUserAgent);

		//check that each Version is not MSIE
		assertTrue(!anUserAgent.is_ie());
		assertTrue(!anUserAgent.is_ie6up());
		return anUserAgent;
    }

    /** 
     *  assertions for the different Mozilla Versions
     */
	public void assertFirefox() throws Exception {
    	String firefox = "Mozilla/5.0 Gecko/20090612 Firefox/3.5";

		UserAgent anUserAgent = assertCommonMozilla(firefox);
        assertTrue(anUserAgent.is_firefox());
		assertTrue(!anUserAgent.is_feature_detection_browser());
    }

    /** 
     *  common assertions for all MSIE Versions
     */    
    public UserAgent assertCommonMSIE(String anUserAgentString) throws Exception {

		UserAgent anUserAgent = new UserAgent(anUserAgentString);

		//check if anUserAgent is serializable
    	anUserAgent = (UserAgent) assertSerializable(anUserAgent);

		//check correct Version
		assertTrue(anUserAgent.is_ie());
		assertTrue(!anUserAgent.is_feature_detection_browser());
		assertTrue(!anUserAgent.is_firefox());
		
		return anUserAgent;
    }

    /**
     *  assertions for the different MSIE Versions
     */
    public void assertMSIE() throws Exception {
    	String msie60 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; (R1 1.1))";
         // Windows XP SP2, IE7 sendet folgenden User-Agent Header: 
    	String msie70_1 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)";
         // Windows 2003 Server, sendet folgenden User-Agent Header: 
    	String msie70_2 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2)";
         // Windows Vista, IE7 sendet folgenden User-Agent Header: 
    	String msie70_3 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)";
    	String msie80 = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)";
		String msie90 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    	
		UserAgent anUserAgent;
		
		//MSIE 60
		anUserAgent = assertCommonMSIE(msie60);
		
		assertTrue(anUserAgent.is_ie6up());
		assertTrue(!anUserAgent.is_ie7up());
		assertTrue(!anUserAgent.is_ie8up());
		assertTrue(!anUserAgent.is_ie9up());
		
		assertEquals("custom IE IE6", anUserAgent.getCSSClasses("custom"));

		// MSIE 70
		String[] msie7 = new String[] {msie70_1, msie70_2, msie70_3};
		for (int i = 0; i < msie7.length; i++) {
			anUserAgent = assertCommonMSIE(msie7[i]);
			assertTrue(anUserAgent.is_ie6up());
			assertTrue(anUserAgent.is_ie7up());
			assertTrue(!anUserAgent.is_ie8up());
			assertTrue(!anUserAgent.is_ie9up());
		}
		
		//MSIE 80
        anUserAgent = assertCommonMSIE(msie80);
        
        assertTrue(anUserAgent.is_ie6up());
        assertTrue(anUserAgent.is_ie7up());
        assertTrue(anUserAgent.is_ie8up());
		assertTrue(!anUserAgent.is_ie9up());

		// MSIE 90
		UserAgent ie9 = assertCommonMSIE(msie90);

		assertTrue(ie9.is_ie6up());
		assertTrue(ie9.is_ie7up());
		assertTrue(ie9.is_ie8up());
		assertTrue(ie9.is_ie9up());
    }
    
	public void isChromeInFeatureMode() {
		String chrome =
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/530.8 (KHTML, like Gecko) Chrome/2.0.178.0 Safari/530.8";
        UserAgent userAgent = new UserAgent(chrome);
		assertTrue(userAgent.is_feature_detection_browser());
	}

	public void isOperaInFeatureMode() {
		// Opera with Presto Test
		String operaPresto = "Opera/9.70 (Linux i686 ; U; zh-cn) Presto/2.2.0";
		UserAgent userAgent = new UserAgent(operaPresto);
		assertTrue(userAgent.is_feature_detection_browser());
        
        // Opera with Gecko Test
		String operaGecko = "Mozilla/5.0 (Linux i686 ; U; en; rv:1.8.1) Gecko/20061208 Firefox/2.0.0 Opera 9.70";
        userAgent = new UserAgent(operaGecko);
		assertTrue(userAgent.is_feature_detection_browser());
	}

	public void testIsSafariInFeatureMode() {
		String safari =
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en) AppleWebKit/526.9 (KHTML, like Gecko) Version/4.0dp1 Safari/526.8";
		UserAgent userAgent = new UserAgent(safari);
		assertTrue(userAgent.is_feature_detection_browser());
	}
    
    public void testIsIE11StandardInFeatureMode() throws Exception {
		String ieStandardModeString = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
    	UserAgent userAgent = new UserAgent(ieStandardModeString);
		assertTrue("User agent should recognized as feature detection browser, but was: " + userAgent.getBrowserName(),
			userAgent.is_feature_detection_browser());
	}
    
	public void testIsIE11CompatibleInFeatureMode() throws Exception {
		String ieCompatibleModeString =
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C)";
		UserAgent userAgent = new UserAgent(ieCompatibleModeString);
		assertTrue(
			"User agent should be recognized as feature detection browser, but was: " + userAgent.getBrowserName(),
			userAgent.is_feature_detection_browser());
	}

	public void testIsNonSpecifiedUserAgentInFeatureMode() throws Exception {
		String unspecifiedAgent = null;
		UserAgent userAgent = new UserAgent(unspecifiedAgent);
		assertTrue("Unspecified user agent should be recognized as feature detection browser!",
			userAgent.is_feature_detection_browser());
	}
    
    /** Return the suite of Tests to execute */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(TestUserAgent.class);
    }

    /** Main function for direct testing */
    public static void main (String[] args) {
    	Logger.configureStdout();
    	
        junit.textui.TestRunner.run (suite ());
    }
}
