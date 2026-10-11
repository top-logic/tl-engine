/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Who a research started by {@code aiResearch} acts as, and how thoroughly it researches (#767).
 *
 * <p>
 * The run itself is covered end to end by {@code TestHeadlessResearch} in tl-ai-eval, on a scripted
 * model; this pins the two rules a caller meets before anything runs.
 * </p>
 */
public class TestHeadlessResearchArguments extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("research-anna");
			ben = TestPerson.createPerson("research-ben");
		}
	}

	/** A user researches for themselves; naming oneself is the same. */
	public void testAUserResearchesForThemselves() {
		assertSame(anna, HeadlessResearch.ownerOf(anna, null));
		assertSame(anna, HeadlessResearch.ownerOf(anna, ""));
		assertSame(anna, HeadlessResearch.ownerOf(anna, anna));
		assertEquals(anna, HeadlessResearch.ownerOf(anna, anna.getName()));
	}

	/** A user cannot start a research that spends another account's budget with its rights. */
	public void testAUserCannotNameAnotherAccount() {
		assertRefused(anna, ben);
		assertRefused(anna, ben.getName());
	}

	/** Without a calling account - a scheduled task - the owner is named, by account or login name. */
	public void testASystemContextNamesTheOwner() {
		assertSame(ben, HeadlessResearch.ownerOf(null, ben));
		assertEquals(ben, HeadlessResearch.ownerOf(null, ben.getName()));
		assertRefused(null, null);
		assertRefused(null, "no-such-account");
		assertRefused(null, Integer.valueOf(42));
	}

	/** The skill's three levels, in any case; standard when none is named; nothing else. */
	public void testTheEffortIsOneOfTheSkillsLevels() {
		assertEquals("standard", HeadlessResearch.effortOf(null));
		assertEquals("standard", HeadlessResearch.effortOf(" "));
		assertEquals("deep", HeadlessResearch.effortOf("Deep"));
		assertEquals("quick", HeadlessResearch.effortOf(" quick "));
		try {
			HeadlessResearch.effortOf("thorough");
			fail("Not a level of the skill.");
		} catch (TopLogicException expected) {
			// Expected.
		}
	}

	/** The lead is told the skill, the effort, that nobody answers, and the question as given. */
	public void testTheLeadIsToldWhatTheSkillNeeds() {
		String prompt = HeadlessResearch.prompt("What changed?", "deep");
		assertTrue(prompt, prompt.contains("TL_deepResearch"));
		assertTrue(prompt, prompt.contains("Effort: deep."));
		assertTrue(prompt, prompt.contains("nobody can answer"));
		assertTrue(prompt, prompt.endsWith("Question:\nWhat changed?"));
	}

	private static void assertRefused(Person current, Object requested) {
		try {
			HeadlessResearch.ownerOf(current, requested);
			fail("Must be refused: " + current + " naming " + requested);
		} catch (TopLogicException expected) {
			// Expected.
		}
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestHeadlessResearchArguments.class),
				ModelService.Module.INSTANCE));
	}
}
