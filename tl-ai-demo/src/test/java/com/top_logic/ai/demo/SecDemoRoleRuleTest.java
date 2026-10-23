/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The foundation every other test of the test bed stands on: that the aspect's <b>declarative</b> role
 * rules produce the persona matrix.
 *
 * <p>
 * Nothing here is about agents. It asserts the substrate: a role is a relationship
 * {@code (group, object, role)}, the tier assignment puts it on the security root and the scope
 * assignment puts it on a project derived from {@code pm:Project#partners}, and the two stay
 * independent because the security root is <b>not</b> a security parent of business objects.
 * </p>
 *
 * <p>
 * It exists because the fixture that preceded issue #290 assigned its roles from Java, so they were
 * present by construction and a rule that produced nothing was invisible. Every row below would have
 * passed vacuously then.
 * </p>
 *
 */
public class SecDemoRoleRuleTest extends SecDemoPersonaTest {

	/** The tier: group membership puts the role on the security root, which is what L1 reads. */
	public void testTierRulePutsTheRoleOnTheSecurityRoot() {
		assertRolesOnRoot(ANNA, "projectPartner");
		assertRolesOnRoot(BEN, "ticketReader");
		assertRolesOnRoot(DORA, "projectPartner");
	}

	/** One account in two groups holds both roles, which is what makes Cara the interesting persona. */
	public void testTwoGroupMembershipsProduceTwoRoles() {
		assertRolesOnRoot(CARA, "projectPartner", "ticketReader");
	}

	/**
	 * The scope: whoever a project lists in {@code partners} holds the role <b>on that project</b>.
	 *
	 * <p>
	 * Derived from ordinary business data by a {@code <rule type="reference">}. Nothing assigned it.
	 * </p>
	 */
	public void testScopeRuleDerivesTheRoleFromTheData() {
		assertTrue("Anna is a partner of Alpha, so the scope rule must give her the role there.",
			holdsRoleOn(ANNA, project(ALPHA), "projectPartner"));
		assertTrue("Cara is a partner of Beta.",
			holdsRoleOn(CARA, project(BETA), "projectPartner"));
	}

	/**
	 * The row that makes the whole design worth having: the tier does not leak into the scope.
	 *
	 * <p>
	 * Anna holds {@code projectPartner} on the security root, and the root is <b>not</b> a security
	 * ancestor of a project, so she does not hold it on Beta. Setting
	 * {@code use-default-security-parent="true"} - which the previous fixture did - would make this
	 * assertion fail, because a root-held role would then be held on every object and "partner of
	 * Alpha" would stop meaning anything.
	 * </p>
	 */
	public void testTheTierDoesNotBecomeAScope() {
		assertFalse("Anna holds projectPartner on the root and must NOT hold it on a project she does"
			+ " not partner on. If this fails, check that use-default-security-parent is false.",
			holdsRoleOn(ANNA, project(BETA), "projectPartner"));
		assertFalse("...and Cara not on Alpha.",
			holdsRoleOn(CARA, project(ALPHA), "projectPartner"));
	}

	/**
	 * Dora holds the tier and scopes nothing. She is the failure mode the fixture demonstrates.
	 */
	public void testDoraScopesNothing() {
		for (TLObject project : projects()) {
			assertFalse("Dora partners on no project, so she must hold no role on " + project,
				holdsRoleOn(DORA, project, "projectPartner"));
		}
	}

	/**
	 * A ticket's project is its security parent, so a partner's scope reaches the project's tickets
	 * without a second rule.
	 */
	public void testSecurityParentCarriesTheScopeToTickets() {
		assertTrue("Anna partners on Alpha, so her role reaches Alpha's tickets through the"
			+ " <security-parents> rule.", holdsRoleOn(ANNA, ticketOf(ALPHA), "projectPartner"));
		assertFalse("...and not the tickets of a project she does not partner on.",
			holdsRoleOn(ANNA, ticketOf(BETA), "projectPartner"));
	}

	/**
	 * A ticket reader holds {@code ticketReader} on <b>every</b> ticket, from its own rule.
	 *
	 * <p>
	 * This is the row that says why a fourth role rule exists. The tier rule puts the role on the
	 * security root, which satisfies {@code <tool-access>} and nothing else: model access resolves a
	 * role by walking the object and its security <em>ancestors</em>, and with
	 * {@code use-default-security-parent} at false the root is an ancestor of nothing. So "pm:Ticket
	 * grants Read to ticketReader" plus "ticketReader on the root" would grant Ben no ticket at all,
	 * and the refusal would look like an empty database.
	 * </p>
	 */
	public void testTypeScopeRuleGivesTheReaderEveryTicket() {
		for (TLObject ticket : tickets()) {
			assertTrue("A ticket reader must hold its role on every ticket, from"
				+ " <rule meta-element=\"pm:Ticket\" role=\"ticketReader\">.",
				holdsRoleOn(BEN, ticket, "ticketReader"));
		}
	}

	/** ...and no ticket makes a reader a partner. */
	public void testTicketReaderIsNotAPartner() {
		assertFalse(holdsRoleOn(BEN, ticketOf(ALPHA), "projectPartner"));
		assertFalse("A reader holds no role on a project either - pm:Project grants Read to"
			+ " projectPartner only, which is what refuses Ben a project.",
			holdsRoleOn(BEN, project(ALPHA), "ticketReader"));
	}

	/** The check of the persona matrix finds nothing for the fixture as the aspect declares it. */
	public void testTheMatrixCheckFindsNothing() {
		assertEquals(List.of(), secDemoSetup().matrixProblems());
	}

	/** A rule that really produces nothing is still reported. */
	public void testAMissingMembershipIsReported() {
		Person dora = actAs(DORA);
		Group partners = Group.getGroupByName("projectPartners");
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			partners.removeMember(dora);
			tx.commit();
		}
		try {
			List<String> problems = secDemoSetup().matrixProblems();
			assertEquals("Only Dora lost her role: " + problems, 1, problems.size());
			assertTrue(problems.get(0), problems.get(0).contains("'dora' the role 'projectPartner'"));
		} finally {
			try (Transaction tx = kb.beginTransaction()) {
				partners.addMember(dora);
				tx.commit();
			}
		}
		assertEquals(List.of(), secDemoSetup().matrixProblems());
	}

	private void assertRolesOnRoot(String login, String... expected) {
		Person account = actAs(login);
		Set<String> actual = roleNames(account, securityRoot());

		Set<String> expectedSet = new TreeSet<>(Set.of(expected));
		assertTrue(login + " must hold " + expectedSet + " on the security root, but holds " + actual
			+ ". The tier rules are <singleton-rule target=\"SecurityStructure#ROOT\"> in"
			+ " 'secdemo.config.xml'.", actual.containsAll(expectedSet));
	}

	private boolean holdsRoleOn(String login, TLObject object, String roleName) {
		Person account = actAs(login);
		BoundedRole role = BoundedRole.getRoleByName(roleName);
		assertNotNull("The role '" + roleName + "' must be declared by InitialRolesManager.", role);
		return AccessManager.getInstance().hasRole(account, (BoundObject) object, Set.of(role));
	}

	private static BoundObject securityRoot() {
		BoundObject root = BoundHelper.getInstance().getDefaultObject();
		assertNotNull("The application must have a security root.", root);
		return root;
	}

	private static Set<String> roleNames(Person account, BoundObject object) {
		Set<String> names = new TreeSet<>();
		for (BoundRole role : AccessManager.getInstance().getRoles(account, object)) {
			names.add(role.getName());
		}
		return names;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoRoleRuleTest.class);
	}
}
