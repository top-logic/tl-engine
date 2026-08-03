/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Tests how {@link AiToolAccessService} reads a policy: when enforcement turns on, and what it refuses
 * to accept.
 */
public class TestAiToolAccessService extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAiToolAccessService.class)));
	}

	/**
	 * The opt-in rule, and the single most important property of this change: an empty policy leaves
	 * the application unenforced, so nothing about its behaviour changes.
	 */
	public void testEmptyPolicyIsUnenforced() {
		AiSecurityMode mode = AiToolAccessService.resolveCeiling(policy());

		assertEquals(AiSecurityMode.LEGACY, mode);
		assertFalse(mode.enforcesToolAccess());
		assertFalse(mode.enforcesModelAccess());
	}

	/** Writing the matrix is the opt-in: a non-empty {@code <tool-access>} enforces. */
	public void testToolAccessTurnsEnforcementOn() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));

		AiSecurityMode mode = AiToolAccessService.resolveCeiling(policy);

		assertEquals(AiSecurityMode.ENFORCED, mode);
		assertTrue(mode.enforcesToolAccess());
	}

	/** An explicit mode always wins over the heuristic - in both directions. */
	public void testExplicitModeWins() {
		ToolAccessPolicy withMatrix = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		withMatrix.setSecurityMode(AiSecurityMode.LEGACY);
		assertEquals(AiSecurityMode.LEGACY, AiToolAccessService.resolveCeiling(withMatrix));

		ToolAccessPolicy empty = policy();
		empty.setSecurityMode(AiSecurityMode.ENFORCED);
		assertEquals(AiSecurityMode.ENFORCED, AiToolAccessService.resolveCeiling(empty));
	}

	/**
	 * A baseline or a background allowlist alone does not enforce.
	 *
	 * <p>
	 * Both only ever narrow or widen an enforced decision. Treating them as the opt-in signal would
	 * turn a deployment that merely stated its background ceiling into one that denies every tool.
	 * </p>
	 */
	public void testBaselineAloneDoesNotEnforce() {
		ToolAccessPolicy policy = policy();
		policy.getBaseline().add(group(ToolGroups.INTERACTION));
		allow(policy, group(ToolGroups.DATA_READ));

		assertEquals(AiSecurityMode.LEGACY, AiToolAccessService.resolveCeiling(policy));
	}

	/**
	 * An enforcing policy that says nothing about unattended runs is rejected.
	 *
	 * <p>
	 * Unattended runs approve every tool call themselves, and {@code <background-allowlist>} is the
	 * only thing that bounds them once approval is gone. Its absence must therefore not be read as
	 * "unrestricted" - so it is not read as anything, it is refused. This asserts which policies the
	 * service considers under-specified; that an {@code InstantiationContext} error aborts the boot is
	 * the engine's behaviour.
	 * </p>
	 */
	public void testEnforcingPolicyMustDeclareBackgroundAllowlist() {
		ToolAccessPolicy enforcing = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));

		assertTrue("An enforcing policy without an allowlist is under-specified.",
			AiToolAccessService.needsBackgroundAllowlist(enforcing));

		allow(enforcing);
		assertFalse("An empty declaration is a decision: no tool without a human.",
			AiToolAccessService.needsBackgroundAllowlist(enforcing));
	}

	/** The explicit opt-out satisfies the requirement - the administrator said it out loud. */
	public void testExplicitOptOutSatisfiesTheRequirement() {
		ToolAccessPolicy enforcing = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		allow(enforcing).setAll(true);

		assertFalse(AiToolAccessService.needsBackgroundAllowlist(enforcing));
	}

	/** An unenforced policy needs no allowlist: nothing is checked in the first place. */
	public void testUnenforcedPolicyNeedsNoAllowlist() {
		assertFalse(AiToolAccessService.needsBackgroundAllowlist(policy()));
	}

	/**
	 * Every role the policy names must exist - the row of the section 4.1 table that says a typo must
	 * fail at boot rather than silently widen or narrow access.
	 *
	 * <p>
	 * The service reports each unknown name through {@code context.error(...)}, the way
	 * {@code SecurityConfigurationService.processRights} does. What is asserted here is which names it
	 * checks; that an {@code InstantiationContext} error aborts the boot is the engine's behaviour.
	 * </p>
	 */
	public void testUnknownRolesAreReported() {
		ToolAccessPolicy policy = policy(
			role("secReader", grants(group(ToolGroups.DATA_READ)), grants()),
			role("secEdtior", grants(group(ToolGroups.SCRIPT)), grants()));

		Set<String> unknown =
			AiToolAccessService.unknownRoles(policy, name -> Set.of("secReader", "secEditor").contains(name));

		assertEquals("The misspelled role is named, the correct one is not.",
			Set.of("secEdtior"), unknown);
	}

	/** A policy naming only existing roles reports nothing. */
	public void testKnownRolesAreAccepted() {
		ToolAccessPolicy policy = policy(role("secReader", grants(group(ToolGroups.DATA_READ)), grants()));

		assertEquals(Set.of(),
			AiToolAccessService.unknownRoles(policy, name -> Set.of("secReader").contains(name)));
	}

	/**
	 * A group name no tool declares is reported, because a grant of it reaches nothing.
	 *
	 * <p>
	 * A warning rather than a boot error: group names are an open string space by design, and a tool
	 * authored in the running application can introduce one after boot.
	 * </p>
	 */
	public void testUnknownGroupsAreReported() {
		ToolAccessPolicy policy = policy(
			role("Clerk", grants(group("data.reed"), group(ToolGroups.DATA_READ)), grants()),
			role("Developer", grants(), grants(group("modle.structure"))));
		policy.getBaseline().add(group(ToolGroups.INTERACTION));

		Set<String> unknown = AiToolAccessService.unknownGroups(policy, ToolGroups.ALL);

		assertEquals(Set.of("data.reed", "modle.structure"), unknown);
	}

	/** A group the policy itself defines counts as known, which is how MCP tools are classified. */
	public void testPolicyDefinedGroupIsKnown() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group("jira")), grants()));
		Set<String> known = new java.util.HashSet<>(ToolGroups.ALL);
		known.add("jira");

		assertEquals(Set.of(), AiToolAccessService.unknownGroups(policy, known));
	}

	private static ToolAccessPolicy policy(RoleAccess... roles) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		for (RoleAccess role : roles) {
			policy.getToolAccess().put(role.getName(), role);
		}
		return policy;
	}

	/** Declares the policy's background allowlist with the given entries. */
	private static BackgroundAllowlist allow(ToolAccessPolicy policy, Grant... entries) {
		BackgroundAllowlist allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
		allowlist.getGrants().addAll(List.of(entries));
		policy.setBackgroundAllowlist(allowlist);
		return allowlist;
	}

	private static RoleAccess role(String name, List<Grant> granted, List<Grant> denied) {
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName(name);
		role.getGrants().addAll(granted);
		role.getDenies().addAll(denied);
		return role;
	}

	private static List<Grant> grants(Grant... entries) {
		return List.of(entries);
	}

	private static Grant group(String name) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setGroup(name);
		return grant;
	}
}
