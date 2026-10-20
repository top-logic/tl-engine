/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentCapabilities.AgentGraph;
import com.top_logic.tl.ai.core.agent.AgentCapabilities.Unusable;
import com.top_logic.tl.ai.core.security.DeclaredTypeAccess;
import com.top_logic.tl.ai.core.security.DeclaredTypeAccess.Verdict;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;

/**
 * Tests that {@link AgentCapabilities} applies the declared-type rule of
 * {@link DeclaredTypeAccess}, and says so in the language of types rather than of capabilities
 * (follow-up of issue #258).
 *
 * <p>
 * Issue #258 wired {@link ToolMethod#types()} into {@link ToolPermissionFilter}, which is what the
 * provider request and the invocation gate ask. {@link AgentCapabilities} - what {@code listTools},
 * the skill catalog, {@code loadSkill} and the delegate filter ask - kept asking about capabilities
 * alone, so it advertised tools the filter drops and, where it did refuse, named a capability the
 * acting user already holds. That is the wrong-remedy defect #517 fixed for withheld agents, one
 * layer down.
 * </p>
 *
 * <p>
 * The whole fixture is a hand-written
 * {@link com.top_logic.tl.ai.core.security.DeclaredTypeAccess.TypeReadAccess}, so nothing here
 * boots an application model: {@code myapp:Customer} is out of reach and {@code myapp:Order} is
 * not. What the real model rights answer is {@code SecDemoDeclaredTypesTest}'s question.
 * </p>
 */
public class TestCapabilityDeclaredTypes extends TestCase {

	/** Readable for nobody in this fixture. */
	private static final String WITHHELD_TYPE = "myapp:Customer";

	/** Readable for everybody in this fixture. */
	private static final String READABLE_TYPE = "myapp:Order";

	/** {@code data.read}, declared for the type nobody may read. */
	private static final String CUSTOMER_TOOL = "data.listCustomers";

	/** {@code data.read}, declared for the type everybody may read. */
	private static final String ORDER_TOOL = "data.listOrders";

	/** {@code data.write}, declaring nothing - the capability-denied tool of the mixed cases. */
	private static final String WRITE_TOOL = "data.createInstance";

	/** {@code data.write} <em>and</em> declared for the withheld type - both gates would refuse it. */
	private static final String WRITE_CUSTOMER_TOOL = "data.createCustomer";

	/** Carries {@link #CUSTOMER_TOOL} and nothing else. */
	private static final String CUSTOMER_AGENT = "customerAgent";

	/** Carries {@link #ORDER_TOOL} and nothing else. */
	private static final String ORDER_AGENT = "orderAgent";

	/** Carries a type-withheld tool and a capability-denied one - the mixed case. */
	private static final String BOTH_AGENT = "bothAgent";

	/** Delegates to all three of the above. */
	private static final String PARENT_AGENT = "parent";

	// ===== The tool itself =====

	/**
	 * A tool whose declared types are out of reach is not usable, and is not explained by a
	 * capability.
	 *
	 * <p>
	 * The consequence for {@code listTools}, which asks exactly these two methods per entry: the tool
	 * moves from "callable right now" to the withheld list, with a reason the user can act on.
	 * </p>
	 */
	public void testTypeWithheldToolIsNotPermitted() {
		AgentCapabilities capabilities = capabilities();

		assertFalse("The schema filter drops it, so the catalog must not advertise it.",
			capabilities.permits(tool(CUSTOMER_TOOL)));
		String reason = capabilities.unusableReason(tool(CUSTOMER_TOOL));
		assertNotNull(reason);
		assertTrue("The type is the remedy, so it has to be named: " + reason,
			reason.contains(WITHHELD_TYPE));
		assertFalse("The user holds data.read; naming it would send them after a grant they have: "
			+ reason, reason.contains(ToolGroups.DATA_READ));
	}

	/** The same tool for a readable declared type: usable, and unchanged by this fix. */
	public void testReadableDeclaredTypeKeepsTheTool() {
		AgentCapabilities capabilities = capabilities();

		assertTrue(capabilities.permits(tool(ORDER_TOOL)));
		assertNull(capabilities.unusableReason(tool(ORDER_TOOL)));
	}

	/**
	 * The capability gate answers before the declared types do, exactly as in
	 * {@link ToolPermissionFilter#denialReason(Tool)}.
	 *
	 * <p>
	 * A user who may not use the tool at all has to be sent after the capability. Naming the type
	 * would point at a Read grant that would still leave the tool unreachable.
	 * </p>
	 */
	public void testTheCapabilityAnswersBeforeTheType() {
		String reason = capabilities().unusableReason(tool(WRITE_CUSTOMER_TOOL));

		assertNotNull(reason);
		assertTrue("The capability decided, so it is what the message names: " + reason,
			reason.contains(ToolGroups.DATA_WRITE));
		assertFalse("The type must not be named where the capability decided: " + reason,
			reason.contains(WITHHELD_TYPE));
	}

