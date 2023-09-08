/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.roles;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.base.roles.Role;
import com.top_logic.base.roles.RoleRepository;

/**
 * Test class for RoleRepository.
 *
 * @author  Mathias Maul
 */
public class TestRoleRepository extends TestCase {
	
	private RoleRepository rr;
	
	public TestRoleRepository (String name) {
        super (name);
    }


    @Override
	public void setUp () {
		rr = new RoleRepository();
	}
	

	public void test_getRoleByName() {
		Role r1 = new Role ("role1", "hi there!");

		rr.addRole(r1);
		
		Role r = rr.getRoleByName(r1.getName());
		
		assertTrue(r.getName() == r1.getName());
		assertTrue(r.getDescription() == r1.getDescription());
	}


	public void test_addRole() {
		Role r2 = new Role ("addrole", "this is a new role");
		
		rr.addRole(r2);
		
		Role r3 = rr.getRoleByName(r2.getName());
		assertEquals(r3,r2);
		
	}


	public void test_removeRole() {
		Role r4 = new Role("removerole", "my lifespan is quite short");
		
		rr.addRole(r4);
		rr.removeRole(r4);
		
		assertNull(rr.getRoleByName(r4.getName()));
	}


	public void test_getCSVSizeRemove() {
		rr.removeAllRoles();
		
		rr.addRole(new Role("r1", "dummy1"));
		rr.addRole(new Role("r2", "dummy2"));
		rr.addRole(new Role("r3", "dummy3"));

		assertEquals(3, rr.size());
		
		String csv = rr.getCSV();
        assertTrue(csv.indexOf("r1") >= 0);
        assertTrue(csv.indexOf("r2") >= 0);
        assertTrue(csv.indexOf("r3") >= 0);
		
		rr.removeAllRoles();
		
		assertEquals(0 , rr.size());
	}

	public void test_contains()  {
		rr.removeAllRoles();
		
		rr.addRole(new Role("huhu", "dummy"));
		
		assertTrue( rr.contains("huhu"));
		assertTrue(!rr.contains("hihi"));
	}


    public static Test suite () {
        return new TestSuite (TestRoleRepository.class);
    }


    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
