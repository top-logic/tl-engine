/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.ReloadableManager.ReloadFailure;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.admin.system.ReloadOutcome;
import com.top_logic.layout.view.admin.system.ReloadableFunctions;

/**
 * Test of {@link ReloadableFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestReloadableFunctions extends TestCase {

	private static final String COUNTING = "TestReloadableFunctions.counting";

	private static final String FAILING = "TestReloadableFunctions.failing";

	private static final String REFUSING = "TestReloadableFunctions.refusing";

	private static final String UNKNOWN = "TestReloadableFunctions.unknown";

	private CountingReloadable _counting;

	private TestReloadable _failing;

	private TestReloadable _refusing;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_counting = new CountingReloadable(COUNTING);
		_failing = new FailingReloadable(FAILING);
		_refusing = new RefusingReloadable(REFUSING);
		ReloadableManager manager = ReloadableManager.getInstance();
		manager.addReloadable(_counting);
		manager.addReloadable(_failing);
		manager.addReloadable(_refusing);
	}

	@Override
	protected void tearDown() throws Exception {
		ReloadableManager manager = ReloadableManager.getInstance();
		manager.removeReloadable(_counting);
		manager.removeReloadable(_failing);
		manager.removeReloadable(_refusing);
		super.tearDown();
	}

	public void testOfService() {
		ResourcesModule.Module module = ResourcesModule.Module.INSTANCE;
		assertTrue("The resources service is running in the test setup.", module.isActive());
		String key = ReloadableFunctions.ofService(module);
		assertNotNull("The resources service is a registered reloadable.", key);
		assertSame(module.getImplementationInstance(), ReloadableManager.getInstance().get(key));

		assertNull(ReloadableFunctions.ofService(null));
	}

	public void testDescribe() {
		assertEquals(CountingReloadable.class.getSimpleName(), ReloadableFunctions.label(COUNTING));
		assertNull(ReloadableFunctions.label(UNKNOWN));
		assertNull(ReloadableFunctions.label(null));
	}

	public void testLabels() {
		assertEquals(
			CountingReloadable.class.getSimpleName() + " (" + COUNTING + ")" + ReloadableFunctions.LABEL_SEPARATOR
				+ UNKNOWN,
			ReloadableFunctions.labels(List.of(COUNTING, UNKNOWN)));
		assertEquals("", ReloadableFunctions.labels(null));
	}

	public void testReload() {
		ReloadOutcome outcome = ReloadableFunctions.reload(List.of(COUNTING));
		assertFalse(ReloadableFunctions.failed(outcome));
		assertEquals(List.of(COUNTING), outcome.reloaded());
		assertEquals(1, _counting.getCount());

		assertFalse(ReloadableFunctions.failed(ReloadableFunctions.reload(List.of(COUNTING))));
		assertEquals(2, _counting.getCount());

		String report = ReloadableFunctions.report(outcome);
		assertTrue(report, report.contains(COUNTING));
	}

	public void testFailuresDoNotStopTheOthers() {
		ReloadOutcome outcome = ReloadableFunctions.reload(List.of(FAILING, REFUSING, COUNTING, UNKNOWN));

		assertTrue(ReloadableFunctions.failed(outcome));
		assertEquals(List.of(COUNTING), outcome.reloaded());
		assertEquals(List.of(FAILING, REFUSING, UNKNOWN), List.copyOf(outcome.failures().keySet()));
		assertEquals(1, _counting.getCount());
		assertEquals(1, _failing.getCount());
		assertEquals(1, _refusing.getCount());

		ReloadFailure failure = outcome.failures().get(FAILING);
		assertTrue(failure.cause() instanceof IllegalStateException);
		assertNull(outcome.failures().get(REFUSING).cause());
		assertNull(outcome.failures().get(UNKNOWN).cause());

		String report = ReloadableFunctions.report(outcome);
		assertTrue(report, report.contains(failureMessage(FAILING)));
		assertTrue(report, report.contains(REFUSING));
		assertTrue(report, report.contains(UNKNOWN));
		assertFalse("No stack trace: " + report, report.contains("\tat "));
	}

	public void testReloadNothing() {
		assertEquals(List.of(), ReloadableFunctions.reload(null).reloaded());
		assertFalse(ReloadableFunctions.failed(ReloadableFunctions.reload(List.of())));
		assertFalse(ReloadableFunctions.failed(null));
		assertNull(ReloadableFunctions.report(null));
		assertEquals(0, _counting.getCount());
	}

	static String failureMessage(String name) {
		return "Expected failure of " + name;
	}

	private static abstract class TestReloadable implements Reloadable {

		private final String _name;

		private int _count;

		TestReloadable(String name) {
			_name = name;
		}

		int getCount() {
			return _count;
		}

		@Override
		public boolean reload() {
			_count++;
			return doReload();
		}

		abstract boolean doReload();

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getDescription() {
			return "Description of " + _name;
		}

		@Override
		public boolean usesXMLProperties() {
			return false;
		}
	}

	private static class CountingReloadable extends TestReloadable {

		CountingReloadable(String name) {
			super(name);
		}

		@Override
		boolean doReload() {
			return true;
		}
	}

	private static class FailingReloadable extends TestReloadable {

		FailingReloadable(String name) {
			super(name);
		}

		@Override
		boolean doReload() {
			throw new IllegalStateException(failureMessage(getName()));
		}
	}

	private static class RefusingReloadable extends TestReloadable {

		RefusingReloadable(String name) {
			super(name);
		}

		@Override
		boolean doReload() {
			return false;
		}
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestReloadableFunctions.class);
	}

}
