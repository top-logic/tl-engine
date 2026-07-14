/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test that completed subagent results are injected into the conversation at turn start.
 */
public class TestResultInjection extends TestCase {

	/** */
	public void testInjectsUndeliveredResultsAsSystemMessage() {
		SubagentJobRegistry registry = new SubagentJobRegistry(2);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			SubagentJob job = registry.register("helper", "do x", new CancellationToken());
			job.markTerminal(SubagentJobStatus.DONE,
				new SubagentResult("computed result", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

			Conversation conversation = new Conversation();
			AgentLoop.injectSubagentResultsForTest(conversation);

			boolean found = false;
			for (Message m : conversation.messages()) {
				if (m instanceof SystemMessage sm && sm.content().contains("computed result")) {
					found = true;
				}
			}
			assertTrue("subagent result injected as system message", found);
			assertTrue("delivered flag flipped", job.isDelivered());
		} finally {
			registry.shutdown();
		}
	}
}
