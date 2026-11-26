/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.scripting;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.TLContextManager;

/**
 * That {@code aiSessionRename} and {@code aiSessionDelete} act only for an account that may write
 * the chat, and that deleting one first asks its live run to stop.
 *
 * <p>
 * These two functions are what a row command of a session list calls, so their input is whatever a
 * channel happened to hold when the menu opened - never something the caller proved they may touch.
 * The check of the Write right therefore lives in the function, and this is what says so. The
 * rights come from the shipped policy (the owner reads and writes) and from the test role
 * {@link #READER_ROLE}, which only reads.
 * </p>
 */
public class TestSessionScriptFunctions extends BasicTestCase {

	/** The role of the test configuration that may read a session but not write it. */
	static final String READER_ROLE = "TestChatReader";

	private AgentExecutionStore _store;

	/**
	 * The two accounts, made once for the whole suite.
	 *
	 * <p>
	 * Per-test accounts would have to be deleted again, and the delete is a knowledge-base write on
	 * the test's own thread - which {@link #as(Person, Runnable)} has just handed back to whoever
	 * held the context before, leaving it without a person to write as. Nothing here changes an
	 * account, so one pair serves every test.
	 * </p>
	 */
	private static Person anna;

	private static Person ben;

	private Person _anna;

	private Person _ben;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = AgentExecutionStore.getInstance();
		if (anna == null) {
			anna = TestPerson.createPerson("session-functions-anna");
			ben = TestPerson.createPerson("session-functions-ben");
		}
		_anna = anna;
		_ben = ben;
	}

	/** The owner renames their own chat, and the list's sort key moves with it. */
	public void testOwnerRenames() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Vorher");
		advance();

		as(_anna, () -> AIScriptFunctions.sessionRename(session, "Nachher"));
		advance();

		assertEquals("Nachher", session.getTitle());
		assertNotNull("A rename bumps lastUpdated, which is what the list groups by.",
			session.getLastUpdated());
	}

	/** A blank title is refused: a chat with no name is unfindable in its own list. */
	public void testBlankTitleRefused() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Behalten");
		advance();

		as(_anna, () -> {
			assertRefused(() -> AIScriptFunctions.sessionRename(session, "   "));
			assertRefused(() -> AIScriptFunctions.sessionRename(session, null));
		});
		advance();

		assertEquals("Behalten", session.getTitle());
	}

	/** Someone else's chat is refused, and stays as it was. */
	public void testForeignSessionRefused() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Annas Chat");
		advance();

		as(_ben, () -> {
			assertRefused(I18NConstants.ERROR_SESSION_WRITE_DENIED,
				() -> AIScriptFunctions.sessionRename(session, "Bens Chat"));
			assertRefused(I18NConstants.ERROR_SESSION_WRITE_DENIED,
				() -> AIScriptFunctions.sessionDelete(session));
		});
		advance();

		assertEquals("Annas Chat", session.getTitle());
		assertNotNull("The chat must still be there.", _store.findSessionById(idOf(session)));
	}

	/** An account that may read the chat but not write it is refused as well. */
	public void testReaderRefused() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Nur lesen");
		advance();
		grantReader(session, _ben);

		as(_ben, () -> {
			assertTrue("The reader holds Read on the session.",
				ModelAccessRights.getInstance().isAllowed(_ben, session, SimpleBoundCommandGroup.READ));
			assertRefused(I18NConstants.ERROR_SESSION_WRITE_DENIED,
				() -> AIScriptFunctions.sessionRename(session, "Bens Titel"));
			assertRefused(I18NConstants.ERROR_SESSION_WRITE_DENIED,
				() -> AIScriptFunctions.sessionDelete(session));
		});
		advance();

		assertEquals("Nur lesen", session.getTitle());
		assertNotNull("The chat must still be there.", _store.findSessionById(idOf(session)));
	}

	/** Anything that is not a session is refused rather than silently doing nothing. */
	public void testNonSessionRefused() throws Exception {
		as(_anna, () -> {
			assertRefused(() -> AIScriptFunctions.sessionDelete("9999"));
			assertRefused(() -> AIScriptFunctions.sessionDelete(null));
			assertRefused(() -> AIScriptFunctions.sessionRename(Integer.valueOf(7), "x"));
		});
	}

	/**
	 * An administrator may act on a chat that is not theirs.
	 *
	 * <p>
	 * The bypass of the model-based security, so that a list over all sessions works as an
	 * administration view.
	 * </p>
	 */
	public void testAdministratorActsOnAnotherAccountsChat() throws Exception {
		Person admin = PersonManager.getManager().getRoot();
		assertTrue("The test needs an administrator to act as.", admin.isAdmin());
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Annas Chat");
		advance();

		as(admin, () -> AIScriptFunctions.sessionRename(session, "Von der Verwaltung umbenannt"));
		advance();

		assertEquals("Von der Verwaltung umbenannt", session.getTitle());
	}

	/**
	 * A chat with a run still in it is asked to stop before anything is deleted.
	 *
	 * <p>
	 * The cancel is asserted on its own because the delete that follows takes the execution row
	 * with it: once {@code sessionDelete} has returned there is nothing left to read the marker
	 * from. {@link #testOwnerDeletesTheirOwnChat()} is the other half - that the delete then
	 * happens - and {@code deleteSession} runs the two in that order.
	 * </p>
	 */
	public void testALiveRunIsAskedToStop() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Läuft noch");
		AgentExecution running =
			_store.beginExecution(session, "run-to-cancel", "test-agent", "test-model", null, null);
		AgentExecution finished =
			_store.beginExecution(session, "run-already-done", "test-agent", "test-model", null, null);
		_store.finishExecution(finished, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null,
			"{\"turns\":[]}", "done");
		advance();
		assertFalse("Nothing asked it to stop yet.", _store.isCancelRequested(running));

		assertEquals("Only the run that is still going is signalled.", 1,
			_store.cancelRunningExecutions(session));
		assertTrue("The marker is committed and readable at HEAD.", _store.isCancelRequested(running));
		assertFalse("A run that is over has nothing to cancel.", _store.isCancelRequested(finished));
	}

	/** The owner deletes their own chat and it is gone, runs and all. */
	public void testOwnerDeletesTheirOwnChat() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Wegwerfen");
		_store.beginExecution(session, "run-in-deleted-chat", "test-agent", "test-model", null, null);
		advance();
		String id = idOf(session);
		assertNotNull("The chat is there before the delete.", _store.findSessionById(id));

		as(_anna, () -> assertTrue(AIScriptFunctions.sessionDelete(session)));
		advance();

		assertNull("The chat is gone.", _store.findSessionById(id));
		assertNull("Its run went with it.", _store.findExecutionByRunId("run-in-deleted-chat"));
	}

	/**
	 * Deleting a chat that is already gone is done, not an error.
	 *
	 * <p>
	 * A second window's delete lands on the same object after the first window already deleted it:
	 * the call wanted the chat gone, and it is gone, so the call answers {@code true} rather than
	 * throwing over the deleted object.
	 * </p>
	 */
	public void testDeletingAnAlreadyDeletedChatIsDone() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, _anna, "Weg");
		advance();
		String id = idOf(session);
		assertNotNull("The chat is there before the delete.", _store.findSessionById(id));

		as(_anna, () -> assertTrue(AIScriptFunctions.sessionDelete(session)));
		advance();
		assertNull("The first delete takes the chat.", _store.findSessionById(id));
		assertFalse("The wrapper is invalid after the delete, so the second call hits the early return.",
			session.tValid());

		as(_anna, () -> assertTrue("The second delete is done, not an exception.",
			AIScriptFunctions.sessionDelete(session)));
	}

	/**
	 * Every call runs with {@code person} as the acting account, which is what the guard reads,
	 * with the security checks on, so that the rights are decided for that account.
	 */
	static void as(Person person, Runnable job) {
		ThreadContext.<Void, RuntimeException, RuntimeException> withSecurityChecks(true, () -> {
			TLContextManager.inPersonContext(person, job::run);
			return null;
		});
	}

	/** Gives the account the {@link #READER_ROLE} on the given object. */
	static void grantReader(TLObject object, Person account) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			BoundedRole.assignRole(object, account, BoundedRole.getRoleByName(READER_ROLE));
			tx.commit();
		}
		advance();
	}

	/** That the given call is refused with a {@link TopLogicException}, not carried out. */
	private static void assertRefused(Runnable job) {
		try {
			job.run();
			fail("Expected the call to be refused.");
		} catch (TopLogicException expected) {
			// The refusal, which the view shows in its snackbar.
		}
	}

	/** That the given call is refused with the given message. */
	static void assertRefused(ResKey message, Runnable job) {
		try {
			job.run();
			fail("Expected the call to be refused.");
		} catch (TopLogicException expected) {
			assertEquals(message, expected.getErrorKey());
		}
	}

	/** The session's persistent id, as {@link AgentExecutionStore#findSessionById(String)} takes it. */
	private static String idOf(AgentSession session) {
		return KBUtils.getWrappedObjectName(session).toString();
	}

	/** Advances the caller's session revision so the store's writer-thread commits become visible. */
	static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	/** Creates the test suite: KB + PersonManager (for real owners) around the store. */
	public static Test suite() {
		Test test = new TestSuite(TestSessionScriptFunctions.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			AccessManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
