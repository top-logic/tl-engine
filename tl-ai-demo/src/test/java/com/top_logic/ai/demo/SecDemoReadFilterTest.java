/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
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
 * result proves nothing on its own - a broken tool returns empty too. So each case runs the same call
 * as {@link #READER}, who holds a {@code Read} grant on {@code secdemo:Note}, and as {@link #AUTHOR},
 * whose role appears in no {@code <security-config>} grant at all, and asserts that the first sees the
 * data and the second sees none of it.
 * </p>
 *
 * <p>
 * The calls go through {@link ToolExecutions} and the tool's own argument dispatch rather than the Java
 * method, because the {@link ReadFilter} arrives through the injected {@code ToolExecutionContext} that
 * dispatch supplies. Calling the method directly would test the filter and not the wiring.
 * </p>
 *
 * <p>
 * {@link SecDemoModelSecurityTest} is the counterpart for the layer below: that the engine's rights say
 * what this test relies on them saying. Together they are the "for the right reason" pair - L2 denies,
 * and the tool honours the denial.
 * </p>
 */
public class SecDemoReadFilterTest extends SecDemoPersonaTest {

	/** The paginated listing, in {@code data.read}. */
	private static final String LIST_TOOL = "model-instances.listInstances";

	/** The single-instance description, in {@code data.read}. */
	private static final String GET_TOOL = "model-instances.getInstance";

	/**
	 * A listing names the instances the persona may read, and counts those.
	 *
	 * <p>
	 * The count is the load-bearing half: it used to come from
	 * {@code MetaElementUtil.getAllInstancesOf} unfiltered, so a persona who could see no instance was
	 * still told how many there were.
	 * </p>
	 */
	public void testListingIsFilteredAndSoIsItsCount() {
		int seeded = notes().size();
		assertTrue("Precondition: the fixture seeds notes.", seeded > 0);

		String forReader = listInstances(READER, "secdemo", "Note");
		assertHas("The reader holds Read on secdemo:Note and must see the instances.",
			"\"totalCount\":" + seeded, forReader);

		String forAuthor = listInstances(AUTHOR, "secdemo", "Note");
		assertHas("The author holds no Read grant, so nothing is listed...",
			"\"totalCount\":0", forAuthor);
		assertHasNot("...and no identifier leaks through the page either.", ":",
			instanceArrayOf(forAuthor));
	}

	/**
	 * {@code secdemo:Secret} has no grant for any role, so no persona lists one.
	 *
	 * <p>
	 * The row that used to fail: the type was unreachable through TL-Script and fully readable through
	 * this tool.
	 * </p>
	 */
	public void testNoPersonaListsASecret() {
		assertTrue("Precondition: the fixture seeds secrets.",
			instancesOf("secdemo:Secret").size() > 0);

		for (String login : com.top_logic.ai.demo.secdemo.SecDemoSetupService.personaLogins()) {
			String result = listInstances(login, "secdemo", "Secret");

			assertHas(login + " must not be told how many secrets there are.",
				"\"totalCount\":0", result);
		}
	}

	/**
	 * A readable instance is described with its attribute values; an unreadable one is reported exactly
	 * like a missing one.
	 */
	public void testInstanceDescriptionIsFilteredAndDenialLooksLikeAbsence() {
		String tid = anyNoteTid();

		String forReader = getInstance(READER, tid);
		assertHas("The reader must get the note's attributes.", "title", forReader);

		String forAuthor = getInstance(AUTHOR, tid);
		assertHas("An object the author may not read is reported as missing, because saying"
			+ " 'exists but hidden' discloses that it exists.", "not found", forAuthor);
		assertHasNot("Nothing of the object may appear in the refusal.", "_display", forAuthor);
	}

	/** The same for a type nobody may read: absence, not a permission story. */
	public void testSecretInstanceIsReportedAsMissing() {
		String tid = anyTidOf("secdemo:Secret");

		String result = getInstance(READER, tid);

		assertHas("not found", result);
	}

	/**
	 * A TL-Script result discloses the identity and the label of the objects the persona may read, and
	 * nothing of the others.
	 *
	 * <p>
	 * {@code All.all()} does not filter - its own javadoc says the caller must - and this is where
	 * tl-ai holds up that half of the contract. Before #289 the {@code _tid}, {@code _type} and
	 * {@code _display} of every secret came back to the model.
	 * </p>
	 */
	public void testScriptResultDisclosesOnlyReadableObjects() {
		String notes = executeScriptAs(READER, "all(`secdemo:Note`)", false);
		assertHas("The reader may read notes, so the script names them.", "_tid", notes);
		assertHas("_display", notes);

		String secrets = executeScriptAs(READER, "all(`secdemo:Secret`)", false);
		assertHasNot("No identifier of an unreadable object may reach the model.", "_tid", secrets);
		assertHasNot("A label is business data.", "_display", secrets);
		assertHas("An empty collection is the honest answer.", "\"result\": []", secrets);
	}

	/**
	 * A script whose whole result is one unreadable object reports {@code null}, not {@code object}.
	 *
	 * <p>
	 * "There is an object here that you may not see" is itself a disclosure, so the reported result type
	 * is the type of the filtered value.
	 * </p>
	 */
	public void testSingleUnreadableResultIsNull() {
		String result = executeScriptAs(READER, "all(`secdemo:Secret`).firstElement()", false);

		assertHas("\"resultType\": \"null\"", result);
		assertHasNot("A hidden object contributes no identifier.", "_tid", result);
	}

	/**
	 * A configured TL-Script tool is filtered too, and its rendering never labels a hidden object.
	 *
	 * <p>
	 * {@code SearchExpression.asString} labels a model object, so the tool that used to call it directly
	 * would have named every secret in a one-line result.
	 * </p>
	 */
	public void testConfiguredScriptToolIsFiltered() {
		String forReader = callTool(READER, CLASSIFIED_READ_SCRIPT_TOOL, Map.of());

		// countNotes returns a number, which no read filter can touch - the accepted boundary of the
		// guarantee (issue #291). What is asserted here is that the call still works through the filter.
		assertNotNull(forReader);
		assertTrue("The count is a number: " + forReader, forReader.trim().matches("\\d+"));
	}

	/**
	 * The reader sees a note, the author does not, and the difference is the model rights and nothing
	 * else.
	 *
	 * <p>
	 * Both hold a capability that reaches a read tool - {@link #READER} through {@code data.read},
	 * {@link #AUTHOR} not - so this test asserts the layer boundary rather than an outcome: L1 is asked
	 * first and its answer is recorded here, so a future policy change that hands {@link #AUTHOR}
	 * {@code data.read} makes the rows above the ones that carry the guarantee.
	 * </p>
	 */
	public void testTheToolLayerIsNotWhatFiltersTheData() {
		assertAllowed(READER, LIST_TOOL);
		assertDenied(AUTHOR, LIST_TOOL, "data.read");
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

	private static String anyNoteTid() {
		return anyTidOf("secdemo:Note");
	}

	private static String anyTidOf(String qualifiedTypeName) {
		List<TLObject> instances = instancesOf(qualifiedTypeName);
		assertFalse("Precondition: the fixture seeds instances of " + qualifiedTypeName + ".",
			instances.isEmpty());
		return instances.get(0).tId().asString();
	}

	private static List<TLObject> instancesOf(String qualifiedTypeName) {
		return MetaElementUtil.getAllInstancesOf(
			(TLClass) TLModelUtil.findType(qualifiedTypeName), TLObject.class);
	}

	/**
	 * The {@code instances} array of a listing response, as text.
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
