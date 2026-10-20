/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The persona walkthrough of the developer article {@code ai/security/role-model-worked-example}, one
 * test per row.
 *
 * <p>
 * This is the layer that carries every per-object and per-type guarantee in the design: L1 can only
 * ever say "you may use the write tools at all", so which project Anna may edit is decided here and
 * nowhere else. The rows are asserted through {@link ModelSecurity}, the façade every tl-ai tool
 * reaches, because that is the seam the tools depend on; two rows additionally run TL-Script compiled
 * with security, to show that the engine agrees with the façade rather than the other way round.
 * </p>
 *
 * <p>
 * The row that matters most is {@link #testAnnaCannotEditAProjectSheDoesNotPartnerOn()}: Anna holds
 * {@code data.write} deployment-wide and cannot write project Beta. L1 was never going to be what
 * stopped her, and it did not have to be. {@link SecDemoToolAccessTest} is where the L1 half of the
 * same pair lives.
 * </p>
 *
 * <p>
 * {@link SecDemoRoleRuleTest} is what this test stands on: that the declarative rules produced the
 * roles at all. If both fail, start there.
 * </p>
 */
public class SecDemoModelSecurityTest extends SecDemoPersonaTest {

	// --- Anna: the tier is global, the scope is not -------------------------------------------------

	/** A partner may edit the project they partner on. */
	public void testAnnaEditsHerOwnProject() {
		assertTrue("Anna partners on Alpha and pm:Project grants Write to projectPartner.",
			securityOf(ANNA).canWrite(project(ALPHA)));
		assertTrue(securityOf(ANNA).canRead(project(ALPHA)));
	}

	/**
	 * The same partner may not edit a project she does not partner on - and L1 passed.
	 *
	 * <p>
	 * The whole architecture in one row. Anna holds {@code data.write} on the security root, so the
	 * tool layer lets every write tool through for her; what stops her here is that she holds
	 * {@code projectPartner} on Alpha and not on Beta. A design in which L1 were the control could not
	 * express this, because at the moment L1 runs there is no object yet.
	 * </p>
	 */
	public void testAnnaCannotEditAProjectSheDoesNotPartnerOn() {
		assertAllowed(ANNA, SCRIPT_TOOL);

		assertFalse("Anna holds no role on Beta, so the model rights refuse the write - while the tool"
			+ " layer let her through.", securityOf(ANNA).canWrite(project(BETA)));
		assertFalse("She may not even read it.", securityOf(ANNA).canRead(project(BETA)));
	}

	/** A partner reaches the tickets of their project through the security parent, without a rule. */
	public void testAnnaEditsATicketOfHerProject() {
		assertTrue("Alpha is the security parent of its tickets, so Anna's scope reaches them.",
			securityOf(ANNA).canWrite(ticketOf(ALPHA)));
	}

	/** ...and not the tickets of another project. */
	public void testAnnaCannotEditATicketOfAnotherProject() {
		assertFalse(securityOf(ANNA).canWrite(ticketOf(BETA)));
		assertFalse("Nor read it: pm:Ticket grants Read to projectPartner and ticketReader, and Anna"
			+ " holds neither on a ticket of Beta.", securityOf(ANNA).canRead(ticketOf(BETA)));
	}

	/**
	 * Nobody deletes a ticket, because no {@code Delete} grant exists.
	 *
	 * <p>
	 * Deny-by-default without writing a denial: a type with no grant for an operation resolves to an
	 * empty role set, and a check against an empty role set is false. A {@code Write} grant is not a
	 * licence to delete.
	 * </p>
	 */
	public void testNobodyDeletesATicket() {
		assertFalse("Anna may write this ticket and must not delete it.",
			securityOf(ANNA).canDelete(ticketOf(ALPHA)));
		assertFalse(securityOf(CARA).canDelete(ticketOf(BETA)));
		assertFalse(securityOf(BEN).canDelete(ticketOf(ALPHA)));
	}

	// --- Ben: the loud case and the quiet one ------------------------------------------------------

	/**
	 * A ticket reader reads every ticket.
	 *
	 * @see SecDemoRoleRuleTest#testTypeScopeRuleGivesTheReaderEveryTicket()
	 */
	public void testBenReadsEveryTicket() {
		for (TLObject ticket : tickets()) {
			assertTrue("Ben must read " + ticket, securityOf(BEN).canRead(ticket));
		}
	}

	/** A ticket reader writes none of them. */
	public void testBenWritesNoTicket() {
		assertFalse("pm:Ticket grants Write to projectPartner only.",
			securityOf(BEN).canWrite(ticketOf(ALPHA)));
	}

	/**
	 * A ticket reader is refused a ticket creation at <b>L1</b> - the loud case.
	 *
	 * <p>
	 * The only row of the matrix the tool layer decides. Ben holds no {@code data.write}, so the
	 * capability the commit path costs is missing and he is told which one; L2 would have refused him
	 * too, and never runs.
	 * </p>
	 */
	public void testBenIsRefusedACreationAtTheToolLayer() {
		String denial = securityFor(BEN).capabilityDenialReason("data.write", "creating a ticket");

		assertNotNull("Ben holds no data.write.", denial);
		assertTrue(denial, denial.contains("data.write"));

		assertFalse("...and the model rights would have refused him as well, which is why this is the"
			+ " one row where the order matters.", securityOf(BEN).canCreate(type("pm:Ticket"), (TLObject) null));
	}

	/**
	 * A ticket reader may not read a project, and the refusal is <b>empty</b> rather than an error.
	 *
	 * <p>
	 * The engine's read semantics, and the reason the boot warning of #289 exists: an agent acting for
	 * such an account may report "there are no projects" rather than "you may not see them".
	 * </p>
	 */
	public void testBenReadsNoProject() {
		assertFalse("pm:Project grants Read to projectPartner only.",
			securityOf(BEN).canRead(project(ALPHA)));

		actAs(BEN);
		assertTrue("A refused read comes back empty, not as an error.",
			asCollection(eval("all(`pm:Project`).get(`pm:Project#name`)")).isEmpty());
	}

	/**
	 * A ticket reader reads a ticket's title and not its body.
	 *
	 * <p>
	 * Per-attribute Read, from the {@code <part name="pm:Ticket#body">} grant. An attribute-level grant
	 * narrows the class-level decision: it is consulted only after the class-level check has passed,
	 * and its absence means the attribute inherits that decision. The tool-visible half of this is
	 * {@code SecDemoReadFilterTest}, where the attribute is simply absent from the description.
	 * </p>
	 */
	public void testBenReadsATicketTitleAndNotItsBody() {
		TLObject ticket = ticketOf(ALPHA);

		assertTrue(securityOf(BEN).canRead(ticket, part("pm:Ticket#title")));
		assertFalse("pm:Ticket#body is granted to projectPartner only.",
			securityOf(BEN).canRead(ticket, part("pm:Ticket#body")));
	}

	// --- Cara: two roles on one account -----------------------------------------------------------

	/** A partner of Beta edits Beta's tickets. */
	public void testCaraEditsATicketOfHerProject() {
		assertTrue(securityOf(CARA).canWrite(ticketOf(BETA)));
	}

	/** ...reads a ticket of another project, as a ticket reader... */
	public void testCaraReadsATicketOfAnotherProject() {
		assertTrue("Cara holds ticketReader, so she reads every ticket.",
			securityOf(CARA).canRead(ticketOf(ALPHA)));
	}

	/** ...and may not edit it: a reader is not a writer, and she has no scope on Alpha. */
	public void testCaraCannotEditATicketOfAnotherProject() {
		assertFalse(securityOf(CARA).canWrite(ticketOf(ALPHA)));
	}

	/** Holding both roles reads a ticket's body, because one of them is projectPartner. */
	public void testCaraReadsATicketBody() {
		assertTrue("The <part> grant names projectPartner, and Cara holds it - on Beta.",
			securityOf(CARA).canRead(ticketOf(BETA), part("pm:Ticket#body")));
	}

	// --- Dora: the failure mode -------------------------------------------------------------------

	/**
	 * Dora holds the tier and scopes nothing: write tools, and nothing to write.
	 *
	 * <p>
	 * The drift the tier/scope split makes possible - two assignments that can disagree. It argues for
	 * the boot-time warning of #289, not for a different architecture, and this test is what keeps the
	 * fixture honest about it.
	 * </p>
	 */
	public void testDoraCanWriteNothing() {
		for (TLObject project : projects()) {
			assertFalse("Dora partners on nothing, so every project is refused: " + project,
				securityOf(DORA).canWrite(project));
			assertFalse(securityOf(DORA).canRead(project));
		}
		for (TLObject ticket : tickets()) {
			assertFalse(securityOf(DORA).canWrite(ticket));
		}
	}

	/** And her reads come back empty rather than denied, which is the part that misleads. */
	public void testDorasReadsAreEmptyRatherThanDenied() {
		actAs(DORA);

		assertTrue("Nothing tells Dora that there are projects she may not see.",
			asCollection(eval("all(`pm:Project`).get(`pm:Project#name`)")).isEmpty());
	}

	/** She does hold the tool grant, which is exactly what makes the mismatch possible. */
	public void testDoraHoldsTheWriteToolsSheCannotUse() {
		assertAllowed(DORA, CLASSIFIED_WRITE_SCRIPT_TOOL);
	}

	// --- The engine agrees with the façade --------------------------------------------------------

	/** A partner may create a project; the engine refuses everybody else. */
	public void testCreationFollowsTheCreateGrant() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		actAs(ANNA);
		try (Transaction tx = kb.beginTransaction()) {
			assertNotNull("projectPartner holds Create on pm:Project.", eval("new(`pm:Project`)"));
			tx.rollback();
		}

		actAs(BEN);
		assertDenied("new(`pm:Project`)");
	}

	/**
	 * An administrator bypasses all of it, which is why testing as one proves nothing.
	 */
	public void testAdministratorBypassesEverything() {
		assertNotNull("The test application must have an administrator.", anyAdministrator());

		ModelSecurity admin = ModelSecurity.UNCHECKED;
		assertTrue("Stated as a reminder rather than as a mechanism: an admin account is not a persona,"
			+ " and every deny row above passes vacuously for one.", admin.canWrite(project(BETA)));
	}

	private ModelSecurity securityOf(String login) {
		return securityFor(login).modelSecurity();
	}

	private static TLClass type(String qualifiedName) {
		return (TLClass) TLModelUtil.findType(qualifiedName);
	}

	private static TLStructuredTypePart part(String qualifiedName) {
		return TLModelUtil.findPart(qualifiedName);
	}

	private void assertDenied(String script) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			try {
				eval(script);
				fail("Expected '" + script + "' to be denied for account "
					+ TLContext.currentUser().getName() + ".");
			} catch (TopLogicException expected) {
				// Expected: the engine reports the missing right as an application error.
			} finally {
				tx.rollback();
			}
		}
	}

	/**
	 * Evaluates the given TL-Script the way {@code TLScriptExecutor} does: compiled with the security
	 * checks enabled.
	 */
	private static Object eval(String script) {
		QueryExecutor executor = QueryExecutor.compile(parse(script));
		return executor.executeWith(executor.context(true, null, null), Args.none());
	}

	private static Expr parse(String script) {
		try {
			return ExprFormat.INSTANCE.getValue("script", script);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to parse TL-Script expression: " + script, ex);
		}
	}

	private static Collection<?> asCollection(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return List.of(value);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoModelSecurityTest.class);
	}

}
