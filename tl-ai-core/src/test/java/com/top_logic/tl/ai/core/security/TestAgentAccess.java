/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentRoleAccess;

/**
 * Tests for the agent half of {@link RoleAccessPolicy}: which agents a role may run.
 *
 * <p>
 * One test per rule of the developer article {@code ai/security/run-access}. The other two
 * dimensions share the evaluator and differ only in their default and in what the agent itself
 * contributes - see {@link TestModelAccess} and {@link TestMcpAccess}.
 * </p>
 */
public class TestAgentAccess extends TestCase {

	/**
	 * The typed configuration items resolve nested config types through the {@code TypeIndex}, which
	 * needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAgentAccess.class)));
	}

	/** With nothing configured, everybody runs every agent - today's behaviour. */
	public void testDefaultIsAll() {
		RoleAccessPolicy access = agents(TypedConfiguration.newConfigItem(ToolAccessPolicy.class));

		assertGranted(access.check(Set.of("Clerk"), "TL_ModelEditor", Set.of()),
			RoleAccessPolicy.DEFAULT_ROLE);
		assertGranted(access.check(Set.of(), "TL_ModelEditor", Set.of()),
			RoleAccessPolicy.DEFAULT_ROLE);
	}

	/** A section set to "none" lets nobody run anything but what a role entry permits. */
	public void testDefaultNone() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getAgentAccess().setDefault(AgentDefault.NONE);
		put(policy, role("Clerk", List.of("TL_ModelExplorer"), List.of()));

