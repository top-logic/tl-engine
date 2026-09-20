/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.core.tools.model.ModelDigestGenerator;

/**
 * End-to-end test that the model digest describes only the types the acting persona may read
 * (issue #295).
 *
 * <p>
 * The digest is the one read surface that reaches the model <em>in one piece</em>: a single
 * {@code outlineModel} call hands over the whole application model, so a type nobody told it about is
 * a type the agent now knows the name, the attributes and the relationships of. It is scoped
 * accordingly, and it is scoped by the same rights everything else is - here through
 * {@code accessibleTypes(READ)}, because what the digest describes is types and not objects.
 * </p>
 *
 * <p>
 * Each case is a <b>pair</b>, as in {@link SecDemoReadFilterTest}: a type missing for one persona is
 * asserted present for another, so an empty answer cannot pass for a filtered one.
 * </p>
 *
 * <p>
 * The calls go through {@link ToolExecutions} and the tool's own argument dispatch rather than the
 * Java method, because the rights arrive through the injected {@code ToolExecutionContext} that
 * dispatch supplies. Calling the method directly would test the scoping and not the wiring.
 * </p>
 *
 * @see ModelDigestGenerator
 */
public class SecDemoModelDigestTest extends SecDemoPersonaTest {

	/** In {@code model.read}: the whole-model digest. */
	private static final String OUTLINE_TOOL = "model-browser.outlineModel";

	/**
	 * A ticket reader is told about the type it may read, and not about the one it may not.
	 *
	 * <p>
	 * Both types live in the same module and the persona holds the same {@code model.read} capability
	 * for both, so the difference is the {@code <security-config>} and nothing else.
	 * </p>
	 */
	public void testTheDigestNamesOnlyTheTypesThePersonaMayRead() {
		String forBen = outline(BEN);
		assertHas("A ticket reader holds Read on pm:Ticket.", "class Ticket", forBen);
		assertHasNot("...and none on pm:Project, so the digest must not describe it.",
			"class Project", forBen);

		String forAnna = outline(ANNA);
		assertHas("A project partner holds Read on both.", "class Project", forAnna);
		assertHas("The pair is what makes the omission attributable to the grant.",
			"class Ticket", forAnna);
	}

	/**
	 * A module the persona may read no type of is absent with everything in it.
	 *
	 * <p>
	 * The demo's own {@code luigis.pizzashop} model carries no grant at all, which is the
	 * deny-by-default case: no persona of this fixture may read any of its types, so no persona is told
	 * that the module exists. An administrator, who bypasses the rights, is told - which is what makes
	 * this a statement about the scoping rather than about the module being missing.
	 * </p>
	 */
	public void testAModuleWithNoReadableTypeIsAbsent() {
		String forAnna = outline(ANNA);
		assertHasNot("No persona holds a grant in the pizza shop model.", "## luigis.pizzashop",
			forAnna);
		assertHas("...while the module the fixture does grant on is there.", "## pm", forAnna);
	}

	/**
	 * Structure is not instances: the persona who may read no project still learns the type.
	 *
	 * <p>
	 * {@code Dora} holds {@code projectPartner} at the security root and on no object, so every
	 * instance surface answers her with nothing ({@link SecDemoReadFilterTest}). The digest is a
	 * different question and gets a different answer: the type is one she may read, even though there
	 * is no instance of it she may. Collapsing the two would describe the wrong thing.
	 * </p>
	 */
	public void testTheDigestIsAboutTypesAndNotAboutInstances() {
		assertHas("Dora's role grants Read on pm:Project at the root, so the type is hers to know.",
			"class Project", outline(DORA));

		assertHas("...and no instance of it is.", "\"totalCount\":0",
			callTool(DORA, LIST_TOOL, listArgs("pm", "Project")));
	}

	/**
	 * The tool layer is not what scopes the digest, and this row records that it passed.
	 *
	 * <p>
	 * Anna and Ben hold the same {@code model.read} capability and get different digests, so the
	 * difference above is attributable to the model rights and not to the invocation gate.
	 * </p>
	 */
	public void testTheToolLayerIsNotWhatScopesTheDigest() {
		assertAllowed(ANNA, OUTLINE_TOOL);
		assertAllowed(BEN, OUTLINE_TOOL);
		assertAllowed(DORA, OUTLINE_TOOL);
	}

	private String outline(String login) {
		Map<String, Object> args = new HashMap<>();
		args.put("includeFramework", Boolean.FALSE);
		return callTool(login, OUTLINE_TOOL, args);
	}

	private static Map<String, Object> listArgs(String module, String type) {
		Map<String, Object> args = new HashMap<>();
		args.put("moduleName", module);
		args.put("typeName", type);
		return args;
	}

	private String callTool(String login, String qualifiedToolName, Map<String, Object> args) {
		SecurityContext security = securityFor(login);
		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return ToolExecutions.execute(tool(qualifiedToolName), new HashMap<>(args), context).text();
	}

	private static void assertHas(String message, String expected, String actual) {
		assertTrue(message + " Looked for '" + expected + "' in: " + actual, actual.contains(expected));
	}

	private static void assertHasNot(String message, String forbidden, String actual) {
		assertFalse(message + " Found '" + forbidden + "' in: " + actual, actual.contains(forbidden));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoModelDigestTest.class);
	}
}
