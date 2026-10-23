/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a background thread reaches a browser window within the time it can afford, and that work
 * it gave up on is not done behind its back.
 */
public class TestWindowInteraction extends BasicTestCase {

	private WindowInteraction _interaction;

	private ReactWindowRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_interaction = new WindowInteraction();
		_registry = new ReactWindowRegistry("test-session");
	}

	@Override
	protected void tearDown() throws Exception {
		_interaction.close();
		super.tearDown();
	}

	/** A window nobody is busy in is entered at once, in its own sub-session. */
	public void testAFreeWindowRunsTheWork() {
		TLSubSessionContext window = subSession();
		AtomicReference<TLSubSessionContext> seen = new AtomicReference<>();

		boolean ran = _interaction.run(_registry, window, 5_000, () -> seen.set(TLContextManager.getSubSession()));

		assertTrue(ran);
		assertSame("The work runs in the window's sub-session.", window, seen.get());
	}

	/**
	 * A window busy with a browser command for longer than the caller waits is reported as not
	 * reached, and the work does not run once the command has finished either: the caller has
	 * already told its model that nothing was written.
	 */
	public void testABusyWindowCallsTheWorkOff() throws Exception {
		AtomicInteger runs = new AtomicInteger();

		boolean ran;
		try (Interaction command = _registry.beginInteraction()) {
			ran = _interaction.run(_registry, subSession(), 100, runs::incrementAndGet);
		}
		// The thread that waited for the interaction gets it now, and must leave without the work.
		Thread.sleep(500);

		assertFalse(ran);
		assertEquals(0, runs.get());
	}

	/** What the work throws reaches the caller, as it did when the work ran on the caller's thread. */
	public void testAFailureReachesTheCaller() {
		try {
			_interaction.run(_registry, subSession(), 5_000, () -> {
				throw new IllegalStateException("broken channel");
			});
			fail("The failure was swallowed.");
		} catch (IllegalStateException ex) {
			assertEquals("broken channel", ex.getMessage());
		}
	}

	/** The test thread's sub-session, standing in for the window's. */
	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	/**
	 * The suite, with the services a sub-session context needs.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestWindowInteraction.class), ModelService.Module.INSTANCE));
	}

}
