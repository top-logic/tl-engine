/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;

/**
 * The security mode an agent definition names for itself, against a real boot.
 *
 * <p>
 * The fixture's deployment is {@code ENFORCED}, which is the strictest mode there is, so this booted
 * application can only show the half of the rule that says <b>no</b>: the agent
 * {@code TL_secDemoLaxAgent} asks to be {@code LEGACY} and is enforced anyway. That is the direction
 * that has to hold, because authoring an agent is itself a privileged action and must not be a way
 * around what the deployment decided. The other direction - an agent raising the mode of an
 * unmigrated deployment - needs a {@code LEGACY} deployment and is unit-tested in {@code tl-ai-core}
 * ({@code TestAiSecurityMode}, {@code TestTopLogicSecurityContext}, {@code TestAgentSecurityFloor}).
 * </p>
 *
 * <p>
 * Both places an agent definition can live are covered: the service configuration the fixture writes,
 * and a knowledge-base agent created here - the latter because the value crosses a storage mapping on
 * that path, and a mapping that is wrong fails at the first write rather than at boot.
 * </p>
 *
 * @see doc/security/testbed.md scenario F1
 */
public class SecDemoAgentFloorTest extends SecDemoPersonaTest {

	/** The fixture agent that declares {@link AiSecurityMode#LEGACY} for itself. */
	private static final String LAX_AGENT = "TL_secDemoLaxAgent";

	/**
	 * Precondition: this deployment enforces. Every case below is about what an agent can do to that.
	 */
	public void testTheFixtureDeploymentEnforces() {
		assertEquals("The test bed configures a <tool-access> matrix, so the ceiling is ENFORCED."
			+ " Without that, the cases below would pass vacuously.",
			AiSecurityMode.ENFORCED, AiToolAccessService.ceiling());
	}

	/** The declaration of the fixture agent is read from its configuration. */
	public void testTheFixtureAgentDeclaresALaxerMode() {
		AgentDefinition definition = AgentDefinitionQuery.getInstance().findAgentByName(LAX_AGENT);

		assertNotNull("The agent '" + LAX_AGENT + "' must be configured by 'secdemo.config.xml'.",
			definition);
		assertEquals(AiSecurityMode.LEGACY, definition.securityFloor());
	}

	/** An agent that says nothing about security has no declaration - absent stays absent. */
	public void testAnAgentThatSaysNothingHasNoDeclaration() {
		for (AgentDefinition definition : AgentDefinitionQuery.getInstance().getAllAgents()) {
			if (!LAX_AGENT.equals(definition.name())) {
				assertNull("The agent '" + definition.name() + "' declares no security mode, so its"
					+ " definition must carry none - not the first constant of the enum.",
					definition.securityFloor());
			}
		}
	}

	/**
	 * The resolution: the laxer declaration loses, and the run of that agent really is checked.
	 *
	 * <p>
	 * The second half matters as much as the first. A resolved mode of {@code ENFORCED} that came with
	 * an unchecked authorization would be a label rather than a control, so the run is asked the
	 * question a run of any other agent would be asked: may this persona use a tool its roles do not
	 * grant?
	 * </p>
	 */
	public void testALaxerDeclarationLosesAgainstTheDeployment() {
		AiSecurityMode declared = AgentDefinitionQuery.getInstance().securityFloorOf(LAX_AGENT);

		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.stricter(AiToolAccessService.ceiling(), declared));

		TopLogicSecurityContext security =
			TopLogicSecurityContext.forRun(actAs(READER), false, declared);

		assertEquals(AiSecurityMode.ENFORCED, security.securityMode());
		assertFalse("A run of the lax agent must still be refused what the persona's roles do not"
			+ " grant.", security.hasToolPermission(tool(AGENT_META_TOOL)));
		assertTrue("...and still be allowed what they do grant.",
			security.hasToolPermission(tool(DATA_READ_TOOL)));
	}

	/**
	 * The same declaration made in the knowledge base - the Agent Editor's half of the feature.
	 *
	 * <p>
	 * Writes and reads the value back through the model attribute, so the {@code <enum-storage>}
	 * mapping that turns the mode into a stored string and back is exercised rather than assumed.
	 * </p>
	 */
	public void testAKnowledgeBaseAgentCarriesItsDeclaration() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String name = "secDemoFloorProbe";
		Agent agent;
		try (Transaction tx = kb.beginTransaction()) {
			agent = TlAiCoreFactory.getInstance().createAgent();
			agent.setName(name);
			agent.setSecurityMode(AiSecurityMode.ENFORCED);
			tx.commit();
		}
		try {
			assertEquals("The stored mode must survive the round trip through its storage mapping.",
				AiSecurityMode.ENFORCED, agent.getSecurityMode());
			assertEquals("...and reach the definition every consumer of an agent reads.",
				AiSecurityMode.ENFORCED, AgentDefinitionQuery.getInstance().securityFloorOf(name));
		} finally {
			try (Transaction tx = kb.beginTransaction()) {
				agent.tDelete();
				tx.commit();
			}
		}
	}

	/** Boots the test bed plus the service holding the configured agent definitions. */
	public static Test suite() {
		return suite(SecDemoAgentFloorTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}
