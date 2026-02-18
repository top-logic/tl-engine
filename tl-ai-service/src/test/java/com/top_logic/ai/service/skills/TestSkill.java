/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkill extends TestCase {

	public void testSkillCreation() {
		Skill skill = new Skill("test-skill", "A test skill", SkillTier.CORE, "You are a test agent.");
		assertEquals("test-skill", skill.name());
		assertEquals("A test skill", skill.description());
		assertEquals(SkillTier.CORE, skill.tier());
		assertEquals("You are a test agent.", skill.content());
	}

	public void testSkillContentNonEmpty() {
		Skill skill = new Skill("s", "d", SkillTier.CORE, "content");
		assertFalse(skill.content().isEmpty());
	}

	public void testSkillWithOnDemandTier() {
		Skill skill = new Skill("test-skill", "A test skill", SkillTier.ON_DEMAND, "You are a test agent.");
		assertEquals(SkillTier.ON_DEMAND, skill.tier());
	}
}
