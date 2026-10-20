/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
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
		case "script-tool.changeData" -> ToolGroups.SCRIPT;
		case "ask-user.askUser" -> ToolGroups.INTERACTION;
		case "self.listTools" -> ToolGroups.INTROSPECTION;
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
		baseline(policy, group(ToolGroups.EGRESS));

		Decision decision = access(policy).check(Set.of(), "web.webFetch");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals("(baseline)", ((Decision.Granted) decision).byRole());
	}

	/**
	 * {@link ToolGroups#INTERACTION} needs no configuration at all.
	 *
	 * <p>
	 * The policy below declares an empty {@code <baseline>} and gives its one role an empty grant list,
	 * which is the narrowest state a policy can express. An agent that cannot ask a question is broken,
	 * so this capability is not left to configuration.
	 * </p>
	 */
	public void testInteractionIsGrantedWithoutAnyConfiguration() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(), grants()));
		assertEquals("The precondition of this test: the baseline grants nothing.",
			List.of(), policy.getBaseline().getGrants());

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
			role("Clerk", grants(tools("mcp__jira__createIssue")), grants())));

		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__createIssue") instanceof Decision.Granted);
	}

	/** One entry may name several tools; each of them is reached. */
	public void testOneEntryNamesSeveralTools() {
		RoleToolAccess access = access(policy(
			role("Clerk",
				grants(tools("mcp__jira__createIssue", "mcp__jira__addComment")), grants())));

		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__createIssue") instanceof Decision.Granted);
		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__addComment") instanceof Decision.Granted);
		assertTrue("A third tool of the same server was not named.",
			access.check(Set.of("Clerk"), "mcp__jira__deleteIssue") instanceof Decision.Denied);
	}

	/**
	 * A per-tool grant names a tool, and nothing that merely starts like it.
	 *
	 * <p>
	 * There is no pattern syntax: a tool that appears later - a new Java tool, one a
	 * {@code createScriptTool} call added to a factory, one a connected MCP server grew - is denied
	 * until an administrator names it. That is the fail-closed reading, and it is what keeps the group
	 * a tool costs a decision an administrator made rather than one whoever created the tool made.
	 * </p>
	 */
	public void testPerToolGrantIsNotAPrefix() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(tools("mcp__jira__create")), grants())));

		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__createIssue") instanceof Decision.Denied);
		assertTrue(access.check(Set.of("Clerk"), "mcp__jira__create") instanceof Decision.Granted);
	}

	/** A tool an escalation group holds is granted by name, like any other. */
	public void testPerToolGrantOfAnEscalationTool() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(tools("model-editor.createClass")), grants())));

		assertTrue(access.check(Set.of("Developer"), "model-editor.createClass") instanceof Decision.Granted);
		assertTrue("Its sibling in the same group was not named.",
			access.check(Set.of("Developer"), "model-editor.deleteModelElement") instanceof Decision.Denied);
	}

	/** The group of an escalation tool grants every tool in it, as any group grant does. */
	public void testGroupGrantOfAnEscalationGroup() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants())));

		Decision decision = access.check(Set.of("Developer"), "model-editor.createClass");

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals(ToolGroups.MODEL_STRUCTURE, ((Decision.Granted) decision).viaGroup());
	}

	/** A per-tool deny carves one tool out of a group another held role grants. */
	public void testPerToolDenyBeatsAnotherRolesGroupGrant() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants()),
			role("Probation", grants(), grants(tools("model-editor.createClass")))));

		assertTrue(access.check(Set.of("Developer", "Probation"), "model-editor.createClass")
			instanceof Decision.Denied);
		assertTrue("The rest of the group is untouched.",
			access.check(Set.of("Developer", "Probation"), "model-editor.deleteModelElement")
				instanceof Decision.Granted);
	}

	/** A deny by tool name beats a grant of that tool's whole group. */
	public void testPerToolDenyBeatsGroupGrant() {
		RoleToolAccess access = access(policy(
			role("Clerk",
				grants(group(ToolGroups.DATA_READ)),
				grants(tools("model-search.searchModel")))));

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
	 * ...and it beats the second implicit grant the same way.
	 *
	 * <p>
	 * {@code introspection} - {@code listTools}, an agent saying what it carries - is implicit for the
	 * same reason {@code interaction} is: withholding it is what made the agent guess at a named tool
	 * and blame a capability that had nothing to do with it. The asymmetry the design keeps is that
	 * silence cannot cause that and an explicit {@code <deny>} can, because the second is a decision
	 * somebody made. Every capability in {@link RoleToolAccess#IMPLICIT_BASELINE} is covered here, so a
	 * third one cannot be added without deciding this question for it too.
	 * </p>
	 */
	public void testDenyBeatsEveryImplicitGrant() {
		Map<String, String> toolsByCapability = Map.of(
			ToolGroups.INTERACTION, "ask-user.askUser",
			ToolGroups.INTROSPECTION, "self.listTools");

		for (String capability : RoleToolAccess.IMPLICIT_BASELINE) {
			String toolName = toolsByCapability.get(capability);
			assertNotNull("A newly implicit capability needs a tool here, and a decision about the"
				+ " deny: " + capability, toolName);

			ToolAccessPolicy policy = policy(role("Probation", grants(), grants(group(capability))));

			assertTrue(capability + " must be granted without being configured.",
				access(policy).check(Set.of("Clerk"), toolName) instanceof Decision.Granted);
			assertTrue("An explicit deny of " + capability + " must still win.",
				access(policy).check(Set.of("Probation"), toolName) instanceof Decision.Denied);
			assertTrue(capability + " must survive an undeclared background allowlist.",
				access(policy).allowedInBackground(toolName));
		}
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
			role("Clerk", grants(tools("instance-browser.listInstances")), grants())));
		RoleToolAccess bySimple = access(policy(
			role("Clerk", grants(tools("listInstances")), grants())));

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
			role("Clerk", grants(tools("instance-browser__listInstances")), grants())));

		ToolIdentity tool =
			new ToolIdentity("listInstances", "instance-browser.listInstances", ToolGroups.DATA_READ);

		assertTrue(access.check(Set.of("Clerk"), tool) instanceof Decision.Granted);
	}

	/** A group assignment in the policy matches either of a tool's two names, too. */
	public void testConfiguredGroupMatchesEitherName() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.EGRESS)), grants()));
		classify(policy, ToolGroups.EGRESS, "gitea.createGiteaIssue");

		ToolIdentity tool = new ToolIdentity("createGiteaIssue", "gitea.createGiteaIssue", null);

		assertEquals(ToolGroups.EGRESS, access(policy).groupOf(tool));
		assertTrue(access(policy).check(Set.of("Clerk"), tool) instanceof Decision.Granted);
	}

	/**
	 * A {@code <tool-groups>} entry classifies an otherwise ungrouped TL-Script tool, which is how an
	 * administrator makes one usable at all.
	 *
	 * <p>
	 * A TL-Script tool has no annotation to carry a group, so it is ungrouped and denied to everyone
	 * until the policy classifies it. The entry has to match the <code>{factory}.{tool}</code> form,
	 * which is the name the tool is configured under and the one an administrator writes.
	 * </p>
	 *
	 * <p>
	 * Every tool is named, one by one. A second tool of the same factory is a second decision, and it
	 * is denied until an administrator takes it - which is what keeps whoever may call
	 * {@code createScriptTool} from choosing the capability their new tool costs.
	 * </p>
	 */
	public void testConfiguredGroupClassifiesAnUngroupedScriptTool() {
		ToolAccessPolicy policy = policy(role("Clerk", grants(group(ToolGroups.DATA_READ)), grants()));
		classify(policy, ToolGroups.DATA_READ, "reporting.listOpenTickets");

		RoleToolAccess access = access(policy);

		// As the registry identifies it: bare name, qualified {factory}.{tool}, no declared group.
		ToolIdentity scriptTool = new ToolIdentity("listOpenTickets", "reporting.listOpenTickets", null);

		assertEquals(ToolGroups.DATA_READ, access.groupOf(scriptTool));
		assertTrue(access.check(Set.of("Clerk"), scriptTool) instanceof Decision.Granted);

		// A second tool of the same factory was not named, so it stays ungrouped and denied.
		ToolIdentity sibling = new ToolIdentity("wipeEverything", "reporting.wipeEverything", null);
		assertNull(access.groupOf(sibling));
		assertTrue(access.check(Set.of("Clerk"), sibling) instanceof Decision.Denied);
	}

	/** A denial says which capability is missing, so the model and an operator can both act on it. */
	public void testDenialNamesTheMissingCapability() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		Decision decision = access.check(Set.of("Clerk"), "script-tool.changeData");

		String reason = ((Decision.Denied) decision).reason();
		assertTrue(reason, reason.contains("script-tool.changeData"));
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
		classify(policy, ToolGroups.EGRESS, "mcp__jira__createIssue", "model-search.searchModel");

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

	/** A group grant is explained as the grant of the role that wrote it. */
	public void testExplainNamesTheGrantingRole() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		ToolAccessExplanation explanation =
			access.explain(Set.of("Clerk"), identity("instance-browser.listInstances"));

		assertTrue(explanation.allowed());
		assertEquals(ToolAccessRule.Kind.ROLE_GRANT, explanation.kind());
		assertEquals("Clerk", explanation.decidedBy().roleName());
		assertEquals(ToolGroups.DATA_READ, explanation.decidedBy().group());
		assertFalse("The entry named a group, not this tool.", explanation.decidedBy().isPerTool());
		assertNull("Nothing was overridden.", explanation.overridden());
	}

	/** A per-tool grant is told apart from a group grant, because the sentences differ. */
	public void testExplainTellsAPerToolGrantApart() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(tools("model-editor.createClass")), grants())));

		ToolAccessRule rule =
			access.explain(Set.of("Clerk"), identity("model-editor.createClass")).decidedBy();

		assertEquals(ToolAccessRule.Kind.ROLE_GRANT, rule.kind());
		assertTrue(rule.isPerTool());
		assertEquals("model-editor.createClass", rule.namedTool());
		assertNull("A per-tool entry names no group.", rule.group());
	}

	/** A deny names the role that wrote it <em>and</em> the grant it took away. */
	public void testExplainNamesTheOverriddenGrant() {
		RoleToolAccess access = access(policy(
			role("Developer", grants(group(ToolGroups.MODEL_STRUCTURE)), grants()),
			role("Probation", grants(), grants(group(ToolGroups.MODEL_STRUCTURE)))));

		ToolAccessExplanation explanation = access.explain(Set.of("Developer", "Probation"),
			identity("model-editor.createClass"));

		assertFalse(explanation.allowed());
		assertEquals(ToolAccessRule.Kind.ROLE_DENY, explanation.kind());
		assertEquals("Probation", explanation.decidedBy().roleName());
		assertEquals("Without this, the sentence cannot say what the deny took away.",
			ToolAccessRule.Kind.ROLE_GRANT, explanation.overridden().kind());
		assertEquals("Developer", explanation.overridden().roleName());
	}

	/** A deny that took nothing away says so by overriding nothing. */
	public void testExplainOverridesNothingWhenNothingGranted() {
		RoleToolAccess access = access(policy(
			role("Probation", grants(), grants(group(ToolGroups.MODEL_STRUCTURE)))));

		ToolAccessExplanation explanation =
			access.explain(Set.of("Probation"), identity("model-editor.createClass"));

		assertEquals(ToolAccessRule.Kind.ROLE_DENY, explanation.kind());
		assertNull(explanation.overridden());
	}

	/** The three baseline states are three different rules, not one. */
	public void testExplainTellsTheBaselineStatesApart() {
		ToolAccessPolicy declared = policy();
		baseline(declared, group(ToolGroups.DATA_READ));
		assertEquals(ToolAccessRule.Kind.BASELINE,
			access(declared).explain(Set.of(), identity("instance-browser.listInstances")).kind());

		// policy() declares an empty <baseline>, which grants nothing.
		assertEquals(ToolAccessRule.Kind.NOT_GRANTED,
			access(policy()).explain(Set.of(), identity("instance-browser.listInstances")).kind());

		ToolAccessPolicy absent = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		assertEquals(ToolAccessRule.Kind.DEFAULT_BASELINE,
			access(absent).explain(Set.of(), identity("instance-browser.listInstances")).kind());
	}

	/** The implicit baseline is its own rule: nobody configured it and nobody can take it away. */
	public void testExplainNamesTheImplicitBaseline() {
		RoleToolAccess access = access(policy());

		assertEquals(ToolAccessRule.Kind.IMPLICIT_BASELINE,
			access.explain(Set.of(), identity("ask-user.askUser")).kind());
		assertEquals(ToolAccessRule.Kind.IMPLICIT_BASELINE,
			access.explain(Set.of(), identity("self.listTools")).kind());
	}

	/** An ungrouped tool is denied by a rule of its own, because the fix is a different one. */
	public void testExplainTellsUnclassifiedFromUngranted() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ)), grants())));

		assertEquals(ToolAccessRule.Kind.UNCLASSIFIED,
			access.explain(Set.of("Clerk"), identity("nowhere.nothing")).kind());
		assertEquals(ToolAccessRule.Kind.NOT_GRANTED,
			access.explain(Set.of("Clerk"), identity("model-editor.createClass")).kind());
	}

	/** Where a tool's group comes from, which is what explains a de-classification. */
	public void testGroupSource() {
		ToolAccessPolicy policy = policy();
		classify(policy, ToolGroups.EGRESS, "script-tool.changeData");

		RoleToolAccess access = access(policy);

		assertEquals("A <tool-groups> entry wins over the declaration and says so.",
			RoleToolAccess.GroupSource.CONFIGURED,
			access.groupSourceOf(identity("script-tool.changeData")));
		assertEquals(ToolGroups.EGRESS, access.groupOf(identity("script-tool.changeData")));
		assertEquals(RoleToolAccess.GroupSource.DECLARED,
			access.groupSourceOf(identity("instance-browser.listInstances")));
		assertEquals(RoleToolAccess.GroupSource.NONE,
			access.groupSourceOf(identity("nowhere.nothing")));
	}

	/** The explanation of a capability reads the group entries, and knows the baseline states. */
	public void testExplainCapability() {
		RoleToolAccess access = access(policy(
			role("Clerk", grants(group(ToolGroups.DATA_READ), tools("model-editor.createClass")),
				grants())));

		assertEquals("Clerk",
			access.explainCapability(Set.of("Clerk"), ToolGroups.DATA_READ).decidedBy().roleName());
		assertEquals("A per-tool grant says nothing about the capability as such.",
			ToolAccessRule.Kind.NOT_GRANTED,
			access.explainCapability(Set.of("Clerk"), ToolGroups.MODEL_STRUCTURE).kind());
	}

	/** The identity of a tool as these tests know it: name and declared group, no registry. */
	private static ToolIdentity identity(String toolName) {
		return new ToolIdentity(toolName, null, GROUPS.groupOf(toolName));
	}

	private static RoleToolAccess access(ToolAccessPolicy policy) {
		return new RoleToolAccess(policy, GROUPS);
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

	private static Grant tools(String... names) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setTools(List.of(names));
		return grant;
	}

	/** Puts the named tools into the named group of the given policy. */
	private static void classify(ToolAccessPolicy policy, String group, String... toolNames) {
		GroupMembership membership = TypedConfiguration.newConfigItem(GroupMembership.class);
		membership.setName(group);
		membership.setTools(List.of(toolNames));
		policy.getToolGroups().put(group, membership);
	}

	/** A tool with neither a declared nor a configured group has none. */
	public void testGroupOfUnknownTool() {
		assertNull(access(policy()).groupOf("nowhere.nothing"));
		assertEquals(Map.of(), TypedConfiguration.newConfigItem(ToolAccessPolicy.class).getToolGroups());
	}
}
