/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What {@link ToolMethod#types()} does to a persona of the security test bed (issue #258).
 *
 * <p>
 * The pair under test is {@code secdemo-projects.listProjects} and
 * {@code secdemo-projects.listProjectsUndeclared}: the same listing, the same {@code data.read}
 * group, and one declares {@code pm:Project} while the other declares nothing. Every persona holds
 * {@code data.read}, so the tool-access policy is out of the picture here and the only thing that can
 * make a difference is the declaration.
 * </p>
 *
 * <p>
 * The three answers the personas get are the case for the mechanism, in one class:
 * </p>
 * <ul>
 * <li><b>Ben</b> ({@code ticketReader}) holds no Read right on {@code pm:Project} at all. He is
 * refused before the tool runs, with a sentence naming the type - where the undeclared twin answers
 * him "No projects.", which is true and which a model reports to the user as "this application has no
 * projects".</li>
 * <li><b>Anna</b> ({@code projectPartner}) is offered the tool and gets her project.</li>
 * <li><b>Dora</b> holds {@code projectPartner} on the security root and on nothing else. She passes
 * this check - it reads roles on the root, like every L1 check - and her answer is empty, decided per
 * object. A declaration is a coarse pre-flight refusal, never the per-object check.</li>
 * </ul>
 *
 * @see SecDemoSchemaFilterTest
 */
public class SecDemoDeclaredTypesTest extends SecDemoPersonaTest {

	/** Declares {@code pm:Project}; in {@code data.read}. */
	private static final String DECLARED_TOOL = "secdemo-projects.listProjects";

	/** The same listing, declaring nothing. */
	private static final String UNDECLARED_TOOL = "secdemo-projects.listProjectsUndeclared";

	/** Precondition: the pair differs in the declaration and in nothing else that decides. */
	public void testThePairDiffersOnlyInTheDeclaration() {
		Tool declared = tool(DECLARED_TOOL);
		Tool undeclared = tool(UNDECLARED_TOOL);

		assertEquals(List.of("pm:Project"), declared.types());
		assertEquals(List.of(), undeclared.types());
		assertEquals(declared.group(), undeclared.group());
		assertEquals(declared.mode(), undeclared.mode());
	}

	/**
	 * A persona without read access to the declared type is refused before the tool runs, and the
	 * refusal names the type.
	 */
	public void testAPersonaWithoutReadAccessIsRefused() {
		String reason = new ToolPermissionFilter(securityFor(BEN)).denialReason(tool(DECLARED_TOOL));

		assertNotNull("Ben holds no Read right on pm:Project and must be refused.", reason);
		assertTrue("The refusal must name the type: " + reason, reason.contains("pm:Project"));
		assertTrue("...and must say that read access is the cause: " + reason,
			reason.contains("no read access"));
		assertFalse("Ben holds data.read, so no capability is missing and none may be blamed: "
			+ reason, reason.contains("capability"));
	}

	/**
	 * The same persona, the same listing, without the declaration: it runs and answers "No projects."
	 *
	 * <p>
	 * The reason the declaration exists. Neither tool tells Ben anything false; only one of them tells
	 * him something he can act on.
	 * </p>
	 */
	public void testTheUndeclaredTwinAnswersTheSamePersonaWithAnEmptyResult() {
		SecurityContext ben = securityFor(BEN);

		assertTrue("The undeclared tool declares nothing to check, so it stays available.",
			new ToolPermissionFilter(ben).test(tool(UNDECLARED_TOOL)));
		assertEquals("No projects.", run(ben, UNDECLARED_TOOL));
	}

	/** A persona with read access to the declared type is offered the tool and gets its result. */
	public void testAPersonaWithReadAccessGetsTheResult() {
		SecurityContext anna = securityFor(ANNA);

		assertNull(new ToolPermissionFilter(anna).denialReason(tool(DECLARED_TOOL)));
		assertEquals("Projects: " + ALPHA, run(anna, DECLARED_TOOL));
	}

	/**
	 * The declared tool is absent from the schemas Ben's request would carry, and present in Anna's.
	 *
	 * <p>
	 * The same {@link ToolPermissionFilter} {@code AgentLoop.callProviderOnce} builds, over the same
	 * catalog for both personas - so an absent schema here is an absent schema in the request, and it
	 * is the persona and not the catalog that decided.
	 * </p>
	 */
	public void testTheDeclaredToolIsFilteredOutOfTheRequestPerPersona() {
		List<String> benSees = schemaNamesFor(BEN);
		List<String> annaSees = schemaNamesFor(ANNA);

		assertFalse("Ben must not be offered a tool every call to which is refused: " + benSees,
			benSees.contains(tool(DECLARED_TOOL).name()));
		assertTrue("The undeclared twin is offered to him: " + benSees,
			benSees.contains(tool(UNDECLARED_TOOL).name()));
		assertTrue("Anna is offered both: " + annaSees,
			annaSees.contains(tool(DECLARED_TOOL).name())
				&& annaSees.contains(tool(UNDECLARED_TOOL).name()));
	}

	/**
	 * Dora passes the check and still sees nothing - the boundary of what a declaration decides.
	 *
	 * <p>
	 * She holds {@code projectPartner} on the security root, which is where this check reads roles,
	 * and on no project, which is where the per-object check reads them. Pinning this keeps the two
	 * apart: a pre-flight check that tried to answer "will this account see anything" would have to
	 * read every instance, which is the work it exists to avoid.
	 * </p>
	 */
	public void testATierRoleWithoutScopePassesTheCheckAndFindsNothing() {
		SecurityContext dora = securityFor(DORA);

		assertNull("Dora's role grants Read on pm:Project, so the declaration does not refuse her.",
			new ToolPermissionFilter(dora).denialReason(tool(DECLARED_TOOL)));
		assertEquals("No projects.", run(dora, DECLARED_TOOL));
	}

	/**
	 * What the tool disclosed to the given run, executed the way a tool call in a run executes it.
	 *
	 * <p>
	 * The envelope around it is read off by {@link MarkedResults}: a {@code @ToolMethod} answer is
	 * marked as model data (#478), and what this class is about is the disclosure inside it.
	 * </p>
	 */
	private static String run(SecurityContext security, String qualifiedToolName) {
		ToolContext context =
			new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		return MarkedResults.disclosed(
			ToolExecutions.execute(tool(qualifiedToolName), Map.of(), context).text());
	}

	/**
	 * The names of the tools this persona's provider request would carry, over a catalog holding the
	 * pair and one tool of every capability the fixture grants.
	 */
	private List<String> schemaNamesFor(String login) {
		ToolCatalog catalog = new ToolCatalog();
		for (String name : List.of(DECLARED_TOOL, UNDECLARED_TOOL, MODEL_READ_TOOL, LIST_TOOL)) {
			catalog.register(tool(name));
		}
		return catalog.freeze().toolSchemas(new ToolPermissionFilter(securityFor(login))).stream()
			.map(ToolSchema::name)
			.toList();
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoDeclaredTypesTest.class);
	}

}
