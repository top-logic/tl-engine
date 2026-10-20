/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.HashMap;
import java.util.List;
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
 *
 * <p>
 * The filter covers <b>objects and attribute values</b>. What a script turns into a plain value
 * before the filter runs - an aggregate is a number, a string coercion is a label - carries no
 * {@code TLObject} the filter could recognise; there the engine's own evaluation is what protects the
 * data: a tool's script runs secured, so {@code all(...)}, references and the inputs deliver only the
 * objects the acting account may discover, and a count or a label computed from them discloses nothing
 * else. Asserted here rather than left to be rediscovered - see
 * {@link #testAnAggregateCountsOnlyDiscoverableObjects} and
 * {@link #testStringCoercionLabelsOnlyDiscoverableObjects}, with
 * {@link #testFilteringInsideTheScriptIsTheControl} for the control.
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
	 * A query result discloses the identity and the label of the objects the persona may read, and
	 * nothing of the others.
	 *
	 * <p>
	 * The secured evaluation of the script already narrows {@code all()} to the objects the account may
	 * discover; the read filter holds up the other half of the contract for the <em>objects</em> in the
	 * result, the ones it may discover but not read. A script that coerces its objects to strings first
	 * leaves nothing to filter, which is {@link #testStringCoercionLabelsOnlyDiscoverableObjects}.
	 * </p>
	 */
	public void testScriptResultDisclosesOnlyReadableObjects() {
		String forAnna = queryAs(ANNA, "all(`pm:Project`)");
		assertHas("Anna may read her project, so the query names it.", "[id:", forAnna);
		assertHas(ALPHA, forAnna);

		String forDora = queryAs(DORA, "all(`pm:Project`)");
		assertHasNot("No identifier of an unreadable object may reach the model.", "[id:", forDora);
		assertHasNot("A label is business data.", ALPHA, forDora);
		assertEquals("An empty collection is the honest answer.", "(empty list)", MarkedResults.disclosed(forDora));
	}

	/**
	 * A query whose whole result is one unreadable object reports {@code null}, not the object.
	 */
	public void testSingleUnreadableResultIsNull() {
		String result = queryAs(DORA, "all(`pm:Project`).firstElement()");

		assertEquals("null", MarkedResults.disclosed(result));
		assertHasNot("A hidden object contributes no identifier.", "[id:", result);
	}

	/**
	 * An aggregate counts only what the acting account may discover, and the fixture ships both tools
	 * so a reader can see where they differ.
	 *
	 * <p>
	 * {@code countTickets} evaluates ``all(`pm:Ticket`).size()``. The number is computed before the
	 * read filter sees anything, but the script runs secured, so {@code all} delivers only the tickets
	 * the account may discover - for Dora, none. {@code countVisibleTickets} filters inside the script
	 * for the <em>Read</em> right; it differs from the first for an account that may discover a ticket
	 * without reading it, and agrees with it for every persona of this fixture. Neither tool's
	 * description claims to be the other.
	 * </p>
	 */
	public void testAnAggregateCountsOnlyDiscoverableObjects() {
		int seeded = tickets().size();

		assertEquals("Dora may discover no ticket, so the count over all() is zero.",
			"0", MarkedResults.disclosed(callTool(DORA, CLASSIFIED_READ_SCRIPT_TOOL, Map.of())));
		assertEquals("The filtered count reports what she may actually see.",
			"0", MarkedResults.disclosed(callTool(DORA, FILTERED_COUNT_SCRIPT_TOOL, Map.of())));

		assertEquals("A persona who may read the tickets counts every one of them.", String.valueOf(seeded),
			MarkedResults.disclosed(callTool(BEN, CLASSIFIED_READ_SCRIPT_TOOL, Map.of())));
		assertEquals("...with either tool.", String.valueOf(seeded),
			MarkedResults.disclosed(callTool(BEN, FILTERED_COUNT_SCRIPT_TOOL, Map.of())));
	}

	/**
	 * An attribute read over objects the account may not discover yields nothing, in either spelling.
	 *
	 * <p>
	 * Collection access (``all(...).get(...)``) and an explicit {@code map} agree, because the objects
	 * never enter the computation: {@code all} delivers only the discoverable ones, so there is no
	 * entry whose attribute read could be denied. For a partner the same expressions name her project
	 * and not the other.
	 * </p>
	 */
	public void testADeniedAttributeReadYieldsNothingInEitherSpelling() {
		String collectionAccess = queryAs(DORA, "all(`pm:Project`).get(`pm:Project#name`)");
		assertEquals("Collection access over no discoverable project.", "(empty list)",
			MarkedResults.disclosed(collectionAccess));

		String mapped = queryAs(DORA, "all(`pm:Project`).map(p -> $p.get(`pm:Project#name`))");
		assertEquals("An explicit map has nothing to map over either.", "(empty list)",
			MarkedResults.disclosed(mapped));

		for (String result : new String[] { collectionAccess, mapped }) {
			assertHasNot("No attribute value of an unreadable object is disclosed.", ALPHA, result);
			assertHasNot(BETA, result);
		}

		String forAnna = queryAs(ANNA, "all(`pm:Project`).map(p -> $p.get(`pm:Project#name`))");
		assertHas("Anna reads the name of the project she partners on.", ALPHA, forAnna);
		assertHasNot("...and nothing of the other.", BETA, forAnna);
	}

	/**
	 * A string coercion labels only the objects the account may discover.
	 *
	 * <p>
	 * ``toString($obj)`` and ``"" + $obj`` resolve to {@code SearchExpression.asString}, which falls
	 * through to {@code MetaLabelProvider}: the label of an object the account holds is not
	 * access-checked, and the result is a {@code String} by the time {@link ReadFilter} sees it. What
	 * keeps a label from leaking is that the account never holds an object it may not discover - the
	 * secured {@code all} does not deliver it. A label is therefore visible exactly where the object
	 * is, which is also how the user interface renders a referenced object; confidential information
	 * still must not be encoded in object names.
	 * </p>
	 */
	public void testStringCoercionLabelsOnlyDiscoverableObjects() {
		String coerced = queryAs(DORA, "all(`pm:Project`).map(p -> toString($p))");
		assertEquals("Dora may discover no project, so there is nothing to label.", "(empty list)",
			MarkedResults.disclosed(coerced));
		assertHasNot(ALPHA, coerced);
		assertHasNot(BETA, coerced);

		String concatenated = queryAs(DORA, "all(`pm:Project`).map(p -> \"\" + $p)");
		assertHasNot("Concatenation takes the same route.", BETA, concatenated);

		String forAnna = queryAs(ANNA, "all(`pm:Project`).map(p -> toString($p))");
		assertHas("Anna gets the label of the project she partners on.", ALPHA, forAnna);
		assertHasNot("...and not the label of the other.", BETA, forAnna);
	}

	/**
	 * The control for {@link #testStringCoercionLabelsOnlyDiscoverableObjects}, in the fixture rather
	 * than in prose: filter inside the script for the Read right, before anything coerces.
	 *
	 * <p>
	 * ``filterSecurity(...)`` is the engine's own function for exactly this - it applies the current
	 * user's read permissions to a value, dropping the objects the user may not read. Once the objects
	 * are gone there is nothing left to label. This is what the cheat sheet of the query language and
	 * the skill that changes data tell an agent to do, and this row is why they can.
	 * </p>
	 */
	public void testFilteringInsideTheScriptIsTheControl() {
		String filtered = queryAs(DORA, "filterSecurity(all(`pm:Project`)).map(p -> toString($p))");

		assertEquals("Nothing survives the filter, so nothing is labelled.", "(empty list)",
			MarkedResults.disclosed(filtered));
		assertHasNot(ALPHA, filtered);
		assertHasNot(BETA, filtered);

		String forAnna = queryAs(ANNA, "filterSecurity(all(`pm:Project`)).map(p -> toString($p))");
		assertHas("...and it is a filter, not a blanket refusal: Anna keeps the project she partners on.",
			ALPHA, forAnna);
		assertHasNot("...and loses the one she does not, which is what D7 hands back unfiltered.",
			BETA, forAnna);
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
		return callTool(login, GET_TOOL, Map.of("tids", List.of(tid)));
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
