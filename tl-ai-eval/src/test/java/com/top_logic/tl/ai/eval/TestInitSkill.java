/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.tl.ai.core.agent.ApplicationContextContributor;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Albert's {@code TL_init} skill, run end to end: the shipped {@code TL_SingleAgent} loads the skill,
 * reads the existing context, reads a derived attribute's calculation rule and saves the application
 * context with the user's approval - and the next run of Albert is given it (Gitea #667).
 *
 * <p>
 * In tl-ai-eval because this module boots the agent stack the way a chat request meets it - the
 * shipped agent definitions, the tool registry, the tool-access gate, a knowledge base - with a model
 * whose answers a test writes. What the model answers is scripted; everything the answers set in
 * motion is the real thing. Its {@code LLMService} configuration replaces the shipped one as a whole
 * and lists no prompt contributor, as every application that has a configuration of its own does,
 * so the context reaching the next run is also the proof that it needs none.
 * </p>
 *
 * <p>
 * Whether a real model follows the skill's workflow is not something a scripted model can say; that
 * is the live check of the pull request.
 * </p>
 */
public class TestInitSkill extends BasicTestCase {

	private static final String ALBERT = "TL_SingleAgent";

	private static final String DERIVED = "initfixture:Item#total";

	/** A fragment of the calculation rule in {@code initfixture.model.xml}. */
	private static final String RULE = "$item.get(`initfixture:Item#price`) * $item.get(`initfixture:Item#quantity`)";

	private static final String DOCUMENT = "# Application context: Init fixture\n\n"
		+ "## Purpose\nPrices items.\n\n"
		+ "## Calculated values\n- `initfixture:Item#total`: what all units of an item cost together.";

