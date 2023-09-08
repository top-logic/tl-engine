
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.base.security.authorisation.symbols.Authorisation;
import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.base.security.authorisation.symbols.SymbolException;
import com.top_logic.basic.Logger;

/**
 * Test class for the implementation of a authorisation system.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestAuthorisation extends TestCase {

    /** The authorisation to be tested. */
    private Authorisation authorisation;

    /**
     * Constructor needed for framework.
     *
     * @param    aName    The name of the test.
     */
    public TestAuthorisation (String aName) {
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
        this.authorisation = SecurityContext.getAuthorisation ();
    }

    /**
     * Test, if an authorisation with name "test1" exists. This should be the
     * case for the tests.
     */
    public void test_getACLTest1 () {
        this.doTest ("toplogic.admin");
    }

    /**
     * Test, if a user with name "UnKnOwN" exists. This test should 
     * normaly return null.
     */
	public void test_getACLUNKNOWN ()  {
        this.doTestFail ("UnKnOwN");
	}

    /**
     * Executes the test with the given name.
     *
     * @param    aName    The name to be tested.
     */
    private void doTest (String aName) {
        Symbol theSymbol = this.authorisation.getSymbol (aName);

        this.log ("getSymbol(\"" + aName + "\"): " + theSymbol);

        assertNotNull (theSymbol);
    }

    /**
     * Executes the test with the given name.
     *
     * @param    aName    The name to be tested.
     */
    private void doTestFail (String aName) {
        try {
            Symbol theSymbol = this.authorisation.getSymbol (aName);

            fail ("getSymbol(\"" + aName + "\"): " +
                  "unexpected return value: " + theSymbol);
        }
        catch (SymbolException ex) {
            this.log ("getSymbol(\"" + aName + "\"): OK, exception is " + ex);
        }
    }

    /**
     * Write the given message to stdout.
     *
     * @param    aMessage    The message to be printed.
     */
    private void log (String aMessage) {
        Logger.info (aMessage, this);
    }

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite (TestAuthorisation.class));
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
