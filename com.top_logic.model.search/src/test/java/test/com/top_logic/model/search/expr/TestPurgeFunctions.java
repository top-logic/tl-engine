/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.PurgeFunctions;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for the TL-Script functions that remove deleted objects.
 *
 * @see PurgeFunctions
 */
@SuppressWarnings("javadoc")
public class TestPurgeFunctions extends AbstractSearchExpressionTest {

	/**
	 * Type of the objects created here, stored in a table of its own so that the removal of an
	 * object of this test touches nothing else.
	 */
	private static final String TYPE = "TestSearchExpression:A";

	private static final String NAME_ATTRIBUTE = "name";

	public void testAnalyzeDeletedObject() throws Exception {
		TLObject obsolete = newObject("obsolete");
		ObjectKey key = obsolete.tHandle().tId();
		delete(obsolete);

		Map<?, ?> report = analyze(key);
		assertEquals("The analysis changes nothing.", Boolean.TRUE, report.get(PurgeFunctions.KEY_DRY_RUN));
		assertEquals("A deleted object blocks nothing: " + report, Boolean.FALSE,
			report.get(PurgeFunctions.KEY_BLOCKED));
		assertEquals("There is something to remove: " + report, Boolean.FALSE,
			report.get(PurgeFunctions.KEY_EMPTY));

		Map<?, ?> entry = hullEntry(report, key);
		assertNotNull("The object itself is in the hull: " + report, entry);
		assertEquals("The object was named, not reached from another one.", "SEED",
			entry.get(PurgeFunctions.KEY_ORIGIN));
		assertNull(entry.get(PurgeFunctions.KEY_REFERENCE));
		assertNull(entry.get(PurgeFunctions.KEY_CAUSE));
		assertEquals(key.getObjectType().getName(), entry.get(PurgeFunctions.KEY_TYPE));

		Map<?, ?> erasedRows = (Map<?, ?>) report.get(PurgeFunctions.KEY_ERASED_ROWS);
		assertFalse("The rows of the object are counted: " + report, erasedRows.isEmpty());
		assertTrue("Every table with a count has rows to erase: " + erasedRows,
			erasedRows.values().stream().allMatch(count -> ((Number) count).doubleValue() > 0));
	}

	public void testAnalyzeLivingObject() throws Exception {
		TLObject alive = newObject("alive");
		ObjectKey key = alive.tHandle().tId();

		Map<?, ?> report = analyze(key);
		assertEquals("A living object cannot be removed: " + report, Boolean.TRUE,
			report.get(PurgeFunctions.KEY_BLOCKED));
		List<?> blockers = (List<?>) report.get(PurgeFunctions.KEY_BLOCKERS);
		assertEquals("Exactly the living object blocks: " + report, 1, blockers.size());
		Map<?, ?> blocker = (Map<?, ?>) blockers.get(0);
		assertEquals(key.getObjectName().toString(), blocker.get(PurgeFunctions.KEY_ID));
		assertEquals("SEED", blocker.get(PurgeFunctions.KEY_ORIGIN));
	}

	public void testAnalyzeList() throws Exception {
		TLObject first = newObject("first");
		TLObject second = newObject("second");
		ObjectKey firstKey = first.tHandle().tId();
		ObjectKey secondKey = second.tHandle().tId();
		delete(first);
		delete(second);

		Map<?, ?> report = (Map<?, ?>) eval("a -> b -> purgeAnalyze([$a, $b])", firstKey, secondKey);
		assertNotNull("Both objects are in the hull: " + report, hullEntry(report, firstKey));
		assertNotNull("Both objects are in the hull: " + report, hullEntry(report, secondKey));
	}

	public void testAnalyzeHistoricObject() throws Exception {
		TLObject obsolete = newObject("historic");
		ObjectKey key = obsolete.tHandle().tId();
		TLObject historic = WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(), obsolete);
		delete(obsolete);

		Map<?, ?> report = (Map<?, ?>) eval("obj -> purgeAnalyze($obj)", historic);
		assertNotNull("An object is removed from every revision, whichever one names it: " + report,
			hullEntry(report, key));
	}

	public void testAnalyzeOfSomethingElse() throws Exception {
		try {
			eval("purgeAnalyze('not an object')");
			fail("A value that is no object must be rejected.");
		} catch (TopLogicException ex) {
			// Expected: the text is no object identifier either.
		}
	}

	public void testPurgeNeedsMaintenanceWindow() throws Exception {
		TLObject obsolete = newObject("obsolete");
		ObjectKey key = obsolete.tHandle().tId();
		delete(obsolete);

		Map<?, ?> before = analyze(key);
		try {
			eval("key -> purgeDeleted($key)", key);
			fail("Without a maintenance window nothing may be removed.");
		} catch (TopLogicException ex) {
			assertEquals("The violated precondition is what refuses the removal.",
				PersistencyMaintenance.checkPreconditions(), functionErrorKey(ex));
		}
		assertEquals("A refused removal changes nothing.", before, analyze(key));
	}

	/**
	 * The key of the failure a function reported: either the failure itself, or its direct cause
	 * when the evaluation wrapped it in a failure naming the expression. Nothing else sits in
	 * between.
	 */
	private static ResKey functionErrorKey(TopLogicException failure) {
		Throwable cause = failure.getCause();
		if (cause instanceof TopLogicException inner) {
			return inner.getErrorKey();
		}
		return failure.getErrorKey();
	}

	/** The report of analyzing the removal of the object with the given identifier. */
	private Map<?, ?> analyze(ObjectKey key) throws Exception {
		return (Map<?, ?>) eval("key -> purgeAnalyze($key)", key);
	}

	/** The entry of the given object in the hull of the given report, or <code>null</code>. */
	private static Map<?, ?> hullEntry(Map<?, ?> report, ObjectKey key) {
		for (Object entry : (List<?>) report.get(PurgeFunctions.KEY_HULL)) {
			Map<?, ?> member = (Map<?, ?>) entry;
			if (key.getObjectName().toString().equals(member.get(PurgeFunctions.KEY_ID))) {
				return member;
			}
		}
		return null;
	}

	private static TLObject newObject(String name) {
		TLClass type = (TLClass) TLModelUtil.findType(model(), TYPE);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject result = DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type);
			result.tUpdateByName(NAME_ATTRIBUTE, name);
			tx.commit();
			return result;
		}
	}

	private static void delete(TLObject object) {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			object.tDelete();
			tx.commit();
		}
	}

	public static Test suite() {
		return suite(TestPurgeFunctions.class);
	}

}
