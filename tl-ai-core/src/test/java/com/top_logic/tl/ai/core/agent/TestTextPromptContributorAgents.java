/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * The agents an administrator picks for a {@link TextPromptContributor}: every agent of the
 * application is offered - the configured ones and the ones built in the running application - and
 * a name no agent has is refused on save (Gitea #648).
 */
public class TestTextPromptContributorAgents extends BasicTestCase {

	/** An agent of the configuration: the shipped Albert. */
	private static final String CONFIGURED = "TL_SingleAgent";

	/** An agent an administrator built in the running application. */
	private static final String BUILT_AT_RUNTIME = "Test648RuntimeAgent";

	private Agent _runtimeAgent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			_runtimeAgent = TlAiCoreFactory.getInstance().createAgent();
			_runtimeAgent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), BUILT_AT_RUNTIME);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			_runtimeAgent.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * The choice offers the configured agents and the agents of the knowledge base.
	 */
	public void testBothKindsOfAgentAreOffered() {
		List<String> options = new TextPromptContributor.AgentNames().apply();

		assertTrue("A configured agent is offered: " + options, options.contains(CONFIGURED));
		assertTrue("An agent built at runtime is offered: " + options, options.contains(BUILT_AT_RUNTIME));
		assertEquals("Sorted, so that an administrator finds an agent.",
			options.stream().sorted().toList(), options);
	}

	/**
	 * Agents picked from the choice, of either kind, pass the check on save.
	 */
	public void testExistingAgentsPass() throws ConfigurationException {
		assertEquals(List.of(), failures(CONFIGURED + "," + BUILT_AT_RUNTIME));
	}

	/**
	 * No agents at all - every agent gets the text - passes the check.
	 */
	public void testNoAgentsPasses() throws ConfigurationException {
		assertEquals(List.of(), failures(""));
	}

	/**
	 * A name no agent has is refused, and the message names it - a typo, or an agent deleted since
	 * the text was configured.
	 */
	public void testUnknownAgentIsRefused() throws ConfigurationException {
		List<String> failures = failures(CONFIGURED + ",TL_SingelAgent");

		assertEquals(1, failures.size());
		assertTrue(failures.get(0),
			failures.get(0).contains("Unknown agent(s): TL_SingelAgent. Pick the agents from the list."));
	}

	/**
	 * The agent that was just deleted is no longer offered, and naming it is refused.
	 */
	public void testDeletedAgentIsRefused() throws ConfigurationException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			_runtimeAgent.tDelete();
			tx.commit();
		}
		try {
			assertFalse(new TextPromptContributor.AgentNames().apply().contains(BUILT_AT_RUNTIME));
			assertEquals(1, failures(BUILT_AT_RUNTIME).size());
		} finally {
			try (Transaction tx = kb.beginTransaction()) {
				_runtimeAgent = TlAiCoreFactory.getInstance().createAgent();
				_runtimeAgent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), BUILT_AT_RUNTIME);
				tx.commit();
			}
		}
	}

	private static List<String> failures(String agents) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(read("""
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
				agents="%s"
			>
				<text>
					<en>House rule.</en>
				</text>
			</contributor>
			""".formatted(agents)));
		return checker.getFailures().stream()
			.map(failure -> Resources.getInstance(Locale.ENGLISH).getString(failure.getMessage()))
			.toList();
	}

	private static PolymorphicConfiguration<?> read(String xml) throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class);
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("contributor", descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		return (PolymorphicConfiguration<?>) reader.read();
	}

	/**
	 * Boots the knowledge base, the model and the configured agent definitions.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestTextPromptContributorAgents.class),
				ModelService.Module.INSTANCE, TopLogicAgentDefinitions.Module.INSTANCE));
	}
}
