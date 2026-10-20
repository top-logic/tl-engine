/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentCapabilities.AgentGraph;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentRoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
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
 * <p>
 * Since #348 the synchronous side has a refusal of its own
 * ({@link DelegationTool#delegationRefusal(String, AgentCapabilities)}), asked of the parent turn's
 * own context rather than of a background-bounded one, and it is asserted here beside the spawn's so
 * the two cannot drift apart.
 * </p>
 *
 * <p>
 * Since #517 both are asserted against the <em>cause</em> as well: a refusal the capability filter did
 * not make must name no capability, because no capability grant answers it. That needs an
 * {@code <agent-access>} section, so those cases build the security context from a whole
 * {@link ToolAccessPolicy} rather than from a {@link ToolAuthorization} alone.
 * </p>
 *
 * @see SubagentSpawner#detachedSpawnRefusal(String, AgentCapabilities)
 * @see DelegationTool#delegationRefusal(String, AgentCapabilities)
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
	 * A synchronous delegation to that same agent still works: it keeps the parent's reach.
	 *
	 * <p>
	 * It completes inside the parent turn, where the human who could answer an approval prompt still is,
	 * so the allowlist takes nothing away from it that the parent had not lost already.
	 * </p>
	 */
	public void testSynchronousDelegationToTheSameAgentStillWorks() {
		SecurityContext delegate = parentTurn(CLERK).forDelegation(TransactionScope.NOOP).security();

		assertFalse("A synchronous delegate is attended.", delegate.isBackgroundRun());
		assertNull("It keeps the parent's data.write, so the agent can do its whole job.",
			capabilities(delegate).unusableReason(agent(WRITER)));
		assertNull("...and is therefore not refused either.", delegateAs(CLERK, WRITER));
	}

	// ===== The synchronous refusal (#348) =====

	/**
	 * A delegation to an agent every one of whose tools this run is denied is refused, by capability
	 * name - the same fail-closed answer the spawn path gives.
	 */
	public void testFullyDeniedAgentIsNotDelegatedTo() {
		String refusal = delegateAs(VIEWER, WRITER);

		assertNotNull("A delegate that could only come back empty-handed must not be started.", refusal);
		assertTrue("The user's answer is a capability grant, so the capability must be named: " + refusal,
			refusal.contains(ToolGroups.DATA_WRITE));
		assertTrue("The refusal must name the agent the model asked for: " + refusal,
			refusal.contains("'" + WRITER + "'"));
		assertTrue("The model must be told to stop rather than to try again: " + refusal,
			refusal.contains("again will not help"));
	}

	/** The refusal is an ordinary tool error and never suggests the agent is not there. */
	public void testDelegationRefusalDoesNotDenyTheAgentsExistence() {
		String refusal = delegateAs(VIEWER, WRITER);

		assertFalse(refusal, refusal.toLowerCase().contains("does not exist"));
		assertFalse(refusal, refusal.toLowerCase().contains("unknown agent"));
		assertFalse(refusal, refusal.toLowerCase().contains("not found"));
	}

	/**
	 * It says nothing about being unattended, because a synchronous delegate is not.
	 *
	 * <p>
	 * The spawn's wording exists to stop a user being sent to an administrator for a right they hold.
	 * Repeating it here would be the same mistake in the other direction: the allowlist is not what
	 * withheld anything from an attended run.
	 * </p>
	 */
	public void testDelegationRefusalNeverBlamesTheBackgroundAllowlist() {
		String refusal = delegateAs(VIEWER, WRITER);

		assertFalse(refusal, refusal.contains("background allowlist"));
		assertFalse(refusal, refusal.contains("unattended"));
	}

	/** One reachable tool is enough, and an agent that reaches nothing is not a denial. */
	public void testPartlyReachableAgentIsDelegatedTo() {
		assertNull("The read half of its tools is permitted, so the delegate has work it can do.",
			delegateAs(VIEWER, MIXED));
		assertNull(delegateAs(VIEWER, READER));
		assertNull("All zero of its tools are denied is vacuously true.",
			delegateAs(VIEWER, TOOLLESS));
	}

	/** An agent that does not exist is not refused here either: that is not a denial. */
	public void testUnknownDelegateIsNotRefusedHere() {
		assertNull("The delegation must fail where it always did, not with a capability message.",
			delegateAs(VIEWER, "noSuchAgent"));
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

	// ===== The agent-access refusal (#517) =====

	/**
	 * A spawn the {@code <agent-access>} section refuses says so, and names no capability.
	 *
	 * <p>
	 * The clerk reaches a tool of {@link #MIXED} even detached - {@code data.read} is on the allowlist
	 * - so the capability filter refuses nothing here and every capability this run lacks is beside
	 * the point. Naming one would send the user to their administrator for a grant that leaves the
	 * spawn exactly where it is: the agent is withheld from the role, and only an agent grant lifts
	 * that.
	 * </p>
	 */
	public void testAgentAccessRefusalNamesTheAgentGrantAndNoCapability() {
		assertNull("Precondition: nothing but <agent-access> refuses this spawn.",
			spawnAs(CLERK, MIXED));

		String refusal = spawnUnder(agentAccess(CLERK, READER), CLERK, MIXED);

		assertNotNull("An agent this role may not run must not be spawned.", refusal);
		assertTrue("The refusal must name the agent the model asked for: " + refusal,
			refusal.contains("'" + MIXED + "'"));
		assertTrue("...and say that the roles are what withhold it: " + refusal,
			refusal.contains("permitted by none of your roles"));
		assertTrue("...and send the user after an agent grant: " + refusal,
			refusal.contains("no capability grant does"));
		assertNoCapabilityIsBlamed(refusal);
	}

	/** The synchronous path had the same defect and gets the same answer. */
	public void testAgentAccessDelegationRefusalNamesNoCapabilityEither() {
		assertNull("Precondition: nothing but <agent-access> refuses this delegation.",
			delegateAs(CLERK, MIXED));

		String refusal = delegateUnder(agentAccess(CLERK, READER), CLERK, MIXED);

		assertNotNull("An agent this role may not run must not be delegated to.", refusal);
		assertTrue("The refusal must name the agent the model asked for: " + refusal,
			refusal.contains("'" + MIXED + "'"));
		assertTrue("...and say that the roles are what withhold it: " + refusal,
			refusal.contains("permitted by none of your roles"));
		assertNoCapabilityIsBlamed(refusal);
	}

	/** An agent the section does grant is spawned, so the section is a filter and not a wall. */
	public void testAgentAccessRefusesOnlyWhatItWithholds() {
		assertNull("The section grants this one to the clerk's role.",
			spawnUnder(agentAccess(CLERK, READER), CLERK, READER));
		assertNull(delegateUnder(agentAccess(CLERK, READER), CLERK, READER));
	}

	/**
	 * The capability case keeps its capability list: the two refusals stay different messages.
	 *
	 * <p>
	 * The other half of #517. A message that named no capability anywhere would be just as wrong -
	 * where the capability filter <em>is</em> what refused, the capability is the one thing the user
	 * can act on.
	 * </p>
	 */
	public void testTheCapabilityRefusalStillNamesItsCapabilities() {
		String refusal = spawnUnder(agentAccess(VIEWER, WRITER), VIEWER, WRITER);

		assertNotNull("The viewer holds no data.write, so the agent can do nothing for them.", refusal);
		assertTrue("Its role is granted the agent, so only the capability is left to say: " + refusal,
			refusal.contains(ToolGroups.DATA_WRITE));
		assertTrue(refusal, refusal.contains("does not hold"));
	}

	/** That a refusal blames no capability, in none of the ways this message could. */
	private static void assertNoCapabilityIsBlamed(String refusal) {
		assertFalse("A capability grant would change nothing here: " + refusal,
			refusal.contains(ToolGroups.DATA_WRITE));
		assertFalse(refusal, refusal.contains(ToolGroups.DATA_READ));
		assertFalse("Nothing may suggest the user is missing a right: " + refusal,
			refusal.contains("does not hold"));
		assertFalse("...nor that being unattended is what did it: " + refusal,
			refusal.contains("background allowlist"));
		assertTrue("The model must be told to stop rather than to try again: " + refusal,
			refusal.contains("again will not help"));
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

	/**
	 * The refusal a run of the given role gets when it delegates to the named agent synchronously, or
	 * <code>null</code> if the delegation goes ahead.
	 *
	 * <p>
	 * Asked of the context {@code DelegationTool} builds for the child, which is the parent turn's own
	 * - that is exactly what makes the contrast with {@link #spawnAs} the contrast between attended
	 * and unattended, and nothing else.
	 * </p>
	 */
	private static String delegateAs(String role, String targetAgentName) {
		SecurityContext child = parentTurn(role).forDelegation(TransactionScope.NOOP).security();
		return DelegationTool.delegationRefusal(targetAgentName, capabilities(child));
	}

	/**
	 * {@link #spawnAs} under a policy that also carries an {@code <agent-access>} section.
	 *
	 * @param policy
	 *        The whole deployment policy, tool access and agent access alike - built through
	 *        {@link TopLogicSecurityContext#forRun(ToolAccessPolicy, Person, Set)} so that both
	 *        dimensions come from the one policy, as they do in a real run.
	 */
	private static String spawnUnder(ToolAccessPolicy policy, String role, String targetAgentName) {
		SecurityContext child = detachedChild(parentTurnUnder(policy, role)).security();
		return SubagentSpawner.detachedSpawnRefusal(targetAgentName, capabilities(child));
	}

	/** {@link #delegateAs} under a policy that also carries an {@code <agent-access>} section. */
	private static String delegateUnder(ToolAccessPolicy policy, String role, String targetAgentName) {
		SecurityContext child =
			parentTurnUnder(policy, role).forDelegation(TransactionScope.NOOP).security();
		return DelegationTool.delegationRefusal(targetAgentName, capabilities(child));
	}

	private static ToolContext parentTurnUnder(ToolAccessPolicy policy, String role) {
		return new ToolContext(TopLogicSecurityContext.forRun(policy, null, Set.of(role)),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/**
	 * The fixture policy with an {@code <agent-access>} section granting the role exactly the named
	 * agents.
	 *
	 * <p>
	 * A role the section names and does not grant an agent reaches none of the others: that is what
	 * makes the refusal here the agent's own rather than a capability's.
	 * </p>
	 */
	private static ToolAccessPolicy agentAccess(String role, String... grantedAgents) {
		ToolAccessPolicy policy = fixturePolicy();
		AgentRoleAccess access = TypedConfiguration.newConfigItem(AgentRoleAccess.class);
		access.setName(role);
		AgentGrant grant = TypedConfiguration.newConfigItem(AgentGrant.class);
		grant.setAgents(List.of(grantedAgents));
		access.getGrants().add(grant);
		policy.getAgentAccess().getRoles().put(role, access);
		return policy;
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
		policy.setBaseline(noBaseline());
		return policy;
	}

	/**
	 * An explicitly empty baseline, so these tests see only the role grants they configure.
	 *
	 * <p>
	 * An <em>absent</em> {@code <baseline>} would mean {@link RoleToolAccess#DEFAULT_BASELINE} - the
	 * read-only default of #293 - and would grant every capability in it to every persona here.
	 * </p>
	 */
	private static Baseline noBaseline() {
		return TypedConfiguration.newConfigItem(Baseline.class);
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
		agents.put(SCRIPTED, new AgentDefinition(SCRIPTED, null, SCRIPTED, null, "", List.of(), List.of(),
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
		return new AgentDefinition(name, null, name, null, "", List.of(tools), List.of(), List.of(), List.of(),
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
