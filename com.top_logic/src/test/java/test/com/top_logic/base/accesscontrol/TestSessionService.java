/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.LoginPageServlet;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.event.bus.Bus;
import com.top_logic.knowledge.wrap.person.PersonManager;


/**
 * Test class for {@link com.top_logic.base.accesscontrol.SessionService}
 *
 * @author     <a href="mailto:mvo@top-logic.com">Michael Vogt</a>
 */
public class TestSessionService extends BasicTestCase {

    public TestSessionService (String aName) {
        super (aName);
    }

    public void testAddRemoveSession () throws Exception {
        
        ServletRunner		sr = new ServletRunner();
        sr.registerServlet("lpServlet", LoginPageServlet.class.getName());

        ServletUnitClient   sc = sr.newClient();

        WebRequest  myRequest  = new PostMethodWebRequest( "http://ignore/lpServlet" );

        InvocationContext   ic = sc.newInvocation(myRequest);

		HttpServletRequest servletRequest = ic.getRequest();

        ThreadContext.pushSuperUser();
        try {
			HttpServletResponse response = ic.getResponse();
			try (LoginCredentials login =
				LoginCredentials.fromUserAndPassword(PersonManager.getManager().getRoot(), "root1234".toCharArray())) {
				Login.getInstance().login(servletRequest, response, login);
			}
            
    		SessionService myService = SessionService.getInstance();
    		
    		assertTrue(myService.validateSession(servletRequest));
    		
			final Collection<String> sessionIDsAfterLogin = myService.getSessionIDs();
			assertTrue("expected there is a current session",
				sessionIDsAfterLogin.contains(servletRequest.getSession(false).getId()));
			assertEquals("expected there is exactly one session", 1, sessionIDsAfterLogin.size());
			
			myService.invalidateSession(myService.getSession(servletRequest));
    		
			final Collection<String> sessionIDsAfterLogout = myService.getSessionIDs();
			assertTrue("expected there is a no session when session was invalided", sessionIDsAfterLogout.isEmpty());
        } finally {
            ThreadContext.popSuperUser();
        }
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        Test innerTest = new TestSuite (TestSessionService.class);
		innerTest =
			ServiceTestSetup.createSetup(innerTest, Bus.Module.INSTANCE, SessionService.Module.INSTANCE,
				Login.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
        
    }

}
