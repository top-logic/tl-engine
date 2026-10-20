/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * A background subagent started by a run that no chat started - here the eval's route, which a
 * TL-Script run and a scheduled task take as well: {@code AgentRunner} with a recorded conversation.
 *
 * <p>
 * {@code spawn_subagent} used to answer "requires an active chat session" there, because only the
 * chat bound a registry. {@code AgentRunner} now binds the conversation's own.
 * </p>
 */
public class TestSubagentOutsideChat extends BasicTestCase {

	private static final String LEAD = "Eval_Lead";

	private static final String WORKER = "Eval_Scripted";

	private static final String TASK = "Count the open tickets of project Alpha.";

	private static final String FINDING = "Project Alpha has 3 open tickets.";

	/** The lead spawns, waits, and reports what the subagent found - no chat anywhere. */
	public void testARunOutsideAChatSpawnsAndHearsBack() {
		ScriptedProvider.install(TestSubagentOutsideChat::reply);
		Person root = PersonManager.getManager().getRoot();
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root, "TestSubagentOutsideChat");

		String key = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session));
		// Borrowed beside the run, so that the registry can be read after the run let go of it.
		try (SubagentRegistries.Lease lease = SubagentRegistries.borrow(key)) {
			Conversation conversation = run(root, session);

			String answer = lastText(conversation);
			assertTrue("The lead read the subagent's result: " + answer, answer.contains(FINDING));
			List<SubagentJob> jobs = lease.registry().byStatus(SubagentJobStatus.values());
			assertEquals("The conversation's registry ran the subagent.", 1, jobs.size());
			assertEquals(SubagentJobStatus.DONE, jobs.get(0).status());
			assertEquals(WORKER, jobs.get(0).targetAgentName());
		}
	}

	/** A run outside a chat leaves no registry behind once its subagent ended and was delivered. */
	public void testARunOutsideAChatLeavesNoRegistryBehind() {
		ScriptedProvider.install(TestSubagentOutsideChat::reply);
		Person root = PersonManager.getManager().getRoot();
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root, "TestSubagentOutsideChat");

		Conversation conversation = run(root, session);

		assertTrue(lastText(conversation).contains(FINDING));
		assertNull(SubagentRegistries.find(
			SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session))));
	}

	private static Conversation run(Person root, AgentSession session) {
		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(root, RunEntryPoint.SCRIPT, false)),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellation), cancellation);
		Conversation conversation = new Conversation();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent("Find out how many open tickets project Alpha has."))));
			AgentRunner.RecordedRun run = AgentRunner.run(LEAD, conversation, null, context, event -> {
				// Only the outcome matters here.
			}, null, env.logListener().log(), session, null);
			assertEquals(StopReason.COMPLETE, run.result().stopReason());
			return conversation;
		} finally {
			context.transaction().close();
			HistoryUtils.updateSessionRevision();
		}
	}

	/** The lead spawns, then waits, then reports; the subagent answers its task at once. */
	private static List<ContentPart> reply(ProviderRequest request, int turn) {
		if (String.valueOf(request.messages().get(0)).contains(TASK)) {
			return List.of(new TextContent(FINDING));
		}
		switch (turn) {
			case 0:
				return List.of(new ToolCall("call-spawn", "spawn_subagent",
					"{\"agent\": \"" + WORKER + "\", \"task\": \"" + TASK + "\"}"));
			case 1:
				return List.of(new ToolCall("call-wait", "wait_for_subagent", "{}"));
			default:
				String seen = String.valueOf(request.messages());
				return List.of(new TextContent(seen.contains(FINDING)
					? "The subagent says: " + FINDING
					: "The subagent said nothing."));
		}
	}

	private static String lastText(Conversation conversation) {
		List<Message> messages = conversation.messages();
		for (int i = messages.size() - 1; i >= 0; i--) {
			String text = String.valueOf(messages.get(i));
			if (text.contains("The subagent")) {
				return text;
			}
		}
		return String.valueOf(messages);
	}

	/**
	 * The suite, on the services a run passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentOutsideChat.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