	// ===== Skills =====

	/** A skill whose only tool is type-withheld is unusable, and names the type rather than a grant. */
	public void testTypeWithheldSkillIsUnusable() {
		AgentCapabilities capabilities = capabilities();
		SkillDefinition skill = skill("customers", CUSTOMER_TOOL);

		Unusable unusable = capabilities.unusableReason(skill);
		assertNotNull("A skill that would hand over nothing must not count as usable.", unusable);
		assertEquals(Unusable.Cause.TYPE_UNREADABLE, unusable.cause());
		assertFalse("No caller may list capabilities for this.", unusable.aboutCapabilities());
		assertTrue(unusable.message(), unusable.message().contains(WITHHELD_TYPE));
		assertEquals("The capability its tool costs is one this user holds.",
			Set.of(), capabilities.lacking(skill.tools()));
	}

	/** One tool on a readable type keeps the skill: its instructions still apply to that tool. */
	public void testSkillWithOneReachableToolSurvives() {
		assertNull(capabilities().unusableReason(skill("mixed", CUSTOMER_TOOL, ORDER_TOOL)));
	}

	/** The generated skill catalog leaves the type-withheld skill out and keeps the other. */
	public void testSkillCatalogDropsTheTypeWithheldSkill() {
		Map<String, SkillDefinition> skills = new LinkedHashMap<>();
		skills.put("customers", skill("customers", CUSTOMER_TOOL));
		skills.put("orders", skill("orders", ORDER_TOOL));

		String catalog = AgentFactory.skillCatalog(CUSTOMER_AGENT,
			List.of("customers", "orders"), List.of(ORDER_TOOL), skills::get, capabilities());

		assertFalse("A skill that would hand over nothing is not offered: " + catalog,
			catalog.contains("`customers`"));
		assertTrue(catalog, catalog.contains("`orders`"));
	}

	/**
	 * {@code loadSkill} refusing a type-withheld skill says what is missing and names no capability.
	 */
	public void testSkillAbsenceReasonNamesTheTypeAndNoCapability() {
		Map<String, SkillDefinition> skills = Map.of("customers", skill("customers", CUSTOMER_TOOL));
		AgentCapabilities capabilities = capabilities(graph(skills));

		String reason = capabilities.skillAbsenceReason("customers", List.of("customers"));

		assertNotNull(reason);
		assertTrue(reason, reason.contains(WITHHELD_TYPE));
		assertFalse("The account holds data.read - naming it would be the #517 defect again: " + reason,
			reason.contains(ToolGroups.DATA_READ));
		assertTrue("The model must not fill the gap with a capability of its own: " + reason,
			reason.contains(AgentCapabilities.NO_CAPABILITY_GUESSING));
	}

	// ===== Agents =====

	/** An agent whose only reachable tool is type-withheld is refused, and no capability is named. */
	public void testTypeWithheldAgentIsRefusedWithoutACapability() {
		AgentCapabilities capabilities = capabilities();

		Unusable unusable = capabilities.unusableReason(agent(CUSTOMER_AGENT));
		assertNotNull(unusable);
		assertEquals(Unusable.Cause.TYPE_UNREADABLE, unusable.cause());
		assertFalse(unusable.aboutCapabilities());
		assertTrue(unusable.message(), unusable.message().contains(WITHHELD_TYPE));
		assertEquals("A capability the user holds is not what this delegate is short of.",
			Set.of(), capabilities.unreachableCapabilities(agent(CUSTOMER_AGENT)));
	}

	/** An agent on a readable declared type is offered, as it was before this fix. */
	public void testAgentOnAReadableTypeIsOffered() {
		assertNull(capabilities().unusableReason(agent(ORDER_AGENT)));
		assertEquals("Only the delegate on the readable type survives: the other two reach nothing.",
			List.of(ORDER_AGENT), AgentFactory.offeredDelegates(agent(PARENT_AGENT), capabilities()));
	}

	/**
	 * A mixed delegate stays a capability question.
	 *
	 * <p>
	 * One of its tools is refused by a capability, so granting that capability really does make the
	 * delegate useful - which is what {@link Unusable#aboutCapabilities()} is for. The types are said
	 * as well, so that the grant is not expected to reach the other tool too.
	 * </p>
	 */
	public void testMixedDenialStaysACapabilityQuestion() {
		Unusable unusable = capabilities().unusableReason(agent(BOTH_AGENT));
		assertNotNull(unusable);
		assertEquals(Unusable.Cause.NO_REACHABLE_TOOL, unusable.cause());
		assertTrue(unusable.aboutCapabilities());
		assertTrue(unusable.message(), unusable.message().contains(ToolGroups.DATA_WRITE));
		assertTrue("The type is said too, so the grant is not expected to reach both tools: "
			+ unusable.message(), unusable.message().contains(WITHHELD_TYPE));
	}

