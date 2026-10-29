/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * The reading list covers the run tree: a page a subagent read is a source of the lead once the
 * subagent's result reached the lead (#617).
 */
public class TestSubagentSources extends TestCase {

	private static final String REPORT = "https://research.example.org/report-2026";

	private static final String ANSWER = "Der Markt wächst [laut dem Bericht](" + REPORT + ").";

	private SubagentJobRegistry _registry;

	private SubagentRegistryBinding.Binding _binding;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry();
		_binding = SubagentRegistryBinding.bind(_registry);
	}

	@Override
	protected void tearDown() throws Exception {
		_binding.close();
		_registry.shutdown();
		super.tearDown();
	}

	/**
	 * The subagent read the report; its result is delivered at the start of the lead's turn, and the
	 * lead's link to the report is a footnote.
	 */
	public void testAPageASubagentReadIsASourceOfTheLead() {
		SubagentJob job = _registry.register("researcher", "Find the market report.", new CancellationToken());
		job.readingList().add(new ReadSource(CitationKinds.WEB, REPORT, "Marktbericht 2026", REPORT,
			"Der Markt wächst um vier Prozent.", "", null));
		job.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("The market grows by four percent (" + REPORT + ").", TokenUsage.EMPTY, 1L, 1,
				StopReason.COMPLETE));

		Part.TextPart answer = leadAnswers();

		assertEquals("Der Markt wächst laut dem Bericht[1].", answer.text());
		List<Citation> citations = answer.citations();
		assertEquals(1, citations.size());
		assertEquals(REPORT, citations.get(0).url());
		assertEquals("Marktbericht 2026", citations.get(0).label());
	}

	/**
	 * Without the subagent nobody read the report: the same answer keeps its words and loses the
	 * link, because a source no tool read does not count.
	 */
	public void testWithoutTheSubagentTheLinkIsDropped() {
		Part.TextPart answer = leadAnswers();

		assertEquals("Der Markt wächst laut dem Bericht.", answer.text());
		assertTrue(answer.citations().isEmpty());
	}

	/** The lead's run: one turn, which answers with a link to the report. */
	private static Part.TextPart leadAnswers() {
		LLMProvider model = (request, handler) -> {
			handler.onEvent(new StreamEvent.StreamStart());
			handler.onEvent(new StreamEvent.TextStart(0));
			handler.onEvent(new StreamEvent.TextDelta(0, ANSWER));
			handler.onEvent(new StreamEvent.TextEnd(0));
			handler.onEvent(new StreamEvent.MessageComplete("lead", request.model(), "end_turn", TokenUsage.EMPTY,
				List.of(new TextContent(ANSWER))));
		};
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		AgentLoop lead = new AgentLoop(new AgentParams("test-model", "You lead.", 3,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), model, catalog);

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Wie entwickelt sich der Markt?"))));
		ConversationLog log = new ConversationLog();
		log.startTurn().setUserParts(
			List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "Wie entwickelt sich der Markt?")));
		AgentRunner.run(lead, "lead", conversation, ToolContext.unrestricted(), event -> {
			// Only the outcome matters.
		}, log, null, null);

		Part.TextPart result = null;
		for (Part part : log.turns().get(log.turns().size() - 1).assistantParts()) {
			if (part instanceof Part.TextPart text) {
				result = text;
			}
		}
		assertNotNull(result);
		return result;
	}

	/** The agent loop executes inside a TopLogic interaction. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestSubagentSources.class)));
	}
}
