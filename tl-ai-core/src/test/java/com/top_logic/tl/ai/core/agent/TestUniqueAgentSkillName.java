/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.Skill;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.util.model.ModelService;

/**
 * Tests the persistence-level uniqueness of {@link Agent} and {@link Skill} names enforced by the
 * unique index declared in {@code tl-ai-coreMeta.xml} (issue #172).
 *
 * <p>
 * These are the authoritative backstop against duplicate names created on paths that bypass the
 * form-level {@code UniqueNameConstraint} (import, migrations, TL-Script, concurrent edits).
 * </p>
 */
public class TestUniqueAgentSkillName extends BasicTestCase {

	/**
	 * A second {@link Agent} committed with an already-used name must be rejected.
	 */
	public void testDuplicateAgentNameRejected() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction tx = kb.beginTransaction()) {
			createAgent(factory, "DuplicateAgent");
			tx.commit();
		}

		assertCommitRejected("A second Agent named 'DuplicateAgent' must be rejected by the unique name index.",
			kb, () -> createAgent(factory, "DuplicateAgent"));

		assertEquals("Exactly one Agent 'DuplicateAgent' must remain persistent.",
			1, countInstances(TlAiCoreFactory.getAgentType(), Agent.NAME_ATTR, "DuplicateAgent"));
	}

	/**
	 * A second {@link Skill} committed with an already-used name must be rejected.
	 */
	public void testDuplicateSkillNameRejected() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction tx = kb.beginTransaction()) {
			createSkill(factory, "DuplicateSkill");
			tx.commit();
		}

		assertCommitRejected("A second Skill named 'DuplicateSkill' must be rejected by the unique name index.",
			kb, () -> createSkill(factory, "DuplicateSkill"));

		assertEquals("Exactly one Skill 'DuplicateSkill' must remain persistent.",
			1, countInstances(TlAiCoreFactory.getSkillType(), Skill.NAME_ATTR, "DuplicateSkill"));
	}

	/**
	 * {@link Agent} and {@link Skill} names live in separate namespaces: the same name may be used
	 * for one of each.
	 */
	public void testAgentAndSkillMayShareName() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction tx = kb.beginTransaction()) {
			createAgent(factory, "SharedName");
			createSkill(factory, "SharedName");
			tx.commit();
		}

		assertEquals("Agent 'SharedName' must be persistent.",
			1, countInstances(TlAiCoreFactory.getAgentType(), Agent.NAME_ATTR, "SharedName"));
		assertEquals("Skill 'SharedName' must be persistent.",
			1, countInstances(TlAiCoreFactory.getSkillType(), Skill.NAME_ATTR, "SharedName"));
	}

	private static void assertCommitRejected(String message, KnowledgeBase kb, Runnable createDuplicate) {
		boolean rejected = false;
		try (Transaction tx = kb.beginTransaction()) {
			createDuplicate.run();
			tx.commit();
		} catch (Exception expected) {
			// A unique-index violation surfaces as a (wrapped) exception on commit.
			rejected = true;
		}
		assertTrue(message, rejected);
	}

	private static Agent createAgent(TlAiCoreFactory factory, String name) {
		Agent agent = factory.createAgent();
		agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
		return agent;
	}

	private static Skill createSkill(TlAiCoreFactory factory, String name) {
		Skill skill = factory.createSkill();
		skill.tUpdate(TlAiCoreFactory.getNameSkillAttr(), name);
		return skill;
	}

	private static int countInstances(TLClass type, String nameAttr, String name) {
		int count = 0;
		List<TLObject> instances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
		for (TLObject instance : instances) {
			if (instance.tValid() && name.equals(instance.tValueByName(nameAttr))) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Creates the test suite with the required service dependencies (KB + {@link ModelService}).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestUniqueAgentSkillName.class),
				ModelService.Module.INSTANCE));
	}
}
