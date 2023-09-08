/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.util.MetaObjectUtils;


/**
 * Testcase for {@link com.top_logic.dob.meta.MOClassImpl}.
 *
 * @author  Klaus Halfmann / Marco Perra
 */
public class TestMOClassImpl extends TestCase {

	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";

    private static final String A1 = "a1";
    private static final String B1 = "b1";
    private static final String C1 = "c1";
	private static final String D1 = "d1";

	/**
     * Standard CTor for a Testcase.
     *
     * @param name function to execute for testing
     */
    public TestMOClassImpl (String name) {
        super (name);
    }

	/**
	 * Tests {@link MOClass#overrideAttribute(com.top_logic.dob.MOAttribute)}
	 */
	public void testOverrideAttribute() throws DuplicateAttributeException {
		// Create base class A
		MOClassImpl a = new MOClassImpl(A);
		MOAttributeImpl a1 = new MOAttributeImpl(A1, MOPrimitive.LONG, true);
		a.addAttribute(a1);

		MOClassImpl b = new MOClassImpl(B);
		b.setSuperclass(a);
		MOAttributeImpl a1Override = new MOAttributeImpl(A1, MOPrimitive.STRING, true);
		b.overrideAttribute(a1Override);

		b.freeze();

		assertNotNull(b.getAttributeOrNull(A1));
		assertEquals("b overrides attribute with different type.", MOPrimitive.STRING, b.getAttributeOrNull(A1)
			.getMetaObject());
		assertEquals("Override must not affect super class", MOPrimitive.LONG, a.getAttributeOrNull(A1).getMetaObject());
	}

	/**
	 * Tests {@link MOClass#overrideAttribute(com.top_logic.dob.MOAttribute)}
	 */
	public void testOverrideAttributePreservesOrder() throws DuplicateAttributeException {
		// Create base class A
		MOClassImpl a = new MOClassImpl(A);
		MOAttributeImpl a1 = new MOAttributeImpl(A1, MOPrimitive.LONG, true);
		MOAttributeImpl b1 = new MOAttributeImpl(B1, MOPrimitive.LONG, true);
		MOAttributeImpl c1 = new MOAttributeImpl(C1, MOPrimitive.LONG, true);
		a.addAttribute(a1);
		a.addAttribute(b1);
		a.addAttribute(c1);

		MOClassImpl b = new MOClassImpl(B);
		b.setSuperclass(a);
		MOAttributeImpl b1Override = new MOAttributeImpl(B1, MOPrimitive.STRING, true);
		b.overrideAttribute(b1Override);
		MOAttributeImpl d1 = new MOAttributeImpl(D1, MOPrimitive.STRING, true);
		b.addAttribute(d1);

		b.freeze();

		assertEquals(list(a1, b1, c1), a.getAttributes());
		assertEquals("Override attribute must not affect order of attributes.", list(a1, b1Override, c1, d1),
			b.getAttributes());
	}

	/**
	 * Tests {@link MOStructure#getAttributeOrNull(String)}
	 */
	public void testGetAttribute() throws DuplicateAttributeException {
		// Create base class A
		MOClassImpl a = new MOClassImpl(A);
		MOAttributeImpl a1 = new MOAttributeImpl(A1, MOPrimitive.LONG, true);
		a.addAttribute(a1);

		MOClassImpl b = new MOClassImpl(B);
		b.setSuperclass(a);
		MOAttributeImpl d1 = new MOAttributeImpl(D1, MOPrimitive.STRING, true);
		b.addAttribute(d1);

		assertEquals(a1, a.getAttributeOrNull(A1));
		assertEquals(a1, b.getAttributeOrNull(A1));

		assertNull(a.getAttributeOrNull(D1));
		assertEquals(d1, b.getAttributeOrNull(D1));

		b.freeze();

		assertEquals(a1, a.getAttributeOrNull(A1));
		assertEquals(a1, b.getAttributeOrNull(A1));

		assertNull(a.getAttributeOrNull(D1));
		assertEquals(d1, b.getAttributeOrNull(D1));

	}

