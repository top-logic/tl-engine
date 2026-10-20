/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.EffectiveToolAccess;
import com.top_logic.tl.ai.core.security.EffectiveToolAccess.CapabilityAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAccessExplanation;
import com.top_logic.tl.ai.core.security.ToolAccessRule;
import com.top_logic.tl.ai.core.security.ToolAccessRule.Kind;
import com.top_logic.tl.ai.core.security.ToolIdentity;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * The read-only query behind the "Agent-Sicherheit" view, one persona at a time.
 *
 * <p>
 * {@link SecDemoToolAccessTest} asserts what the enforcement gate <em>does</em>; this asserts what the
 * query <em>says about it</em>, including the rule that decided and the rule it overrode. The two must
 * agree everywhere, which is what {@link #testTheQueryAgreesWithTheGate()} pins - a provenance
 * sentence that disagreed with the gate would be worse than no sentence at all.
 * </p>
 *
 * <p>
 * The expectations are the ones hand-computed for the interactive prototype before either existed
 * ({@code sec-config-ux-proto.artifacts/expected-simulator-results.md}), so a change to the fixture,
 * to the evaluation or to the view breaks a row of a document rather than an opinion.
 * </p>
 *
 * @see SecDemoPersonaTest
 */
public class SecDemoEffectiveAccessTest extends SecDemoPersonaTest {

	/** The capabilities the fixture grants nobody, and that therefore have to read the same way. */
	private static final List<String> UNGRANTED_GROUPS = List.of(
		ToolGroups.MODEL_STRUCTURE, ToolGroups.AGENT_AUTHORING, ToolGroups.AGENT_META,
		ToolGroups.WORKSPACE, ToolGroups.EGRESS);

	/**
	 * E1 of the expectations: anna, interactive.
	 *
	 * <p>
	 * Six capabilities allowed - the two implicit ones plus the four her role grants - five denied
	 * because no role grants them, and the unclassified tool denied by a rule of its own.
	 * </p>
	 */
	public void testAnnaInteractive() {
		EffectiveToolAccess access = accessFor(ANNA, false);

		assertRule(access, ToolGroups.INTERACTION, true, Kind.IMPLICIT_BASELINE, null);
		assertRule(access, ToolGroups.INTROSPECTION, true, Kind.IMPLICIT_BASELINE, null);
		for (String group : List.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ,
			ToolGroups.DATA_WRITE, ToolGroups.SCRIPT)) {
			ToolAccessRule rule = assertRule(access, group, true, Kind.ROLE_GRANT, null);
			assertEquals("The fixture grants these through projectPartner and nothing else.",
				"projectPartner", rule.roleName());
		}
		for (String group : UNGRANTED_GROUPS) {
			assertRule(access, group, false, Kind.NOT_GRANTED, null);
		}

		assertEquals("Six allowed capabilities, as hand-computed.", 6, allowedCount(access));

		ToolAccessExplanation unclassified = access.explain(UNCLASSIFIED_SCRIPT_TOOL);
		assertFalse(unclassified.allowed());
		assertEquals("The tool belongs to no group, which is a different fix from a missing grant.",
			Kind.UNCLASSIFIED, unclassified.kind());
	}

	/**
	 * E2 of the expectations: ben, interactive - one discriminating row against anna.
	 */
	public void testBenInteractive() {
		EffectiveToolAccess access = accessFor(BEN, false);

		for (String group : List.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ, ToolGroups.SCRIPT)) {
			assertEquals("ticketReader",
				assertRule(access, group, true, Kind.ROLE_GRANT, null).roleName());
		}
		assertRule(access, ToolGroups.DATA_WRITE, false, Kind.NOT_GRANTED, null);
		for (String group : UNGRANTED_GROUPS) {
			assertRule(access, group, false, Kind.NOT_GRANTED, null);
		}

		assertEquals("Five allowed capabilities: the two implicit ones plus the three of ticketReader.",
			5, allowedCount(access));
	}

	/**
	 * E3 of the expectations: anna, unattended.
	 *
	 * <p>
	 * The row this whole API exists for. {@code data.write} flips to denied, and the reason is not her
	 * role - which still grants it - but the allowlist. Saying "no role grants it" here would send an
	 * administrator to the wrong line of the policy.
	 * </p>
	 */
	public void testAnnaBackground() {
		EffectiveToolAccess access = accessFor(ANNA, true);

		assertRule(access, ToolGroups.INTERACTION, true, Kind.IMPLICIT_BASELINE, null);
		assertRule(access, ToolGroups.INTROSPECTION, true, Kind.IMPLICIT_BASELINE, null);
		for (String group : List.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ, ToolGroups.SCRIPT)) {
			assertRule(access, group, true, Kind.ROLE_GRANT, null);
		}

		ToolAccessExplanation write = access.explainCapability(ToolGroups.DATA_WRITE);
		assertFalse("The fixture leaves data.write off the background allowlist.", write.allowed());
		assertEquals(Kind.BACKGROUND_CEILING, write.kind());
		assertNotNull("The overridden grant is what makes the sentence.", write.overridden());
		assertEquals(Kind.ROLE_GRANT, write.overridden().kind());
		assertEquals("projectPartner", write.overridden().roleName());

		assertEquals("Five allowed capabilities: data.write is the one the ceiling takes away.",
			5, allowedCount(access));
		assertRule(access, ToolGroups.MODEL_STRUCTURE, false, Kind.NOT_GRANTED, null);
		assertNull("The ceiling took nothing away here, so it must not be blamed: the allowlist is"
			+ " not where an administrator fixes a capability no role grants.",
			access.explainCapability(ToolGroups.MODEL_STRUCTURE).overridden());
	}

	/**
	 * The write tool of the fixture, in both modes, told apart by its reason.
	 *
	 * <p>
	 * The capability rows above are about groups; this is the same contrast at the tool the personas
	 * actually call, which is what a reader of the view expands the row to see.
	 * </p>
	 */
	public void testTheWriteToolIsCappedNotUngranted() {
		ToolAccessExplanation attended =
			accessFor(ANNA, false).explain(CLASSIFIED_WRITE_SCRIPT_TOOL);
		assertTrue(attended.allowed());
		assertEquals(Kind.ROLE_GRANT, attended.decidedBy().kind());

		ToolAccessExplanation unattended =
			accessFor(ANNA, true).explain(CLASSIFIED_WRITE_SCRIPT_TOOL);
		assertFalse(unattended.allowed());
		assertEquals(Kind.BACKGROUND_CEILING, unattended.kind());
		assertEquals("projectPartner", unattended.overridden().roleName());

		assertEquals("Ben is refused the same tool for a different reason, and the view has to say"
			+ " which.", Kind.NOT_GRANTED,
			accessFor(BEN, false).explain(CLASSIFIED_WRITE_SCRIPT_TOOL).kind());
	}

	/**
	 * Dora is indistinguishable from Anna at the tool layer - and the view says why that is not the
	 * whole story.
	 *
	 * <p>
	 * The bonus check of the expectations: identical rows, plus the cross-layer warning that her role
	 * holds data capabilities the model rights do not back. Without the footer, the view would report
	 * a working configuration for the fixture's designated failure mode.
	 * </p>
	 */
	public void testDoraReadsLikeAnnaAndWarns() {
		EffectiveToolAccess dora = accessFor(DORA, false);
		EffectiveToolAccess anna = accessFor(ANNA, false);

		assertEquals("Same role on the security root, so the tool layer cannot tell them apart.",
			verdicts(anna), verdicts(dora));

		Map<String, Set<String>> warnings = AiToolAccessService.getInstance()
			.dataRolesWithoutModelRights();
		assertTrue("projectPartner holds data.read and data.write and appears in the model rights,"
			+ " so this fixture warns about neither role; if that changed, the footer of the view"
			+ " changed with it: " + warnings, warnings.isEmpty());
	}

	/**
	 * Root: every row allowed, and every row saying that this proves nothing about the policy.
	 */
	public void testAdministratorBypassIsNamedAsSuch() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);
		EffectiveToolAccess access = AiToolAccessService.effectiveAccessFor(administrator, false);

		assertTrue(access.isAdmin());
		for (CapabilityAccess row : access.capabilities()) {
			assertTrue("An administrator reaches every capability: " + row.group(), row.isAllowed());
		}
		ToolAccessExplanation structure = access.explainCapability(ToolGroups.MODEL_STRUCTURE);
		assertEquals(Kind.ADMIN_BYPASS, structure.kind());
		assertEquals("Without naming what the bypass covered up, the row reads as a statement about"
			+ " the policy - which it is not.", Kind.NOT_GRANTED, structure.overridden().kind());

		ToolAccessExplanation unclassified = access.explain(UNCLASSIFIED_SCRIPT_TOOL);
		assertTrue(unclassified.allowed());
		assertEquals(Kind.ADMIN_BYPASS, unclassified.kind());
		assertEquals(Kind.UNCLASSIFIED, unclassified.overridden().kind());
	}

	/**
	 * A3: the background allowlist binds administrators too, and the view has to show that.
	 */
	public void testAdministratorIsCappedInBackground() {
		Person administrator = anyAdministrator();
		EffectiveToolAccess access = AiToolAccessService.effectiveAccessFor(administrator, true);

		ToolAccessExplanation write = access.explainCapability(ToolGroups.DATA_WRITE);
		assertFalse("An unattended run has nobody to approve, whoever it acts as.", write.allowed());
		assertEquals(Kind.BACKGROUND_CEILING, write.kind());
		assertEquals("The sentence has to name what was capped, which for an administrator is the"
			+ " bypass itself.", Kind.ADMIN_BYPASS, write.overridden().kind());

		assertTrue("The implicit capabilities are not rationed by the allowlist.",
			access.explainCapability(ToolGroups.INTERACTION).allowed());
	}

	/**
	 * The hypothetical case: a role, with no account holding it.
	 */
	public void testHypotheticalRole() {
		EffectiveToolAccess access =
			AiToolAccessService.effectiveAccessForRoles(Set.of("ticketReader"), false);

		assertFalse("A role is not an administrator; the bypass belongs to the account.",
			access.isAdmin());
		assertEquals(Set.of("ticketReader"), access.roleNames());
		assertRule(access, ToolGroups.DATA_READ, true, Kind.ROLE_GRANT, null);
		assertRule(access, ToolGroups.DATA_WRITE, false, Kind.NOT_GRANTED, null);
	}

	/**
	 * The catalog's own columns: which roles reach a tool, where its group comes from, and the tool
	 * nobody classified.
	 */
	public void testCatalogColumns() {
		AiToolAccessService service = AiToolAccessService.getInstance();
		RoleToolAccess policy = service.getAccess();

		ToolIdentity readTool = identify(LIST_TOOL);
		assertEquals("Both roles hold data.read.", Set.of("projectPartner", "ticketReader"),
			service.rolesReaching(readTool));
		assertFalse("The fixture declares an empty baseline, so nothing reaches everyone.",
			service.baselineReaches(readTool));
		assertEquals(RoleToolAccess.GroupSource.DECLARED, policy.groupSourceOf(readTool));

		ToolIdentity scriptTool = identify(CLASSIFIED_READ_SCRIPT_TOOL);
		assertEquals("A TL-Script tool has no annotation, so only <tool-groups> can classify it.",
			RoleToolAccess.GroupSource.CONFIGURED, policy.groupSourceOf(scriptTool));
		assertEquals(ToolGroups.DATA_READ, policy.groupOf(scriptTool));

		ToolIdentity writeTool = identify(CLASSIFIED_WRITE_SCRIPT_TOOL);
		assertEquals("Only the partner holds data.write.", Set.of("projectPartner"),
			service.rolesReaching(writeTool));

		ToolIdentity unclassified = identify(UNCLASSIFIED_SCRIPT_TOOL);
		assertEquals(RoleToolAccess.GroupSource.NONE, policy.groupSourceOf(unclassified));
		assertNull(policy.groupOf(unclassified));
		assertEquals("The catalog's warning row: nobody reaches it.", Set.of(),
			service.rolesReaching(unclassified));
		assertFalse(service.baselineReaches(unclassified));
	}

	/**
	 * The unclassified tool of the fixture is in the view, and it is the only one.
	 */
	public void testTheUnclassifiedToolIsReported() {
		List<Tool> unclassified = accessFor(ANNA, false).unclassifiedTools();
		EffectiveToolAccess access = accessFor(ANNA, false);

		List<String> names = unclassified.stream().map(access::qualifiedName).toList();
		assertTrue("The fixture's deliberately unclassified tool must appear: " + names,
			names.contains(UNCLASSIFIED_SCRIPT_TOOL));
		assertEquals("If a second tool lost its group, the view would say so and this test would"
			+ " tell you which: " + names, 1, names.size());
	}

	/**
	 * The query and the gate never disagree - for every registered tool, every persona, both modes.
	 *
	 * <p>
	 * The one assertion that makes the view trustworthy. It compares against
	 * {@code TopLogicSecurityContext.hasToolPermission}, which is the check the agent loop performs,
	 * so a divergence is a view that lies about a real run.
	 * </p>
	 */
	public void testTheQueryAgreesWithTheGate() {
		for (String login : SecDemoSetupService.personaLogins()) {
			for (Boolean background : List.of(Boolean.FALSE, Boolean.TRUE)) {
				boolean unattended = background.booleanValue();
				EffectiveToolAccess access = accessFor(login, unattended);
				for (Tool tool : registeredTools()) {
					boolean gate = securityFor(login, unattended).hasToolPermission(tool);
					assertEquals(login + (unattended ? " (background)" : "")
						+ " on '" + access.qualifiedName(tool) + "'", gate,
						access.explain(tool).allowed());
				}
			}
		}
	}

	/** Every capability row of the given evaluation, by group name. */
	private static Map<String, Boolean> verdicts(EffectiveToolAccess access) {
		Map<String, Boolean> result = new TreeMap<>();
		for (CapabilityAccess row : access.capabilities()) {
			result.put(row.group(), Boolean.valueOf(row.isAllowed()));
		}
		return result;
	}

	private static int allowedCount(EffectiveToolAccess access) {
		int count = 0;
		for (CapabilityAccess row : access.capabilities()) {
			if (row.isAllowed()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Asserts the verdict and the deciding rule of one capability row.
	 *
	 * @param expectedRole
	 *        The role the rule must name, or <code>null</code> not to check it.
	 * @return The deciding rule, for further assertions.
	 */
	private static ToolAccessRule assertRule(EffectiveToolAccess access, String group,
			boolean expectedAllowed, Kind expectedKind, String expectedRole) {
		ToolAccessExplanation explanation = access.explainCapability(group);

		assertEquals(group + ": " + explanation, expectedAllowed, explanation.allowed());
		assertEquals(group + ": " + explanation, expectedKind, explanation.kind());
		if (expectedRole != null) {
			assertEquals(group, expectedRole, explanation.decidedBy().roleName());
		}
		return explanation.decidedBy();
	}

	private EffectiveToolAccess accessFor(String login, boolean background) {
		return AiToolAccessService.effectiveAccessFor(account(login), background);
	}

	private static ToolIdentity identify(String qualifiedName) {
		return AiToolAccessService.identities().identify(tool(qualifiedName));
	}

	private static Iterable<Tool> registeredTools() {
		return ToolRegistryService.getInstance().getTools();
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoEffectiveAccessTest.class);
	}

}
