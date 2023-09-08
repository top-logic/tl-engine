
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

/**
 * Test class for Role.
 *
 * @author  Mathias Maul
 */
public class TestRole extends TestCase {
	
	private Role r1;
	private Role r2;

	public TestRole (String name) {
        super (name);
    }

    @Override
	public void setUp () {
		r1 = new Role("role1", "hi! i am role number one!");
		r2 = new Role("role2", "role two has a strange name");
	}

	public void test_setget()  {
		String name;
		String desc;
		
		name = "foo";
		desc = "bar";
		
		r1.setName(name);
		r1.setDescription(desc);

		assertEquals(name, r1.getName());
		assertEquals(desc, r1.getDescription());
	}

	public void test_equals()  {
		r1.setName("foo");
		r1.setDescription("desc");

		r2.setName("bar");
		r2.setDescription("desc");

		assertTrue(!(r1.equals(r2)));	// equals() tests for equal name fields	only
		
		
		r1.setName("foo");
		r1.setDescription("descONE");

		r2.setName("foo");
		r2.setDescription("descTWO");

		assertEquals(r1, r2);	// now the names are equal, and so should be the Roles.
	}
	

	public void test_clone()  {
		Role rDolly1 = new Role("dolly", "baaaaaa");

		Role rDolly2 = (Role)rDolly1.clone();
		
		assertTrue(rDolly1 != rDolly2);	// references should be different
		
		// note that using equals() only would not be sufficient here
		assertTrue(rDolly1.getName() == rDolly2.getName());
		assertTrue(rDolly1.getDescription() == rDolly2.getDescription());
		
	}


	public void test_hash()  {
		// hash codes should be the same for two Roles if the names are equal
		
		Role r1 = new Role("foo", "doesntmatter");
		Role r2 = new Role("foo", "doesntmatteraswell");
		
		assertTrue(r1.hashCode() == r2.hashCode());
	}

	/**
     * The suite of tests to execute. 
	 */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestRole.class);
		
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
