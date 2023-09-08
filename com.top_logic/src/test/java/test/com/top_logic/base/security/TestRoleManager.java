/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.base.security.authorisation.roles.RoleManager;

/** 
 * Test case for the role manager.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestRoleManager extends TestCase {

    private static final String ROLE_1 = "test1";

    private static final String ROLE_2 = "test2";

    private static final String ROLE_3 = "test3";

    private static final String ROLE_4 = "test4";

    private static final String[] ROLES_1 = {ROLE_1, ROLE_2};

    private static final String[] ROLES_2 = {ROLE_3, ROLE_4};

    private static final String[] ROLES_3 = {ROLE_1, ROLE_2, null, ROLE_4};

    private RoleManager manager;

    /**
     * Constructor for TestRoleManager.
     */
    public TestRoleManager(String name) {
        super(name);
    }

    /**
     * Used for framework.
     *
     * @throws    Exception    If something in setup fails.
     */
    @Override
	public void setUp () throws Exception {
        super.setUp();

        this.manager = RoleManager.getInstance();
    }

    /*
     * Test for int getID(String)
     */
    public void testGetIDString() {
        int theID = this.manager.getID(ROLE_1);

        assertTrue("ID is smaller than zero.", theID >= 0);

        assertEquals("ID is not the same on second call.", 
                          theID, this.manager.getID(ROLE_1));

        assertTrue("ID is the same for different roles.", 
                        theID != this.manager.getID(ROLE_2));

        try {
            theID = this.manager.getID("");
            fail("Got ID " + theID + " for empty role.");
        }
        catch (IllegalArgumentException ex) {
            // Expected behavior
        }

        try {
            theID = this.manager.getID((String) null);

            fail("Got ID " + theID + " for role null.");
        }
        catch (IllegalArgumentException ex) {
            // Expected behavior
        }
    }

    /*
     * Test for int[] getID(String[])
     */
    public void testGetIDStringArray() {
        int[] theID = this.manager.getID(ROLES_1);

        assertEquals("Length is invalid.", ROLES_1.length, theID.length);

        int[] theID2 = this.manager.getID(ROLES_1);

        assertEquals("ID length is not the same on second call.", 
                          theID.length, theID2.length);

        for (int thePos = 0; thePos < theID.length; thePos++) {
            assertEquals("ID is not the same on second call.", 
                              theID[thePos], theID2[thePos]);
        }

        theID2 = this.manager.getID(ROLES_2);

        assertEquals("ID length is not the same on second call.", 
                          theID.length, theID2.length);

        for (int thePos = 0; thePos < theID.length; thePos++) {
            assertTrue("ID is same on other role list.", 
                            theID[thePos] != theID2[thePos]);
        }

        try {
            theID = this.manager.getID(ROLES_3);
            fail("Got ID " + theID + " for empty role.");
        }
        catch (IllegalArgumentException ex) { /* expected */ }

        try {
            theID = this.manager.getID((String[]) null);
            fail("Got ID " + theID + " for role null.");
        }
        catch (IllegalArgumentException ex) { /* expected */ }
    }

    public void testGetRole() {
        int    theID   = this.manager.getID(ROLE_2);
        String theRole = this.manager.getRole(theID);

        assertNotNull("Empty role for ID " + theID, theRole);

        assertEquals("Wrong role for ID " + theID, ROLE_2, theRole);

        try {
            theRole = this.manager.getRole(-1);

            fail("Got role '" + theRole + "' for ID -1");
        }
        catch (IllegalArgumentException ex) {
            // Expected behavior
        }
    }

    public void testGetRoles() {
        List theList = this.manager.getRoles();

        assertNotNull("List of roles is null.", theList);
    }

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite() {
        return new TestSuite(TestRoleManager.class);
    }

    /**
     * Main class to start test without UI.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
