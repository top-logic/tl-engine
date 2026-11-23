/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.base.cluster.ClusterManagerSetup;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.LockService;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.service.KnowledgeBaseException;

/**
 * Base class for tests of a failing application start through
 * {@link AbstractStartStopListener#contextInitialized(ServletContextEvent)}.
 *
 * <p>
 * A service whose start-up throws a given problem is started as module system of the application.
 * Each test checks that the problem is reported exactly once with its stack trace and that the
 * application is shut down afterwards. Whether the start runs immediately or deferred is decided by
 * the custom configuration of the concrete test class.
 * </p>
 */
public abstract class StartupFailureTestCase extends TestCase {

	private CollectingLogListener _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_log = new CollectingLogListener(Set.of(Level.ERROR, Level.FATAL), true);
	}

	@Override
	protected void tearDown() throws Exception {
		_log.deactivate();
		FailingService.setProblem(null);
		super.tearDown();
	}

	/**
	 * A {@link RuntimeException} thrown while starting a service.
	 */
	public void testRuntimeException() {
		checkStartupFailure(new IllegalStateException("Startup failure for test."));
	}

	/**
	 * An {@link Error} thrown while starting a service.
	 */
	public void testError() {
		checkStartupFailure(new OutOfMemoryError("Startup failure for test."));
	}

	/**
	 * A checked exception thrown while starting a service.
	 */
	public void testCheckedException() {
		checkStartupFailure(new Exception("Startup failure for test."));
	}

	private void checkStartupFailure(Throwable problem) {
		FailingService.setProblem(problem);
		assertFalse("Service started during boot is active before the start.", LockService.Module.INSTANCE.isActive());

		startApplication(new TestingStartStopListener());

		assertFalse("Service started during boot is not shut down.", LockService.Module.INSTANCE.isActive());
		assertFalse("Failing service is active.", FailingService.Module.INSTANCE.isActive());
		assertFalse("Deferred boot service is not shut down.", DeferredBootService.Module.INSTANCE.isActive());

		List<LogEntry> errors = _log.getLogEntries();
		assertEquals("Exactly one report of the startup failure expected: " + errors, 1, errors.size());
		LogEntry report = errors.get(0);
		assertTrue("Unexpected report: " + report.getMessage(), report.getMessage().startsWith("Failed to start"));
		assertTrue("Report without stack trace.", report.getException().hasValue());
		assertTrue("Report does not contain the cause.", hasCause(report.getException().getElseError(), problem));
	}

	/**
	 * Starts the given application and checks that its start fails.
	 */
	protected abstract void startApplication(AbstractStartStopListener application);

	/**
	 * Lets the {@link AbstractStartStopListener} initialize the application.
	 */
	protected static void initializeContext(AbstractStartStopListener application) {
		application.contextInitialized(new ServletContextEvent(createServletContext()));
	}

	private static boolean hasCause(Throwable reported, Throwable problem) {
		for (Throwable ex = reported; ex != null; ex = ex.getCause()) {
			if (ex == problem) {
				return true;
			}
		}
		return false;
	}

	private static ServletContext createServletContext() {
		String webapp = Workspace.topLevelWebapp().getAbsolutePath();
		return (ServletContext) Proxy.newProxyInstance(StartupFailureTestCase.class.getClassLoader(),
			new Class<?>[] { ServletContext.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getRealPath" -> webapp;
				case "getContextPath" -> "";
				case "getMajorVersion" -> 6;
				case "getMinorVersion" -> 0;
				// Marks the application as running in a test, see ContainerDetector#isTesting().
				case "getServerInfo" -> "test container";
				case "hashCode" -> System.identityHashCode(proxy);
				case "equals" -> proxy == args[0];
				case "toString" -> "TestServletContext";
				default -> null;
			});
	}

	/**
	 * Creates the test setup for the given concrete test class.
	 *
	 * <p>
	 * The class provides its custom configuration in a <code>.properties.config.xml</code> file next
	 * to it.
	 * </p>
	 */
	protected static Test suite(Class<? extends StartupFailureTestCase> testClass) {
		Test test = new CustomPropertiesSetup(testClass, true);
		test = ClusterManagerSetup.setupClusterTables(test);
		return KBSetup.getSingleKBTest(test);
	}

	/**
	 * {@link AbstractStartStopListener} whose module system consists of the {@link FailingService}
	 * only.
	 */
	private static class TestingStartStopListener extends AbstractStartStopListener {

		@Override
		protected Version getVersion() {
			return Version.getApplicationVersion();
		}

		@Override
		protected XMLPropertiesConfig initXMLProperties(ServletContext context) throws Exception {
			return XMLProperties.existingConfig();
		}

		@Override
		protected void initDefaultUncaughtExceptionHandler() {
			// Keep the handler of the test environment.
		}

		@Override
		void startupModuleSystem(Version theVersion) throws ModuleException, KnowledgeBaseException {
			ModuleUtil.INSTANCE.startUp(FailingService.Module.INSTANCE);
		}

		@Override
		protected void initSubClassHook(ServletContext context) throws Exception {
			fail("Application must not be initialized after a failed module system start.");
		}

		@Override
		protected void exitSubClassHook(ServletContext context) {
			// Nothing to clean up.
		}

	}

	/**
	 * Service whose start-up throws the problem set by {@link #setProblem(Throwable)}.
	 */
	public static class FailingService extends ManagedClass {

		private static Throwable _problem;

		/**
		 * Sets the problem the next start-up throws.
		 */
		static void setProblem(Throwable problem) {
			_problem = problem;
		}

		@Override
		protected void startUp() {
			super.startUp();
			FailingService.<RuntimeException> throwUnchecked(_problem);
		}

		@SuppressWarnings("unchecked")
		private static <E extends Throwable> void throwUnchecked(Throwable problem) throws E {
			throw (E) problem;
		}

		/**
		 * {@link BasicRuntimeModule} of the {@link FailingService}.
		 */
		public static class Module extends BasicRuntimeModule<FailingService> {

			/**
			 * Singleton {@link FailingService.Module} instance.
			 */
			public static final FailingService.Module INSTANCE = new FailingService.Module();

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return NO_DEPENDENCIES;
			}

			@Override
			public Class<FailingService> getImplementation() {
				return FailingService.class;
			}

			@Override
			protected FailingService newImplementationInstance() throws ModuleException {
				return new FailingService();
			}

		}

	}

}
