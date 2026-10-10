/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Tests {@link SpawnSubagentTool}: async spawn path and no-registry fallback.
 */
public class TestSpawnSubagentTool extends TestCase {

	/**
	 * When a {@link SubagentJobRegistry} is bound, execute() returns a handle immediately
	 * (non-blocking) and registers exactly one job.
	 */
	public void testAsyncReturnsHandleWithoutBlocking() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(2);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			// Fake factory: buildSubagentLoop throws immediately so the spawned job finishes (FAILED)
			// quickly. The point is non-blocking return + registration, not subagent success.
			DelegationTool.AgentLoopFactory fakeFactory = new DelegationTool.AgentLoopFactory() {
				@Override
				public AgentLoop buildAgentLoop(String agentName, SecurityContext security) {
					throw new UnsupportedOperationException("Should not be called in async path");
				}

				@Override
				public AgentLoop buildSubagentLoop(String agentName, SecurityContext security) {
					throw new RuntimeException("Fake failure: no real LLM available in test");
				}
			};

			SpawnSubagentTool tool = new SpawnSubagentTool(fakeFactory);

			long t0 = System.currentTimeMillis();
			ToolResult result = tool.execute(Map.of("agent", "helper", "task", "do x"), ToolContext.EMPTY);
			long elapsed = System.currentTimeMillis() - t0;

			assertTrue("returned a handle text containing 'started'", result.text().contains("started"));
			assertTrue("did not block on the child run (elapsed=" + elapsed + "ms)", elapsed < 1000);

			// Wait briefly for the background thread to register (it may still be QUEUED or RUNNING).
			// Give it up to 500ms to reach a terminal state.
			long deadline = System.currentTimeMillis() + 500;
			while (registry.byStatus(SubagentJobStatus.values()).size() < 1
				&& System.currentTimeMillis() < deadline) {
				Thread.sleep(10);
			}

			int total = registry.byStatus(SubagentJobStatus.values()).size();
			assertEquals("one job registered", 1, total);
		} finally {
			registry.shutdown();
		}
	}

	/**
	 * When NO registry is bound - a run recorded under no conversation - execute() refuses.
	 */
	public void testNoRegistryReturnsError() {
		// No SubagentRegistryBinding.bind(...) — registry is null on this thread.
		DelegationTool.AgentLoopFactory factory = (agentName, security) -> {
			throw new UnsupportedOperationException("Should not be reached");
		};

		SpawnSubagentTool tool = new SpawnSubagentTool(factory);
		ToolResult result = tool.execute(Map.of("agent", "helper", "task", "do something"), ToolContext.EMPTY);

		assertTrue(result.text(), result.text().startsWith("ERROR: spawn_subagent needs a run recorded under a"
			+ " conversation"));
	}
}
