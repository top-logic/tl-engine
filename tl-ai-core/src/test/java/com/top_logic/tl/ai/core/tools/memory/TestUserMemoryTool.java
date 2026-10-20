/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.environment.UserMemories;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryConfig;
import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * That {@code remember} keeps something only where the user agreed to it in this turn, that
 * {@code recall} finds it again, and that neither ever reaches another account's memories.
 *
 * <p>
 * The refusal is the point of the ticket: an agent that decides on its own that something is worth
 * keeping is stopped by the tool, not by a line in its prompt.
 * </p>
 */
public class TestUserMemoryTool extends BasicTestCase {

	private UserMemoryTool _tool;

	private AgentExecutionStore _store;

	/** Two accounts for the whole suite; see {@code TestSessionScriptFunctions} for why. */
	private static Person anna;

	private static Person ben;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new UserMemoryTool();
		_store = AgentExecutionStore.getInstance();
		if (anna == null) {
			anna = TestPerson.createPerson("memory-tool-anna");
			ben = TestPerson.createPerson("memory-tool-ben");
		}
	}

	/** Nobody agreed to anything, so nothing is kept and the agent is told to ask. */
	public void testRefusedWithoutAgreement() throws Exception {
		try (MemoryConsentBinding.Binding consent =
			MemoryConsentBinding.bindIfAbsent(new MemoryConsent("Wie hoch ist die Rechnung?"))) {
			String result = as(anna, () -> _tool.remember("preference", "Mag kurze Antworten",
				ToolContext.EMPTY));

			assertEquals(UserMemoryTool.NO_CONSENT, result);
		}
		advance();
		assertEquals("Nothing was written.", List.of(), _store.memoriesOf(anna, 0));
	}

	/** A run with no consent record at all is a run that may keep nothing. */
	public void testRefusedWithoutAConsentRecord() throws Exception {
		String result =
			as(anna, () -> _tool.remember("fact", "Arbeitet in der Buchhaltung", ToolContext.EMPTY));

		assertEquals(UserMemoryTool.NO_CONSENT, result);
		advance();
		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/** The user asked for it in their own words, so it is kept. */
	public void testAcceptedOnTheUsersOwnRequest() throws Exception {
		try (MemoryConsentBinding.Binding consent = MemoryConsentBinding
			.bindIfAbsent(new MemoryConsent("Merke dir, dass ich kurze Antworten mag."))) {
			String result = as(anna, () -> _tool.remember("preference",
				"Bevorzugt kurze Antworten.", ToolContext.EMPTY));

			assertTrue(result, result.startsWith("Stored as a preference:"));
		}
		advance();

		List<UserMemory> kept = _store.memoriesOf(anna, 0);
		assertEquals(1, kept.size());
		assertEquals("Bevorzugt kurze Antworten.", kept.get(0).getText());
		assertEquals(MemoryKind.PREFERENCE, AgentExecutionStore.memoryKindOf(kept.get(0)));
		assertNotNull("A memory records when it was kept.", kept.get(0).getCreatedAt());
	}

	/** The agent asked and the user agreed, so it is kept. */
	public void testAcceptedAfterTheUserAgreed() throws Exception {
		MemoryConsent consent = new MemoryConsent("Danke, das war hilfreich.");
		try (MemoryConsentBinding.Binding bound = MemoryConsentBinding.bindIfAbsent(consent)) {
			String refused = as(anna, () -> _tool.remember("feedback",
				"Findet ausführliche Erklärungen hilfreich.", ToolContext.EMPTY));
			assertEquals("Not asked yet, so not agreed.", UserMemoryTool.NO_CONSENT, refused);

			consent.recordAnswer("Soll ich mir merken, dass Sie das hilfreich fanden?", "Ja, gerne");
			String stored = as(anna, () -> _tool.remember("feedback",
				"Findet ausführliche Erklärungen hilfreich.", ToolContext.EMPTY));
			assertTrue(stored, stored.startsWith("Stored as a feedback:"));
		}
		advance();
		assertEquals(1, _store.memoriesOf(anna, 0).size());
	}

	/** The user declined, so nothing is kept however the agent phrases the call. */
	public void testNothingIsKeptAfterADecline() throws Exception {
		MemoryConsent consent = new MemoryConsent("Danke.");
		consent.recordAnswer("Soll ich mir merken, dass Sie kurze Antworten bevorzugen?", "Nein");
		try (MemoryConsentBinding.Binding bound = MemoryConsentBinding.bindIfAbsent(consent)) {
			assertEquals(UserMemoryTool.NO_CONSENT,
				as(anna, () -> _tool.remember("preference", "Kurze Antworten", ToolContext.EMPTY)));
		}
		advance();
		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/** An unknown kind is named back to the model rather than guessed at. */
	public void testUnknownKind() throws Exception {
		try (MemoryConsentBinding.Binding bound =
			MemoryConsentBinding.bindIfAbsent(new MemoryConsent("Merke dir das."))) {
			String result =
				as(anna, () -> _tool.remember("opinion", "Irgendetwas", ToolContext.EMPTY));

			assertTrue(result, result.startsWith("error: Unknown kind 'opinion'"));
		}
		advance();
		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/** A memory is a sentence about the user, not a document. */
	public void testTooLongIsRefused() throws Exception {
		String tooLong = "x".repeat(UserMemorySettings.maxLength() + 1);
		try (MemoryConsentBinding.Binding bound =
			MemoryConsentBinding.bindIfAbsent(new MemoryConsent("Merke dir das."))) {
			String result = as(anna, () -> _tool.remember("fact", tooLong, ToolContext.EMPTY));

			assertTrue(result, result.contains("at most " + UserMemorySettings.maxLength()));
		}
		advance();
		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/** An installation that keeps no memories refuses both verbs. */
	public void testSwitchedOff() throws Exception {
		UserMemoryConfig off = TypedConfiguration.newConfigItem(UserMemoryConfig.class);
		off.update(off.descriptor().getProperty(UserMemoryConfig.ENABLED), Boolean.FALSE);

		try (UserMemorySettings.Overridden overridden = UserMemorySettings.override(off);
				MemoryConsentBinding.Binding bound =
					MemoryConsentBinding.bindIfAbsent(new MemoryConsent("Merke dir das."))) {
			assertEquals(UserMemoryTool.DISABLED,
				as(anna, () -> _tool.remember("fact", "Irgendetwas", ToolContext.EMPTY)));
			assertEquals(UserMemoryTool.DISABLED, as(anna, () -> _tool.recall("", ToolContext.EMPTY)));
		}
		advance();
		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/** What was kept is found again by its words, and nothing else is. */
	public void testRecall() throws Exception {
		_store.createMemory(anna, MemoryKind.FACT, "Arbeitet in der Buchhaltung.", null);
		_store.createMemory(anna, MemoryKind.PREFERENCE, "Bevorzugt kurze Antworten.", null);
		advance();

		String hit = as(anna, () -> _tool.recall("Buchhaltung", ToolContext.EMPTY));
		assertTrue(hit, hit.contains("[fact] Arbeitet in der Buchhaltung."));
		assertFalse("Only what matches.", hit.contains("kurze Antworten"));

		String all = as(anna, () -> _tool.recall("", ToolContext.EMPTY));
		assertTrue(all, all.contains("Arbeitet in der Buchhaltung."));
		assertTrue(all, all.contains("Bevorzugt kurze Antworten."));

		String none = as(anna, () -> _tool.recall("Fahrradreparatur", ToolContext.EMPTY));
		assertTrue(none, none.contains("Nothing kept about this user matches"));
	}

	/**
	 * No agent of one user ever reaches another user's memories.
	 *
	 * <p>
	 * The owner filter sits in the store's reader, so this holds for every reader there is - the
	 * recall tool, the environment block, a view.
	 * </p>
	 */
	public void testAnotherAccountSeesNothing() throws Exception {
		_store.createMemory(anna, MemoryKind.FACT, "Annas Geheimnis.", null);
		advance();

		String recalled = as(ben, () -> _tool.recall("Geheimnis", ToolContext.EMPTY));
		assertFalse(recalled, recalled.contains("Annas Geheimnis."));

		assertEquals("Nor through the environment block.", List.of(), as(ben, () ->
			UserMemories.load(TLContext.currentUser())));
		assertEquals(List.of(), _store.memoriesOf(ben, 0));
	}

	/** The newest memories are the ones the environment block is opened with. */
	public void testTheEnvironmentBlockReadsTheNewestFirst() throws Exception {
		_store.createMemory(anna, MemoryKind.FACT, "Das Ältere.", null);
		Thread.sleep(10);
		_store.createMemory(anna, MemoryKind.FACT, "Das Neuere.", null);
		advance();

		List<UserMemories.Entry> loaded = as(anna, () -> UserMemories.load(TLContext.currentUser()));
		assertEquals(2, loaded.size());
		assertEquals("Das Neuere.", loaded.get(0).text());
		assertEquals("Das Ältere.", loaded.get(1).text());
	}

	/**
	 * A memory records the run it came out of, without the caller ever dereferencing that run.
	 *
	 * <p>
	 * The regression this pins down was found end to end and not here: the tool used to read
	 * {@code execution.getSession()} on the agent thread, whose view of the database predates the
	 * execution row the store's writer thread had just created, and every single call came back as
	 * "created after access revision". So the session revision is deliberately <em>not</em> advanced
	 * between the two calls below - that gap is the bug.
	 * </p>
	 */
	public void testTheOriginIsReadOnTheWriterThread() throws Exception {
		AgentSession session = _store.createSession(SessionKind.CHAT, anna, "Ein Chat");
		AgentExecution execution =
			_store.beginExecution(session, "run-of-a-memory", "test-agent", "test-model", null, null);

		UserMemory memory =
			_store.createMemory(anna, MemoryKind.FACT, "Aus einem laufenden Lauf.", execution);
		advance();

		assertEquals("run-of-a-memory", memory.getRunId());
		assertEquals("The memory knows the conversation it came out of.", session, memory.getSession());

		_store.deleteSession(session);
		advance();
	}

	@Override
	protected void tearDown() throws Exception {
		// Every test asserts over the whole of an account's memories, so each starts from nothing.
		for (Person account : List.of(anna, ben)) {
			for (UserMemory memory : _store.memoriesOf(account, 0)) {
				_store.deleteMemory(memory);
			}
		}
		advance();
		super.tearDown();
	}

	/** Runs the given call with {@code person} as the acting account. */
	private static <T> T as(Person person, java.util.function.Supplier<T> job) {
		TLContext context = TLContext.getContext();
		assertNotNull("The test needs a bound context to act in.", context);
		Person before = context.getPerson();
		context.setPerson(person);
		try {
			return job.get();
		} finally {
			context.setPerson(before);
		}
	}

	/** Advances the session revision so the store's writer-thread commits become visible. */
	private static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	/** Creates the test suite: KB + PersonManager (for real owners) around the store. */
	public static Test suite() {
		Test test = new TestSuite(TestUserMemoryTool.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
