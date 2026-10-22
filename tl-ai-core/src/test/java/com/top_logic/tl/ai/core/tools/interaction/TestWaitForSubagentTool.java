/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Test for {@link WaitForSubagentTool}.
 */
public class TestWaitForSubagentTool extends TestCase {

	/** */
	public void testReturnsCompletedEnvelopes() {
		SubagentJobRegistry registry = new SubagentJobRegistry(2);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			SubagentJob job = registry.register("helper", "do x", new CancellationToken());
			job.markTerminal(SubagentJobStatus.DONE,
				new SubagentResult("the answer is 42", TokenUsage.EMPTY, 10L, 2, StopReason.COMPLETE));

			ToolResult result = new WaitForSubagentTool().execute(
				Map.of("subagentIds", List.of(job.subagentId())), ToolContext.EMPTY);
			assertTrue(result.text().contains("the answer is 42"));
		} finally {
			registry.shutdown();
		}
	}

	/** */
	public void testUnknownIdYieldsClearError() {
		SubagentJobRegistry registry = new SubagentJobRegistry(2);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			ToolResult result = new WaitForSubagentTool().execute(
				Map.of("subagentIds", List.of("sa-999"), "timeoutMs", 100),
				ToolContext.EMPTY);
			assertTrue(result.text().toLowerCase().contains("no such")
				|| result.text().toLowerCase().contains("unknown"));
		} finally {
			registry.shutdown();
		}
	}
}
