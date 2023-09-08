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

import com.top_logic.base.security.SecurityContext;
import com.top_logic.base.security.UserSecurityException;
import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Test for {@link com.top_logic.base.security.SecurityContext}. 
 * 
 * This class should test the behavior of the context class.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestSecurityContext extends BasicTestCase {

    /** 
     * Default Constructor
     *
     * @param name name of fucntion to execute for testing.
     */
    public TestSecurityContext (String name) {
        super (name);
    }

    /**
     * Create an environment, where the user returned by loadUser is the
     * default user.
     *
     * @throws    Exception    If init fails.
     */
    @Override
	public void setUp () throws Exception {
		super.setUp();

		TLContext newContext = TLContext.getContext();
		newContext.setCurrentPerson(this.loadPerson());
    }
    
    /**
     * Test, what happens to an empty permission request.
     */
	public void testEmptySymbol ()  {
        assertFalse(ThreadContext.isSuperUser());
        assertFalse("Shoud not have Permission for empty symbol", 
                     SecurityContext.hasPermission (""));
	}

    /**
     * Test, what happens to an permission request for a special symbol.
     */
	public void testSymbol() throws UserSecurityException {
        String theSymbol = "toplogic.query.editor";

        assertNotNull (SecurityContext.getSymbol (theSymbol));
        assertTrue ("Expected Permission for '" + theSymbol + "'", 
            SecurityContext.hasPermission (theSymbol));
        
        SecurityContext.checkPermission(theSymbol);
        
        Symbol sym = SecurityContext.getSymbol(theSymbol);
        SecurityContext.checkPermission(sym, "No Message needed");
        
        SecurityContext.checkPermission(loadPerson().getUser(), sym, "No Message needed");
        
        assertTrue(!SecurityContext.isAdmin());
    }

    /**
     * Load the Person with name "dummy".
     *
     * @return    The loaded user.
     */
	private Person loadPerson() {
		return TestPersonSetup.getTestPerson();
    }

    /**
     * The suite of Tests to execute.
     */
    public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(TestPersonSetup
			.wrap(new TestSuite(TestSecurityContext.class)));
    }

    /**
     * main function for direct Execution.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }

}