    /**
     * Test index creation.
     */
    public void testIndex() throws IncompatibleTypeException, DuplicateAttributeException {
    	// Create base class A
		MOClassImpl a = new MOClassImpl(A);
		MOAttributeImpl a1;
		a.addAttribute(a1 = new MOAttributeImpl(A1, MOPrimitive.LONG, true));

		MOIndexImpl idxA;
		a.addIndex(idxA = new MOIndexImpl("idxA", new MOAttributeImpl[] { a1 }));

		assertEquals(list(idxA), a.getIndexes());

		a.freeze();
		
		assertEquals(list(idxA), a.getIndexes());
		
		// Create B extends A adding a new index.

		MOClassImpl b = new MOClassImpl(B);
		b.setSuperclass(a);
		
		MOAttributeImpl b1;
		b.addAttribute(b1 = new MOAttributeImpl(B1, MOPrimitive.STRING, true));

		MOIndexImpl idxB;
		b.addIndex(idxB = new MOIndexImpl("idxB", new MOAttributeImpl[] { b1 }));

		assertEquals(list(idxA, idxB), b.getIndexes());
		
		b.freeze();

		assertEquals(list(idxA, idxB), b.getIndexes());
		
		// Create C extends B overriding an index.
		
		MOClassImpl c = new MOClassImpl(C);
		c.setSuperclass(b);
		
		MOAttributeImpl c1;
		c.addAttribute(c1 = new MOAttributeImpl(C1, MOPrimitive.DOUBLE, true));

		assertEquals(list(idxA, idxB), c.getIndexes());
		
		MOIndexImpl idxAOverrideC;
		c.addIndex(idxAOverrideC = new MOIndexImpl("idxA", new MOAttributeImpl[] { c1, a1 }));
		
		assertEquals(list(idxAOverrideC, idxB), c.getIndexes());
		
		c.freeze();
		
		assertEquals(list(idxAOverrideC, idxB), c.getIndexes());
    }

    /**
     * Test for some boring aspects of MOClassImpl.
     */
    public void testIsIt () {
        
        MOClass c =  new MOClassImpl(A);
        
        assertTrue(MetaObjectUtils.isClass(c));
        assertTrue(!MetaObjectUtils.isPrimitive(c));
    }
    
	/**
	 * Test for {@link com.top_logic.dob.meta.MOClassImpl#isAbstract} 
	 * and      {@link com.top_logic.dob.meta.MOClassImpl#setAbstract}.
	 */
	public void testIsAbstract() {
		MOClassImpl a = new MOClassImpl(A);
		// test with default-value
		assertTrue(!a.isAbstract());
		// test setAbstract and isAbstract
		a.setAbstract(true);
		assertTrue(a.isAbstract());
		a.setAbstract(false);
		assertTrue(!a.isAbstract());
		a = null;
	}    

    /**
     * Test for {@link com.top_logic.dob.meta.MOClassImpl} isInherited.
     */
    public void testIsInherited () {
    	
    	MOClass a   = new MOClassImpl(A);
    	MOClass b  = new MOClassImpl(B);
    	MOClass c  = new MOClassImpl(C);
    	MOClass d = new MOClassImpl(D);
    	
    	a.freeze();
    	b.setSuperclass(a);
    	c.setSuperclass(a);
    	
    	c.freeze();
    	d.setSuperclass(c);

    	// Before freeze (inefficient).
        checkTestInherited(a, b, c, d);

    	a.freeze();
    	b.freeze();
    	d.freeze();
    	
    	// After freeze (efficient).
        checkTestInherited(a, b, c, d);
    }

	private void checkTestInherited(MOClass a, MOClass b, MOClass c, MOClass d) {
		// The good one
        assertTrue (b.isInherited (a));
        assertTrue (c.isInherited (a));
        assertTrue (d.isInherited (c));
        assertTrue (d.isInherited (a));

        assertTrue (b.isSubtypeOf(b));
        assertTrue (b.isSubtypeOf(a));
        assertTrue (d.isSubtypeOf(a));

        assertTrue (b.isSubtypeOf(B));
        assertTrue (b.isSubtypeOf(A));
        assertTrue (d.isSubtypeOf(A));

        // The bad one
        assertTrue (!d.isInherited (b));
        assertTrue (!c.isInherited (b));
        assertTrue (!c.isInherited (d));
        assertTrue (!b.isInherited (c));
        assertTrue (!b.isInherited (d));
        assertTrue (!a.isInherited (b));
        assertTrue (!a.isInherited (c));
        assertTrue (!a.isInherited (d));
	}

	/**
	 * Test that cyclic class hierarchies cannot be constructed.
	 */
	public void testSuperClassCycleCheck() {
		MOClassImpl classA = new MOClassImpl(A);
		classA.setSuperclass(classA);

		try {
			classA.freeze();
			fail("Must not allow freezing classes with cyclic inheritance hierarchies.");
		} catch (IllegalStateException ex) {
			// Expected
		}
	}
	
    /**
     * Test some Attribute related fucntions.
     */
    public void testAttributes () throws Exception {
		MOClassImpl a = new MOClassImpl(A);
		MOClassImpl b = new MOClassImpl(B);
		MOClassImpl c = new MOClassImpl(C);
		MOClassImpl d = new MOClassImpl(D);

		MOAttributeImpl a1 = new MOAttributeImpl(A1, MOPrimitive.BOOLEAN);
		a.addAttribute(a1);
		a.freeze();

		b.setSuperclass(a);
		c.setSuperclass(a);

		MOAttributeImpl c1 = new MOAttributeImpl(C1, MOPrimitive.BOOLEAN);
		c.addAttribute(c1);
		c.freeze();

		d.setSuperclass(c);

		MOAttributeImpl b1 = new MOAttributeImpl(B1, MOPrimitive.BOOLEAN);
		b.addAttribute(b1);

		MOAttributeImpl d1 = new MOAttributeImpl(D1, MOPrimitive.BOOLEAN);
		d.addAttribute(d1);

		// Before freeze.
		checkTestAttributes(a, b, c, d, a1);

		b.freeze();
		d.freeze();

		// After freeze.
		checkTestAttributes(a, b, c, d, a1);
    }
    
