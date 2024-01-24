
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;


/**
 * Test class for the implementation of a authentication system.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@SuppressWarnings("javadoc")
public class TestAuthentication extends BasicTestCase {

	private static final String USER_ID = TestPersonSetup.USER_ID;

	/** The authentication to be tested. */
    private AuthenticationDevice authentication;

    /**
     * Constructor needed for framework.
     *
     * @param    aName    The name of the test.
     */
    public TestAuthentication (String aName) {
        super (aName);
    }

    /**
     * Used for framework.
     *
     * @throws    Exception    If something in setup fails.
     */
    @Override
	public void setUp () throws Exception {
        super.setUp();
        this.authentication = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
    }

	public void test_checkPasswordTest1() {
		this.doTest(USER_ID, TestPersonSetup.USER_PASSWORD.toCharArray());
    }

    public void test_checkPasswordTest1Fail () {
		this.doTestFail(USER_ID, (TestPersonSetup.USER_PASSWORD + "xyz").toCharArray());
    }

    public void test_checkPasswordTest1NullWord () {
        this.doTestFail (USER_ID, null);
    }

    public void test_checkPasswordTest2Fail () {
		this.doTestFail(USER_ID, "12".toCharArray());
    }

    /**
     * Executes the test with the given name.
     *
     * @param    aName    The name to be tested.
     */
	private void doTest(String aName, char[] aPass) {
		try (LoginCredentials login = login(aName, aPass)) {
			assertTrue(this.authentication.authentify(login));
		}
    }

    /**
     * Executes the test with the given name.
     *
     * @param    aName    The name to be tested.
     */
	private void doTestFail(String aName, char[] aPass) {
		try (LoginCredentials login = login(aName, aPass)) {
			assertFalse(this.authentication.authentify(login));
		}
    }

	private LoginCredentials login(String user, char[] password) {
		return LoginCredentials.fromUsernameAndPassword(user, password);
	}

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite () {
		Test test = new TestSuite(TestAuthentication.class);
		test = ServiceTestSetup.createSetup(test, Login.Module.INSTANCE);
		test = TestPersonSetup.wrap(test);
		return PersonManagerSetup.createPersonManagerSetup(test);
    }

    /**
     * Main class to start test without UI.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
