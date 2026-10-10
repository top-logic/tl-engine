/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.model.ApplicationContext;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.util.model.ModelService;

/**
 * The application context reaches the system prompt of the agents a user talks to, and of no other
 * (Gitea #667).
 */
public class TestApplicationContextContributor extends BasicTestCase {

	private static final AgentModelAssignment ASSIGNMENT = new AgentModelAssignment("test-model",
		null, null, null, null, null, null, null, null, null, null, null, null, null, null);

	private static final String TEXT = "# Application context: Test\n\n## Purpose\nTracks test orders.";

	private static final AgentDefinition ALBERT = agent("TL_SingleAgent", false);

	private static final AgentDefinition IMPROVER = agent("TL_PromptImprover", true);

	@Override
	protected void tearDown() throws Exception {
		write(null, Set.of());
		super.tearDown();
	}

	/** Every application has the object - it is the singleton of {@code tl.ai.core}. */
	public void testTheApplicationHasOne() {
		assertNotNull(ApplicationContextContributor.current());
	}

	/** An application that wrote no context gets nothing added. */
	public void testNothingWrittenNothingAdded() {
		assertNull(ApplicationContextContributor.INSTANCE.contribute(ALBERT, ASSIGNMENT));
		assertEquals("You are Albert.", prompt(ALBERT));
	}

	/**
	 * A written context is part of the prompt of an agent a user talks to, after its own text - with
	 * no contributor configured at all, which is what every application that overrides the
	 * {@code LLMService} configuration as a whole has.
	 */
	public void testTheTextReachesTheAgentsPromptWithNoContributorConfigured() {
		assertEquals("No LLMService runs here, so nothing is configured.", List.of(),
			LLMService.promptContributors());
		write(TEXT, Set.of());

		assertEquals("You are Albert.\n\n" + TEXT, prompt(ALBERT));
	}

	/** The installation's own contributors speak first; the application context comes after them. */
	public void testItComesAfterTheConfiguredContributors() {
		write(TEXT, Set.of());
		SystemPromptContributor houseRules = (agent, assignment) -> "## House rules";

		List<SystemPromptContributor> contributors = new ArrayList<>(List.of(houseRules));
		contributors.add(ApplicationContextContributor.INSTANCE);
		assertEquals("You are Albert.\n\n## House rules\n\n" + TEXT,
			AgentFactory.appendContributions(ALBERT, ASSIGNMENT, ALBERT.systemPrompt(), contributors));
		assertSame(ApplicationContextContributor.INSTANCE, last(AgentFactory.promptContributors()));
	}

	private static SystemPromptContributor last(List<SystemPromptContributor> contributors) {
		return contributors.get(contributors.size() - 1);
	}

	/** An agent the application runs on its own never gets it, and never pays for it. */
	public void testAnInfrastructureAgentDoesNotGetIt() {
		write(TEXT, Set.of());

		assertNull(ApplicationContextContributor.INSTANCE.contribute(IMPROVER, ASSIGNMENT));
	}

	/** Naming agents narrows the context to them. */
	public void testNamedAgentsNarrowIt() {
		write(TEXT, Set.of("Order_Assistant"));

		assertNull(ApplicationContextContributor.INSTANCE.contribute(ALBERT, ASSIGNMENT));
		assertEquals(TEXT,
			ApplicationContextContributor.INSTANCE.contribute(agent("Order_Assistant", false), ASSIGNMENT));
	}

	private static String prompt(AgentDefinition agent) {
		return AgentFactory.appendContributions(agent, ASSIGNMENT, agent.systemPrompt(),
			AgentFactory.promptContributors());
	}

	private static void write(String text, Set<String> agents) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ApplicationContext context = ApplicationContextContributor.current();
			context.setText(text);
			context.setAgents(agents);
			tx.commit();
		}
	}

	private static AgentDefinition agent(String name, boolean infrastructure) {
		return new AgentDefinition(name, null, "", null, "You are Albert.", List.of(), List.of(), List.of(),
			List.of(), AgentSource.CONFIG, infrastructure);
	}

	/**
	 * The suite, on a knowledge base with the model service running.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestApplicationContextContributor.class),
				ModelService.Module.INSTANCE));
	}
}
