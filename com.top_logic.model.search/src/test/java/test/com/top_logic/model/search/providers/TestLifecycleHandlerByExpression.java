/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import static test.com.top_logic.model.search.providers.TestChangeHandlerByExpression.*;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLLifecycleHandlers;
import com.top_logic.model.search.providers.I18NConstants;
import com.top_logic.model.search.providers.LifecycleHandlerByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for a {@link LifecycleHandlerByExpression} annotated with {@link TLLifecycleHandlers}.
 */
@SuppressWarnings("javadoc")
public class TestLifecycleHandlerByExpression extends AbstractSearchExpressionTest {

	private static final String COUNTER = "TestHandlersByExpression:Counter";

	private static final String COUNTER_REF = "counter";

	/**
	 * Name of an item whose deleting script returns nothing.
	 */
	private static final String NO_REACTION = "no-reaction";

	/**
	 * Name of an item whose deleting script returns a value that is not a function.
	 */
	private static final String INVALID = "invalid";

	public void testCreatedScriptInitializes() {
		TLObject item = newObject(ITEM, "a");
		assertEquals("created:a", item.tValueByName(LOG));
	}

	public void testNoCreationReportForObjectDeletedInSameTransaction() {
		TLObject counter = newObject(COUNTER, "c");
		TLClass type = (TLClass) TLModelUtil.findType(ITEM);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject item = DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type);
			item.tUpdateByName(NAME, "transient");
			item.tUpdateByName(COUNTER_REF, counter);
			item.tDelete();
			tx.commit();
		}
		assertNull("Deleting script is not executed for an object created in the same transaction.",
			counter.tValueByName(LOG));
	}

	public void testDeletingScriptModifiesSurvivingObject() {
		TLObject counter = newObject(COUNTER, "c");
		TLObject item = newObject(ITEM, "x");
		update(item, COUNTER_REF, counter);

		delete(item);

		assertFalse(item.tValid());
		assertEquals("Function returned by the deleting script runs after the deletion with null argument.",
			"deleted:x:null;", counter.tValueByName(LOG));
	}

	public void testDeletingScriptWithoutResult() {
		TLObject counter = newObject(COUNTER, "c");
		TLObject item = newObject(ITEM, NO_REACTION);
		update(item, COUNTER_REF, counter);

		delete(item);

		assertFalse(item.tValid());
		assertNull(counter.tValueByName(LOG));
	}

	public void testDeletingScriptWithInvalidResult() {
		TLObject item = newObject(ITEM, INVALID);

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			item.tDelete();
			tx.commit();
			fail("Deletion must fail, if the deleting script returns no function.");
		} catch (RuntimeException ex) {
			assertErrorKey(I18NConstants.ERROR_DELETING_SCRIPT_RESULT_NOT_A_FUNCTION__OBJECT_RESULT, ex);
		}
		assertTrue("Failed deletion is rolled back.", item.tValid());

		// Allow cleanup.
		update(item, NAME, "valid");
	}

	private static void assertErrorKey(ResKeyTemplate expected, Throwable ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof TopLogicException tlEx
				&& expected.getKey().equals(tlEx.getErrorKey().plain().getKey())) {
				return;
			}
		}
		throw new AssertionError("Expected error " + expected.getKey() + ", got: " + ex, ex);
	}

	public static Test suite() {
		return suite(TestLifecycleHandlerByExpression.class);
	}

}
