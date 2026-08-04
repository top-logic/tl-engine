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
import com.top_logic.tl.ai.core.security.RoleToolAccess.Decision;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ToolRef;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Tests for {@link RoleToolAccess}.
 *
 * <p>
 * One test per row of the "Semantics, stated precisely" table in section 4.1 of
 * {@code doc/security/agent-security-architecture.md}, which is the normative specification of this
 * class. The only row not covered here is the boot-time rejection of an unknown role name, which
 * belongs to the service that reads the policy - see {@code TestAiToolAccessService}.
 * </p>
 */
public class TestRoleToolAccess extends TestCase {

	/**
	 * The typed configuration items resolve nested config types through the {@code TypeIndex},
	 * which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRoleToolAccess.class)));
	}

	private static final DeclaredGroups GROUPS = toolName -> switch (toolName) {
		case "instance-browser.listInstances", "model-search.searchModel" -> ToolGroups.DATA_READ;
		case "model-editor.createClass", "model-editor.deleteModelElement" -> ToolGroups.MODEL_STRUCTURE;
		case "script-tool.executeTLScript" -> ToolGroups.SCRIPT;
		case "ask-user.askUser" -> ToolGroups.INTERACTION;
		case "web.webFetch" -> ToolGroups.EGRESS;
		default -> null;
	};

	/** A role granted a group may use its tools. */
	public void testGroupGrant() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		Decision decision = access.check(Set.of("Clerk"), "instance-browser.listInstances");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals("Clerk", ((Decision.Granted) decision).byRole());
		assertEquals(ToolGroups.DATA_READ, ((Decision.Granted) decision).viaGroup());
	}

	/** Deny by default: a group that was never granted is not usable. */
	public void testUngrantedGroupDenied() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		Decision decision = access.check(Set.of("Clerk"), "model-editor.createClass");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
		assertTrue(((Decision.Denied) decision).reason().contains(ToolGroups.MODEL_STRUCTURE));
	}

	/** A role that appears nowhere in the policy gets nothing. */
	public void testUnknownRoleDenied() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		assertTrue(access.check(Set.of("Stranger"), "instance-browser.listInstances")
			instanceof Decision.Denied);
	}

	/** Grants of several held roles are unioned. */
	public void testGrantsUnionAcrossRoles() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()),
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants())));

		assertTrue(access.check(Set.of("Clerk", "Developer"), "instance-browser.listInstances")
			instanceof Decision.Granted);
		assertTrue(access.check(Set.of("Clerk", "Developer"), "model-editor.createClass")
			instanceof Decision.Granted);
	}

	/** An explicit deny on one held role beats a grant on another. */
	public void testDenyWinsOverGrantFromAnotherRole() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants()),
			role("Probation", grants(), grants(group(ToolGroups.MODEL_STRUCTURE)))));

		Decision decision = access.check(Set.of("Developer", "Probation"), "model-editor.createClass");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
		assertTrue(((Decision.Denied) decision).reason().contains("Probation"));
	}

	/** The baseline applies to everyone, including a user with no roles at all. */
	public void testBaselineAppliesWithoutRoles() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		policy.getBaseline().add(group(ToolGroups.EGRESS));

		Decision decision = access(policy).check(Set.of(), "web.webFetch");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals("(baseline)", ((Decision.Granted) decision).byRole());
	}

	/**
	 * {@link ToolGroups#INTERACTION} needs no configuration at all.
	 *
	 * <p>
	 * The policy below declares no {@code <baseline>} and gives its one role an empty grant list, which
	 * is the state a forgotten baseline produces. An agent that cannot ask a question is broken, so this
	 * capability is not left to configuration.
	 * </p>
	 */
	public void testInteractionIsGrantedWithoutAnyConfiguration() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(), grants()));
		assertEquals("The precondition of this test: nothing declares the baseline.",
			List.of(), policy.getBaseline());

		RoleToolAccess access = access(policy);

		for (Set<String> heldRoles : List.of(Set.<String>of(), Set.of("Clerk"), Set.of("Stranger"))) {
			Decision decision = access.check(heldRoles, "ask-user.askUser");

			assertTrue(heldRoles + ": " + decision, decision instanceof Decision.Granted);
			assertEquals("(baseline)", ((Decision.Granted) decision).byRole());
		}

		assertTrue("Nothing else becomes reachable through it.",
			access.check(Set.of("Clerk"), "instance-browser.listInstances") instanceof Decision.Denied);
	}

	/** The implicit grant reaches an unattended run too, whatever the allowlist says. */
	public void testInteractionIsAllowedInBackgroundWithoutBeingListed() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(), grants()));

		assertTrue("An undeclared allowlist must not silence a background run.",
			access(policy).allowedInBackground("ask-user.askUser"));

		allow(policy, group(ToolGroups.DATA_READ));
		assertTrue("Nor must an allowlist that simply does not mention it.",
			access(policy).allowedInBackground("ask-user.askUser"));
	}

	/** A tool in no group is denied even to a broadly granted role. */
	public void testUngroupedToolDenied() {
		RoleToolAccess access = access(policy(
			role("Developer",
				grants(group(ToolGroups.DATA_READ), group(ToolGroups.MODEL_STRUCTURE)), grants())));

		Decision decision = access.check(Set.of("Developer"), "mcp__jira__createIssue");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
		assertTrue(((Decision.Denied) decision).reason().contains("no tool group"));
	}

	/** An ungrouped tool becomes usable through an explicit per-tool grant. */
	public void testPerToolGrantReachesUngroupedTool() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(tool("mcp__jira__createIssue")), grants())));

		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__createIssue") instanceof Decision.Granted);
	}

	/** A wildcard grant never reaches a privileged group. */
	public void testWildcardDoesNotReachPrivilegedGroup() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(tool("model-editor.*"), tool("instance-browser.*")), grants())));

		assertTrue(access.check(Set.of("Clerk"), "model-editor.createClass") instanceof Decision.Denied);
		assertTrue(access.check(Set.of("Clerk"), "instance-browser.listInstances") instanceof Decision.Granted);
	}

	/** A privileged tool named in full is granted - the administrator was explicit. */
	public void testExplicitPrivilegedToolGrant() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(tool("model-editor.createClass")), grants())));

		assertTrue(access.check(Set.of("Developer"), "model-editor.createClass") instanceof Decision.Granted);
	}

	/** A privileged group named in full is granted - the wildcard rule narrows nothing else. */
	public void testExplicitPrivilegedGroupGrant() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants())));

		Decision decision = access.check(Set.of("Developer"), "model-editor.createClass");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals(ToolGroups.MODEL_STRUCTURE, ((Decision.Granted) decision).viaGroup());
	}

	/**
	 * A wildcard <em>deny</em> may reach a privileged group, unlike a wildcard grant.
	 *
	 * <p>
	 * The asymmetry is the point: the rule exists so that a broad pattern cannot accidentally hand out
	 * an escalation primitive. A deny can only ever narrow access, so there is nothing to protect
	 * against.
	 * </p>
	 */
	public void testWildcardDenyMayReachPrivilegedGroup() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants()),
			role("Probation", grants(), grants(tool("model-editor.*")))));

		assertTrue(access.check(Set.of("Developer", "Probation"), "model-editor.createClass")
			instanceof Decision.Denied);
	}

	/** A deny by tool name beats a grant of that tool's whole group. */
	public void testPerToolDenyBeatsGroupGrant() {
		RoleToolAccess access = access(policy(
			role("Clerk",
				grants(group(ToolGroups.DATA_READ)),
				grants(tool("model-search.searchModel")))));

		assertTrue(access.check(Set.of("Clerk"), "instance-browser.listInstances")
			instanceof Decision.Granted);
		Decision decision = access.check(Set.of("Clerk"), "model-search.searchModel");
		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
		assertTrue(((Decision.Denied) decision).reason().contains("Clerk"));
	}

	/**
	 * A deny beats the baseline too - including the implicit {@link RoleToolAccess#IMPLICIT_BASELINE}
	 * grant.
	 *
	 * <p>
	 * The baseline is a grant, and the table says a deny wins over any grant. Evaluating denies first
	 * is what makes "carve this out for everyone with this role" mean what it says. The implicit grant
	 * exists so that <em>silence</em> cannot leave an agent unable to ask a question; an administrator
	 * who writes the deny has said the opposite out loud, and that decision stands.
	 * </p>
	 */
	public void testDenyBeatsBaseline() {
		ToolAccessPolicy policy = policy(
			role("Probation", grants(), grants(group(ToolGroups.INTERACTION))));

		assertTrue(access(policy).check(Set.of("Probation"), "ask-user.askUser")
			instanceof Decision.Denied);
		assertTrue(access(policy).check(Set.of("Clerk"), "ask-user.askUser")
			instanceof Decision.Granted);
	}

	/**
	 * A per-tool grant matches either of a tool's two names.
	 *
	 * <p>
	 * The registry and the administrator use the qualified {@code {factory}.{tool}} name; the agent's
	 * catalog and the model use the bare one. A grant that matched only one of them would make a
	 * correctly spelled policy silently miss.
	 * </p>
	 */
	public void testPerToolGrantMatchesEitherName() {
		RoleToolAccess byQualified = access(policy(
			role("Clerk", grants(tool("instance-browser.listInstances")), grants())));
		RoleToolAccess bySimple = access(policy(
			role("Clerk", grants(tool("listInstances")), grants())));

		ToolIdentity tool =
			new ToolIdentity("listInstances", "instance-browser.listInstances", ToolGroups.DATA_READ);

		assertTrue(byQualified.check(Set.of("Clerk"), tool) instanceof Decision.Granted);
		assertTrue(bySimple.check(Set.of("Clerk"), tool) instanceof Decision.Granted);
	}

	/**
	 * A per-tool grant also matches the provider-safe form of the qualified name.
	 *
	 * <p>
	 * The registry keys tools by {@code factory__tool}, and that is the form an MCP-facing name uses,
	 * so a policy written against it must work as well as one written in dot notation.
	 * </p>
	 */
	public void testPerToolGrantMatchesProviderSafeName() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(tool("instance-browser__listInstances")), grants())));

		ToolIdentity tool =
			new ToolIdentity("listInstances", "instance-browser.listInstances", ToolGroups.DATA_READ);

		assertTrue(access.check(Set.of("Clerk"), tool) instanceof Decision.Granted);
	}

	/** A group assignment in the policy matches either of a tool's two names, too. */
	public void testConfiguredGroupMatchesEitherName() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.EGRESS)), grants()));
		GroupMembership egress = TypedConfiguration.newConfigItem(GroupMembership.class);
		egress.setName(ToolGroups.EGRESS);
		egress.getTools().put("gitea.createGiteaIssue", toolRef("gitea.createGiteaIssue"));
		policy.getToolGroups().put(ToolGroups.EGRESS, egress);

		ToolIdentity tool = new ToolIdentity("createGiteaIssue", "gitea.createGiteaIssue", null);

		assertEquals(ToolGroups.EGRESS, access(policy).groupOf(tool));
		assertTrue(access(policy).check(Set.of("Clerk"), tool) instanceof Decision.Granted);
	}

	/**
	 * A {@code {factory}.*} wildcard classifies a whole factory, which is how an administrator
	 * grants TL-Script tools.
	 *
	 * <p>
	 * A TL-Script tool has no annotation to carry a group, so it is ungrouped and denied to everyone
	 * until the policy classifies it. Naming each one is not workable for a namespace agents author
	 * into, so this is the mechanism that has to work - and it has to match the
	 * <code>{factory}.{tool}</code> form, which is the name the tool is configured under and the one
	 * an administrator writes.
	 * </p>
	 *
	 * <p>
	 * Note what this delegates: everything the factory ever contains gets the group, including tools
	 * created later. See {@code doc/security/tool-access-configuration.md}.
	 * </p>
	 */
	public void testFactoryWildcardClassifiesAnUngroupedScriptTool() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		GroupMembership dataRead = TypedConfiguration.newConfigItem(GroupMembership.class);
		dataRead.setName(ToolGroups.DATA_READ);
		dataRead.getTools().put("reporting.*", toolRef("reporting.*"));
		policy.getToolGroups().put(ToolGroups.DATA_READ, dataRead);

		RoleToolAccess access = access(policy);

		// As the registry identifies it: bare name, qualified {factory}.{tool}, no declared group.
		ToolIdentity scriptTool = new ToolIdentity("listOpenTickets", "reporting.listOpenTickets", null);

		assertEquals(ToolGroups.DATA_READ, access.groupOf(scriptTool));
		assertTrue(access.check(Set.of("Clerk"), scriptTool) instanceof Decision.Granted);

		// A tool of another factory is untouched by the wildcard and stays ungrouped.
		ToolIdentity other = new ToolIdentity("wipeEverything", "custom.wipeEverything", null);
		assertNull(access.groupOf(other));
		assertTrue(access.check(Set.of("Clerk"), other) instanceof Decision.Denied);
	}

	/** A denial says which capability is missing, so the model and an operator can both act on it. */
	public void testDenialNamesTheMissingCapability() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		Decision decision = access.check(Set.of("Clerk"), "script-tool.executeTLScript");

		String reason = ((Decision.Denied) decision).reason();
		assertTrue(reason, reason.contains("script-tool.executeTLScript"));
		assertTrue(reason, reason.contains(ToolGroups.SCRIPT));
	}

	/** The background allowlist narrows, it does not widen: a tool must pass both checks. */
	public void testBackgroundAllowlistIsAnIntersectionNotAGrant() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(), grants()));
		allow(policy, group(ToolGroups.DATA_READ));

		RoleToolAccess access = access(policy);

		// On the allowlist, but no role grants it.
		assertTrue(access.allowedInBackground("instance-browser.listInstances"));
		assertTrue(access.check(Set.of("Clerk"), "instance-browser.listInstances")
			instanceof Decision.Denied);
	}

	/** The policy can group tools that carry no declaration, and overrides one that does. */
	public void testConfiguredGroupOverridesDeclaredGroup() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.EGRESS)), grants()));
		GroupMembership egress = TypedConfiguration.newConfigItem(GroupMembership.class);
		egress.setName(ToolGroups.EGRESS);
		egress.getTools().put("mcp__jira__*", toolRef("mcp__jira__*"));
		egress.getTools().put("model-search.searchModel", toolRef("model-search.searchModel"));
		policy.getToolGroups().put(ToolGroups.EGRESS, egress);

		RoleToolAccess access = access(policy);

		assertEquals(ToolGroups.EGRESS, access.groupOf("mcp__jira__createIssue"));
		assertEquals(ToolGroups.EGRESS, access.groupOf("model-search.searchModel"));
		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__createIssue") instanceof Decision.Granted);
	}

	/**
	 * An undeclared allowlist admits nothing.
	 *
	 * <p>
	 * The absent state of the compensating control must not be the permissive one: an administrator
	 * who writes a {@code <tool-access>} matrix and never learns the allowlist exists must not get
	 * unattended runs bounded only by the user's grants. {@link AiToolAccessService} refuses such a
	 * policy at boot; this is the same rule at the decision point.
	 * </p>
	 */
	public void testBackgroundDeniedWithoutAllowlist() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		assertNull("The precondition of this test: nothing was declared.",
			policy.getBackgroundAllowlist());

		RoleToolAccess access = access(policy);

		assertFalse("An undeclared background allowlist must admit nothing.",
			access.allowedInBackground("model-editor.createClass"));
		assertFalse("Not even a tool the role is granted interactively.",
			access.allowedInBackground("instance-browser.listInstances"));
		assertTrue("The implicit grant is the one exception - see IMPLICIT_BASELINE.",
			access.allowedInBackground("ask-user.askUser"));
	}

	/**
	 * An empty declaration is the strictest setting: nothing without a human, beyond the implicit
	 * {@link RoleToolAccess#IMPLICIT_BASELINE} grant.
	 */
	public void testEmptyAllowlistAdmitsNothing() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		allow(policy);

		RoleToolAccess access = access(policy);

		assertFalse(access.allowedInBackground("instance-browser.listInstances"));
		assertFalse(access.allowedInBackground("model-editor.createClass"));
	}

	/** The explicit opt-out, and the only way to get unrestricted background runs. */
	public void testAllTrueAdmitsEverything() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		BackgroundAllowlist allowlist = allow(policy);
		allowlist.setAll(true);

		RoleToolAccess access = access(policy);

		assertTrue(access.allowedInBackground("instance-browser.listInstances"));
		assertTrue("The user's own grants are still the other half of the intersection.",
			access.allowedInBackground("model-editor.createClass"));
	}

	/** With an allowlist, a background run reaches only the listed groups. */
	public void testBackgroundAllowlistNarrows() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		allow(policy, group(ToolGroups.DATA_READ));

		RoleToolAccess access = access(policy);

		assertTrue(access.allowedInBackground("instance-browser.listInstances"));
		assertFalse(access.allowedInBackground("model-editor.createClass"));
	}

	private static RoleToolAccess access(ToolAccessPolicy policy) {
		return new RoleToolAccess(policy, GROUPS);
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

	private static Grant tool(String name) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setTool(name);
		return grant;
	}

	private static ToolRef toolRef(String name) {
		ToolRef ref = TypedConfiguration.newConfigItem(ToolRef.class);
		ref.setName(name);
		return ref;
	}

	/** Guards the assumption that the three escalation primitives stay privileged. */
	public void testPrivilegedGroups() {
		assertEquals(Set.of(ToolGroups.SCRIPT, ToolGroups.MODEL_STRUCTURE, ToolGroups.AGENT_AUTHORING),
			ToolGroups.PRIVILEGED);
		assertTrue(ToolGroups.isPrivileged(ToolGroups.SCRIPT));
		assertFalse(ToolGroups.isPrivileged(ToolGroups.DATA_READ));
		assertFalse(ToolGroups.isPrivileged(null));
	}

	/** A tool with neither a declared nor a configured group has none. */
	public void testGroupOfUnknownTool() {
		assertNull(access(policy()).groupOf("nowhere.nothing"));
		assertEquals(Map.of(), TypedConfiguration.newConfigItem(ToolAccessPolicy.class).getToolGroups());
	}
}
