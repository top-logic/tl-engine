/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.skills;

import java.util.List;

import junit.framework.TestCase;

/** */
public class TestSkill extends TestCase {

	/** */
	public void testSkillCreation() {
		Skill skill = new Skill("test-skill", "A test skill", "You are a test agent.", List.of());
		assertEquals("test-skill", skill.id());
		assertEquals("A test skill", skill.description());
		assertEquals("You are a test agent.", skill.content());
	}

	/**	 */
	public void testSkillContentNonEmpty() {
		Skill skill = new Skill("s", "d", "content", List.of());
		assertFalse(skill.content().isEmpty());
	}
}
