/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentRoleAccess;
import com.top_logic.tl.ai.core.security.ToolSecurityException;

/**
 * That an agent the acting account may not run is out of reach of every route to it.
 *
 * <p>
 * The ticket's open question (#392): delegation is bounded by the acting principal and not only by
 * the parent agent's {@code delegations} list. That is two mechanisms and this covers both - the
 * catalogue the model is shown, so that it does not ask, and the gate behind it, so that asking
 * anyway does not work. {@code delegate_to_*} and {@code spawn_subagent} both build their child
 * through {@link AgentFactory#buildAgentLoop} / {@link AgentFactory#buildSubagentLoop}, which is why
 * the gate is asserted there.
 * </p>
 *
 * <p>
 * Runs against the agents this module ships in its own configuration, because the refusal has to be
 * about an agent that exists: one that does not is refused as unknown, which is a different answer.
 * </p>
 */
public class TestAgentGate extends TestCase {

	/** An agent of this module's own configuration. */
	private static final String AGENT = "TL_ModelExplorer";

	/** A second one, to show the gate refuses the named agent rather than every agent. */
	private static final String OTHER_AGENT = "TL_SingleAgent";

	/**
	 * The one agent this module ships as {@code infrastructure}: the chat runs it for the user.
	 */
	private static final String INFRASTRUCTURE_AGENT = "TL_PromptImprover";

	private static final String ROLE = "Clerk";

