/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.AgentCapabilities.AgentGraph;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Which agents {@code spawn_subagent} will start, and what it says about the ones it refuses.
 *
 * <p>
 * The spawn counterpart of {@link TestDelegateFilter}, and a separate test because the context the
 * predicate is asked against is a different one. A synchronous delegation inherits the parent turn's
 * security; a detached spawn does not - it is an unattended run, so
 * {@link ToolContext#forDetachedSubagent} bounds it by the {@code <background-allowlist>}. An agent the
 * parent turn reaches in full can therefore be out of reach in the child, which is exactly the case a
 * spawn judged by the parent's context would get wrong.
 * </p>
 *
 * <p>
 * The security contexts are the real ones, built from a hand-written policy the way
 * {@link TestDetachedSubagentCeiling} builds them, and derived through {@code forDetachedSubagent}
 * rather than restated - so a ceiling that stopped applying would fail these tests rather than pass
 * them. Only the agent graph and the tool registry are hand-built, because the real ones need a booted
 * application; the deployed wiring is asserted in {@code SecDemoSpawnFilterTest}.
 * </p>
 *
 * @see SubagentSpawner#detachedSpawnRefusal(String, AgentCapabilities)
 */
public class TestSpawnFilter extends TestCase {

	/** In {@code data.read}: held by both roles, and on the allowlist. */
	private static final String READ_TOOL = "data.readInstances";

	/** In {@code data.write}: held by the clerk only, and deliberately off the allowlist. */
	private static final String WRITE_TOOL = "data.createInstance";

	/** In {@code script}: held by nobody here. */
	private static final String SCRIPT_TOOL = "script.evaluate";

	/** Reaches {@link #WRITE_TOOL} and nothing else. */
	private static final String WRITER = "writer";

	/** Reaches {@link #READ_TOOL} and {@link #WRITE_TOOL} - the partial case. */
	private static final String MIXED = "mixed";

	/** Reaches {@link #READ_TOOL} only. */
	private static final String READER = "reader";

	/** Reaches nothing at all. */
	private static final String TOOLLESS = "toolless";

	/** Reaches {@link #SCRIPT_TOOL} through a skill, and nothing of its own. */
	private static final String SCRIPTED = "scripted";

	/** Holds {@code data.read} and {@code data.write}. */
	private static final String CLERK = "Clerk";

	/** Holds {@code data.read} alone. */
	private static final String VIEWER = "Viewer";

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestSpawnFilter.class)));
	}

	/**
	 * A spawn of an agent every one of whose tools the user is denied is refused, by capability name.
	 */
	public void testFullyDeniedAgentIsNotSpawned() {
		String refusal = spawnAs(VIEWER, WRITER);

		assertNotNull("An agent that could only come back empty-handed must not be spawned.", refusal);
		assertTrue("The user's answer is a capability grant, so the capability must be named: " + refusal,
			refusal.contains(ToolGroups.DATA_WRITE));
		assertTrue("The refusal must name the agent the model asked for: " + refusal,
			refusal.contains("'" + WRITER + "'"));
		assertTrue("A user holding neither must be sent to their administrator for a grant: " + refusal,
			refusal.contains("does not hold"));
		assertFalse("An unattended ceiling did not take this away - the user never held it: " + refusal,
			refusal.contains("background allowlist"));
	}

	/**
	 * The refusal is an ordinary tool error and never suggests the agent is not there.
	 *
	 * <p>
	 * A model told "no such agent" looks for another name for the same work, or reports the deployment
	 * as broken. The agent exists and is configured correctly; what is missing is a grant.
	 * </p>
	 */
	public void testRefusalDoesNotDenyTheAgentsExistence() {
		String refusal = spawnAs(VIEWER, WRITER);

		assertFalse(refusal, refusal.toLowerCase().contains("does not exist"));
		assertFalse(refusal, refusal.toLowerCase().contains("unknown agent"));
		assertFalse(refusal, refusal.toLowerCase().contains("not found"));
		assertTrue("The model must be told to stop rather than to try again: " + refusal,
			refusal.contains("again will not help"));
	}

	/** One reachable tool is enough: the child can still do that part of the job. */
	public void testPartlyReachableAgentIsSpawned() {
		assertNull("The read half of its tools is permitted, so the child has work it can do.",
			spawnAs(VIEWER, MIXED));
		assertNull(spawnAs(VIEWER, READER));
	}

	/**
	 * The subtle case, and the reason this is judged against the child's context: an agent the parent
	 * turn reaches is refused for a <em>detached</em> spawn, and the refusal blames the allowlist.
	 *
	 * <p>
	 * The clerk holds {@code data.write}. The child does not, because it runs with nobody to approve a
	 * tool call and the allowlist does not carry that capability. Telling the clerk they lack a right
	 * they do hold would send them to their administrator for the wrong change.
	 * </p>
	 */
	public void testForegroundReachableAgentIsRefusedForADetachedSpawn() {
		assertNull("Precondition: the clerk's own turn reaches the agent in full.",
			SubagentSpawner.detachedSpawnRefusal(WRITER, capabilities(parentTurn(CLERK).security())));

		String refusal = spawnAs(CLERK, WRITER);

		assertNotNull("A detached child of the same turn cannot reach any of its tools.", refusal);
		assertTrue("The refusal must blame the allowlist: " + refusal,
			refusal.contains("background allowlist"));
		assertTrue("...and say why the allowlist applies at all: " + refusal,
			refusal.contains("unattended"));
		assertTrue("...and name the capability that is out of reach: " + refusal,
			refusal.contains(ToolGroups.DATA_WRITE));
		assertFalse("The clerk does hold data.write, so this must not be reported as a missing right: "
			+ refusal, refusal.contains("does not hold"));
	}

	/**
	 * A synchronous delegation to that same agent is untouched.
	 *
	 * <p>
	 * It completes inside the parent turn, where the human who could answer an approval prompt still is,
	 * so it keeps the parent's reach and nothing about it is filtered.
	 * </p>
	 */
	public void testSynchronousDelegationToTheSameAgentStillWorks() {
		SecurityContext delegate = parentTurn(CLERK).forDelegation(TransactionScope.NOOP).security();

		assertFalse("A synchronous delegate is attended.", delegate.isBackgroundRun());
		assertNull("It keeps the parent's data.write, so the agent can do its whole job.",
			capabilities(delegate).unusableReason(agent(WRITER)));
	}

	/** What the allowlist does admit is spawnable, so the ceiling is not a wall. */
	public void testAllowlistedCapabilityIsStillSpawnable() {
		assertNull("data.read is on the allowlist and the clerk holds it.", spawnAs(CLERK, READER));
	}

	/**
	 * An agent that reaches nothing is spawnable, exactly as such a delegate is offered.
	 *
	 * <p>
	 * "All zero of its tools are denied" is vacuously true. An agent that carries no tools of its own
	 * exists to think or to route, and refusing it would take away something the run never lost.
	 * </p>
	 */
	public void testAgentThatReachesNothingIsSpawnable() {
		assertNull(spawnAs(VIEWER, TOOLLESS));
	}

	/** An agent that does not exist is not refused here: that is not a denial. */
	public void testUnknownAgentIsNotRefusedHere() {
		assertNull("The spawn must fail where it always did, not with a capability message.",
			spawnAs(VIEWER, "noSuchAgent"));
	}

	/** A skill's tools count towards what the child can reach, as they do for a delegate. */
	public void testSkillToolsCountTowardsReachability() {
		String refusal = spawnAs(CLERK, SCRIPTED);

		assertNotNull("Its skill's only tool is in a capability no role here holds.", refusal);
		assertTrue(refusal, refusal.contains(ToolGroups.SCRIPT));
	}

	/**
	 * An application that has configured no {@code <tool-access>} policy refuses nothing.
	 *
	 * <p>
	 * The most important test in this class: {@code LEGACY} is what every unmigrated application runs
	 * in, and a spawn there must behave exactly as it did before this filter existed. The graph fails
	 * the test if it is consulted at all, because an unenforced run must not even look.
	 * </p>
	 */
	public void testLegacyRunRefusesNothing() {
		AgentGraph forbidden = new AgentGraph() {
			@Override
			public AgentDefinition agent(String name) {
				throw new AssertionError("An unenforced run must not walk the agent graph.");
			}

			@Override
			public SkillDefinition skill(String name) {
				throw new AssertionError("An unenforced run must not walk the agent graph.");
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				throw new AssertionError("An unenforced run must not walk the agent graph.");
			}
		};
		ToolContext legacyParent = new ToolContext(new TopLogicSecurityContext(),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		AgentCapabilities capabilities = new AgentCapabilities(TestSpawnFilter::resolve,
			detachedChild(legacyParent).security(), forbidden);

		assertNull(SubagentSpawner.detachedSpawnRefusal(WRITER, capabilities));
	}

	// ===== Helpers =====

	/**
	 * The refusal a run of the given role gets when it spawns the named agent detached, or
	 * <code>null</code> if the spawn goes ahead.
	 *
	 * <p>
	 * Goes through {@link ToolContext#forDetachedSubagent} rather than restating what it does, so the
	 * agreement between the decision and the context the child actually gets is under test too.
	 * </p>
	 */
	private static String spawnAs(String role, String targetAgentName) {
		SecurityContext child = detachedChild(parentTurn(role)).security();
		return SubagentSpawner.detachedSpawnRefusal(targetAgentName, capabilities(child));
	}

	/** The detached child of the given parent turn, exactly as {@link SubagentSpawner} builds it. */
	private static ToolContext detachedChild(ToolContext parent) {
		return parent.forDetachedSubagent(TransactionScope.NOOP, new CancellationToken());
	}

	/**
	 * The context of an attended turn of the given role, auto-approving like a chat session whose user
	 * chose {@code AUTO_APPROVE}.
	 */
	private static ToolContext parentTurn(String role) {
		// No principal: the administrator bypass needs a Person, which cannot be created without a
		// KnowledgeBase. It is covered where it can be, in the SecDemo tests.
		ToolAuthorization authorization = new ToolAuthorization(
			new RoleToolAccess(fixturePolicy(), DeclaredGroups.NONE), null, Set.of(role), false);
		return new ToolContext(TopLogicSecurityContext.enforcedRun(authorization),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/**
	 * Two roles and an allowlist that carries only one of the two data capabilities, so the difference
	 * between a foreground turn and a detached child is observable rather than vacuous.
	 */
	private static ToolAccessPolicy fixturePolicy() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getToolAccess().put(CLERK, role(CLERK, ToolGroups.DATA_READ, ToolGroups.DATA_WRITE));
		policy.getToolAccess().put(VIEWER, role(VIEWER, ToolGroups.DATA_READ));

		BackgroundAllowlist allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
		allowlist.getGrants().add(group(ToolGroups.DATA_READ));
		policy.setBackgroundAllowlist(allowlist);
		return policy;
	}

	private static RoleAccess role(String name, String... groups) {
		RoleAccess access = TypedConfiguration.newConfigItem(RoleAccess.class);
		access.setName(name);
		for (String group : groups) {
			access.getGrants().add(group(group));
		}
		return access;
	}

	private static Grant group(String name) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setGroup(name);
		return grant;
	}

	/** The capability view of the given run over the test's agent graph and tool registry. */
	private static AgentCapabilities capabilities(SecurityContext security) {
		return new AgentCapabilities(TestSpawnFilter::resolve, security, graph());
	}

	private static AgentDefinition agent(String name) {
		AgentDefinition definition = graph().agent(name);
		assertNotNull("No such agent in the test graph: " + name, definition);
		return definition;
	}

	/** One agent per interesting shape, plus the skill one of them declares. */
	private static AgentGraph graph() {
		Map<String, AgentDefinition> agents = new LinkedHashMap<>();
		agents.put(WRITER, withTools(WRITER, WRITE_TOOL));
		agents.put(MIXED, withTools(MIXED, READ_TOOL, WRITE_TOOL));
		agents.put(READER, withTools(READER, READ_TOOL));
		agents.put(TOOLLESS, withTools(TOOLLESS));
		agents.put(SCRIPTED, new AgentDefinition(SCRIPTED, SCRIPTED, "", List.of(), List.of(),
			List.of("scripting"), List.of(), AgentSource.CONFIG));

		SkillDefinition scripting = new SkillDefinition("scripting", "scripting", "content",
			List.of(SCRIPT_TOOL), AgentSource.CONFIG);

		return new AgentGraph() {
			@Override
			public AgentDefinition agent(String name) {
				return agents.get(name);
			}

			@Override
			public SkillDefinition skill(String name) {
				return "scripting".equals(name) ? scripting : null;
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				return null;
			}
		};
	}

	private static AgentDefinition withTools(String name, String... tools) {
		return new AgentDefinition(name, name, "", List.of(tools), List.of(), List.of(), List.of(),
			AgentSource.CONFIG);
	}

	/** Resolves the three fixture tools by name, like the tool registry would. */
	private static Tool resolve(String name) {
		switch (name) {
			case READ_TOOL:
				return tool(READ_TOOL, ToolGroups.DATA_READ, ToolMode.READ);
			case WRITE_TOOL:
				return tool(WRITE_TOOL, ToolGroups.DATA_WRITE, ToolMode.DATA_WRITE);
			case SCRIPT_TOOL:
				return tool(SCRIPT_TOOL, ToolGroups.SCRIPT, ToolMode.READ);
			default:
				return null;
		}
	}

	/** A minimal {@link Tool} that carries a name, a declared group and a mode. */
	private static Tool tool(String name, String group, ToolMode mode) {
		return new Tool() {

			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return "Stub: " + name;
			}

			@Override
			public String group() {
				return group;
			}

			@Override
			public ToolMode mode() {
				return mode;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new UnsupportedOperationException("Not executed in this test.");
			}
		};
	}

}
