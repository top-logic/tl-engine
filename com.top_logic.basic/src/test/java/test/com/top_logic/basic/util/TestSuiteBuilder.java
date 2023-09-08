/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.SetBuilder;

/**
 * Just as the name says, a builder for {@link TestSuite}s. <br/>
 * After the suite has been build ({@link #buildSuite()}),
 * the builder becomes invalid and must not be used any more.
 * 
 * Similar to {@link ListBuilder}, {@link SetBuilder} and {@link MapBuilder}.
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class TestSuiteBuilder {

	private static final String BUILDER_INVALID =
		"The suite has already been build and delivered! This builder has therefore reached the end of its life and is now invalid!";

	private final TestSuite suite = new TestSuite();

	/**
	 * Has the {@link #suite} already been build via {@link #buildSuite()}?
	 * If so, this builder has reached the end of its life and is invalid now.
	 */
	private boolean alive = true;

	/**
	 * @deprecated as it gives no name to the {@link TestSuite}. Use {@link #TestSuiteBuilder(String)} instead.
	 */
	@Deprecated
	public TestSuiteBuilder() {
		// Just declare it and make it public
	}
	
	public TestSuiteBuilder(String suiteName) {
		suite.setName(suiteName);
	}
	
	/** Must not be called after {@link #buildSuite()} has been called! */
	public TestSuiteBuilder addTest(Test test) {
		checkBuilderAlive();
		suite.addTest(test);
		return this;
	}

	/** Must not be called after {@link #buildSuite()} has been called! */
	public TestSuiteBuilder addTestSuite(Class<? extends TestCase> testClass) {
		checkBuilderAlive();
		suite.addTestSuite(testClass);
		return this;
	}

	/**
	 * Convenience redirect to {@link #addAllTestSuites(Class...)}
	 * Must not be called after {@link #buildSuite()} has been called!
	 */
	public TestSuiteBuilder addAllTests(Test... tests) {
		checkBuilderAlive();
		return addAllTests(Arrays.asList(tests));
	}

	/** Must not be called after {@link #buildSuite()} has been called! */
	public TestSuiteBuilder addAllTests(Iterable<Test> tests) {
		checkBuilderAlive();
		for (Test test : tests) {
			suite.addTest(test);
		}
		return this;
	}

	/**
	 * Convenience redirect to {@link #addAllTestSuites(Iterable)}
	 * Must not be called after {@link #buildSuite()} has been called!
	 */
	public TestSuiteBuilder addAllTestSuites(Class<? extends TestCase>... testClasses) {
		checkBuilderAlive();
		return addAllTestSuites(Arrays.asList(testClasses));
	}

	/** Must not be called after {@link #buildSuite()} has been called! */
	public TestSuiteBuilder addAllTestSuites(Iterable<Class<? extends TestCase>> testClasses) {
		checkBuilderAlive();
		for (Class<? extends TestCase> testClass : testClasses) {
			suite.addTestSuite(testClass);
		}
		return this;
	}

	/**
	 * Builds the suite and returns it.
	 * After this method has been called, this builder is invalid and must not be used anymore.
	 * Therefore, must not be called twice!
	 */
	public TestSuite buildSuite() {
		checkBuilderAlive();
		alive = false;
		return suite;
	}

	/**
	 * Returns <code>true</code> after {@link #buildSuite()} has been called.
	 * If this method returns <code>false</code>,
	 * this builder has reached the end of its life and must not be used anymore.
	 */
	public boolean isAlive() {
		return alive;
	}

	private void checkBuilderAlive() {
		if (!alive) {
			throw new IllegalStateException(BUILDER_INVALID);
		}
	}

}
