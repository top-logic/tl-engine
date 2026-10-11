/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * The lead reads that a subagent's report may be incomplete for every stop at a limit (#824).
 */
public class TestSubagentLimitLine extends TestCase {

	/** Every limit a run can stop at puts the line under the report, and no other end does. */
	public void testEveryLimitIsNamed() {
		for (StopReason reason : StopReason.values()) {
			String delivered = SubagentResultDelivery.describe(job(reason));
			assertEquals(reason + ": " + delivered, reason.isLimit(), delivered.contains("_Stopped at its limit"));
		}
	}

	/** The line says which limit it was, outside the subagent's own text. */
	public void testTheLineSaysWhichLimit() {
		assertLine(StopReason.MAX_TURNS, "turn limit");
		assertLine(StopReason.BUDGET_EXHAUSTED, "token or money budget");
		assertLine(StopReason.OUTPUT_LIMIT, "output limit");
	}

	private static void assertLine(StopReason reason, String says) {
		String delivered = SubagentResultDelivery.describe(job(reason));
		int line = delivered.indexOf("_Stopped at its limit");
		assertTrue(delivered, line > delivered.lastIndexOf("</untrusted_subagent_report>"));
		assertTrue(delivered, delivered.substring(line).contains(says));
	}

	private static SubagentJob job(StopReason reason) {
		SubagentJob job = new SubagentJob("sa-1", "worker", "research X", new CancellationToken());
		job.markTerminal(SubagentJobStatus.DONE, new SubagentResult("What I found.", TokenUsage.EMPTY, 1L, 5, reason));
		return job;
	}
}
