/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Tests what {@link AiToolAccessService} refuses to accept in a policy.
 *
 * <p>
 * There is nothing to turn on: the policy is applied to every run. What is left to check at boot is
 * that the policy an administrator wrote says what it looks like it says.
 * </p>
 */
public class TestAiToolAccessService extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAiToolAccessService.class)));
	}

	/**
	 * A policy that names roles and says nothing about unattended runs is rejected.
	 *
	 * <p>
	 * Unattended runs approve every tool call themselves, and {@code <background-allowlist>} is the
	 * only thing that bounds them once approval is gone. Its absence must therefore not be read as
	 * "unrestricted" - so it is not read as anything, it is refused. This asserts which policies the
	 * service considers under-specified; that an {@code InstantiationContext} error aborts the boot is
	 * the engine's behaviour.
	 * </p>
	 */
	public void testPolicyWithRolesMustDeclareBackgroundAllowlist() {
		ToolAccessPolicy granting = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));

		assertTrue("A policy that grants something without an allowlist is under-specified.",
			AiToolAccessService.needsBackgroundAllowlist(granting));

		allow(granting);
		assertFalse("An empty declaration is a decision: no tool without a human.",
			AiToolAccessService.needsBackgroundAllowlist(granting));
	}

	/** The explicit opt-out satisfies the requirement - the administrator said it out loud. */
	public void testExplicitOptOutSatisfiesTheRequirement() {
		ToolAccessPolicy granting = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		allow(granting).setAll(true);

		assertFalse(AiToolAccessService.needsBackgroundAllowlist(granting));
	}

	/**
	 * A policy that grants no role anything needs no allowlist.
	 *
	 * <p>
	 * There is nothing for the administrator to have forgotten: every run of such an application,
	 * attended or not, already reaches only {@link RoleToolAccess#IMPLICIT_BASELINE}. Refusing to boot
	 * would refuse every application that has not written a policy yet.
	 * </p>
	 */
	public void testPolicyWithoutRolesNeedsNoAllowlist() {
		assertFalse(AiToolAccessService.needsBackgroundAllowlist(policy()));

		ToolAccessPolicy baselineOnly = policy();
		baseline(baselineOnly, group(ToolGroups.MODEL_READ));
		assertFalse("A baseline is not a grant table either.",
			AiToolAccessService.needsBackgroundAllowlist(baselineOnly));
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
			role("ticketReader", grants(group(ToolGroups.DATA_READ)), grants()),
			role("ticektReader", grants(group(ToolGroups.SCRIPT)), grants()));

		Set<String> unknown =
			AiToolAccessService.unknownRoles(policy, name -> Set.of("ticketReader", "projectPartner").contains(name));

		assertEquals("The misspelled role is named, the correct one is not.",
			Set.of("ticektReader"), unknown);
	}

	/** A policy naming only existing roles reports nothing. */
	public void testKnownRolesAreAccepted() {
		ToolAccessPolicy policy = policy(role("ticketReader", grants(group(ToolGroups.DATA_READ)), grants()));

		assertEquals(Set.of(),
			AiToolAccessService.unknownRoles(policy, name -> Set.of("ticketReader").contains(name)));
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
		baseline(policy, group(ToolGroups.INTERACTION));

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

	/**
	 * A permission entry that says two things at once is refused.
	 *
	 * <p>
	 * A group and a tool list in the same entry are two decisions written as one, and nothing about the
	 * XML says which the author meant. It is a boot error rather than an interpretation, for the same
	 * reason an unknown role name is.
	 * </p>
	 */
	public void testEntryNamingAGroupAndToolsIsRefused() {
		Grant both = group(ToolGroups.DATA_READ);
		both.setTools(List.of("instance-browser.listInstances"));
		ToolAccessPolicy policy = policy(role("Clerk", grants(both), grants()));

		List<String> problems = AiToolAccessService.underspecifiedEntries(policy);

		assertEquals(String.valueOf(problems), 1, problems.size());
		assertTrue(problems.get(0), problems.get(0).contains("role 'Clerk' <grants> entry 1"));
		assertTrue(problems.get(0), problems.get(0).contains(ToolGroups.DATA_READ));
	}

	/** An entry that says nothing is refused too: it reaches nothing and admits it nowhere. */
	public void testEntryNamingNothingIsRefused() {
		ToolAccessPolicy policy = policy();
		baseline(policy, TypedConfiguration.newConfigItem(Grant.class));
		allow(policy, TypedConfiguration.newConfigItem(Grant.class));

		assertEquals(List.of("<baseline> entry 1 names nothing",
			"<background-allowlist> entry 1 names nothing"),
			AiToolAccessService.underspecifiedEntries(policy));
	}

	/** A policy whose entries each say one thing reports nothing. */
	public void testWellFormedEntriesAreAccepted() {
		Grant byName = TypedConfiguration.newConfigItem(Grant.class);
		byName.setTools(List.of("mcp__jira__createIssue", "mcp__jira__addComment"));
		ToolAccessPolicy policy = policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ), byName), grants(group(ToolGroups.EGRESS))));
		baseline(policy, group(ToolGroups.MODEL_READ));
		allow(policy, group(ToolGroups.DATA_READ));

		assertEquals(List.of(), AiToolAccessService.underspecifiedEntries(policy));
	}

	/**
	 * A grant naming a role nobody can hold on the security root is reported.
	 *
	 * <p>
	 * The failure it catches is the one the tier/scope split makes possible: a role that the
	 * application's rules only ever produce on business objects governs model access and reaches the
	 * tool layer never, because tool access reads roles on the root. Existing is not the same as
	 * reachable, and {@link AiToolAccessService#unknownRoles} only answers the first question.
	 * </p>
	 */
	public void testRolesNobodyCanHoldOnTheRootAreReported() {
		ToolAccessPolicy policy = policy(
			role("projectPartner", grants(group(ToolGroups.DATA_WRITE)), grants()),
			role("ticketReader", grants(group(ToolGroups.DATA_READ)), grants()));

		Set<String> unreachable = AiToolAccessService.rolesUnreachableOnSecurityRoot(policy,
			name -> "projectPartner".equals(name));

		assertEquals("Only the role no rule puts on the root is named.",
			Set.of("ticketReader"), unreachable);
	}

	/**
	 * A role written with an empty grant list is not reported.
	 *
	 * <p>
	 * "This role reaches nothing" is a statement the fixture of the test bed makes on purpose. Whether
	 * anybody holds such a role changes nothing, and warning about it would punish saying the emptiness
	 * out loud.
	 * </p>
	 */
	public void testDeliberatelyEmptyRoleIsNotReportedAsUnreachable() {
		ToolAccessPolicy policy = policy(role("bare", grants(), grants()));

		assertEquals(Set.of(),
			AiToolAccessService.rolesUnreachableOnSecurityRoot(policy, name -> false));
	}

	/**
	 * A role granted data tools that no model-access grant names is reported - the "Dora" case.
	 *
	 * <p>
	 * She holds the tier and scopes nothing, so every read comes back empty and every write is refused,
	 * and the engine's read semantics make the first look like "there is no data" rather than "you may
	 * not see it". The two grant tables never talk to each other by design; this is the boot-time
	 * warning that pays for the design.
	 * </p>
	 */
	public void testDataRolesWithoutModelRightsAreReported() {
		ToolAccessPolicy policy = policy(
			role("projectPartner", grants(group(ToolGroups.DATA_READ), group(ToolGroups.DATA_WRITE)),
				grants()),
			role("dora", grants(group(ToolGroups.DATA_WRITE), group(ToolGroups.MODEL_READ)), grants()));

		Map<String, Set<String>> reported = AiToolAccessService.dataRolesWithoutModelRights(policy,
			name -> null, Set.of("projectPartner"));

		assertEquals("Only the role with no model rights is named.", Set.of("dora"), reported.keySet());
		assertEquals("The message names the capability that is worthless, not the harmless one.",
			Set.of(ToolGroups.DATA_WRITE), reported.get("dora"));
	}

	/** A role holding no data capability at all is not reported: it promises nothing about data. */
	public void testRoleWithoutDataCapabilitiesIsNotReported() {
		ToolAccessPolicy policy = policy(
			role("author", grants(group(ToolGroups.AGENT_AUTHORING)), grants()));

		assertEquals(Map.of(),
			AiToolAccessService.dataRolesWithoutModelRights(policy, name -> null, Set.of()));
	}

	/** A data capability reached by naming a single tool counts the same as naming its group. */
	public void testDataCapabilityReachedByToolNameCounts() {
		Grant byName = TypedConfiguration.newConfigItem(Grant.class);
		byName.setTools(List.of("secdemo-editing.addTicket"));
		ToolAccessPolicy policy = policy(role("dora", grants(byName), grants()));

		Map<String, Set<String>> reported = AiToolAccessService.dataRolesWithoutModelRights(policy,
			name -> "secdemo-editing.addTicket".equals(name) ? ToolGroups.DATA_WRITE : null, Set.of());

		assertEquals(Set.of(ToolGroups.DATA_WRITE), reported.get("dora"));
	}

	/** Only Read, Write and Create make a data tool useful, so only those count as model rights. */
	public void testDeleteAloneIsNotAModelRight() {
		assertEquals(Set.of("projectPartner", "ticketReader"),
			AiToolAccessService.rolesWithModelRights(securityConfig(
				classRights("pm:Project", "Read", "projectPartner"),
				classRights("pm:Ticket", "Create", "projectPartner, ticketReader"),
				classRights("pm:Note", "Delete", "janitor"))));
	}

	private static SecurityConfigurationService.Config securityConfig(
			SecurityConfigurationService.ModelAccessRights... rights) {
		SecurityConfigurationService.Config config =
			TypedConfiguration.newConfigItem(SecurityConfigurationService.Config.class);
		for (SecurityConfigurationService.ModelAccessRights entry : rights) {
			config.getSecurityConfig().put(entry.getName(), entry);
		}
		return config;
	}

	private static SecurityConfigurationService.ModelAccessRights classRights(String typeName,
			String operation, String roles) {
		SecurityConfigurationService.TLClassAccessRights rights =
			TypedConfiguration.newConfigItem(SecurityConfigurationService.TLClassAccessRights.class);
		rights.setName(typeName);
		AccessGrant grant = TypedConfiguration.newConfigItem(AccessGrant.class);
		// AccessGrant declares no setter for its operation, so it is updated generically.
		grant.update(grant.descriptor().getProperty(AccessGrant.OPERATION),
			new CommandGroupReference(operation));
		for (String roleName : roles.split(",")) {
			RoleConfig role = TypedConfiguration.newConfigItem(RoleConfig.class);
			role.setName(roleName.trim());
			grant.getRoles().add(role);
		}
		rights.getGrants().put(grant.getOperation(), grant);
		return rights;
	}

	private static ToolAccessPolicy policy(RoleAccess... roles) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		for (RoleAccess role : roles) {
			policy.getToolAccess().put(role.getName(), role);
		}
		baseline(policy);
		return policy;
	}

	/**
	 * Declares the policy's baseline with the given entries.
	 *
	 * <p>
	 * Called with no entries by {@link #policy(RoleAccess...)}, so that these tests assert role grants
	 * against an explicitly empty baseline rather than against
	 * {@link RoleToolAccess#DEFAULT_BASELINE}. The default is what an <em>absent</em> element means, and
	 * it is tested where it belongs - see {@code TestDefaultBaseline}.
	 * </p>
	 */
	private static void baseline(ToolAccessPolicy policy, Grant... entries) {
		Baseline baseline = TypedConfiguration.newConfigItem(Baseline.class);
		baseline.getGrants().addAll(List.of(entries));
		policy.setBaseline(baseline);
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
