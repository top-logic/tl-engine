/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * What {@code spawn_subagent} does when nothing could come of the agent a persona asks for, on the
 * framework agents the deployment really ships.
 *
 * <p>
 * The spawn half of {@link SecDemoDelegateFilterTest}. {@code list_agents} is already filtered, so
 * before this an agent could read a name out of a filtered listing, spawn it anyway, and get a child run
 * that filtered its own tool schemas down to nothing - a prompt line, a provider round trip and a report
 * that says nothing. The refusal happens before any of it: the tool call is answered without the loop
 * factory ever being asked for a loop and without a job being registered, and both are asserted rather
 * than assumed.
 * </p>
 *
 * <p>
 * What decides it in this fixture is {@code <agent-access>} (#392) rather than the capability filter:
 * every persona reaches a tool of every shipped delegate, so no spawn of this fixture is refused for
 * want of a capability, while a persona holding {@link #RESTRICTED_ROLE} is refused every agent but
 * {@link #EXPLORER} because its role may not run one. The assertions are therefore per persona (#445).
 * </p>
 *
 * <p>
 * Which is also what the <em>wording</em> is asserted against here (#517): a fixture whose every refusal
 * comes from {@code <agent-access>} is exactly the one that catches a message explaining it as a missing
 * capability. It used to list the capabilities the agent's tools happen to need - true of the tools,
 * beside the point for the refusal, and enough to send a user to their administrator for a grant that
 * would have changed nothing.
 * </p>
 *
 * <p>
 * The refusal for want of a capability - its wording, and that no child run is started - is pinned by
 * {@code TestSpawnFilter} and {@code TestDetachedSubagentCeiling} in {@code tl-ai-core} against
 * hand-built policies, because a policy in which an agent is wholly unreachable is one this fixture
 * deliberately does not deploy.
 * </p>
 *
 * @see SubagentSpawner#detachedSpawnRefusal(String, AgentCapabilities)
 */
public class SecDemoSpawnFilterTest extends SecDemoPersonaTest {

	/** Reads the model and its instances, and nothing else. */
	private static final String EXPLORER = "TL_ModelExplorer";

	/** Asks questions, which every persona may do. */
	private static final String EDITOR_AGENT = "TL_ModelEditor";

	/** Runs TL-Script, and keeps tasks - the second of which no role of this fixture may do. */
	private static final String SCRIPT_AGENT = "TL_TLScriptAgent";

	/**
	 * The one role the fixture's {@code <agent-access>} section names, and which it grants
	 * {@link #EXPLORER} alone.
	 *
	 * @see SecDemoDelegateFilterTest
	 */
	private static final String RESTRICTED_ROLE = "ticketReader";

	/**
	 * A spawn is refused exactly where the persona's roles may not run the agent, and let through
	 * everywhere else.
	 *
	 * <p>
	 * Every persona of this fixture holds both read capabilities and {@code script}, so every shipped
	 * delegate has at least one tool each of them could reach, and the capability half of the decision
	 * refuses nothing here - which is the correct outcome and not a hole. What does refuse is
	 * {@code <agent-access>}: a persona holding {@link #RESTRICTED_ROLE} is granted {@link #EXPLORER}
	 * alone, so a spawn of either other agent is answered before a child run is built.
	 * </p>
	 *
	 * <p>
	 * That {@code list_agents} already leaves those agents out of that persona's listing is not enough:
	 * the model can name an agent it read elsewhere, and this is the gate that is asked when it does.
	 * </p>
	 */
	public void testSpawnIsRefusedWhereTheRoleMayNotRunTheAgent() {
		int restrictedPersonas = 0;
		for (Map.Entry<String, List<String>> persona : SecDemoSetupService.personaRoles().entrySet()) {
			String login = persona.getKey();
			boolean restricted = persona.getValue().contains(RESTRICTED_ROLE);

			assertNull(login + " may run '" + EXPLORER + "' whichever roles it holds, and reaches both"
				+ " of its capabilities, so the spawn decision must let it through.",
				spawnDecisionFor(login, EXPLORER));

			for (String agentName : List.of(SCRIPT_AGENT, EDITOR_AGENT)) {
				String refusal = spawnDecisionFor(login, agentName);
				if (restricted) {
					assertNotNull(login + " holds '" + RESTRICTED_ROLE + "', which may not run '"
						+ agentName + "', so the spawn must be refused.", refusal);
					assertTrue("A refusal must name the agent it is about: " + refusal,
						refusal.contains(agentName));
					assertAgentAccessIsBlamed(login, refusal);
				} else {
					assertNull(login + " may run '" + agentName + "' and reaches a tool of it, so the"
						+ " spawn decision must let it through.", refusal);
				}
			}

			if (restricted) {
				restrictedPersonas++;
			}
		}

		assertTrue("The fixture must restrict at least one persona, otherwise no spawn of it is ever"
			+ " refused and this test asserts only the permissive half.", restrictedPersonas > 0);
		assertTrue("The fixture must leave at least one persona unrestricted, otherwise nothing shows"
			+ " that the refusal follows the role.",
			restrictedPersonas < SecDemoSetupService.personaLogins().size());
	}

	/** An unenforced run spawns whatever it is asked for, as it did before this filter existed. */
	public void testUnenforcedRunRefusesNothing() {
		assertNull(SubagentSpawner.detachedSpawnRefusal(EXPLORER,
			AgentCapabilities.forRun(SecurityContext.UNRESTRICTED)));
	}

	/**
	 * The decision belongs to the child's context, not to the parent turn's.
	 *
	 * <p>
	 * On the deployed policy this can assert the direction only: no framework agent of this deployment
	 * lives entirely inside the capabilities the demo allowlist withholds, so no persona can be shown
	 * losing one to the ceiling here. What is asserted is that the child never reaches more than its
	 * parent turn, and the case where the difference actually refuses a spawn is
	 * {@code TestSpawnFilter.testForegroundReachableAgentIsRefusedForADetachedSpawn}.
	 * </p>
	 */
	public void testTheChildsCeilingIsWhatDecides() {
		SecurityContext turn = securityFor(ANNA);
		AgentDefinition explorer = agent(EXPLORER);

		assertTrue("A detached child of Anna's turn is an unattended run.",
			turn.withBackgroundCeiling().isBackgroundRun());
		assertTrue("Whatever the parent turn cannot reach, the child cannot reach either.",
			AgentCapabilities.forRun(turn.withBackgroundCeiling()).unreachableCapabilities(explorer)
				.containsAll(AgentCapabilities.forRun(turn).unreachableCapabilities(explorer)));
	}

	/**
	 * A delegation the same {@code <agent-access>} section refuses says the same thing (#517).
	 *
	 * <p>
	 * The synchronous path composes its own message and had the same defect, so it is asserted beside
	 * the spawn's rather than left to be discovered later. It loses nothing to the background
	 * allowlist - a synchronous delegate is attended - so the agent grant is the only cause there is.
	 * </p>
	 */
	public void testDelegationIsRefusedWithTheSameCause() {
		for (Map.Entry<String, List<String>> persona : SecDemoSetupService.personaRoles().entrySet()) {
			String login = persona.getKey();
			if (!persona.getValue().contains(RESTRICTED_ROLE)) {
				continue;
			}
			for (String agentName : List.of(SCRIPT_AGENT, EDITOR_AGENT)) {
				String refusal = DelegationTool.delegationRefusal(agentName, AgentCapabilities
					.forRun(parentTurnOf(login).forDelegation(TransactionScope.NOOP).security()));

				assertNotNull(login + " may not run '" + agentName + "' synchronously either.",
					refusal);
				assertAgentAccessIsBlamed(login, refusal);
			}
		}
	}

	/**
	 * That a refusal blames the agent grant and nothing else.
	 *
	 * <p>
	 * The capability list is the damaging part: the model is told to relay what the message names, so a
	 * capability named here reaches an administrator as the change to make, and making it leaves the
	 * spawn refused. Every framework capability is checked rather than the three this fixture happens
	 * to produce, so the assertion does not quietly narrow when the shipped agents change.
	 * </p>
	 */
	private static void assertAgentAccessIsBlamed(String login, String refusal) {
		assertTrue(login + ": the refusal must say that the roles are what withhold the agent: "
			+ refusal, refusal.contains("permitted by none of your roles"));
		for (String capability : ToolGroups.ALL) {
			assertFalse(login + ": a grant of '" + capability + "' would change nothing here, so the"
				+ " refusal must not name it: " + refusal, refusal.contains(capability));
		}
		assertFalse(login + ": nothing may suggest a missing capability: " + refusal,
			refusal.contains("does not hold"));
		assertFalse(login + ": nor that being unattended is what did it: " + refusal,
			refusal.contains("background allowlist"));
	}

	/**
	 * The refusal a detached spawn of the named agent gets in a run of the given persona, or
	 * <code>null</code> if the spawn goes ahead.
	 */
	private String spawnDecisionFor(String login, String targetAgentName) {
		return SubagentSpawner.detachedSpawnRefusal(targetAgentName, AgentCapabilities.forRun(
			parentTurnOf(login).security().withBackgroundCeiling()));
	}

	/** The context of an attended chat turn of the given persona, auto-approving as a session may. */
	private ToolContext parentTurnOf(String login) {
		return new ToolContext(securityFor(login), ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	private static AgentDefinition agent(String name) {
		AgentDefinition definition = AgentDefinitionQuery.getInstance().findAgentByName(name);
		assertNotNull("The framework agent '" + name + "' must exist; this test bed assumes the agents"
			+ " shipped in 'tl-ai-core.conf.config.xml'.", definition);
		return definition;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoSpawnFilterTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}
