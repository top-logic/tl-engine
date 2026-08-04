/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Map;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SpawnSubagentTool;
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolResult;

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
 * {@code secbare1} - which holds neither - can do nothing with it, while {@code secreader1} holds both.
 * </p>
 *
 * @see SubagentSpawner#detachedSpawnRefusal(String, AgentCapabilities)
 */
public class SecDemoSpawnFilterTest extends SecDemoPersonaTest {

	/** Reads the model and its instances, and nothing else. */
	private static final String EXPLORER = "TL_ModelExplorer";

	/** Asks questions, which every persona may do. */
	private static final String EDITOR_AGENT = "TL_ModelEditor";

	/**
	 * A persona that can do nothing with the agent it names gets no child run, and is told which
	 * capability is missing.
	 */
	public void testSpawnOfAnUnreachableAgentIsRefused() {
		String refusal = refusedSpawnOf(BARE, EXPLORER);

		assertTrue("secbare1 holds neither read capability, so the child could only have come back"
			+ " empty-handed: " + refusal, refusal.startsWith("ERROR:"));
		assertTrue("The refusal must name the capabilities an operator has to grant: " + refusal,
			refusal.contains(ToolGroups.MODEL_READ) && refusal.contains(ToolGroups.DATA_READ));
		assertTrue("...and the agent the model asked for: " + refusal,
			refusal.contains("'" + EXPLORER + "'"));
		assertTrue("A persona holding neither must be sent for a role grant rather than blaming the"
			+ " allowlist: " + refusal, refusal.contains("does not hold"));
		assertFalse(refusal, refusal.contains("background allowlist"));
	}

	/** The refusal never suggests the agent is absent - it is configured and shipped. */
	public void testRefusalDoesNotDenyTheAgentsExistence() {
		assertNotNull("Precondition: the agent this test says exists does exist.", agent(EXPLORER));

		String refusal = refusedSpawnOf(BARE, EXPLORER);

		assertFalse(refusal, refusal.toLowerCase().contains("does not exist"));
		assertFalse(refusal, refusal.toLowerCase().contains("not found"));
		assertTrue("The model must be told to stop rather than to try another name: " + refusal,
			refusal.contains("again will not help"));
	}

	/**
	 * A persona that can use the same agent is not refused it, so the filter is not a blanket one.
	 *
	 * <p>
	 * The decision, not the whole tool call: a spawn that goes ahead takes a pool thread and a
	 * sub-session, and what it would run into here is the absence of an LLM key rather than anything
	 * about security. That the tool returns a handle when the decision lets it through is pinned in
	 * {@code TestSpawnSubagentTool}.
	 * </p>
	 */
	public void testSpawnOfAReachableAgentIsNotRefused() {
		assertNull("secreader1 holds both read capabilities.", spawnDecisionFor(READER, EXPLORER));
		assertNull("Every persona reaches TL_ModelEditor through 'interaction'.",
			spawnDecisionFor(BARE, EDITOR_AGENT));
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
		SecurityContext turn = securityFor(WRITER);
		AgentDefinition explorer = agent(EXPLORER);

		assertTrue("A detached child of secwriter1's turn is an unattended run.",
			turn.withBackgroundCeiling().isBackgroundRun());
		assertTrue("Whatever the parent turn cannot reach, the child cannot reach either.",
			AgentCapabilities.forRun(turn.withBackgroundCeiling()).unreachableCapabilities(explorer)
				.containsAll(AgentCapabilities.forRun(turn).unreachableCapabilities(explorer)));
	}

	/**
	 * Runs the real {@code spawn_subagent} tool as the given persona and returns the refusal it answers
	 * with.
	 *
	 * <p>
	 * The whole tool call, so the refusal is asserted where the model meets it. The loop factory and the
	 * job registry both fail the test if the spawn gets past the decision, which is what makes "no child
	 * run and no provider round trip" a statement rather than a claim - and is why this helper is only
	 * for a spawn that must be refused.
	 * </p>
	 */
	private String refusedSpawnOf(String login, String targetAgentName) {
		DelegationTool.AgentLoopFactory refusingFactory = new DelegationTool.AgentLoopFactory() {
			@Override
			public AgentLoop buildAgentLoop(String agentName, SecurityContext security) {
				throw new AssertionError("The synchronous path is not involved in a spawn.");
			}

			@Override
			public AgentLoop buildSubagentLoop(String agentName, SecurityContext security) {
				throw new AssertionError("A refused spawn must not build the child's loop.");
			}
		};
		ToolContext parentTurn = parentTurnOf(login);

		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(registry)) {
			ToolResult result = new SpawnSubagentTool(refusingFactory)
				.execute(Map.of("agent", targetAgentName, "task", "Say what you can see."), parentTurn);

			assertEquals("A refused spawn must not have registered a job: " + result.text(), 0,
				registry.byStatus(SubagentJobStatus.values()).size());
			return result.text();
		} finally {
			registry.shutdown();
		}
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