	/** The spawn refusal names the type and no capability, and does not suggest a role grant. */
	public void testSpawnRefusalNamesTheTypeAndNoCapability() {
		String refusal = SubagentSpawner.detachedSpawnRefusal(CUSTOMER_AGENT, capabilities());

		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains(WITHHELD_TYPE));
		assertFalse("Listing capabilities here is the #517 defect: " + refusal,
			refusal.contains(ToolGroups.DATA_READ));
	}

	/** The synchronous twin answers the same way. */
	public void testDelegationRefusalNamesTheTypeAndNoCapability() {
		String refusal = DelegationTool.delegationRefusal(CUSTOMER_AGENT, capabilities());

		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains(WITHHELD_TYPE));
		assertFalse("Listing capabilities here is the #517 defect: " + refusal,
			refusal.contains(ToolGroups.DATA_READ));
	}

	// ===== Helpers =====

	/** The capability view of a principal who holds {@code data.read} and nothing else. */
	private static AgentCapabilities capabilities() {
		return capabilities(graph(Map.of()));
	}

	private static AgentCapabilities capabilities(AgentGraph graph) {
		return capabilities(Set.of(CUSTOMER_TOOL, ORDER_TOOL), graph);
	}

	private static AgentCapabilities capabilities(Set<String> permittedTools, AgentGraph graph) {
		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return permittedTools.contains(tool.name());
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		return new AgentCapabilities(TestCapabilityDeclaredTypes::tool, security, graph, typeAccess());
	}

	/** {@link #WITHHELD_TYPE} is out of reach, every other name is readable. */
	private static DeclaredTypeAccess typeAccess() {
		return new DeclaredTypeAccess(
			name -> WITHHELD_TYPE.equals(name) ? Verdict.UNREADABLE : Verdict.READABLE);
	}

	/** The four tools of this fixture, or <code>null</code> for a name nobody registered. */
	private static Tool tool(String name) {
		Map<String, Tool> registry = new HashMap<>();
		registry.put(CUSTOMER_TOOL, declared(CUSTOMER_TOOL, ToolGroups.DATA_READ, WITHHELD_TYPE));
		registry.put(ORDER_TOOL, declared(ORDER_TOOL, ToolGroups.DATA_READ, READABLE_TYPE));
		registry.put(WRITE_TOOL, declared(WRITE_TOOL, ToolGroups.DATA_WRITE));
		registry.put(WRITE_CUSTOMER_TOOL, declared(WRITE_CUSTOMER_TOOL, ToolGroups.DATA_WRITE, WITHHELD_TYPE));
		return registry.get(name);
	}

	private static Tool declared(String name, String group, String... types) {
		return ToolStubs.builder(name).group(group).types(List.of(types)).build();
	}

	private static AgentDefinition agent(String name) {
		AgentDefinition definition = graph(Map.of()).agent(name);
		assertNotNull("No such agent in the test graph: " + name, definition);
		return definition;
	}

	/** The four agents of this fixture, over the given skills. */
	private static AgentGraph graph(Map<String, SkillDefinition> skills) {
		Map<String, AgentDefinition> agents = new LinkedHashMap<>();
		agents.put(CUSTOMER_AGENT, withTools(CUSTOMER_AGENT, CUSTOMER_TOOL));
		agents.put(ORDER_AGENT, withTools(ORDER_AGENT, ORDER_TOOL));
		agents.put(BOTH_AGENT, withTools(BOTH_AGENT, CUSTOMER_TOOL, WRITE_TOOL));
		agents.put(PARENT_AGENT, delegating(PARENT_AGENT, CUSTOMER_AGENT, ORDER_AGENT, BOTH_AGENT));

		return new AgentGraph() {

			@Override
			public AgentDefinition agent(String name) {
				return agents.get(name);
			}

			@Override
			public SkillDefinition skill(String name) {
				return skills.get(name);
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				return null;
			}
		};
	}

	private static AgentDefinition withTools(String name, String... tools) {
		return new AgentDefinition(name, null, name, null, "", List.of(tools), List.of(), List.of(),
			List.of(), AgentSource.CONFIG);
	}

	private static AgentDefinition delegating(String name, String... delegations) {
		return new AgentDefinition(name, null, name, null, "", List.of(), List.of(), List.of(),
			List.of(delegations), AgentSource.CONFIG);
	}

	private static SkillDefinition skill(String name, String... tools) {
		return new SkillDefinition(name, name, "content of " + name, List.of(tools), AgentSource.CONFIG);
	}

}