	/** The tools that run in the order the script calls them. */
	private static final List<String> CALLS = List.of("loadSkill", "readApplicationContext", "loadSkill",
		"getCurrentAnnotations", "saveApplicationContext");

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ApplicationContextContributor.current().setText(null);
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * The whole workflow: every step runs, the rule is read, the save is the one call that needs an
	 * approval and the one change the run makes, and the next run is given the saved context.
	 */
	public void testInitWritesTheContextAndChangesNothingElse() {
		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(new TextContent("I will write the application context."),
				call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("readApplicationContext", "{}"));
			case 2 -> List.of(call("loadSkill", "{\"skillId\": \"TL_modelExploration\"}"));
			case 3 -> List.of(call("getCurrentAnnotations",
				"{\"targetRef\": \"" + DERIVED + "\", \"includeInherited\": true}"));
			case 4 -> List.of(call("saveApplicationContext", "{\"text\": " + json(DOCUMENT) + "}"));
			default -> List.of(new TextContent("Saved."));
		});
		List<ApprovalRequest> approvals = new ArrayList<>();
		long before = lastRevision();

		Run run = run("Write the application context.", request -> {
			approvals.add(request);
			return new ApprovalResult.Approved();
		});

		assertEquals(StopReason.COMPLETE, run.stopReason());
		List<ToolResultMessage> results = run.results();
		assertEquals(CALLS, results.stream().map(ToolResultMessage::toolName).toList());
		for (ToolResultMessage result : results) {
			assertTrue(result.toolName() + " failed: " + result.state(),
				result.state() instanceof ToolState.Completed);
		}
		assertTrue("Before anything is saved, the read says so: " + text(results.get(1)),
			text(results.get(1)).contains("No application context has been written yet."));
		assertTrue("The derived attribute's rule reaches the model: " + text(results.get(3)),
			text(results.get(3)).contains(RULE));

		assertEquals("Exactly one call needs a person's approval: the save.",
			List.of("saveApplicationContext"), approvals.stream().map(ApprovalRequest::toolName).toList());
		assertEquals("The person approves the text that is saved.",
			DOCUMENT, approvals.get(0).arguments().get("text"));
		assertEquals(DOCUMENT, ApplicationContextContributor.current().getText());
		assertEquals("The run changed the application context and nothing else of the application.",
			List.of(), applicationChangesSince(before));

		List<String> prompts = new ArrayList<>();
		ScriptedProvider.install((request, turn) -> {
			prompts.add(request.systemPrompt());
			return List.of(new TextContent("Hello."));
		});
		run("Hello.", ApprovalCallback.AUTO_APPROVE);
		assertEquals(1, prompts.size());
		assertTrue("The next run of Albert is given the context.", prompts.get(0).contains(DOCUMENT));
	}

	/**
	 * A person who declines the save keeps the application as it was.
	 */
	public void testDeclinedSaveWritesNothing() {
		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("saveApplicationContext", "{\"text\": " + json(DOCUMENT) + "}"));
			default -> List.of(new TextContent("What shall I change?"));
		});

		run("Write the application context.", request -> new ApprovalResult.Rejected("Not yet."));

		assertNull(ApplicationContextContributor.current().getText());
	}

	private record Run(StopReason stopReason, Conversation conversation) {

		List<ToolResultMessage> results() {
			List<ToolResultMessage> result = new ArrayList<>();
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage toolResult) {
					result.add(toolResult);
				}
			}
			return result;
		}
	}

	private static Run run(String prompt, ApprovalCallback approval) {
		Person root = PersonManager.getManager().getRoot();
		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(root, RunEntryPoint.CHAT, false)),
			approval, new TransactionScope(cancellation), cancellation);
		Conversation conversation = new Conversation();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent(prompt))));
			Turn turn = env.logListener().log().startTurn();
			turn.setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), prompt)));
			AgentRunner.RecordedRun run = AgentRunner.run(ALBERT, conversation, null, context, event -> {
				// Only the outcome matters here.
			}, null, env.logListener().log(),
				AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root, "TestInitSkill"), null);
			return new Run(run.result().stopReason(), conversation);
		} finally {
			context.transaction().close();
			HistoryUtils.updateSessionRevision();
		}
	}

	/**
	 * Everything committed since the given revision that is neither the application context nor the
	 * record of the run itself - which the execution store keeps in the tables of {@code tl.ai.execution},
	 * on its own thread. A reference row counts as part of the object it belongs to.
	 */
	private static List<String> applicationChangesSince(long before) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Set<String> runTables = runRecordTables();
		List<ItemEvent> events = new ArrayList<>();
		ChangeSetReader reader = kb.getChangeSetReader(ReaderConfigBuilder.createConfig(
			HistoryUtils.getRevision(before + 1), Revision.CURRENT));
		try {
			for (ChangeSet changes = reader.read(); changes != null; changes = reader.read()) {
				events.addAll(changes.getCreations());
				events.addAll(changes.getUpdates());
				events.addAll(changes.getDeletions());
			}
		} finally {
			reader.close();
		}

		Set<TLID> allowed = new HashSet<>();
		allowed.add(ApplicationContextContributor.current().tId().getObjectName());
		for (ItemEvent event : events) {
			if (runTables.contains(event.getObjectType().getName())) {
				allowed.add(event.getObjectName());
			}
		}
		List<String> result = new ArrayList<>();
		for (ItemEvent event : events) {
			TLID owner = event.getObjectName();
			if (event instanceof ItemChange change && change.getValues().get("source") instanceof ObjectKey source) {
				owner = source.getObjectName();
			}
			if (!allowed.contains(owner)) {
				result.add(event.getObjectType().getName() + ":" + event.getObjectName());
			}
		}
		return result;
	}

	private static Set<String> runRecordTables() {
		Set<String> result = new HashSet<>();
		for (TLClass type : ModelService.getApplicationModel().getModule("tl.ai.execution").getClasses()) {
			String table = TLAnnotations.getTable(type);
			if (!TLAnnotations.GENERIC_TABLE_NAME.equals(table)) {
				result.add(table);
			}
		}
		return result;
	}

	private static long lastRevision() {
		HistoryUtils.updateSessionRevision();
		return HistoryUtils.getLastRevision().getCommitNumber();
	}

	private static String text(ToolResultMessage result) {
		return result.state() instanceof ToolState.Completed completed ? completed.result() : "";
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	private static String json(String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	/**
	 * The suite, on the services a chat request passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInitSkill.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMService.Module.INSTANCE));
	}
}
