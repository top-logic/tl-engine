/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestTestSuiteBuilder extends TestCase {

	private class DummyTest implements Test {

		@Override
		public int countTestCases() {
			return 1;
		}

		@Override
		public void run(TestResult result) {
			throw new UnsupportedOperationException("Dummy tests cannot be run!");
		}

	}

	private class DummySuiteAlpha extends TestCase {

		public void testDummyTestMethodAlpha() {}

		@Override
		public String toString() {
			return getClass().getCanonicalName();
		}
	}

	private class DummySuiteBeta extends TestCase {

		public void testDummyTestMethodBeta() {}

		@Override
		public String toString() {
			return getClass().getCanonicalName();
		}
	}

	private static final String SUITE_NAME = "Some Test Suite Name";

	public TestTestSuiteBuilder(String name) {
		super(name);
	}

	public void testIsAliveOnNewBuilder() {
		assertTrue("The builder is not alive, immediately after creation!", new TestSuiteBuilder(SUITE_NAME).isAlive());
	}

	public void testIsAliveOnNonEmptyBuilder() {
		assertTrue("The builder is no longer alive, after a test has been added!", new TestSuiteBuilder(SUITE_NAME).addTest(new DummyTest()).isAlive());
	}

	public void testIsAliveOnDeadBuilder() {
		TestSuiteBuilder builder = new TestSuiteBuilder(SUITE_NAME);
		builder.buildSuite();
		assertFalse("The builder has delivered the suite and should be dead now, but it claims to be alive!", builder.isAlive());
	}

	public void testBuildEmptySuite() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).buildSuite();
		assertEquals("Added no test but the built suite says something else!", 0, resultSuite.testCount());
	}

	public void testAddTest() {
		Test dummyTest = new DummyTest();
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addTest(dummyTest).buildSuite();
		assertEquals(1, resultSuite.testCount());
		assertEquals(dummyTest, resultSuite.testAt(0));
	}

	public void testAddAllTestsForVarArgs() {
		Test dummyTest1 = new DummyTest();
		Test dummyTest2 = new DummyTest();
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addAllTests(dummyTest1, dummyTest2).buildSuite();
		assertEquals(2, resultSuite.testCount());
		assertEquals(dummyTest1, resultSuite.testAt(0));
		assertEquals(dummyTest2, resultSuite.testAt(1));
	}

	public void testAddAllTestsForVarArgsWithNoTest() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addAllTests().buildSuite();
		assertEquals(0, resultSuite.testCount());
	}

	public void testAddAllTestsForIterables() {
		Test dummyTest1 = new DummyTest();
		Test dummyTest2 = new DummyTest();
		List<Test> testList = Arrays.asList(dummyTest1, dummyTest2);
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addAllTests(testList).buildSuite();
		assertEquals(2, resultSuite.testCount());
		assertEquals(dummyTest1, resultSuite.testAt(0));
		assertEquals(dummyTest2, resultSuite.testAt(1));
	}

	public void testAddAllTestsForIterablesWithNoTest() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addAllTests(Collections.<Test>emptyList()).buildSuite();
		assertEquals(0, resultSuite.testCount());
	}
	
	public void testAddTestSuite() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addTestSuite(DummySuiteAlpha.class).buildSuite();
		assertEquals(1, resultSuite.testCount());
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(0).toString());
	}

	public void testAddAllTestSuitesForVarArgs() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME)
			.addAllTestSuites(DummySuiteAlpha.class, DummySuiteBeta.class).buildSuite();
		assertEquals(2, resultSuite.testCount());
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(0).toString());
		assertEquals(DummySuiteBeta.class.getName(), resultSuite.testAt(1).toString());
	}

	public void testAddAllTestSuitesForVarArgsWithNoSuite() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME).addAllTestSuites().buildSuite();
		assertEquals(0, resultSuite.testCount());
	}
	
	public void testAddAllTestSuitesForIterables() {
		List<Class<? extends TestCase>> testList =
			Arrays.<Class<? extends TestCase>> asList(DummySuiteAlpha.class, DummySuiteBeta.class);
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME)
		.addAllTestSuites(testList).buildSuite();
		assertEquals(2, resultSuite.testCount());
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(0).toString());
		assertEquals(DummySuiteBeta.class.getName(), resultSuite.testAt(1).toString());
	}

	public void testAddAllTestSuitesForIterablesWithNoTest() {
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME)
			.addAllTestSuites(Collections.<Class<? extends TestCase>> emptyList()).buildSuite();
		assertEquals(0, resultSuite.testCount());
	}

	public void testAllAddMethodsInCombination() {
		Test dummyTestAlpha = new DummyTest();
		Test dummyTestBeta = new DummyTest();
		Test dummyTestGamma = new DummyTest();
		Test dummyTestDelta = new DummyTest();
		Test dummyTestEpsilon = new DummyTest();
		
		TestSuite resultSuite = new TestSuiteBuilder(SUITE_NAME)
			.addTest(dummyTestAlpha)
			.addAllTests(dummyTestBeta, dummyTestGamma)
			.addAllTests(Arrays.asList(dummyTestDelta, dummyTestEpsilon))
			.addTestSuite(DummySuiteAlpha.class)
			.addAllTestSuites(DummySuiteAlpha.class, DummySuiteBeta.class)
			.addAllTestSuites(Arrays.<Class<? extends TestCase>> asList(DummySuiteAlpha.class, DummySuiteBeta.class))
			.buildSuite();

		assertEquals(10, resultSuite.testCount());

		assertEquals(dummyTestAlpha, resultSuite.testAt(0));
		assertEquals(dummyTestBeta, resultSuite.testAt(1));
		assertEquals(dummyTestGamma, resultSuite.testAt(2));
		assertEquals(dummyTestDelta, resultSuite.testAt(3));
		assertEquals(dummyTestEpsilon, resultSuite.testAt(4));
		
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(5).toString());
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(6).toString());
		assertEquals(DummySuiteBeta.class.getName(), resultSuite.testAt(7).toString());
		assertEquals(DummySuiteAlpha.class.getName(), resultSuite.testAt(8).toString());
		assertEquals(DummySuiteBeta.class.getName(), resultSuite.testAt(9).toString());
	}

	public void testConstructorPassesSuiteName() {
		String actualName = new TestSuiteBuilder(SUITE_NAME).buildSuite().getName();
		assertEquals(SUITE_NAME, actualName);
	}
	
	public static Test suite() {
		return new TestSuite(TestTestSuiteBuilder.class);
	}

}
