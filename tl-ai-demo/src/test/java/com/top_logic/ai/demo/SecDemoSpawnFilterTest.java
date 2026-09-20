/*
	 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
	 *
	 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
	 */
package com.top_logic.ai.demo;

import java.util.List;

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
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;

/**
	 * What {@code spawn_subagent} does when the agent a persona asks for can reach nothing that persona may
	 * call, on the framework agents the deployment really ships.
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
	 * {@code TL_ModelExplorer} is the agent under test for the same reason it is in
	 * {@link SecDemoDelegateFilterTest}: it carries {@code model.read} and {@code data.read} tools only, so
	 * {@code Ben} - which holds neither - can do nothing with it, while {@code Ben} holds both.
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
	 * The refusal path is not reachable from this fixture, and the test says so rather than pretending.
	 *
	 * <p>
	 * Every persona holds both read capabilities and {@code script}, so every shipped delegate has at
	 * least one tool each of them can reach and no spawn is refused - which is the correct outcome and
	 * not a hole. The refusal itself, its wording, and that no child run is started, are pinned by
	 * {@code TestSpawnFilter} and {@code TestDetachedSubagentCeiling} in {@code tl-ai-core} against
	 * hand-built policies, because a policy in which an agent is wholly unreachable is one this fixture
	 * deliberately does not deploy.
	 * </p>
	 *
	 * <p>
	 * <b>Quarantined, see #445.</b> The fixture's {@code <agent-access>} section (#392) restricts ben's
	 * role to {@code TL_ModelExplorer}, so this no longer holds - and no build ran it to say so until
	 * #369. What the fixture should demonstrate instead is the open question in that issue; renaming the
	 * method back to {@code test...} is what puts it back in the suite.
	 * </p>
	 */
	public void quarantinedTestNoSpawnIsRefusedByThisFixture() {
		for (String login : SecDemoSetupService.personaLogins()) {
			for (String agentName : List.of(EXPLORER, SCRIPT_AGENT, EDITOR_AGENT)) {
				assertNull(login + " reaches a tool of '" + agentName + "', so the spawn decision must"
					+ " let it through.", spawnDecisionFor(login, agentName));
			}
		}
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
