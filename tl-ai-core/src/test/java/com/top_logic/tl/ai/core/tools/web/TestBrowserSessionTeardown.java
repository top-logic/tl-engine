/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;

/**
 * Gated end-to-end tests that closing a {@link BrowserSession} really destroys its operating-system
 * processes - including on the <b>broken-session</b> path, where it used not to.
 *
 * <p>
 * <b>The defect.</b> Every Playwright call of a session is confined to one single-threaded executor.
 * When a call wedges, the outer deadline in {@code callOnThread} raises
 * {@link BrowserSessionBrokenException} - but {@code cancel(true)} cannot interrupt a native read on
 * the driver pipe, so the thread stays wedged. {@code close()} then submitted its teardown to that
 * <em>same</em> executor: the task queued behind the wedged call, the bounded {@code get} expired,
 * {@code shutdownNow()} dropped the queued task, and the Chromium plus node driver processes lived on
 * for the JVM's lifetime - while {@link BrowserSessionPool} had already released the session's slot.
 * So {@code max-concurrent-sessions} stopped bounding real memory on exactly the path the
 * broken-session exception exists for.
 * </p>
 *
 * <p>
 * A real browser is the only honest fixture here: the assertion is about OS processes, so it is made
 * against {@link ProcessHandle}s, not against a mock. Gated behind
 * {@link BrowserE2e} like the other browser E2E tests; it needs <b>no network</b>.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserSessionTeardown extends TestCase {

	/** No navigation happens, so the domain policy is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** How long the wedge occupies the session thread - far beyond the close budget below. */
	private static final long WEDGE_MILLIS = 60_000;

	/** Close budget used here, so the test does not sit out the production 30 s. */
	private static final long CLOSE_BUDGET_MILLIS = 500;

	/** How long the processes are given to actually disappear after {@code close()} returned. */
	private static final long REAP_TIMEOUT_MILLIS = 10_000;

	/**
	 * <b>The regression.</b> With the session thread wedged in a way interruption cannot end - exactly
	 * the state a {@link BrowserSessionBrokenException} reports - {@code close()} must still leave no
	 * process behind. Before the fix the driver and its Chromium survived {@code close()} and outlived
	 * the JVM's interest in them entirely.
	 */
	public void testWedgedSessionLeavesNoProcessBehind() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("wedged-teardown", TIMEOUT_MILLIS, ALLOW_ANY);
		List<ProcessHandle> tree;
		try {
			ProcessHandle driver = session.driverProcessForTest();
			assertNotNull("the Playwright driver process must be identifiable, otherwise a wedged "
				+ "session cannot be force-terminated at all", driver);
			assertTrue("the driver is running", driver.isAlive());

			tree = new ArrayList<>(driver.descendants().toList());
			tree.add(driver);
			assertTrue("the browser hangs off the driver, so the tree is more than the driver alone: "
				+ describe(tree), tree.size() > 1);

			// Wedge the session thread uninterruptibly, then close: the teardown task can never run.
			session.wedgeSessionThreadForTest(WEDGE_MILLIS);
		} catch (RuntimeException | Error failure) {
			session.close();
			throw failure;
		}

		long closeStarted = System.nanoTime();
		session.close(CLOSE_BUDGET_MILLIS);
		long closeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - closeStarted);

		assertTrue("close() must not wait out the wedge - the pool's teardown thread would be pinned "
			+ "for the whole wedge: took " + closeMillis + " ms", closeMillis < WEDGE_MILLIS / 2);
		awaitAllGone(tree);
		for (ProcessHandle process : tree) {
			assertFalse("a browser process survived the teardown of a wedged session: "
				+ describe(List.of(process)), process.isAlive());
		}
	}

	/** The ordinary path is unchanged: an orderly close leaves nothing running either. */
	public void testHealthySessionClosesWithoutLeavingProcesses() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("clean-teardown", TIMEOUT_MILLIS, ALLOW_ANY);
		ProcessHandle driver = session.driverProcessForTest();
		assertNotNull(driver);
		List<ProcessHandle> tree = new ArrayList<>(driver.descendants().toList());
		tree.add(driver);

		session.close();

		awaitAllGone(tree);
		for (ProcessHandle process : tree) {
			assertFalse("an orderly close left a process behind: " + describe(List.of(process)),
				process.isAlive());
		}
	}

	/**
	 * The pool's slot accounting survives the broken path: a session whose thread wedged is torn down
	 * and its slot handed back, so the cap keeps admitting work rather than deadlocking on a resource
	 * that no longer exists.
	 */
	public void testPoolReleasesTheSlotOfAWedgedSession() {
		BrowserE2e.assumeEnabled();
		List<BrowserSession> opened = new ArrayList<>();
		BrowserSessionPool<BrowserSession> pool = new BrowserSessionPool<>(1, 60_000L, 30_000L,
			System::nanoTime,
			key -> {
				BrowserSession session =
					BrowserSession.open(key, TIMEOUT_MILLIS, ALLOW_ANY);
				opened.add(session);
				return session;
			},
			session -> session.close(CLOSE_BUDGET_MILLIS));
		try {
			BrowserSession wedged =
				pool.run("wedged", session -> {
					session.wedgeSessionThreadForTest(WEDGE_MILLIS);
					return session;
				});
			ProcessHandle driver = wedged.driverProcessForTest();
			assertNotNull(driver);
			List<ProcessHandle> tree = new ArrayList<>(driver.descendants().toList());
			tree.add(driver);

			pool.invalidate("wedged");

			// A cap of one: the second session can only be admitted once the first one's slot came
			// back, which only happens after its destroyer returned.
			pool.run("next", session -> session);

			assertEquals("the cap still bounds exactly one live resource", 1, pool.liveResourceCount());
			awaitAllGone(tree);
			for (ProcessHandle process : tree) {
				assertFalse("the evicted session's processes are gone, so the slot the pool handed on "
					+ "really was free: " + describe(List.of(process)), process.isAlive());
			}
		} finally {
			pool.close();
			opened.forEach(session -> session.close(CLOSE_BUDGET_MILLIS));
		}
	}

	/** Waits until every process has exited, or the reap timeout elapses. */
	private static void awaitAllGone(List<ProcessHandle> processes) {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(REAP_TIMEOUT_MILLIS);
		while (System.nanoTime() < deadline) {
			if (processes.stream().noneMatch(ProcessHandle::isAlive)) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private static String describe(List<ProcessHandle> processes) {
		StringBuilder out = new StringBuilder();
		for (ProcessHandle process : processes) {
			out.append(process.pid()).append('=')
				.append(process.info().command().orElse("?")).append(' ');
		}
		return out.toString();
	}}