    public void testDuplicateAttributes() throws DuplicateAttributeException {
		MOClassImpl a = new MOClassImpl(A);
		MOAttributeImpl a1 = new MOAttributeImpl(A1, MOPrimitive.BOOLEAN);
		a.addAttribute(a1);
		
		MOClassImpl b = new MOClassImpl(B);
		b.setSuperclass(a);
		MOAttributeImpl b1 = new MOAttributeImpl(B1, MOPrimitive.BOOLEAN);
		b.addAttribute(b1);
		
		MOClassImpl c = new MOClassImpl(C);
		c.setSuperclass(b);
		MOAttributeImpl c1 = new MOAttributeImpl(C1, MOPrimitive.BOOLEAN);
		c.addAttribute(c1);
		
		try {
			c.addAttribute(new MOAttributeImpl(C1, MOPrimitive.INTEGER));
			fail("Should fail since name is used in same class");
		} catch (DuplicateAttributeException expected) {
			/* expected */
		}
		
		c.addAttribute(new MOAttributeImpl(B1, MOPrimitive.INTEGER));
		try {
			c.freeze();
			fail("Should fail since B1 is used in super class");
		} catch (IllegalStateException expected) {
			/* expected */
		}
		
		MOClassImpl d = new MOClassImpl(D);
		d.setSuperclass(b);
		d.addAttribute(new MOAttributeImpl(A1, MOPrimitive.INTEGER));
		try {
			d.freeze();
			fail("Should fail since A1 is used in parent class");
		} catch (IllegalStateException expected) {
			/* expected */
		}
    }

	private void checkTestAttributes(MOClassImpl a, MOClassImpl b, MOClassImpl c, MOClassImpl d, MOAttributeImpl a1)
			throws NoSuchAttributeException {
		
		// Improve coverage
        assertNotNull(a.toString());
        assertNotNull(b.toString());
        assertNotNull(c.toString());
        assertNotNull(d.toString());
        
        // The good one
        assertTrue (    a.hasAttribute(A1));
        assertTrue (    a.getDeclaredAttributes().contains(a1));
        assertTrue (    a.getAttributes()        .contains(a1));
        assertSame (a1, a.getAttribute(A1));
        assertTrue (    b.hasAttribute(A1));
        assertTrue (    b.getAttributes()        .contains(a1));
        assertFalse(    b.getDeclaredAttributes().contains(a1));
        assertSame (a1, b.getAttribute(A1));
        assertTrue (    c.hasAttribute(A1));
        assertSame (a1, c.getAttribute(A1));
        assertTrue (    d.hasAttribute(A1));
        assertSame (a1, d.getAttribute(A1));
        assertTrue (    d.getAttributes()       .contains(a1));
        assertFalse(    d.getDeclaredAttributes().contains(a1));

        assertTrue (b.hasAttribute(B1));
        b.getAttribute(B1);
        assertTrue (c.hasAttribute(C1));
        c.getAttribute(C1);
        assertTrue (d.hasAttribute(C1));
        d.getAttribute(C1);

        assertTrue (d.hasAttribute(D1));
        d.getAttribute(D1);

        // The bad one

        assertTrue (!b.hasAttribute(C1));
        try {
	        b.getAttribute(C1);
	        fail("Attribute should not be there");
        } catch (NoSuchAttributeException expected) { /* excpected */ }
        
        assertTrue (!c.hasAttribute(B1));
        try {
	        c.getAttribute(B1);
	        fail("Attribute should not be there");
        } catch (NoSuchAttributeException expected) { /* excpected */ }

        assertTrue (!d.hasAttribute(B1));
        try {
	        d.getAttribute(B1);
	        fail("Attribute should not be there");
        } catch (NoSuchAttributeException expected) { /* excpected */ }
	}

    /**
     * Test for {@link com.top_logic.dob.meta.MOClassImpl#equals}.
     */
    public void testEquals () {
		MOClass a = new MOClassImpl(A);
		MOClass b = new MOClassImpl(B);
		MOClass c = new MOClassImpl(C);
		MOClass d = new MOClassImpl(D);

		// The good one
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
		assertTrue(c.equals(c));
		assertTrue(d.equals(d));

		assertEquals(a.hashCode(), a.hashCode());
		assertEquals(b.hashCode(), b.hashCode());
		assertEquals(c.hashCode(), c.hashCode());
		assertEquals(d.hashCode(), d.hashCode());

		// The bad one
		assertFalse(a.equals(b));
		assertFalse(b.equals(c));
		assertFalse(c.equals(b));
		assertFalse(d.equals(c));

		assertFalse(d.equals(this));
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMOClassImpl.class);
        return suite;
    }


    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
