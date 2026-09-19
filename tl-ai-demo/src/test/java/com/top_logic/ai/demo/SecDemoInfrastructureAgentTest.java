/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentFactory.RunOverrides;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RunAuthorization;

/**
 * That an agent the application runs <em>for</em> a user is not runnable as a chat persona, on the
 * fixture where that used to be possible (#397).
 *
 * <p>
 * The measurement the ticket reports: the fixture writes {@code <agent-access default="all">} and
 * names only {@code ticketReader} in it, so {@link #ANNA} - who holds {@code projectPartner} - falls
 * to the default and may run every agent. Under that policy {@code POST /chat/send} naming the
 * prompt improver was accepted, and the improver answered as a persona. The refusal now comes from
 * what the agent <em>is</em>, so the most permissive policy this repository ships cannot lift it.
 * </p>
 *
 * <p>
 * Asserted at {@link AgentFactory#runRefusal}, which is the one place every entry point asks: the
 * chat's pre-check, the gate the run passes, delegation and the scheduler all arrive there. The
 * HTTP half - a 403 with the sentence the user reads - is pinned in {@code TestChatRunAccess}.
 * </p>
 */
public class SecDemoInfrastructureAgentTest extends SecDemoPersonaTest {

	/** The agent the chat runs for the user: it rewrites a draft and never answers. */
	private static final String IMPROVER = "TL_PromptImprover";

	/** An ordinary persona, which the same policy does grant {@link #ANNA}. */
	private static final String PERSONA = "TL_SingleAgent";

	/** Precondition: the fixture is the permissive one the ticket measured. */
	public void testFixtureGrantsAnnaEveryAgent() {
		assertNull("anna falls to <agent-access default=\"all\"> and may run an ordinary agent.",
			refusalFor(ANNA, PERSONA));
	}

	/** The improver is marked as what it is, rather than being hidden by whoever configures it. */
	public void testTheImproverSaysItIsInfrastructure() {
		AgentDefinition improver = AgentDefinitionQuery.getInstance().findAgentByName(IMPROVER);

		assertNotNull("The deployment ships " + IMPROVER + "; this test bed assumes it.", improver);
		assertTrue("The chat runs it for the user, so it is no conversation partner.",
			improver.infrastructure());
	}

	/** The reproduction: naming it is refused for an account the policy grants every agent. */
	public void testAnnaMayNotTalkToTheImprover() {
		Decision.Denied denied = refusalFor(ANNA, IMPROVER);

		assertNotNull("An agent the application runs for the user must not answer as a persona.",
			denied);
		assertEquals("No role decides this, so no grant can lift it.", Cause.INFRASTRUCTURE_AGENT,
			denied.cause());
		assertTrue("The refusal names the agent: " + denied.message(),
			denied.message().contains(IMPROVER));
	}

	/** Every persona of the fixture, so the rule is about the agent and not about one account. */
	public void testNoPersonaMayTalkToTheImprover() {
		for (String login : SecDemoSetupService.personaLogins()) {
			assertNotNull(login + " must not be able to talk to " + IMPROVER + ".",
				refusalFor(login, IMPROVER));
		}
	}

	/** A request that names no agent is never answered with the one nobody may talk to. */
	public void testTheFallbackAgentIsNeverTheImprover() {
		assertFalse("The fallback must not be an agent the application runs for the user.",
			IMPROVER.equals(AgentFactory.defaultAgentFor(runAccessFor(ANNA))));
	}

	/**
	 * The product's own call path still runs it - the improve-prompt button anna sees.
	 *
	 * <p>
	 * What makes that run legal is the run itself, not which class started it:
	 * {@code PromptImprovementProcessor} builds its context with
	 * {@code SecurityContext.withoutAgentChoice()}.
	 * </p>
	 */
	public void testTheProductStillRunsItForAnna() {
		assertNull("The improve-prompt button runs the very same agent and must keep working.",
			AgentFactory.runRefusal(runAccessFor(ANNA).withoutAgentCheck(), IMPROVER,
				RunOverrides.NONE));
	}

	/** Why a run of the named agent would be refused for the given persona, or <code>null</code>. */
	private Decision.Denied refusalFor(String login, String agentName) {
		return AgentFactory.runRefusal(runAccessFor(login), agentName, RunOverrides.NONE);
	}

	/** What the given persona may run under the policy the fixture ships. */
	private RunAuthorization runAccessFor(String login) {
		return AiToolAccessService.effectiveRunAccessFor(actAs(login));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoInfrastructureAgentTest.class, TopLogicAgentDefinitions.Module.INSTANCE,
			LLMService.Module.INSTANCE);
	}

}
