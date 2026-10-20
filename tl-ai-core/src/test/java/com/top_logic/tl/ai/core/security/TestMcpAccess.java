/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import static com.top_logic.tl.ai.core.security.TestAgentAccess.*;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpRoleAccess;

/**
 * Tests for the MCP half of {@link RoleAccessPolicy}: which servers a role may attach to a single
 * request.
 *
 * <p>
 * The rules a role entry follows are the agent dimension's and are tested there
 * ({@link TestAgentAccess}). What is tested here is what this dimension adds: the default that lets a
 * request reach the agent's own servers and no others.
 * </p>
 */
public class TestMcpAccess extends TestCase {

	/** @see TestAgentAccess#suite() */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestMcpAccess.class)));
	}

	/** With nothing configured, a request reaches the servers its agent declares and no others. */
	public void testDefaultIsAgent() {
		RoleAccessPolicy access = mcp(TypedConfiguration.newConfigItem(ToolAccessPolicy.class));

		assertGranted(access.check(Set.of("Clerk"), "internal-docs", Set.of("internal-docs")),
			RoleAccessPolicy.AGENT_ROLE);
		assertDenied(access.check(Set.of("Clerk"), "jira", Set.of("internal-docs")),
			Cause.NOT_THE_AGENTS);
	}

	/** The section set to "all" is what an application that lets users attach anything writes. */
	public void testDefaultAll() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getMcpAccess().setDefault(McpDefault.ALL);

		assertGranted(mcp(policy).check(Set.of("Clerk"), "jira", Set.of()),
			RoleAccessPolicy.DEFAULT_ROLE);
	}

	/** A named role may attach what it is permitted, and nothing else. */
	public void testNamedRoleIsAnAllowlist() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getMcpAccess().setDefault(McpDefault.ALL);
		put(policy, role("ticketReader", List.of("internal-docs"), List.of()));
		RoleAccessPolicy access = mcp(policy);

		assertGranted(access.check(Set.of("ticketReader"), "internal-docs", Set.of()), "ticketReader");
		assertDenied(access.check(Set.of("ticketReader"), "jira", Set.of()), Cause.NOT_GRANTED);
	}

	/** A refusal by name takes a server away from the agent that declares it. */
	public void testDenyBeatsTheAgentsOwnServer() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of(), List.of("internal-docs")));

		assertDenied(mcp(policy).check(Set.of("ticketReader"), "internal-docs", Set.of("internal-docs")),
			Cause.ROLE_DENY);
	}

	/** What a client is offered under each of the three defaults. */
	public void testWhatIsOffered() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		List<String> configured = List.of("internal-docs", "jira");
		Set<String> declared = Set.of("internal-docs");

		assertEquals(List.of("internal-docs"), mcp(policy).allowed(Set.of("Clerk"), configured, declared));

		policy.getMcpAccess().setDefault(McpDefault.ALL);
		assertEquals(configured, mcp(policy).allowed(Set.of("Clerk"), configured, declared));

		policy.getMcpAccess().setDefault(McpDefault.NONE);
		assertEquals(List.of(), mcp(policy).allowed(Set.of("Clerk"), configured, declared));
	}

	/**
	 * A denied server the agent <em>declares</em> refuses the run, however silent the request is.
	 *
	 * <p>
	 * The counterpart of the deny rule: it answers only when it is asked, and a client that does not
	 * override the servers names none. The agent attaches them anyway, so the run has to ask on its
	 * own behalf - see {@code AgentFactory.refusalFor}.
	 * </p>
	 */
	public void testADeniedDeclaredServerRefusesTheRunItself() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of(), List.of("jira")));
		RunAuthorization access =
			new RunAuthorization(null, null, mcp(policy), null, Set.of("ticketReader"));
		Set<String> declared = Set.of("internal-docs", "jira");

		assertNull("A declared server no role forbids stays reachable.",
			access.declaredServerRefusal("internal-docs", declared));

		Decision.Denied denied = access.declaredServerRefusal("jira", declared);
		assertNotNull("A run that would attach a denied server must not start.", denied);
		assertEquals("It is not a choice of the user's that was turned down.",
			Cause.AGENTS_OWN_DENIED, denied.cause());
		assertNotNull("The refusal has to be showable to the user.", denied.reason());

		assertFalse("Such an agent cannot run for this account, so no menu may offer it.",
			access.allowsAgentsOwn(null, declared));
		assertTrue("An agent that declares only permitted servers still runs.",
			access.allowsAgentsOwn(null, Set.of("internal-docs")));
	}

	private static RoleAccessPolicy mcp(ToolAccessPolicy policy) {
		return RoleAccessPolicy.mcpServers(policy);
	}

	private static void put(ToolAccessPolicy policy, McpRoleAccess role) {
		policy.getMcpAccess().getRoles().put(role.getName(), role);
	}

	private static McpRoleAccess role(String name, List<String> granted, List<String> denied) {
		McpRoleAccess role = TypedConfiguration.newConfigItem(McpRoleAccess.class);
		role.setName(name);
		if (!granted.isEmpty()) {
			role.getGrants().add(entry(granted));
		}
		if (!denied.isEmpty()) {
			role.getDenies().add(entry(denied));
		}
		return role;
	}

	private static McpGrant entry(List<String> names) {
		McpGrant grant = TypedConfiguration.newConfigItem(McpGrant.class);
		grant.setServers(names);
		return grant;
	}
}
