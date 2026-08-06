/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.ToolExecutions;

/**
 * End-to-end test that the data tools disclose only what the acting persona may read (issue #289).
 *
 * <p>
 * Every row here is a <b>pair</b>, deliberately: a persona who may read nothing getting an empty
 * result proves nothing on its own, because a broken tool returns empty too. So each case runs the same
 * call as a persona who may see the data and as one who may not, and asserts both halves.
 * </p>
 *
 * <p>
 * The pairs the previous fixture could not express at all are the per-object ones: Anna lists
 * <em>her</em> project and not the other, though both are instances of the same type and she holds the
 * same tool capability for both. That is per-object filtering, and it needs the tier and the scope to
 * be separate assignments - see {@link SecDemoRoleRuleTest}.
 * </p>
 *
 * <p>
 * The calls go through {@link ToolExecutions} and the tool's own argument dispatch rather than the Java
 * method, because the {@link ReadFilter} arrives through the injected {@code ToolExecutionContext} that
 * dispatch supplies. Calling the method directly would test the filter and not the wiring.
 * {@link SecDemoModelSecurityTest} is the counterpart for the layer below: that the engine's rights say
 * what this test relies on them saying.
 * </p>
 */
public class SecDemoReadFilterTest extends SecDemoPersonaTest {

	/**
	 * A partner lists the project she partners on, and not the one she does not.
	 *
	 * <p>
	 * Two instances of one type, one tool capability, two different answers. The count follows the
	 * filter, because a total taken over the unfiltered list would disclose that a second project
	 * exists.
	 * </p>
	 */
	public void testListingIsFilteredPerObject() {
		assertEquals("Precondition: the fixture seeds two projects.", 2, projects().size());

		String forAnna = listInstances(ANNA, "pm", "Project");
		assertHas("Anna partners on exactly one project, so she is told about exactly one.",
			"\"totalCount\":1", forAnna);

		String forBen = listInstances(BEN, "pm", "Project");
		assertHas("A ticket reader holds no Read grant on pm:Project at all.",
			"\"totalCount\":0", forBen);
		assertEquals("...and no identifier reaches him either.", "", instanceArrayOf(forBen));
	}

	/**
	 * Dora holds the read tools and is told there is nothing, which is the failure mode named.
	 */
	public void testDoraIsToldThereIsNothing() {
		assertAllowed(DORA, LIST_TOOL);

		assertHas("Dora scopes nothing, so the listing is empty - not denied.",
			"\"totalCount\":0", listInstances(DORA, "pm", "Project"));
	}

	/** A ticket reader lists every ticket; a partner lists the same ones, from a different role. */
	public void testTicketListingReflectsTheGrant() {
		int seeded = tickets().size();
		assertTrue("Precondition: the fixture seeds tickets.", seeded > 0);

		assertHas("ticketReader reads every ticket.", "\"totalCount\":" + seeded,
			listInstances(BEN, "pm", "Ticket"));
		assertHas("Dora reads none of them.", "\"totalCount\":0",
			listInstances(DORA, "pm", "Ticket"));
	}

	/**
	 * An unreadable attribute of a readable object is omitted, and the object still renders.
	 *
	 * <p>
	 * Per-attribute Read, from the {@code <part name="pm:Ticket#body">} grant. This is the row that
	 * needs the finer of the two decisions: collapsing them would either hide a readable ticket or
	 * disclose a hidden body.
	 * </p>
	 */
	public void testUnreadableAttributeIsOmittedAndTheObjectRemains() {
		String tid = tidOf(ticketOf(ALPHA));

		String forBen = getInstance(BEN, tid);
		assertHas("Ben reads the ticket...", "title", forBen);
		assertHasNot("...and not its body, which is granted to projectPartner only.", "body", forBen);

		String forAnna = getInstance(ANNA, tid);
		assertHas("Anna partners on Alpha, so she reads both.", "title", forAnna);
		assertHas("The pair is what makes the omission attributable to the grant.", "body", forAnna);
	}

	/**
	 * An object the persona may not read is reported exactly like one that does not exist.
	 *
	 * <p>
	 * "Exists but hidden" would disclose that it exists, which is the disclosure the read filter is
	 * there to prevent, so the two answers are deliberately spelled the same way.
	 * </p>
	 */
	public void testUnreadableObjectIsReportedAsMissing() {
		String tid = tidOf(project(BETA));

		String forAnna = getInstance(ANNA, tid);
		assertHas("Anna does not partner on Beta.", "not found", forAnna);
		assertHasNot("Nothing of the project may appear in the refusal.", "_display", forAnna);

		String forCara = getInstance(CARA, tid);
		assertHas("Cara does, so she gets it.", "\"tid\"", forCara);
		assertHasNot("not found", forCara);
	}

	/**
	 * A TL-Script result discloses the identity and the label of the objects the persona may read, and
	 * nothing of the others.
	 *
	 * <p>
	 * {@code All.all()} does not filter - its own javadoc says the caller must - and this is where
	 * tl-ai holds up that half of the contract.
	 * </p>
	 */
	public void testScriptResultDisclosesOnlyReadableObjects() {
		String forAnna = executeScriptAs(ANNA, "all(`pm:Project`)", false);
		assertHas("Anna may read her project, so the script names it.", "_tid", forAnna);
		assertHas("_display", forAnna);

		String forDora = executeScriptAs(DORA, "all(`pm:Project`)", false);
		assertHasNot("No identifier of an unreadable object may reach the model.", "_tid", forDora);
		assertHasNot("A label is business data.", "_display", forDora);
		assertHas("An empty collection is the honest answer.", "\"result\": []", forDora);
	}

