/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.ConfigLoaderTestUtil;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.RearrangableThreadContextSetup;
import test.com.top_logic.basic.TestUtils.RearrangeToEnd;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.KBSetup.KBType;
import test.com.top_logic.layout.scripting.runtime.TestedApplication;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.scripting.runtime.Application;

/**
 * {@link TestSetup} that start the application from the wrapped test's module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationTestSetup extends RearrangableThreadContextSetup {

	/**
	 * Counts nested setups and prevents overriding of the
	 * {@link ApplicationTestSetup#application} variable.
	 */
	private static MutableInteger setupCnt = new MutableInteger();
	static TestedApplication application;

	private AssertNoErrorLogListener _logListener;

	private ApplicationTestSetup(Test test) {
		super(test, setupCnt);
	}
	
	@Override
	public void doSetUp() throws Exception {
		String mainLayoutDefinition = LayoutConfig.getDefaultLayout();
		application = new TestedApplication(ConfigLoaderTestUtil.getApplicationRoot(), mainLayoutDefinition);

		_logListener = new AssertNoErrorLogListener();
		_logListener.activate();
		try {
			application.startup();
		} finally {
			_logListener.deactivate();
		}
	}

	@Override
	public void basicRun(TestResult result) {
		super.basicRun(result);
		if (_logListener == null) {
			/* This happens when multiple ApplicationTestSetup's are nested. In this case the
			 * doSetUp() and doTearDown() methods are not called, but the basicRun(TestResult)
			 * method. */
			assert setupCnt.intValue() > 1 : "Setup count is == 1 iff doSetup() was called";
			return;
		}

		List<LogEntry> entries = _logListener.getAndClearLogEntries();
		if (!entries.isEmpty()) {
			StringBuilder message = new StringBuilder("Errors (" + entries.size() + ") logged during startup: ");
			for (LogEntry entry : entries) {
				message.append("\n\t");
				message.append(entry.getMessage());
			}

			// Only report a single failure, since at most one is displayed in the Eclipse JUnit
			// view.
			AssertionFailedError failure = new AssertionFailedError(message.toString());
			failure.initCause(entries.get(0).getException().getElse(null));
			result.addFailure(this, failure);
		}
	}

	@Override
	public void doTearDown() throws Exception {
		application.terminate();
		application = null;
		_logListener = null;
	}
	
	/**
	 * The module's application.
	 */
	public static Application getApplication() {
		return application;
	}

	/**
	 * Build a layout name that loads a test layout relative to the given test class.
	 * 
	 * <p>
	 * The layout definition is expected to have the same name as the test class with the extension
	 * <code>.xml</code>.
	 * </p>
	 * 
	 * @param testClass
	 *        The unit test class.
	 * @return The layout name for {@link Application#login(Person, TimeZone, Locale, String)}.
	 */
	public static String loadLayoutDefinition(Class<?> testClass) throws IOException {
		String testLayoutDefinition = testClass.getSimpleName() + ".xml";
		File layoutFile = new File(ConfigLoaderTestUtil.getApplicationRoot(),
			CustomPropertiesDecorator.createFileName(testClass, testLayoutDefinition));
		
		assert layoutFile.exists() : "Missing layout file: " + layoutFile.getAbsolutePath();
		assert layoutFile.isFile() : "Not a (layout) file: " + layoutFile.getAbsolutePath();
		
		return ComponentTestUtils.getTestLayoutScope(layoutFile);
	}

	/**
	 * Creates a setup for the given test starting the module's application (without the test
	 * configuration).
	 */
	public static Test setupApplication(Test test) {
		// enclose the KBSetup with another TestSetup to ensure that this test
		// is not merged within unit tests when calling
		// TestUtils.rearrangeTest(Test)
		return new Setup(kbTest(test));
	}

	/**
	 * Creates a setup for the given test starting the module's application (without the test
	 * configuration) and forces the usage of the given database.
	 */
	public static Test setupApplication(Test test, KBType kb) {
		// enclose the KBSetup with another TestSetup to ensure that this test
		// is not merged within unit tests when calling
		// TestUtils.rearrangeTest(Test)
		return new Setup(kbTest(test, kb));
	}

	/**
	 * Creates a setup for the given test class starting the module's application (without the test
	 * configuration).
	 */
	public static Test setupApplication(Class<? extends Test> testClass) {
		// enclose the KBSetup with another TestSetup to ensure that this test
		// is not merged within unit tests when calling
		// TestUtils.rearrangeTest(Test)
		return setupApplication(new TestSuite(testClass));
	}

	/**
	 * Creates a setup for the given test starting the module's test application (including the test
	 * configuration).
	 */
	public static Test setupTestApplication(Test test) {
		return new TestAppSetup(kbTest(test));
	}

	/**
	 * Creates a setup for the given test class starting the module's test application (including
	 * the test configuration).
	 */
	public static Test setupTestApplication(Class<? extends Test> testClass) {
		return setupTestApplication(new TestSuite(testClass));
	}

	private static Test kbTest(Test test, KBType kb) {
		return KBSetup.getKBTest(wrap(test), kb);
	}

	private static Test kbTest(Test test) {
		return KBSetup.getSingleKBTest(wrap(test));
	}

	public static Test wrap(Test test) {
		return new ApplicationTestSetup(test);
	}
	
	/**
	 * Setup wrapped around the returned Test (the sequence started with {@link KBSetup}) to ensure
	 * that application tests are not merged within unit tests.
	 * <p>
	 * The wrapped setup must be a {@link RearrangableTestSetup} to ensure that all tests with
	 * application test setup are merged into one setup. <br/>
	 * This class implements {@link RearrangeToEnd}, as application tests have to be executed last.
	 * That's because they don't use the test configuration. And some configuration values are
	 * stored in static variables where they are not updated when the configuration changes. This
	 * will break some none application tests if they are executed after the application tests. (It
	 * might also break some application tests if they are executed after tests that use the test
	 * configuration, but there is currently no such case.)
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class Setup extends RearrangableTestSetup implements RearrangeToEnd {

		private static MutableInteger setupCnt = new MutableInteger();

		private boolean _before;

		public Setup(Test test) {
			super(test, setupCnt);
		}

		/**
		 * Deactivates reading of test configuration, this is done to ensure that the database is
		 * reset which is finally used by the application tests.
		 * 
		 * @see test.com.top_logic.basic.DecoratedTestSetup#doSetUp()
		 */
		@Override
		protected void doSetUp() throws Exception {
			_before = ConfigLoaderTestUtil.INSTANCE.useTestConfiguration(false);
		}

		@Override
		protected void doTearDown() throws Exception {
			ConfigLoaderTestUtil.INSTANCE.useTestConfiguration(_before);
		}
	}

	private static final class TestAppSetup extends RearrangableTestSetup implements RearrangeToEnd {
		private static MutableInteger setupCnt = new MutableInteger();

		private boolean _before;

		public TestAppSetup(Test test) {
			super(test, setupCnt);
		}

		@Override
		protected void doSetUp() throws Exception {
			_before = ConfigLoaderTestUtil.INSTANCE.useTestConfiguration(true);
		}

		@Override
		protected void doTearDown() throws Exception {
			ConfigLoaderTestUtil.INSTANCE.useTestConfiguration(_before);
		}
	}

}
