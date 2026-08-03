/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests the run-scoped half of tool authorization: {@link ToolAuthorization}.
 *
 * <p>
 * {@code TestRoleToolAccess} covers the policy semantics. What is tested here is everything that
 * belongs to the run rather than to the policy - the administrator bypass, the background ceiling, and
 * the guarantee that an application which configured no policy is not checked at all.
 * </p>
 */
public class TestToolAuthorization extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestToolAuthorization.class)));
	}

	/**
	 * The most important test in this class: an application that configured no policy is not
	 * authorized against at all, so its agents behave exactly as they did before this layer existed.
	 */
	public void testUncheckedAllowsEverything() {
		ToolAuthorization unchecked = ToolAuthorization.UNCHECKED;

		assertTrue(unchecked.allows(tool("executeTLScript", ToolGroups.SCRIPT)));
		assertNull(unchecked.denialReason(tool("executeTLScript", ToolGroups.SCRIPT)));
		assertTrue(unchecked.allows("anything.at.all"));
		assertNull(unchecked.denialReason("anything.at.all"));
	}

	/** A granted group lets the tool through. */
	public void testGrantedGroupAllowed() {
		ToolAuthorization auth = auth(clerkPolicy(), Set.of("Clerk"), false);

		assertTrue(auth.allows(tool("listInstances", ToolGroups.DATA_READ)));
	}

	/** An ungranted group is refused, with a reason naming it. */
	public void testUngrantedGroupDenied() {
		ToolAuthorization auth = auth(clerkPolicy(), Set.of("Clerk"), false);
		Tool script = tool("executeTLScript", ToolGroups.SCRIPT);

		assertFalse(auth.allows(script));
		String reason = auth.denialReason(script);
		assertNotNull(reason);
		assertTrue(reason, reason.contains("executeTLScript"));
		assertTrue(reason, reason.contains(ToolGroups.SCRIPT));
	}

	/** {@link ToolAuthorization#allows} and {@link ToolAuthorization#denialReason} must agree. */
	public void testAllowsAgreesWithDenialReason() {
		ToolAuthorization auth = auth(clerkPolicy(), Set.of("Clerk"), false);

		for (Tool tool : List.of(
			tool("listInstances", ToolGroups.DATA_READ),
			tool("executeTLScript", ToolGroups.SCRIPT),
			tool("mysteryTool", ""))) {
			assertEquals(tool.name(), auth.allows(tool), auth.denialReason(tool) == null);
		}
	}

	/** A run with no principal holds no roles, so only the baseline reaches it. */
	public void testRunWithoutPrincipalGetsBaselineOnly() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getBaseline().add(group(ToolGroups.INTERACTION));

		ToolAuthorization auth = auth(policy, Set.of(), false);

		assertTrue(auth.allows(tool("askUser", ToolGroups.INTERACTION)));
		assertFalse(auth.allows(tool("listInstances", ToolGroups.DATA_READ)));
	}

	/**
	 * The background allowlist narrows a run that has no human to answer an approval prompt.
	 *
	 * <p>
	 * Approval is the compensating control that makes a write-classified tool safe; a run that
	 * auto-approves has lost it, so it may only reach what an administrator allowlisted.
	 * </p>
	 */
	public void testBackgroundAllowlistNarrowsTheRun() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.MODEL_STRUCTURE));
		allow(policy, group(ToolGroups.DATA_READ));

		Tool structural = tool("createClass", ToolGroups.MODEL_STRUCTURE);

		assertTrue("Interactively the role's grant applies.",
			auth(policy, Set.of("Clerk"), false).allows(structural));

		ToolAuthorization background = auth(policy, Set.of("Clerk"), true);
		assertFalse(background.allows(structural));
		assertTrue(background.allows(tool("listInstances", ToolGroups.DATA_READ)));
		String reason = background.denialReason(structural);
		assertTrue(reason, reason.contains("background allowlist"));
	}

	/**
	 * Without a declared allowlist an unattended run reaches nothing.
	 *
	 * <p>
	 * The counterpart of the interactive run in {@link #testBackgroundAllowlistNarrowsTheRun()}: the
	 * same role, the same tool, but nobody to approve and no administrator statement about what that
	 * costs. {@code AiToolAccessService} rejects such a policy at boot; if one reaches a decision
	 * anyway, it admits nothing.
	 * </p>
	 */
	public void testBackgroundDeniedWithoutAllowlist() {
		ToolAuthorization auth = auth(clerkPolicy(), Set.of("Clerk"), true);
		Tool granted = tool("listInstances", ToolGroups.DATA_READ);

		assertTrue("Interactively the role's grant applies.",
			auth(clerkPolicy(), Set.of("Clerk"), false).allows(granted));
		assertFalse("Unattended, an undeclared allowlist admits nothing.", auth.allows(granted));
		String reason = auth.denialReason(granted);
		assertTrue(reason, reason.contains("background allowlist"));
	}

	/** The explicit opt-out restores the unrestricted behaviour an administrator may still want. */
	public void testBackgroundUnrestrictedWithExplicitOptOut() {
		ToolAccessPolicy policy = clerkPolicy();
		allow(policy).setAll(true);

		ToolAuthorization auth = auth(policy, Set.of("Clerk"), true);

		assertTrue(auth.allows(tool("listInstances", ToolGroups.DATA_READ)));
		assertFalse("The user's own grants still bound the run.",
			auth.allows(tool("createClass", ToolGroups.MODEL_STRUCTURE)));
	}

	/**
	 * A tool the catalog cannot resolve is known only by name, hence ungrouped, hence denied unless a
	 * per-tool grant names it.
	 */
	public void testUnresolvableToolNameDenied() {
		ToolAuthorization auth = auth(clerkPolicy(), Set.of("Clerk"), false);

		assertFalse(auth.allows("hallucinated_tool"));
		String reason = auth.denialReason("hallucinated_tool");
		assertTrue(reason, reason.contains("no tool group"));
	}

	/** A per-tool grant reaches a tool the catalog cannot resolve. */
	public void testPerToolGrantReachesUnresolvableToolName() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(tool("mcp__jira__createIssue"));

		assertTrue(auth(policy, Set.of("Clerk"), false).allows("mcp__jira__createIssue"));
	}

	/**
	 * The identity resolver supplies the qualified name a per-tool grant may use.
	 *
	 * <p>
	 * Without it, a grant of {@code instance-browser.listInstances} would never match the tool call
	 * the model makes, which carries the bare {@code listInstances}.
	 */
	public void testIdentitiesSupplyTheQualifiedName() {
		ToolAccessPolicy policy = policy(role("Clerk",
			grants(tool("instance-browser.listInstances")), grants()));
		Tool listInstances = tool("listInstances", "");

		RoleToolAccess access = new RoleToolAccess(policy, DeclaredGroups.NONE);
		ToolAuthorization withoutQualifiedName =
			new ToolAuthorization(access, null, Set.of("Clerk"), false);
		ToolAuthorization withQualifiedName = new ToolAuthorization(access, null, Set.of("Clerk"), false,
			t -> new ToolIdentity(t.name(), "instance-browser." + t.name(), t.group()));

		assertFalse(withoutQualifiedName.allows(listInstances));
		assertTrue(withQualifiedName.allows(listInstances));
	}

	private static ToolAuthorization auth(ToolAccessPolicy policy, Set<String> roles, boolean background) {
		// No principal: the administrator bypass needs a Person, which cannot be created without a
		// KnowledgeBase. It is covered where it can be, in the demo's end-to-end procedure.
		return new ToolAuthorization(new RoleToolAccess(policy, DeclaredGroups.NONE), null, roles,
			background);
	}

	/** Declares the policy's background allowlist with the given entries. */
	private static BackgroundAllowlist allow(ToolAccessPolicy policy, Grant... entries) {
		BackgroundAllowlist allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
		allowlist.getGrants().addAll(List.of(entries));
		policy.setBackgroundAllowlist(allowlist);
		return allowlist;
	}

	/** A role granted only {@link ToolGroups#DATA_READ}. */
	private static ToolAccessPolicy clerkPolicy() {
		return policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
	}

	private static ToolAccessPolicy policy(RoleAccess... roles) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		for (RoleAccess role : roles) {
			policy.getToolAccess().put(role.getName(), role);
		}
		return policy;
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

	private static Grant tool(String name) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setTool(name);
		return grant;
	}

	/** A minimal {@link Tool} that carries a name and a declared group. */
	private static Tool tool(String name, String group) {
		return new Tool() {

			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return name;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, name, "{\"type\":\"object\"}");
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public String group() {
				return group;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new UnsupportedOperationException("Authorization is decided before execution.");
			}
		};
	}
}