		assertGranted(agents(policy).check(Set.of("Clerk"), "TL_ModelExplorer", Set.of()), "Clerk");
		assertDenied(agents(policy).check(Set.of("Stranger"), "TL_ModelExplorer", Set.of()),
			Cause.NOT_GRANTED);
	}

	/** A role that is named may run what it is permitted, whatever the default says. */
	public void testNamedRoleIsAnAllowlist() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of("TL_ModelExplorer"), List.of()));
		RoleAccessPolicy access = agents(policy);

		assertGranted(access.check(Set.of("ticketReader"), "TL_ModelExplorer", Set.of()), "ticketReader");
		assertDenied(access.check(Set.of("ticketReader"), "TL_SingleAgent", Set.of()), Cause.NOT_GRANTED);
		// Everybody else keeps what the default gives, which is the point of restricting one role in a
		// running application.
		assertGranted(access.check(Set.of("projectPartner"), "TL_SingleAgent", Set.of()),
			RoleAccessPolicy.DEFAULT_ROLE);
	}

	/** A role entry that permits nothing lets its holders run no agent at all. */
	public void testEmptyEntryDeniesEverything() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of(), List.of()));

		assertDenied(agents(policy).check(Set.of("ticketReader"), "TL_ModelExplorer", Set.of()),
			Cause.NOT_GRANTED);
	}

	/** A refusal wins over every permission of every other role the user holds. */
	public void testDenyWins() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("Clerk", List.of("TL_ModelEditor"), List.of()));
		put(policy, role("Guest", List.of(), List.of("TL_ModelEditor")));

		Decision decision = agents(policy).check(Set.of("Clerk", "Guest"), "TL_ModelEditor", Set.of());

		assertDenied(decision, Cause.ROLE_DENY);
		assertEquals("Guest", ((Decision.Denied) decision).byRole());
	}

	/** The grants of all roles a user holds are unioned. */
	public void testGrantsOfSeveralRolesAreUnioned() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("Clerk", List.of("TL_ModelExplorer"), List.of()));
		put(policy, role("Author", List.of("TL_ModelEditor"), List.of()));
		RoleAccessPolicy access = agents(policy);

		assertGranted(access.check(Set.of("Clerk", "Author"), "TL_ModelExplorer", Set.of()), "Clerk");
		assertGranted(access.check(Set.of("Clerk", "Author"), "TL_ModelEditor", Set.of()), "Author");
	}

	/** Listing what a role may run answers with the permitted candidates only. */
	public void testAllowed() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of("TL_ModelExplorer"), List.of()));

		assertEquals(List.of("TL_ModelExplorer"), agents(policy).allowed(Set.of("ticketReader"),
			List.of("TL_SingleAgent", "TL_ModelExplorer", "TL_ModelEditor"), Set.of()));
	}

	/** Every name a section mentions is reported, which is what the boot check validates. */
	public void testMentionedNames() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("Clerk", List.of("TL_ModelExplorer"), List.of("TL_ModelEditor")));

		assertEquals(Set.of("TL_ModelEditor", "TL_ModelExplorer"), agents(policy).mentionedNames());
	}

	/**
	 * The XML of the ticket parses into the sections this class evaluates.
	 *
	 * <p>
	 * The shape an administrator writes is part of the contract - it appears in the documentation and
	 * in the {@code secdemo} fixture - so it is asserted rather than only exercised by a running
	 * application.
	 * </p>
	 */
	public void testConfigurationSyntax() throws Exception {
		String xml = "<policy config:interface='" + ToolAccessPolicy.class.getName() + "'"
			+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <agent-access default='none'>"
			+ "    <role name='SalesUser'>"
			+ "      <grants><grant agents='sales-assistant, pietro'/></grants>"
			+ "      <denies><deny agents='framework-wiring-agent'/></denies>"
			+ "    </role>"
			+ "  </agent-access>"
			+ "  <model-access default='all'/>"
			+ "  <mcp-access default='none'/>"
			+ "</policy>";

		ConfigurationItem parsed = TypedConfiguration.parse(CharacterContents.newContent(xml));
		ToolAccessPolicy policy = (ToolAccessPolicy) parsed;

		assertEquals(AgentDefault.NONE, policy.getAgentAccess().getDefault());
		assertEquals(ToolAccessPolicy.ModelDefault.ALL, policy.getModelAccess().getDefault());
		assertEquals(ToolAccessPolicy.McpDefault.NONE, policy.getMcpAccess().getDefault());

		RoleAccessPolicy access = agents(policy);
		assertGranted(access.check(Set.of("SalesUser"), "pietro", Set.of()), "SalesUser");
		assertDenied(access.check(Set.of("SalesUser"), "framework-wiring-agent", Set.of()),
			Cause.ROLE_DENY);
		assertDenied(access.check(Set.of("SalesUser"), "TL_SingleAgent", Set.of()), Cause.NOT_GRANTED);
	}

	/** An unconfigured section says the default out loud rather than leaving it to be guessed. */
	public void testDefaultsOfAnEmptyPolicy() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);

		assertEquals(AgentDefault.ALL, policy.getAgentAccess().getDefault());
		assertEquals(ToolAccessPolicy.ModelDefault.ASSIGNED, policy.getModelAccess().getDefault());
		assertEquals(ToolAccessPolicy.McpDefault.AGENT, policy.getMcpAccess().getDefault());
	}

	static void assertGranted(Decision decision, String byRole) {
		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals(byRole, ((Decision.Granted) decision).byRole());
	}

	static void assertDenied(Decision decision, Cause cause) {
		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
		assertEquals(cause, ((Decision.Denied) decision).cause());
	}

	private static RoleAccessPolicy agents(ToolAccessPolicy policy) {
		return RoleAccessPolicy.agents(policy);
	}

	private static void put(ToolAccessPolicy policy, AgentRoleAccess role) {
		policy.getAgentAccess().getRoles().put(role.getName(), role);
	}

	private static AgentRoleAccess role(String name, List<String> granted, List<String> denied) {
		AgentRoleAccess role = TypedConfiguration.newConfigItem(AgentRoleAccess.class);
		role.setName(name);
		if (!granted.isEmpty()) {
			role.getGrants().add(entry(granted));
		}
		if (!denied.isEmpty()) {
			role.getDenies().add(entry(denied));
		}
		return role;
	}

	private static AgentGrant entry(List<String> names) {
		AgentGrant grant = TypedConfiguration.newConfigItem(AgentGrant.class);
		grant.setAgents(names);
		return grant;
	}
}
