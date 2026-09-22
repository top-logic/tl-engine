/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.logging.Level;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse;

/**
 * Tests where {@link ToolResults} puts a caller's framing, which decides what the marking on a
 * model-data result actually claims, and at which level it reports a call that produced no
 * response.
 */
public class TestToolResults extends TestCase {

	/** The success payload is what the caller framed, and it arrives framed. */
	public void testSuccessResponseIsFramed() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> ReadFilter.marked("pm:Project", payload),
			() -> listing());

		assertTrue("the framing is applied: " + json,
			json.contains("<untrusted_model_data source=\"pm:Project\">"));
		assertTrue("and the payload survives it", json.contains("\"module\":\"pm\""));
	}

	/**
	 * The <b>error</b> response is not framed. It is this application's own sentence about a call that
	 * failed - marking it as business data somebody else wrote would be untrue, and would spend the
	 * envelope on a result that discloses nothing.
	 */
	public void testErrorResponseIsNotFramed() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> ReadFilter.marked("pm:Project", payload),
			() -> {
				throw new IllegalArgumentException("Type 'Nope' not found in module 'pm'");
			});

		assertFalse("no envelope belongs around a refusal: " + json, json.contains("untrusted_"));
		assertTrue("and the reason is still reported", json.contains("Type 'Nope' not found"));
	}

	/** A framing that throws is a failed call, not an excuse to hand the model an unframed payload. */
	public void testAFailingFramingCannotLeakAnUnframedPayload() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> {
				throw new IllegalStateException("framing broke");
			},
			() -> listing());

		assertFalse("the unframed payload must not reach the model: " + json,
			json.contains("\"module\""));
		assertTrue(json, json.contains("framing broke"));
	}

	/** The plain form frames nothing, which is what a tool disclosing no model data wants. */
	public void testUnframedInvokeIsUnchanged() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ", () -> listing());

		assertFalse(json, json.contains("untrusted_"));
		assertTrue(json, json.contains("\"module\":\"pm\""));
	}

	// ===== What the operational log is told (Gitea #387) =====

	/**
	 * A refusal is logged at info and without a stack trace.
	 *
	 * <p>
	 * The model named a type this application does not have. Nothing is wrong with the installation,
	 * so nothing in this belongs in the stream an operator watches for faults - and the trace would
	 * only point back at the line that composed the sentence the model is about to read.
	 * </p>
	 */
	public void testARefusalIsLoggedAtInfoWithoutATrace() {
		List<Logger.LogEntry> entries = recorded(() -> ToolResults.invoke(TestToolResults.class,
			"listInstances()", Map.of(), "Error accessing instances: ",
			() -> {
				throw new IllegalArgumentException("Type 'Nope' not found in module 'pm'");
			}));

		List<Logger.LogEntry> errors = atLevel(entries, Level.ERROR);
		assertTrue("A wrong type name is not a fault of this application: " + errors,
			errors.isEmpty());

		List<Logger.LogEntry> infos = atLevel(entries, Level.INFO);
		assertEquals("The refusal must still be visible: " + entries, 1, infos.size());
		Logger.LogEntry refusal = infos.get(0);
		assertTrue(refusal.getMessage(), refusal.getMessage().contains("Type 'Nope' not found"));
		assertFalse("A refusal carries no stack trace.", refusal.getException().hasValue());
	}

	/** A fault of this application keeps its ERROR line and its stack trace. */
	public void testACrashIsStillLoggedAsAnErrorWithItsTrace() {
		IllegalStateException crash = new IllegalStateException("No TaskStore bound to the thread.");
		List<Logger.LogEntry> entries = recorded(() -> ToolResults.invoke(TestToolResults.class,
			"listInstances()", Map.of(), "Error accessing instances: ",
			() -> {
				throw crash;
			}));

		List<Logger.LogEntry> errors = atLevel(entries, Level.ERROR);
		assertEquals("An operator has to see this one: " + entries, 1, errors.size());
		assertSame("and has to see where it came from.", crash,
			errors.get(0).getException().get());
	}

	/** A call that produced a response says nothing at all. */
	public void testASuccessfulCallIsNotLogged() {
		List<Logger.LogEntry> entries = recorded(() -> ToolResults.invoke(TestToolResults.class,
			"listInstances()", Map.of(), "Error accessing instances: ", () -> listing()));

		assertTrue("Nothing failed, so there is nothing to report: " + entries,
			atLevel(entries, Level.ERROR).isEmpty() && atLevel(entries, Level.INFO).isEmpty());
	}

	private static List<Logger.LogEntry> recorded(Runnable work) {
		List<Logger.LogEntry> entries = new ArrayList<>();
		Listener<Logger.LogEntry> listener = entries::add;
		Logger.getListenerRegistry().register(listener);
		try {
			work.run();
		} finally {
			Logger.getListenerRegistry().unregister(listener);
		}
		return entries;
	}

	private static List<Logger.LogEntry> atLevel(List<Logger.LogEntry> entries, Level level) {
		List<Logger.LogEntry> result = new ArrayList<>();
		for (Logger.LogEntry entry : entries) {
			if (entry.getPriority() == level) {
				result.add(entry);
			}
		}
		return result;
	}

	private static InstanceListResponse listing() {
		return InstanceListResponse.create().setModule("pm").setType("Project");
	}
}
