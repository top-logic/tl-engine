/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import test.com.top_logic.basic.SimpleTestFactory;

/**
 * The class {@link TestLocalTestSetup} tests functionality of {@link LocalTestSetup}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLocalTestSetup extends TestCase {

	private static class _LocalTestSetup extends LocalTestSetup {

		private final String _name;

		private final Collection<String> _storage;

		public _LocalTestSetup(Collection<String> storage, String name, Test test) {
			super(test);
			_storage = storage;
			_name = name;
		}

		@Override
		protected void setUpLocal() throws Exception {
			super.setUpLocal();
			_storage.add("setUp_" + _name);
		}

		@Override
		protected void tearDownLocal() throws Exception {
			super.tearDownLocal();
			_storage.add("tearDown_" + _name);
		}

	}

	public void testCorrectSetupCallOrder() {
		final ArrayList<String> callOrder = new ArrayList<>();
		Test innerTest = SimpleTestFactory.newSuccessfulTest("testName");

		LocalTestSetup inner = new _LocalTestSetup(callOrder, "inner", innerTest);
		LocalTestSetup outer = new _LocalTestSetup(callOrder, "outer", inner);
		
		TestResult result = new TestResult();
		outer.run(result);
		
		assertEquals("Ticket #4183: no setup called", 4, callOrder.size());
		assertEquals("setUp_outer", callOrder.get(0));
		assertEquals("setUp_inner", callOrder.get(1));
		assertEquals("tearDown_inner", callOrder.get(2));
		assertEquals("tearDown_outer", callOrder.get(3));
		
	}

	public void testNormalInnerTest() {
		final ArrayList<String> callOrder = new ArrayList<>();
		final String msg = "Inner test run!";
		Test innerTest = new Test() {

			@Override
			public void run(TestResult result) {
				result.startTest(this);
				callOrder.add(msg);
				result.endTest(this);
			}

			@Override
			public int countTestCases() {
				return 1;
			}
		};
		LocalTestSetup inner = new _LocalTestSetup(callOrder, "inner", innerTest);
		LocalTestSetup setup = new _LocalTestSetup(callOrder, "outer", inner);

		TestResult result = new TestResult();
		setup.run(result);

		assertEquals(5, callOrder.size());
		assertEquals("setUp_outer", callOrder.get(0));
		assertEquals("setUp_inner", callOrder.get(1));
		assertEquals(msg, callOrder.get(2));
		assertEquals("tearDown_inner", callOrder.get(3));
		assertEquals("tearDown_outer", callOrder.get(4));
	}

	public void testSetupCalledButNoInnerTestRun() throws Exception {
		final ArrayList<String> callOrder = new ArrayList<>();
		Test innerTest = new Test() {

			@Override
			public int countTestCases() {
				return 1;
			}

			@Override
			public void run(TestResult result) {
				throw new RuntimeException("Test run illegal");
			}

		};
		LocalTestSetup setup = new _LocalTestSetup(callOrder, "inner", innerTest);
		LocalTestSetup outerSetup = new _LocalTestSetup(callOrder, "outer", setup);

		outerSetup.setUp();
		assertEquals(2, callOrder.size());
		assertEquals("setUp_outer", callOrder.get(0));
		assertEquals("setUp_inner", callOrder.get(1));

		callOrder.clear();
		outerSetup.tearDown();
		assertEquals(2, callOrder.size());
		assertEquals("tearDown_inner", callOrder.get(0));
		assertEquals("tearDown_outer", callOrder.get(1));

	}

	public static Test suite() {
		return new TestSuite(TestLocalTestSetup.class);
	}

}