	/**
	 * A script whose whole result is one unreadable object reports {@code null}, not {@code object}.
	 */
	public void testSingleUnreadableResultIsNull() {
		String result = executeScriptAs(DORA, "all(`pm:Project`).firstElement()", false);

		assertHas("\"resultType\": \"null\"", result);
		assertHasNot("A hidden object contributes no identifier.", "_tid", result);
	}

	/**
	 * The boundary, asserted rather than assumed: a count is not filtered, and the fixture ships both
	 * tools so a reader can see the difference.
	 *
	 * <p>
	 * {@code countTickets} evaluates ``all(`pm:Ticket`).size()``, which is a number by the time the read
	 * filter sees it - per-object filtering cannot narrow it, and this is accepted rather than fixed
	 * (issue #291). {@code countVisibleTickets} filters inside the script instead, which is the honest
	 * way to report a count and the only one that works. Neither tool's description claims to be the
	 * other.
	 * </p>
	 */
	public void testAnAggregateIsNotFilteredAndTheFixtureSaysSo() {
		int seeded = tickets().size();

		assertEquals("The unfiltered count reports every ticket, whatever Dora may read.",
			String.valueOf(seeded), callTool(DORA, CLASSIFIED_READ_SCRIPT_TOOL, Map.of()).trim());
		assertEquals("The filtered count reports what she may actually see.",
			"0", callTool(DORA, FILTERED_COUNT_SCRIPT_TOOL, Map.of()).trim());

		assertEquals("For a persona who may read the tickets the two agree, which is why the difference"
			+ " is easy to miss and worth a test.", String.valueOf(seeded),
			callTool(BEN, FILTERED_COUNT_SCRIPT_TOOL, Map.of()).trim());
	}

	/**
	 * A configured script tool never labels an object the persona may not read.
	 *
	 * <p>
	 * {@code SearchExpression.asString} labels a model object, so a tool whose body returns one used to
	 * name it in a one-line result whatever the caller's rights were.
	 * </p>
	 */
	public void testConfiguredScriptToolResultIsFiltered() {
		// The seeded projects are named, and a label is derived from the name, so the label of a project
		// Dora may not read must not appear in what a script tool renders for her.
		String forDora = executeScriptAs(DORA, "all(`pm:Project`).get(`pm:Project#name`)", false);

		assertHasNot(ALPHA, forDora);
		assertHasNot(BETA, forDora);
	}

	/**
	 * The tool layer is not what filters the data, and this row records that it passed.
	 *
	 * <p>
	 * Anna and Dora hold the same {@code data.read} capability and get different answers above; Ben is
	 * refused {@code pm:Project} by L2 while L1 lets him call the listing. So every row above is
	 * attributable to the model rights.
	 * </p>
	 */
	public void testTheToolLayerIsNotWhatFiltersTheData() {
		assertAllowed(ANNA, LIST_TOOL);
		assertAllowed(BEN, LIST_TOOL);
		assertAllowed(DORA, LIST_TOOL);
		assertAllowed(CARA, GET_TOOL);
	}

	private String listInstances(String login, String module, String type) {
		Map<String, Object> args = new HashMap<>();
		args.put("moduleName", module);
		args.put("typeName", type);
		return callTool(login, LIST_TOOL, args);
	}

	private String getInstance(String login, String tid) {
		return callTool(login, GET_TOOL, Map.of("tid", tid));
	}

	private String callTool(String login, String qualifiedToolName, Map<String, Object> args) {
		SecurityContext security = securityFor(login);
		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return ToolExecutions.execute(tool(qualifiedToolName), new HashMap<>(args), context).text();
	}

	private static String tidOf(TLObject object) {
		return object.tId().asString();
	}

	/**
	 * The contents of the {@code instances} array of a listing response, as text.
	 *
	 * <p>
	 * Read out of the JSON rather than the whole document, because the document legitimately contains
	 * the module and type name the caller asked about.
	 * </p>
	 */
	private static String instanceArrayOf(String listingJson) {
		int start = listingJson.indexOf('[', listingJson.indexOf("\"instances\""));
		if (start < 0) {
			return "";
		}
		int end = listingJson.indexOf(']', start);
		return end < 0 ? listingJson.substring(start) : listingJson.substring(start + 1, end);
	}

	private static void assertHas(String expected, String actual) {
		assertHas("Expected to find it.", expected, actual);
	}

	private static void assertHas(String message, String expected, String actual) {
		assertTrue(message + " Looked for '" + expected + "' in: " + actual, actual.contains(expected));
	}

	private static void assertHasNot(String forbidden, String actual) {
		assertHasNot("Must not be disclosed.", forbidden, actual);
	}

	private static void assertHasNot(String message, String forbidden, String actual) {
		assertFalse(message + " Found '" + forbidden + "' in: " + actual, actual.contains(forbidden));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoReadFilterTest.class);
	}
}
