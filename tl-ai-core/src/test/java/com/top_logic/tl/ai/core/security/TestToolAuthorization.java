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
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
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
 *
 * <p>
 * Including the capability-level check
 * ({@link ToolAuthorization#capabilityDenialReason(String, String)}), which an operation uses when the
 * capability it costs depends on an argument the invocation gate cannot see - the commit path of
 * {@code executeTLScript}. It must be decided by the same rules in the same order as a tool, which is
 * what the {@code testCapability*} methods pin.
 * </p>
 */
public class TestToolAuthorization extends TestCase {

	/** The operation a capability-level check is asked about, as {@code TLScriptTool} phrases it. */
	private static final String COMMIT = "executing TL-Script with withCommit=true";

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
		baseline(policy, group(ToolGroups.INTERACTION));

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
		policy.getToolAccess().get("Clerk").getGrants().add(tools("mcp__jira__createIssue"));

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
			grants(tools("instance-browser.listInstances")), grants()));
		Tool listInstances = tool("listInstances", "");

		RoleToolAccess access = new RoleToolAccess(policy, DeclaredGroups.NONE);
		ToolAuthorization withoutQualifiedName =
			new ToolAuthorization(access, null, Set.of("Clerk"), false);
		ToolAuthorization withQualifiedName = new ToolAuthorization(access, null, Set.of("Clerk"), false,
			t -> new ToolIdentity(t.name(), "instance-browser." + t.name(), t.group()));

		assertFalse(withoutQualifiedName.allows(listInstances));
		assertTrue(withQualifiedName.allows(listInstances));
	}

	/**
	 * A capability the role does not hold is refused even though the tool that asks for it is granted.
	 *
	 * <p>
	 * The rule that closes the commit path of {@code executeTLScript}: the tool is classified
	 * {@link ToolGroups#SCRIPT}, which the role holds, and committing costs
	 * {@link ToolGroups#DATA_WRITE}, which it does not.
	 * </p>
	 */
	public void testCapabilityWithoutAGrantIsDenied() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.SCRIPT));
		ToolAuthorization auth = auth(policy, Set.of("Clerk"), false);

		assertNull("The tool itself is granted.", auth.denialReason(tool("executeTLScript", ToolGroups.SCRIPT)));

		String reason = auth.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT);

		assertNotNull("A capability no held role grants must be refused.", reason);
		assertTrue(reason, reason.contains(ToolGroups.DATA_WRITE));
		assertTrue("The refusal must say what was refused: " + reason, reason.contains(COMMIT));
		assertTrue(reason, reason.contains("none of your roles grants"));
	}

	/** A role holding the capability may use it. */
	public void testGrantedCapabilityIsAllowed() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));

		assertNull(auth(policy, Set.of("Clerk"), false)
			.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/** The baseline grants a capability to everyone, as it does a tool. */
	public void testBaselineGrantsTheCapability() {
		ToolAccessPolicy policy = clerkPolicy();
		baseline(policy, group(ToolGroups.DATA_WRITE));

		assertNull(auth(policy, Set.of(), false).capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/** A {@code <deny>} wins over the grant, and the denial names the role that carries it. */
	public void testExplicitDenyOverridesTheCapabilityGrant() {
		ToolAccessPolicy policy = policy(role("Clerk",
			grants(group(ToolGroups.DATA_WRITE)), grants(group(ToolGroups.DATA_WRITE))));

		String reason = auth(policy, Set.of("Clerk"), false)
			.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT);

		assertNotNull(reason);
		assertTrue(reason, reason.contains("explicitly denied to your role 'Clerk'"));
	}

	/**
	 * A per-tool grant does not reach the capability.
	 *
	 * <p>
	 * Granting {@code executeTLScript} by name says the role may run TL-Script; it says nothing about
	 * the second capability that tool can reach through an argument, and reading it as a
	 * {@code data.write} grant would reopen the bypass this check exists to close.
	 * </p>
	 */
	public void testPerToolGrantDoesNotReachTheCapability() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(tools("executeTLScript")), grants()));
		ToolAuthorization auth = auth(policy, Set.of("Clerk"), false);

		assertNull("The tool is granted by name.", auth.denialReason(tool("executeTLScript", ToolGroups.SCRIPT)));
		assertNotNull("...which is not a grant of data.write.",
			auth.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/**
	 * The background allowlist caps a capability exactly as it caps a tool: a role holding
	 * {@code data.write} cannot use it unattended unless an administrator allowlisted it.
	 */
	public void testBackgroundAllowlistCapsTheCapability() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));
		allow(policy, group(ToolGroups.SCRIPT), group(ToolGroups.DATA_READ));

		assertNull("Interactively the role's grant applies.",
			auth(policy, Set.of("Clerk"), false).capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));

		String reason = auth(policy, Set.of("Clerk"), true)
			.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT);

		assertNotNull("An allowlist carrying 'script' and not 'data.write' must refuse the commit.",
			reason);
		assertTrue(reason, reason.contains("background allowlist"));
		assertTrue(reason, reason.contains(ToolGroups.DATA_WRITE));
		assertFalse("Blaming the role would send an operator to the wrong configuration: " + reason,
			reason.contains("none of your roles grants"));
	}

	/** An allowlisted capability the role holds survives an unattended run. */
	public void testAllowlistedCapabilitySurvivesABackgroundRun() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));
		allow(policy, group(ToolGroups.DATA_WRITE));

		assertNull(auth(policy, Set.of("Clerk"), true)
			.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/** Without a declared allowlist an unattended run reaches no capability either. */
	public void testBackgroundCapabilityDeniedWithoutAllowlist() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));

		String reason = auth(policy, Set.of("Clerk"), true)
			.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT);

		assertNotNull(reason);
		assertTrue(reason, reason.contains("background allowlist"));
	}

	/** An application that configured no policy is not checked here either. */
	public void testUncheckedAllowsEveryCapability() {
		assertNull(ToolAuthorization.UNCHECKED.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/**
	 * A run that <em>becomes</em> unattended is narrowed to exactly what it would have been had it
	 * started that way.
	 *
	 * <p>
	 * The derivation a detached subagent needs: its context is built during the parent turn, where a
	 * human still is, and the parent turn then ends while the child keeps working. See
	 * {@code ToolContext#forDetachedSubagent}.
	 * </p>
	 */
	public void testAsBackgroundNarrowsTheRunAfterTheFact() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));
		allow(policy, group(ToolGroups.DATA_READ));

		ToolAuthorization foreground = auth(policy, Set.of("Clerk"), false);
		Tool write = tool("addTicket", ToolGroups.DATA_WRITE);

		assertFalse(foreground.isBackground());
		assertTrue("Precondition: the role holds data.write.", foreground.allows(write));

		ToolAuthorization derived = foreground.asBackground();

		assertTrue(derived.isBackground());
		assertFalse("The derived authorization is bounded by the allowlist.", derived.allows(write));
		assertTrue("...and its denial blames the allowlist: " + derived.denialReason(write),
			derived.denialReason(write).contains("background allowlist"));
		assertTrue("The allowlisted capability survives.",
			derived.allows(tool("listInstances", ToolGroups.DATA_READ)));
		assertTrue("Deriving must not change the run it was derived from.", foreground.allows(write));
		assertNotNull("The capability ceiling is derived too, so the commit path is closed as well.",
			derived.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT));
	}

	/** Deriving twice, or from a run that never checks anything, changes nothing. */
	public void testAsBackgroundIsIdempotentAndLeavesUncheckedAlone() {
		ToolAuthorization background = auth(clerkPolicy(), Set.of("Clerk"), true);

		assertSame("A run that is already unattended has nothing to derive.",
			background, background.asBackground());
		assertSame("An application that configured no policy must see no derivation at all.",
			ToolAuthorization.UNCHECKED, ToolAuthorization.UNCHECKED.asBackground());
		assertFalse(ToolAuthorization.UNCHECKED.isBackground());
		assertEquals(Set.of(), ToolAuthorization.UNCHECKED.capabilitiesWithheldInBackground());
	}

	/**
	 * What the ceiling costs is nameable, in the unit an administrator grants.
	 *
	 * <p>
	 * Silent capability loss is the failure mode this classification must not have: the caller that
	 * detaches a subagent logs this set, and the child's own prompt reports it.
	 * </p>
	 */
	public void testWithheldCapabilitiesNameWhatTheCeilingCosts() {
		ToolAccessPolicy policy = clerkPolicy();
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.DATA_WRITE));
		policy.getToolAccess().get("Clerk").getGrants().add(group(ToolGroups.SCRIPT));
		allow(policy, group(ToolGroups.DATA_READ));

		ToolAuthorization foreground = auth(policy, Set.of("Clerk"), false);

		assertEquals("An attended run loses nothing, so it must claim nothing.",
			Set.of(), foreground.capabilitiesWithheldInBackground());
		assertEquals("Exactly the held capabilities the allowlist does not carry.",
			Set.of(ToolGroups.DATA_WRITE, ToolGroups.SCRIPT),
			foreground.asBackground().capabilitiesWithheldInBackground());
	}

	/**
	 * A capability the roles do not hold is not reported as withheld by the ceiling: it was never
	 * within reach, and naming it would send an operator to the wrong configuration.
	 */
	public void testWithheldCapabilitiesReportOnlyWhatTheRolesHold() {
		ToolAuthorization background = auth(clerkPolicy(), Set.of("Clerk"), true);

		Set<String> withheld = background.capabilitiesWithheldInBackground();

		assertFalse("model.structure is granted to nobody here: " + withheld,
			withheld.contains(ToolGroups.MODEL_STRUCTURE));
		assertEquals("data.read is the only capability the role holds, and no allowlist admits it.",
			Set.of(ToolGroups.DATA_READ), withheld);
	}

	/**
	 * No capability of {@link RoleToolAccess#IMPLICIT_BASELINE} is ever reported as withheld, because
	 * none of them ever is: an unattended run reaches them whatever the allowlist says.
	 */
	public void testTheImplicitBaselineIsNeverWithheld() {
		for (String capability : RoleToolAccess.IMPLICIT_BASELINE) {
			ToolAccessPolicy policy = clerkPolicy();
			baseline(policy, group(capability));
			ToolAuthorization background = auth(policy, Set.of("Clerk"), true);

			assertTrue("Precondition: " + capability + " reaches an unattended run.",
				background.allows(tool("aTool", capability)));
			assertFalse(capability + " in " + background.capabilitiesWithheldInBackground(),
				background.capabilitiesWithheldInBackground().contains(capability));
		}
	}

	/** The explicit opt-out withholds nothing, so there is nothing to report. */
	public void testTheOptOutWithholdsNothing() {
		ToolAccessPolicy policy = clerkPolicy();
		allow(policy).setAll(true);

		assertEquals(Set.of(), auth(policy, Set.of("Clerk"), true).capabilitiesWithheldInBackground());
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

	private static Grant tools(String... names) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setTools(List.of(names));
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
