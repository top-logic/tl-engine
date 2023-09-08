/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for visiting configuration items created with {@link TypedConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTypedConfigurationVisit extends BasicTestCase implements Scenario2 {
	
	private static final class TestingVisitor implements V {
		public TestingVisitor() {
			// Default constructor.
		}
		
		@Override
		public int visitB(B b, int arg) {
			return arg + b.getX();
		}

		@Override
		public int visitC(C c, int arg) {
			return arg + c.getX() + c.getY();
		}

		@Override
		public int visitD(D d, int arg) {
			return arg + d.getZ();
		}
	}

	public void testVisitA() {
		try {
			A a = TypedConfiguration.newConfigItem(A.class);
			a.visit(new TestingVisitor(), 100);
			fail("Visit must terminate abnormally.");
		} catch (AssertionError ex) {
			// Expected, type A is "abstract", since no visit choice matches.
		}
	}
	
	public void testVisitB() {
		B b = TypedConfiguration.newConfigItem(B.class);
		b.setX(5);
		assertEquals(105, b.visit(new TestingVisitor(), 100));
	}
	
	public void testVisitC() {
		C c = TypedConfiguration.newConfigItem(C.class);
		c.setX(7);
		c.setY(20);
		assertEquals(127, c.visit(new TestingVisitor(), 100));
	}
	
	public void testVisitC2() {
		C c = TypedConfiguration.newConfigItem(C2.class);
		c.setX(7);
		c.setY(20);
		assertEquals(127, c.visit(new TestingVisitor(), 100));
	}
	
	public void testVisitD() {
		D d = TypedConfiguration.newConfigItem(D.class);
		d.setZ(13);
		assertEquals(113, d.visit(new TestingVisitor(), 100));
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTypedConfigurationVisit.class));
	}

}
