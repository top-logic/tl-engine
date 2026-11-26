/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2ASubjectResolver;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.ai.core.security.ReadFilter;
import com.top_logic.ai.core.session.ConversationLog;
import com.top_logic.ai.core.session.Part;
import com.top_logic.ai.core.session.Turn;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * The subject an {@link A2ASubjectResolver} finds for an incoming A2A conversation reaches the
 * answer to the calling application only when the calling account may read it, and it never
 * reaches the session.
 *
 * <p>
 * The subject here is a conversation of another account. Under the access policy that
 * {@code tl-ai-core} ships for conversations, only its owner may read it, so the caller of the
 * second test has no Read on it. No language model is involved: the run is refused before its first
 * request, because no agent service runs. Everything the engine names about the subject is named
 * before that point, in the answer.
 * </p>
 */
public class TestRunSubject extends BasicTestCase {

	/** How long a test waits for the run of a task to end. */
	private static final long TIMEOUT_MILLIS = 30_000;

	/** May read {@link #subject}. */
	private static Person owner;

	/** May not read {@link #subject}. */
	private static Person stranger;

	/** What the resolver finds: a conversation of {@link #owner}. */
	private static AgentSession subject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("a2a-subject-owner");
		stranger = TestPerson.createPerson("a2a-subject-stranger");
		subject = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, "Website relaunch");
		HistoryUtils.updateSessionRevision();
	}

	/**
	 * The control case: an account that may read the subject gets it in the answer. The session
	 * stays without a subject: a role rule over the subject of a session would otherwise show the
	 * transcript of the A2A run to each reader of the object.
	 */
	public void testReadableSubjectIsKept() throws Exception {
		A2ATask task = runTask(owner, "ctx-readable").task();

		assertNotNull("The answer names the subject: " + metadata(task),
			metadata(task).get(A2AJson.SUBJECT_METADATA));
		assertNull("The session of an A2A run is about the subject of the run.",
			sessionOf(owner, "ctx-readable").getSubject());
	}

	/**
	 * A subject that the calling account may not read is named nowhere: not in the answer to the
	 * calling application and not on the session.
	 */
	public void testUnreadableSubjectIsNamedNowhere() throws Exception {
		assertFalse("Precondition: the caller may not read the subject.",
			new ModelAccessRightsSecurity(stranger).canRead(subject));

		Run run = runTask(stranger, "ctx-unreadable");
		A2ATask task = run.task();

		assertNull("The answer to the calling application names a subject its account may not read: "
			+ metadata(task), metadata(task).get(A2AJson.SUBJECT_METADATA));
		AgentSession session = sessionOf(stranger, "ctx-unreadable");
		assertNull("The session is about a subject its owner may not read.", session.getSubject());
		assertFalse("The metadata of the session names the identifier of the subject: " + session.getMetadata(),
			session.getMetadata().contains(ReadFilter.idOf(subject)));
		String told = userText(run.backend().conversationLog("ctx-unreadable"));
		assertTrue("The message the agent was given: " + told, told.contains("Which tickets are still open here?"));
		assertFalse("The message names the identifier of the subject: " + told,
			told.contains(ReadFilter.idOf(subject)));
		assertFalse("The message names the subject: " + told,
			told.contains(MetaLabelProvider.INSTANCE.getLabel(subject)));
	}

	/**
	 * A context that a new backend continues, as after a restart of the application, has its checked
	 * subject again, although its session has none and no resolver finds one.
	 */
	public void testRestoredContextKeepsItsSubject() throws Exception {
		runTask(owner, "ctx-restored", "ctx-restored-first", subject);

		A2ATask next = runTask(owner, "ctx-restored", "ctx-restored-next", null).task();

		assertNotNull("The continued context names its subject: " + metadata(next),
			metadata(next).get(A2AJson.SUBJECT_METADATA));
		assertNull("The session of an A2A run is about the subject of the run.",
			sessionOf(owner, "ctx-restored").getSubject());
	}

	/** A task and the backend that ran it. */
	private record Run(A2ATask task, AgentRunBackend backend) {
		// Plain data.
	}

	/** Runs one task of the exposed agent as the given account, with a resolver that finds {@link #subject}. */
	private static Run runTask(Person caller, String contextId) throws Exception {
		return runTask(caller, contextId, contextId + "-task", subject);
	}

	/**
	 * Runs one task of the exposed agent as the given account, until its run ends.
	 *
	 * <p>
	 * Each call has a backend of its own, as an application after a restart has.
	 * </p>
	 *
	 * @param found
	 *        What the resolver of the service finds, <code>null</code> for nothing.
	 */
	private static Run runTask(Person caller, String contextId, String taskId, TLObject found) throws Exception {
		AgentRunBackend backend = new AgentRunBackend(service(found));
		A2ATask task = new A2ATask(taskId, contextId, "alpha", caller.getName());
		try {
			backend.start(task,
				A2AJson.textMessage(A2AJson.ROLE_USER, contextId, task.id(), "Which tickets are still open here?"),
				Map.of());
			long end = System.currentTimeMillis() + TIMEOUT_MILLIS;
			while (!task.state().isTerminal()) {
				assertTrue("The run did not end in time.", System.currentTimeMillis() < end);
				Thread.sleep(50);
			}
		} finally {
			backend.shutDown();
		}
		HistoryUtils.updateSessionRevision();
		return new Run(task, backend);
	}

	/** The text of the user turns of a conversation log. */
	private static String userText(ConversationLog log) {
		assertNotNull("The backend holds no conversation for the context.", log);
		StringBuilder result = new StringBuilder();
		for (Turn turn : log.turns()) {
			for (Part part : turn.userParts()) {
				if (part instanceof Part.TextPart text) {
					result.append(text.text()).append('\n');
				}
			}
		}
		return result.toString();
	}

	/** The service the backend serves: one exposed agent, and a resolver that finds the given object. */
	private static A2AService service(TLObject found) throws Exception {
		A2AService.Config<?> config = (A2AService.Config<?>) TypedConfiguration.parse(CharacterContents.newContent(
			"<service xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + A2AService.Config.class.getName() + "'"
				+ " class='" + A2AService.class.getName() + "' app='alpha-app'>"
				+ "<agents><agent path='alpha' agent='TL_ModelExplorer'/></agents>"
				+ "</service>"));
		return new A2AService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config) {
			@Override
			public List<A2ASubjectResolver> subjectResolvers() {
				return List.of(request -> found);
			}
		};
	}

	/** The session the backend recorded the given context in. */
	private static AgentSession sessionOf(Person caller, String contextId) {
		for (AgentSession session : AgentExecutionStore.getInstance().sessionsForOwner(caller,
			SessionKind.BACKGROUND)) {
			String metadata = session.getMetadata();
			if (metadata != null && metadata.contains("\"" + contextId + "\"")) {
				return session;
			}
		}
		fail("No session was recorded for context " + contextId + ".");
		return null;
	}

	private static Map<String, Object> metadata(A2ATask task) {
		return Json.obj(task.toJson(null), "metadata");
	}

	/**
	 * The services of the conversations and of their access policy, and the tool access the context
	 * of a run is built with.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunSubject.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}
