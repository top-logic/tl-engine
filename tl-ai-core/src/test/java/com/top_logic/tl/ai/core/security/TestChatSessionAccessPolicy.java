/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionType;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.TaskState;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The shipped access policy for the conversations of the agents (Ticket #30144): <b>the owner, plus
 * administrators</b>, in an application with model-based security.
 *
 * <p>
 * Asked the way a view asks: TL-Script evaluated for a user who is no administrator, which hides
 * every object that user may not discover. Nothing is stubbed - the roles come from the role rules
 * of {@code tl-ai-core.chatSecurity.config.xml}, the grants and access parents from its
 * {@code <security-config>}.
 * </p>
 */
public class TestChatSessionAccessPolicy extends BasicTestCase {

	/** The scoped session list of a host page, as documented on the chat element's subject. */
	private static final String SESSIONS_ABOUT =
		"x -> $x.referers(`tl.ai.execution:AgentSession#subject`)";

	/** Every session the acting user can see. */
	private static final String ALL_SESSIONS = "all(`tl.ai.execution:AgentSession`)";

	/** Every memory the acting user can see. */
	private static final String ALL_MEMORIES = "all(`tl.ai.execution:UserMemory`)";

	/** The runs of a session, as the chat list of the demo asks for them. */
	private static final String EXECUTIONS_OF =
		"s -> $s.referers(`tl.ai.execution:AgentExecution#session`)";

	/** The plan of a session. */
	private static final String TASKS_OF = "s -> $s.referers(`tl.ai.execution:AgentTask#session`)";

	/** The questions a run asked. */
	private static final String INTERACTIONS_OF =
		"e -> $e.referers(`tl.ai.execution:InteractionRequest#execution`)";

	/** A script function returning the session it was given, as {@code chatStart} returns one. */
	private static final String RENAME = "s -> aiSessionRename($s, 'renamed')";

	/** The accounts and objects, made once: no test here changes them in a way another one sees. */
	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	private static TLObject subject;

	private static AgentSession ownedSession;

	private static AgentSession foreignSession;

	private static AgentSession ownerlessSession;

	private static AgentExecution ownedExecution;

	private static AgentTask ownedTask;

	private static InteractionRequest ownedInteraction;

	private static UserMemory ownedMemory;

	private static UserMemory foreignMemory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("chat-policy-owner");
		stranger = TestPerson.createPerson("chat-policy-stranger");
		administrator = TestPerson.createPerson("chat-policy-admin");
		// Any object can be what a conversation is about. The owner's own account is one the owner
		// may read, as a host page's object is: referers() looks up nothing from an object the
		// acting user may not read.
		subject = owner;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			administrator.setAdmin(true);
			tx.commit();
		}

		AgentExecutionStore store = AgentExecutionStore.getInstance();
		ownedSession = store.createSession(SessionKind.CHAT, owner, "owned");
		foreignSession = store.createSession(SessionKind.CHAT, stranger, "foreign");
		ownerlessSession = store.createSession(SessionKind.BACKGROUND, null, "background");
		store.setSubject(ownedSession, subject);
		store.setSubject(foreignSession, subject);
		store.setSubject(ownerlessSession, subject);

		ownedExecution = store.beginExecution(ownedSession, "chat-policy-run", "agent", null, null, null);
		store.syncTasks(ownedSession,
			List.of(new TaskState("1", "a step", null, TaskStore.STATUS_PENDING, 1)), null);
		ownedInteraction = store.createInteraction(ownedExecution, InteractionType.QUESTION,
			"chat-policy-question", "A question", "Which one?", null);

		ownedMemory = store.createMemory(owner, MemoryKind.FACT, "owned memory", null);
		foreignMemory = store.createMemory(stranger, MemoryKind.FACT, "foreign memory", null);
		HistoryUtils.updateSessionRevision();
		ownedTask = store.tasksOf(ownedSession).get(0);
	}

	/**
	 * Runs each test with the security checks on, so that the rights are decided for the account
	 * each question names, not bypassed for the system context the test runs in.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

	/**
	 * The owner finds their own session through the subject, and not the session another user
	 * holds about the same object.
	 */
	public void testReferersFindOwnSessionOnly() {
		assertEquals(Set.of(ownedSession), asSet(evalAs(owner, SESSIONS_ABOUT, subject)));
	}

	/** An administrator sees every session, the ownerless one of a background job included. */
	public void testAdministratorFindsEverySession() {
		assertEquals(Set.of(ownedSession, foreignSession, ownerlessSession),
			asSet(evalAs(administrator, SESSIONS_ABOUT, subject)));
	}

	/** A scan over all sessions keeps the acting user's own. */
	public void testAllKeepsOwnSessionsOnly() {
		Set<Object> visible = asSet(evalAs(owner, ALL_SESSIONS));
		assertTrue(visible.contains(ownedSession));
		assertFalse(visible.contains(foreignSession));
		assertFalse("A session without an owner has no role holder.", visible.contains(ownerlessSession));
	}

	/** A script function's result reaches the view when it is the user's own session. */
	public void testScriptFunctionReturnsOwnSession() {
		assertEquals(ownedSession, evalAs(owner, RENAME, ownedSession));
	}

	/** The runs, the plan and the questions of a session are readable with the session. */
	public void testPartsFollowTheirSession() {
		assertEquals(Set.of(ownedExecution), asSet(evalAs(owner, EXECUTIONS_OF, ownedSession)));
		assertEquals(Set.of(ownedTask), asSet(evalAs(owner, TASKS_OF, ownedSession)));
		assertEquals(Set.of(ownedInteraction), asSet(evalAs(owner, INTERACTIONS_OF, ownedExecution)));

		assertEquals(Set.of(), asSet(evalAs(stranger, EXECUTIONS_OF, ownedSession)));
		assertEquals(Set.of(), asSet(evalAs(stranger, TASKS_OF, ownedSession)));
		assertEquals(Set.of(), asSet(evalAs(stranger, INTERACTIONS_OF, ownedExecution)));
	}

	/** A memory is its owner's. */
	public void testAllKeepsOwnMemoriesOnly() {
		Set<Object> visible = asSet(evalAs(owner, ALL_MEMORIES));
		assertTrue(visible.contains(ownedMemory));
		assertFalse(visible.contains(foreignMemory));
	}

	/**
	 * The owner may edit their session, which a rename form needs, and nobody else may read or edit
	 * it; a memory is only read.
	 */
	public void testOwnerWritesOwnSessionOnly() {
		assertTrue(rights(owner).canRead(ownedSession));
		assertTrue(rights(owner).canWrite(ownedSession));
		assertFalse(rights(stranger).canRead(ownedSession));
		assertFalse(rights(stranger).canWrite(ownedSession));

		assertTrue(rights(owner).canRead(ownedMemory));
		assertFalse(rights(owner).canWrite(ownedMemory));
		assertFalse(rights(stranger).canRead(ownedMemory));
	}

	private static ModelSecurity rights(Person account) {
		return new ModelAccessRightsSecurity(account);
	}

	/** Evaluates the given TL-Script for the given account, with the security checks on. */
	private static Object evalAs(Person account, String script, Object... args) {
		return TLContextManager.<Object, RuntimeException, RuntimeException> inPersonContext(account,
			() -> TLScriptExecutor.compile(script).executeWith(Args.some(args)));
	}

	private static Set<Object> asSet(Object value) {
		if (value == null) {
			return Set.of();
		}
		if (value instanceof Collection<?> collection) {
			return new HashSet<>(collection);
		}
		return Set.of(value);
	}

	/**
	 * Creates the test suite with the services the policy is made of: the model, the access manager
	 * that derives the roles, the rights service that grants them, and the store of the
	 * conversations.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSessionAccessPolicy.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
