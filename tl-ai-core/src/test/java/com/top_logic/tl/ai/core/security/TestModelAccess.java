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
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelRoleAccess;

/**
 * Tests for the model half of {@link RoleAccessPolicy}: which models a role may choose for a single
 * request.
 *
 * <p>
 * The rules a role entry follows are the agent dimension's and are tested there
 * ({@link TestAgentAccess}). What is tested here is what this dimension adds: the default that lets
 * nobody choose a model, and the model an agent is assigned, which is not a choice.
 * </p>
 */
public class TestModelAccess extends TestCase {

	/** @see TestAgentAccess#suite() */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestModelAccess.class)));
	}

	/** With nothing configured, a request runs on the model its agent is assigned. */
	public void testDefaultIsAssigned() {
		RoleAccessPolicy access = models(TypedConfiguration.newConfigItem(ToolAccessPolicy.class));

		assertGranted(access.check(Set.of("Clerk"), "claude-haiku-4-5", Set.of("claude-haiku-4-5")),
			RoleAccessPolicy.AGENT_ROLE);
		assertDenied(access.check(Set.of("Clerk"), "claude-sonnet-4-6", Set.of("claude-haiku-4-5")),
			Cause.NOT_THE_AGENTS);
	}

	/** The section set to "all" is what an application that wants a free model menu writes. */
	public void testDefaultAll() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getModelAccess().setDefault(ModelDefault.ALL);

		assertGranted(models(policy).check(Set.of("Clerk"), "claude-sonnet-4-6", Set.of()),
			RoleAccessPolicy.DEFAULT_ROLE);
	}

	/** A named role may choose what it is permitted, and nothing else. */
	public void testNamedRoleIsAnAllowlist() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.getModelAccess().setDefault(ModelDefault.ALL);
		put(policy, role("ticketReader", List.of("claude-haiku-4-5"), List.of()));
		RoleAccessPolicy access = models(policy);

		assertGranted(access.check(Set.of("ticketReader"), "claude-haiku-4-5", Set.of()), "ticketReader");
		assertDenied(access.check(Set.of("ticketReader"), "claude-sonnet-4-6", Set.of()),
			Cause.NOT_GRANTED);
	}

	/**
	 * The model the agent is assigned is permitted even to a role that is permitted no model.
	 *
	 * <p>
	 * The load-bearing case: every client of {@code /chat/send} sends the model it is showing, which is
	 * the agent's own until the user picks another one. A rule that called that a choice would refuse
	 * every request of a restricted role.
	 * </p>
	 */
	public void testTheAgentsOwnModelIsNotAChoice() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of(), List.of()));

		assertGranted(models(policy).check(Set.of("ticketReader"), "claude-opus-4-1",
			Set.of("claude-opus-4-1")), RoleAccessPolicy.AGENT_ROLE);
	}

	/** A refusal by name wins even over the model the agent is assigned. */
	public void testDenyBeatsTheAgentsOwnModel() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		put(policy, role("ticketReader", List.of(), List.of("claude-opus-4-1")));

		assertDenied(models(policy).check(Set.of("ticketReader"), "claude-opus-4-1",
			Set.of("claude-opus-4-1")), Cause.ROLE_DENY);
	}

	/**
	 * The menu a client is offered under each of the three defaults.
	 *
	 * <p>
	 * "assigned" and "none" refuse the same requests and differ here, which is the whole of the
	 * difference between them: the second says the choice should not be shown at all.
	 * </p>
	 */
	public void testWhatIsOffered() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		List<String> configured = List.of("claude-haiku-4-5", "claude-sonnet-4-6");
		Set<String> assigned = Set.of("claude-haiku-4-5");

		assertEquals(List.of("claude-haiku-4-5"),
			models(policy).allowed(Set.of("Clerk"), configured, assigned));

		policy.getModelAccess().setDefault(ModelDefault.ALL);
		assertEquals(configured, models(policy).allowed(Set.of("Clerk"), configured, assigned));

		policy.getModelAccess().setDefault(ModelDefault.NONE);
		assertEquals(List.of(), models(policy).allowed(Set.of("Clerk"), configured, assigned));
	}

	private static RoleAccessPolicy models(ToolAccessPolicy policy) {
		return RoleAccessPolicy.models(policy);
	}

	private static void put(ToolAccessPolicy policy, ModelRoleAccess role) {
		policy.getModelAccess().getRoles().put(role.getName(), role);
	}

	private static ModelRoleAccess role(String name, List<String> granted, List<String> denied) {
		ModelRoleAccess role = TypedConfiguration.newConfigItem(ModelRoleAccess.class);
		role.setName(name);
		if (!granted.isEmpty()) {
			role.getGrants().add(entry(granted));
		}
		if (!denied.isEmpty()) {
			role.getDenies().add(entry(denied));
		}
		return role;
	}

	private static ModelGrant entry(List<String> names) {
		ModelGrant grant = TypedConfiguration.newConfigItem(ModelGrant.class);
		grant.setModels(names);
		return grant;
	}
}
