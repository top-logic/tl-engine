/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * Tests for {@code aiRunInfo()}, which is how a configured script tool learns which run it is
 * serving - the one thing a Java tool could read and a script tool could not (Gitea #381).
 */
public class TestRunInfoScriptFunction extends TestCase {

	private static final RunInfo RUN =
		new RunInfo("run-1", "conv-1", "albert", "test-model", "anthropic", "exec-1", 2, null);

	/**
	 * Inside a tool call the function answers the run the call belongs to.
	 */
	public void testInsideARun() {
		try (RunInfoBinding.Binding bound = RunInfoBinding.bind(RUN)) {
			Map<String, Object> info = AIScriptFunctions.runInfo();

			assertEquals("run-1", info.get("runId"));
			assertEquals("conv-1", info.get("conversationId"));
			assertEquals("albert", info.get("agentName"));
			assertEquals("test-model", info.get("model"));
			assertEquals("anthropic", info.get("provider"));
			assertEquals("exec-1", info.get("executionId"));
			assertEquals(Integer.valueOf(2), info.get("delegationDepth"));
		}
	}

	/**
	 * Outside a run the function answers every key with nothing rather than failing: the same
	 * script may be called from a command or a search.
	 */
	public void testOutsideARun() {
		Map<String, Object> info = AIScriptFunctions.runInfo();

		assertNull(info.get("runId"));
		assertNull(info.get("conversationId"));
		assertNull(info.get("agentName"));
		assertNull(info.get("model"));
		assertNull(info.get("provider"));
		assertNull(info.get("executionId"));
		assertEquals(Integer.valueOf(0), info.get("delegationDepth"));
	}

	/**
	 * The binding is restored when the call ends, so the next script on this thread reads nothing.
	 */
	public void testBindingDoesNotOutliveTheCall() {
		try (RunInfoBinding.Binding bound = RunInfoBinding.bind(RUN)) {
			assertEquals("run-1", AIScriptFunctions.runInfo().get("runId"));
		}

		assertNull(AIScriptFunctions.runInfo().get("runId"));
	}

}