	/** The configured agents have to be readable, and the policy items need the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			new TestSuite(TestAgentGate.class), TopLogicAgentDefinitions.Module.INSTANCE));
	}

	/** Building the loop of an agent the account may not run is refused, not answered. */
	public void testBuildAgentLoopRefusesWhatTheAccountMayNotRun() {
		try {
			AgentFactory.getInstance().buildAgentLoop(AGENT, denying(AGENT));
			fail("Building the loop of an agent this account may not run must be refused.");
		} catch (ToolSecurityException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(AGENT));
			assertNotNull("The refusal has to be showable to the user.", expected.reason());
		}
	}

	/** A subagent is the same run under another name, and is refused the same way. */
	public void testBuildSubagentLoopRefusesWhatTheAccountMayNotRun() {
		try {
			AgentFactory.getInstance().buildSubagentLoop(AGENT, denying(AGENT));
			fail("Spawning an agent this account may not run must be refused.");
		} catch (ToolSecurityException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(AGENT));
		}
	}

	/**
	 * The gate refuses the agent it was asked about, not every agent.
	 *
	 * <p>
	 * Without this the two cases above would pass for a gate that refuses everything, which is the
	 * failure this whole layer is written to avoid.
	 * </p>
	 */
	public void testAPermittedAgentIsNotRefused() {
		assertNotRefused(() -> AgentFactory.getInstance().buildAgentLoop(AGENT, permitting(AGENT)));
	}

	/**
	 * The delegate catalogue drops an agent the account may not run, so the model does not ask.
	 *
	 * <p>
	 * The tool resolver answers nothing on purpose: a delegate whose tool names do not resolve is
	 * <em>kept</em> by every other rule of {@code unusableReason}, so a reason here can only come
	 * from the agent gate.
	 * </p>
	 */
	public void testDelegateCatalogueDropsWhatTheAccountMayNotRun() {
		AgentDefinition delegate = AgentDefinitionQuery.getInstance().findAgentByName(AGENT);
		assertNotNull("This module configures " + AGENT + "; the test needs it.", delegate);

		String reason = new AgentCapabilities(name -> null, denying(AGENT)).unusableReason(delegate);
		assertNotNull("An agent the account may not run must not be offered as a delegate.", reason);
		assertTrue(reason, reason.contains("may not run"));

		assertNull("An agent the account may run stays in the catalogue.",
			new AgentCapabilities(name -> null, permitting(AGENT)).unusableReason(delegate));
	}

	/** A run whose agent the product chose is outside the allowlist - the prompt improver's case. */
	public void testAnAgentTheProductChoseIsNotGated() {
		assertNotRefused(() -> AgentFactory.getInstance()
			.buildAgentLoop(AGENT, denying(AGENT).withoutAgentChoice()));
	}

	/** The shipped improver carries the mark the rest of this reasons about. */
	public void testTheImproverIsMarkedInfrastructure() {
		AgentDefinition improver =
			AgentDefinitionQuery.getInstance().findAgentByName(INFRASTRUCTURE_AGENT);

		assertNotNull("This module configures " + INFRASTRUCTURE_AGENT + "; the test needs it.",
			improver);
		assertTrue("The chat runs it for the user, so it is not a persona.", improver.infrastructure());
		assertFalse("An ordinary agent is a persona.",
			AgentDefinitionQuery.getInstance().findAgentByName(AGENT).infrastructure());
	}

	/**
	 * An infrastructure agent is refused although the account's role grants it (#397).
	 *
	 * <p>
	 * The point of the rule: the refusal is not the policy's, so no grant lifts it - not a role's,
	 * and not the {@code default="all"} of an application that wrote no policy at all.
	 * </p>
	 */
	public void testAnInfrastructureAgentIsRefusedAlthoughTheRoleGrantsIt() {
		try {
			AgentFactory.getInstance()
				.buildAgentLoop(INFRASTRUCTURE_AGENT, grantingEveryAgent());
			fail("An agent the application runs for the user must not be runnable as a persona.");
		} catch (ToolSecurityException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(INFRASTRUCTURE_AGENT));
			assertNotNull("The refusal has to be showable to the user.", expected.reason());
		}
	}

	/** Reaching it through a delegation or a spawn is the same run under another name. */
	public void testAnInfrastructureAgentIsNoSubagentEither() {
		try {
			AgentFactory.getInstance()
				.buildSubagentLoop(INFRASTRUCTURE_AGENT, grantingEveryAgent());
			fail("Spawning an agent the application runs for the user must be refused.");
		} catch (ToolSecurityException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(INFRASTRUCTURE_AGENT));
		}
	}

	/**
	 * The product's own call path still runs it - the improve-prompt button.
	 *
	 * <p>
	 * {@code PromptImprovementProcessor} builds its run with exactly this context. What says the
	 * agent is not the user's choice is the context, not which class called: a caller that does not
	 * say so is refused whatever it is.
	 * </p>
	 */
	public void testTheProductStillRunsItsOwnInfrastructureAgent() {
		assertNotRefused(() -> AgentFactory.getInstance()
			.buildAgentLoop(INFRASTRUCTURE_AGENT, grantingEveryAgent().withoutAgentChoice()));
	}

	/** The delegate catalogue drops it too, so no model is shown an agent it may not hand work to. */
	public void testDelegateCatalogueDropsAnInfrastructureAgent() {
		AgentDefinition improver =
			AgentDefinitionQuery.getInstance().findAgentByName(INFRASTRUCTURE_AGENT);

		String reason = new AgentCapabilities(name -> null, grantingEveryAgent())
			.unusableReason(improver);
		assertNotNull("An agent the application runs for the user must not be offered as a delegate.",
			reason);
		assertTrue(reason, reason.contains("infrastructure"));
	}

	/** A request that names no agent is never answered with one nobody may talk to. */
	public void testTheFallbackAgentIsNeverInfrastructure() {
		String fallback = AgentFactory.defaultAgentFor(grantingEveryAgent().runAuthorization());

		assertNotNull("Every agent is granted here, so there is one to fall back to.", fallback);
		assertFalse("The fallback must not be an agent the application runs for the user.",
			INFRASTRUCTURE_AGENT.equals(fallback));
	}

	/**
	 * That the given call is not refused by the agent gate.
	 *
	 * <p>
	 * Only the refusal is asserted. Finishing the loop needs a provider for the agent's model, which
	 * this test deliberately does not boot - it is about who may run an agent, not about what the run
	 * then does, and starting the provider stack would make the test depend on a configured API key.
	 * </p>
	 */
	private static void assertNotRefused(Runnable call) {
		try {
			call.run();
		} catch (ToolSecurityException refused) {
			fail("Refused although this account may run the agent: " + refused.getMessage());
		} catch (RuntimeException expected) {
			// Anything but the gate: the loop cannot be finished without a provider.
		}
	}

	/** A security context of an account whose one role permits exactly the given agent. */
	private static TopLogicSecurityContext permitting(String agentName) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		AgentRoleAccess role = TypedConfiguration.newConfigItem(AgentRoleAccess.class);
		role.setName(ROLE);
		AgentGrant grant = TypedConfiguration.newConfigItem(AgentGrant.class);
		grant.setAgents(List.of(agentName));
		role.getGrants().add(grant);
		policy.getAgentAccess().getRoles().put(ROLE, role);

		return TopLogicSecurityContext.enforcedRun(new RunAuthorization(
			RoleAccessPolicy.agents(policy), null, null, null, Set.of(ROLE)));
	}

	/**
	 * A security context of an account no role restricts - what {@code <agent-access default="all">}
	 * and an application that wrote no policy both amount to.
	 */
	private static TopLogicSecurityContext grantingEveryAgent() {
		return TopLogicSecurityContext.enforcedRun(
			new RunAuthorization(null, null, null, null, Set.of()));
	}

	/** A security context of an account whose one role forbids exactly the given agent. */
	private static TopLogicSecurityContext denying(String agentName) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		AgentRoleAccess role = TypedConfiguration.newConfigItem(AgentRoleAccess.class);
		role.setName(ROLE);
		AgentGrant deny = TypedConfiguration.newConfigItem(AgentGrant.class);
		deny.setAgents(List.of(agentName));
		role.getDenies().add(deny);
		policy.getAgentAccess().getRoles().put(ROLE, role);

		return TopLogicSecurityContext.enforcedRun(new RunAuthorization(
			RoleAccessPolicy.agents(policy), null, null, null, Set.of(ROLE)));
	}
}
