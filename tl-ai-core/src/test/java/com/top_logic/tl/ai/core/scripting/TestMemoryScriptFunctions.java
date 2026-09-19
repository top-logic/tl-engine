/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * That {@code aiMemoryDelete} acts only for the account a memory belongs to.
 *
 * <p>
 * This is what the Forget command of a memory list calls, so its input is whatever a channel
 * happened to hold when the menu opened - never something the caller proved they may touch. The
 * ownership check therefore lives in the function, and this is what says so.
 * </p>
 */
public class TestMemoryScriptFunctions extends BasicTestCase {

	private AgentExecutionStore _store;

	/** Two accounts for the whole suite; see {@code TestSessionScriptFunctions} for why. */
	private static Person anna;

	private static Person ben;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = AgentExecutionStore.getInstance();
		if (anna == null) {
			anna = TestPerson.createPerson("memory-functions-anna");
			ben = TestPerson.createPerson("memory-functions-ben");
		}
	}

	/** The owner forgets their own memory and it is gone. */
	public void testOwnerDeletesTheirOwn() throws Exception {
		UserMemory memory = _store.createMemory(anna, MemoryKind.FACT, "Wegwerfen.", null);
		advance();
		assertEquals(1, _store.memoriesOf(anna, 0).size());

		as(anna, () -> assertTrue(AIScriptFunctions.memoryDelete(memory)));
		advance();

		assertEquals("The memory is gone.", List.of(), _store.memoriesOf(anna, 0));
	}

	/** Someone else's memory is refused, and stays as it was. */
	public void testForeignMemoryRefused() throws Exception {
		UserMemory memory = _store.createMemory(anna, MemoryKind.FACT, "Annas Erinnerung.", null);
		advance();

		as(ben, () -> assertRefused(() -> AIScriptFunctions.memoryDelete(memory)));
		advance();

		assertEquals("It must still be there.", 1, _store.memoriesOf(anna, 0).size());
	}

	/** Anything that is not a memory is refused rather than silently doing nothing. */
	public void testNonMemoryRefused() throws Exception {
		as(anna, () -> {
			assertRefused(() -> AIScriptFunctions.memoryDelete(null));
			assertRefused(() -> AIScriptFunctions.memoryDelete("9999"));
			assertRefused(() -> AIScriptFunctions.memoryDelete(Integer.valueOf(7)));
		});
	}

	/** A single-element selection is the one row the menu was opened on. */
	public void testSingleElementSelection() throws Exception {
		UserMemory memory = _store.createMemory(anna, MemoryKind.FACT, "Aus einer Liste.", null);
		advance();

		as(anna, () -> assertTrue(AIScriptFunctions.memoryDelete(List.of(memory))));
		advance();

		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	/**
	 * An administrator may delete a memory that is not theirs.
	 *
	 * <p>
	 * The same bypass {@code aiSessionDelete} has, so that a list over all memories works as an
	 * administration view.
	 * </p>
	 */
	public void testAdministratorDeletesAnothersMemory() throws Exception {
		Person admin = PersonManager.getManager().getRoot();
		assertTrue("The test needs an administrator to act as.", admin.isAdmin());
		UserMemory memory = _store.createMemory(anna, MemoryKind.FACT, "Von der Verwaltung.", null);
		advance();

		as(admin, () -> assertTrue(AIScriptFunctions.memoryDelete(memory)));
		advance();

		assertEquals(List.of(), _store.memoriesOf(anna, 0));
	}

	@Override
	protected void tearDown() throws Exception {
		for (Person account : List.of(anna, ben)) {
			for (UserMemory memory : _store.memoriesOf(account, 0)) {
				_store.deleteMemory(memory);
			}
		}
		advance();
		super.tearDown();
	}

	/** Every call runs with {@code person} as the acting account, which is what the guard reads. */
	private static void as(Person person, Runnable job) {
		TLContext context = TLContext.getContext();
		assertNotNull("The test needs a bound context to act in.", context);
		Person before = context.getPerson();
		context.setPerson(person);
		try {
			job.run();
		} finally {
			context.setPerson(before);
		}
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

	/** Advances the caller's session revision so the store's writer-thread commits become visible. */
	private static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	/** Creates the test suite: KB + PersonManager (for real owners) around the store. */
	public static Test suite() {
		Test test = new TestSuite(TestMemoryScriptFunctions.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
