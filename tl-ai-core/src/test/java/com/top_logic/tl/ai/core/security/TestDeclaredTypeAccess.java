/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.DeclaredTypeAccess.Verdict;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * The rule {@link DeclaredTypeAccess} applies to {@link ToolMethod#types()} (issue #258).
 *
 * <p>
 * The model lookup is replaced by a table of verdicts: what is under test is which declarations
 * refuse a tool and what the refusal says, not the engine's role evaluation, which
 * {@link TestModelAccessRightsSecurity} covers and which the persona tests of the test bed exercise
 * against a booted application.
 * </p>
 */
public class TestDeclaredTypeAccess extends TestCase {

	private static final String CUSTOMER = "myapp:Customer";

	private static final String CONTRACT = "myapp:Contract";

	/** A tool that declares nothing is never refused, and no right is asked for it. */
	public void testAnUndeclaredToolIsNeverRefused() {
		DeclaredTypeAccess access = new DeclaredTypeAccess(name -> {
			fail("A tool that declares nothing must not cost a rights lookup.");
			return null;
		});
		Tool tool = ToolStubs.tool("listInstances", ToolMode.READ);

		assertTrue(access.permits(tool));
		assertNull(access.denialReason(tool));
	}

	/** The declared type is readable: the tool passes. */
	public void testAReadableDeclaredTypePermits() {
		DeclaredTypeAccess access = access(Map.of(CUSTOMER, Verdict.READABLE));
		Tool tool = ToolStubs.onTypes("listCustomers", CUSTOMER);

		assertTrue(access.permits(tool));
		assertNull(access.denialReason(tool));
	}

	/**
	 * The declared type is unreadable: the tool is refused, and the refusal names the type and the
	 * cause.
	 *
	 * <p>
	 * Both halves of the sentence are pinned deliberately. Naming the type is what lets the model say
	 * something the user can act on; saying that a model access right is the cause is what stops it
	 * sending them to the tool-access policy, where there is nothing to change.
	 * </p>
	 */
	public void testAnUnreadableDeclaredTypeRefuses() {
		DeclaredTypeAccess access = access(Map.of(CUSTOMER, Verdict.UNREADABLE));
		Tool tool = ToolStubs.onTypes("listCustomers", CUSTOMER);

		assertFalse(access.permits(tool));

		String reason = access.denialReason(tool);
		assertNotNull(reason);
		assertTrue("The refusal must name the tool: " + reason, reason.contains("listCustomers"));
		assertTrue("The refusal must name the type: " + reason, reason.contains(CUSTOMER));
		assertTrue("The refusal must name read access as the cause: " + reason,
			reason.contains("no read access"));
		assertTrue("The refusal must say which layer to fix: " + reason,
			reason.contains("model access right"));
		assertTrue("The refusal must stop the model reporting it as an empty result: " + reason,
			reason.contains("there is no such data"));
	}

	/**
	 * One readable declaration is enough, even beside an unreadable one.
	 *
	 * <p>
	 * The tool can still do the half of its job the reader is entitled to, and the per-object checks
	 * keep the other half out of its answer. Refusing here would take away work the user is allowed
	 * to do - see the class comment of {@link DeclaredTypeAccess}.
	 * </p>
	 */
	public void testOneReadableDeclarationIsEnough() {
		DeclaredTypeAccess access = access(Map.of(CUSTOMER, Verdict.READABLE, CONTRACT, Verdict.UNREADABLE));
		Tool tool = ToolStubs.onTypes("listBusiness", CUSTOMER, CONTRACT);

		assertTrue(access.permits(tool));
		assertNull(access.denialReason(tool));
	}

	/** Every declaration unreadable: refused, and the refusal names all of them. */
	public void testEveryDeclarationUnreadableRefuses() {
		DeclaredTypeAccess access = access(Map.of(CUSTOMER, Verdict.UNREADABLE, CONTRACT, Verdict.UNREADABLE));
		Tool tool = ToolStubs.onTypes("listBusiness", CUSTOMER, CONTRACT);

		assertFalse(access.permits(tool));

		String reason = access.denialReason(tool);
		assertTrue("Both types must be named: " + reason,
			reason.contains(CUSTOMER) && reason.contains(CONTRACT));
	}

	/**
	 * A name this application's model does not have is skipped rather than refused.
	 *
	 * <p>
	 * A typo in a declaration is a broken declaration, not a statement about rights. Turning it into
	 * a denial would take a working tool away from every user of the installation, and gain nothing:
	 * what protects the data is the per-object check either way.
	 * </p>
	 */
	public void testAnUnknownTypeNameIsSkipped() {
		DeclaredTypeAccess access = access(Map.of("myapp:Costumer", Verdict.NO_SUCH_TYPE));
		Tool tool = ToolStubs.onTypes("listCustomers", "myapp:Costumer");

		assertTrue("A tool whose only declaration is unresolvable must behave as if it declared"
			+ " nothing.", access.permits(tool));
		assertNull(access.denialReason(tool));
	}

	/** An unresolvable name beside an unreadable one neither rescues the tool nor is named. */
	public void testAnUnknownNameDoesNotRescueAnUnreadableOne() {
		DeclaredTypeAccess access =
			access(Map.of("myapp:Costumer", Verdict.NO_SUCH_TYPE, CONTRACT, Verdict.UNREADABLE));
		Tool tool = ToolStubs.onTypes("listBusiness", "myapp:Costumer", CONTRACT);

		assertFalse(access.permits(tool));

		String reason = access.denialReason(tool);
		assertTrue("The unreadable type must be named: " + reason, reason.contains(CONTRACT));
		assertFalse("A name the model does not have must not be reported as a missing right: "
			+ reason, reason.contains("Costumer"));
	}

	/** The declaration order is the order the refusal names the types in. */
	public void testTheRefusalFollowsTheDeclarationOrder() {
		DeclaredTypeAccess access = access(Map.of(CUSTOMER, Verdict.UNREADABLE, CONTRACT, Verdict.UNREADABLE));

		String contractFirst = access.denialReason(ToolStubs.onTypes("a", CONTRACT, CUSTOMER));
		String customerFirst = access.denialReason(ToolStubs.onTypes("b", CUSTOMER, CONTRACT));

		assertTrue(contractFirst, contractFirst.indexOf(CONTRACT) < contractFirst.indexOf(CUSTOMER));
		assertTrue(customerFirst, customerFirst.indexOf(CUSTOMER) < customerFirst.indexOf(CONTRACT));
	}

	/**
	 * A verdict table, and a declared name outside it is a test error rather than a silent pass.
	 */
	private static DeclaredTypeAccess access(Map<String, Verdict> verdicts) {
		return new DeclaredTypeAccess(name -> {
			Verdict verdict = verdicts.get(name);
			assertNotNull("The test declares no verdict for '" + name + "'; known: "
				+ List.copyOf(verdicts.keySet()), verdict);
			return verdict;
		});
	}

}
