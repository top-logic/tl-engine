/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkill extends TestCase {

	public void testSkillCreation() {
		Skill skill = new Skill("test-skill", "A test skill", Skill.Mode.ON_DEMAND, "You are a test agent.");
		assertEquals("test-skill", skill.id());
		assertEquals("A test skill", skill.description());
		assertEquals(Skill.Mode.ON_DEMAND, skill.mode());
		assertEquals("You are a test agent.", skill.content());
	}

	public void testSkillContentNonEmpty() {
		Skill skill = new Skill("s", "d", Skill.Mode.ON_DEMAND, "content");
		assertFalse(skill.content().isEmpty());
	}

	public void testAutoMode() {
		Skill skill = new Skill("s", "d", Skill.Mode.AUTO, "content");
		assertEquals(Skill.Mode.AUTO, skill.mode());
	}

	public void testModeParse() {
		assertEquals(Skill.Mode.AUTO, Skill.Mode.parse("auto"));
		assertEquals(Skill.Mode.AUTO, Skill.Mode.parse("AUTO"));
		assertEquals(Skill.Mode.ON_DEMAND, Skill.Mode.parse("ondemand"));
		assertEquals(Skill.Mode.ON_DEMAND, Skill.Mode.parse("unknown"));
	}
}
