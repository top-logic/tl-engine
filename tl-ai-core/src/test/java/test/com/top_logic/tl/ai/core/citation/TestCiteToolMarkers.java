/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tl.ai.core.citation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.citation.CitationMarkers;
import com.top_logic.tl.ai.core.tools.interaction.CiteTool;

/**
 * The cite tool numbers its markers per run, not per thread.
 *
 * <p>
 * The tool is {@code READ}-classified, so two cite calls of one turn are dispatched on two pool
 * threads. A counter that lived in a {@code ThreadLocal} gave both of them the number 1, the model
 * was told to write {@code [1]} for two different sources, and the second source was lost.
 * </p>
 */
public class TestCiteToolMarkers extends TestCase {

	/** Two cite calls running in parallel on one run get two different markers. */
	public void testParallelCallsGetDistinctMarkers() throws Exception {
		CiteTool tool = new CiteTool();
		ToolContext context = runContext();

		int calls = 8;
		CyclicBarrier together = new CyclicBarrier(calls);
		ExecutorService pool = Executors.newFixedThreadPool(calls);
		try {
			List<Callable<String>> work = new java.util.ArrayList<>();
			for (int i = 0; i < calls; i++) {
				String url = "https://example.test/page/" + i;
				work.add(() -> {
					together.await(10, TimeUnit.SECONDS);
					return tool.cite("web", url, "Page", url, null, null, context.withToolCallId("call-" + url));
				});
			}
			Set<String> markers = Collections.synchronizedSet(new HashSet<>());
			for (Future<String> done : pool.invokeAll(work)) {
				markers.add(done.get(10, TimeUnit.SECONDS));
			}
			assertEquals("Every parallel cite call must get its own marker number.", calls, markers.size());
		} finally {
			pool.shutdownNow();
		}
	}

	/** A copy of a context keeps the run's counter; a delegated child starts its own. */
	public void testCounterFollowsTheRunAndNotTheContextCopy() {
		CiteTool tool = new CiteTool();
		ToolContext context = runContext();

		assertEquals("1", tool.cite("web", "a", "A", "", null, null, context.withToolCallId("one")));
		assertEquals("2", tool.cite("web", "b", "B", "", null, null, context.withToolCallId("two")));
		assertEquals("3", tool.cite("web", "c", "C", "", null, null, context.withProgressReporter(
			context.progressReporter())));

		ToolContext child = context.forDelegation(TransactionScope.NOOP);
		assertEquals("A delegated run writes its own answer and numbers from 1.",
			"1", tool.cite("web", "d", "D", "", null, null, child));
	}

	/** The run's counter restarts when the next run begins on the same context. */
	public void testResetRestartsTheNumbering() {
		CitationMarkers markers = new CitationMarkers();
		assertEquals(1, markers.next());
		assertEquals(2, markers.next());
		markers.reset();
		assertEquals(1, markers.next());
	}

	/** An empty id is refused rather than numbered. */
	public void testEmptyIdIsRefused() {
		ToolContext context = runContext();
		assertEquals("Error: id is required.",
			new CiteTool().cite("web", " ", "A", "", null, null, context));
		assertEquals("The refused call must not consume a marker.", 1, context.citationMarkers().next());
	}

	private static ToolContext runContext() {
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);
	}
}
