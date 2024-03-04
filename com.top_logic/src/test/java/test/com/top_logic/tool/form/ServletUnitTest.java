/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.form;

import java.io.IOException;
import java.net.MalformedURLException;

import jakarta.servlet.http.HttpServlet;

import test.com.top_logic.basic.BasicTestCase;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * A Common Subclass fo Testcases using ServletUnit.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class ServletUnitTest extends BasicTestCase {    
   
    /** A Client setup for every test using our Help Servlet */
    protected static ServletUnitClient client;

    /** An eventual result of a validation. */
    protected static boolean result;

    /** Create a POSTRequest for out Testing Servlet */
    protected PostMethodWebRequest createPostRequest() {
        return new PostMethodWebRequest("http://ignored/test/HelperServlet");
    }

    /** Create a InvocationContext for our TEsing Servlet */
    protected InvocationContext createInvocationContext() throws MalformedURLException, IOException {
        return client.newInvocation("http://ignored/test/HelperServlet");
    }

    /**
     * Make superclass CTor accessible. 
     */
    public ServletUnitTest() {
        super();
    }

    /**
     * Make superclass CTor accessible. 
     */
    public ServletUnitTest(String name) {
        super(name);
    }

    /** Return the Class of the Servlet used for Testing */
    protected abstract Class<? extends HttpServlet> getServletClass();

    /**
     * Overriden to setup a ServletUnitClient for Testing.
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
    
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("test/HelperServlet", getServletClass().getName());
        
        client = sr.newClient();
    }

 
    /**
     * Overriden to cleanup the static variables.
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        client   = null;
    }


}
