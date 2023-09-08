/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Test {@link com.top_logic.dob.meta.MOStructureImpl}.
 *
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestMOStructureImpl extends TestCase {

	private static final String A = "A";
	
	private static final String A1 = "a1";
	private static final String A2 = "a2";
	private static final String A3 = "a3";

	/**
     * Standard CTor for a Test-case.
     *
     * @param name function to execute for testing
     */
    public TestMOStructureImpl (String name) {
        super (name);
    }

    /**
     * Test index creation.
     */
    public void testIndex() throws IncompatibleTypeException, DuplicateAttributeException {
		MOStructureImpl a = new MOStructureImpl(A);
		MOAttributeImpl x;
		a.addAttribute(x = new MOAttributeImpl(A1, MOPrimitive.LONG, true));
		MOAttributeImpl y;
		a.addAttribute(y = new MOAttributeImpl(A2, MOPrimitive.STRING, true));
		MOIndexImpl idx;
		a.addIndex(idx = new MOIndexImpl("idx_xy", new MOAttributeImpl[] { x, y }));

		assertEquals(list(idx), a.getIndexes());

		a.freeze();

		assertEquals(list(idx), a.getIndexes());
    }
    
    /** Test the default CTor and some trivial things. */
    public void testDefault() {
		MOStructureImpl a = new MOStructureImpl("CamelCaseName");

		assertTrue(!MetaObjectUtils.isClass(a));
		assertTrue(!MetaObjectUtils.isCollection(a));
		assertTrue(!MetaObjectUtils.isPrimitive(a));
		assertTrue(MetaObjectUtils.isStructure(a));

		assertTrue(a.isSubtypeOf(a));
		assertEquals(a, a);

		assertEquals("CAMEL_CASE_NAME", a.getDBName());

		assertTrue(a.getIndexes().isEmpty());

		a.setDBName("ALLUPPERCASENAME");
		assertEquals("ALLUPPERCASENAME", a.getDBName());
    }

    /** Test CTor with given Size */
    public void testIntialSize() throws DuplicateAttributeException {
		MOStructureImpl a = new MOStructureImpl(A, 10);

		a.addAttribute(new MOAttributeImpl(A1, MOPrimitive.INTEGER));
		a.addAttribute(new MOAttributeImpl(A2, MOPrimitive.STRING));
		a.addAttribute(new MOAttributeImpl(A3, MOPrimitive.DOUBLE));

		try {
			a.addAttribute(new MOAttributeImpl(A1, MOPrimitive.BYTE));
			fail("Expected DuplicateAttributeException");
		} catch (DuplicateAttributeException expected) {
			/* excpected */
		}

		checkTestInitialSize(a);

		a.freeze();

		checkTestInitialSize(a);
    }

	private void checkTestInitialSize(MOStructureImpl moi) {
		// Order must be preserved !
		String names[] = moi.getAttributeNames();
		assertEquals(3, names.length);
		assertEquals(A1, names[0]);
		assertEquals(A2, names[1]);
		assertEquals(A3, names[2]);

		List dba = moi.getDBAttributes();
		assertEquals(3, dba.size());
	}

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMOStructureImpl.class);
        return suite;
    }


    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }


}
