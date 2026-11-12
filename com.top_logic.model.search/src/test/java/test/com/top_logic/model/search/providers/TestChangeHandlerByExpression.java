/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLChangeHandlers;
import com.top_logic.model.search.providers.ChangeHandlerByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for a {@link ChangeHandlerByExpression} annotated with {@link TLChangeHandlers}.
 */
@SuppressWarnings("javadoc")
public class TestChangeHandlerByExpression extends AbstractSearchExpressionTest {

	static final String ITEM = "TestHandlersByExpression:Item";

	static final String NAME = "name";

	static final String STATUS = "status";

	static final String LOG = "log";

	public void testChangeSetsOtherAttribute() {
		TLObject item = newObject(ITEM, "a");
		assertEquals("created:a", item.tValueByName(LOG));

		update(item, STATUS, "open");
		assertEquals("Handler receives old value, new value and attribute.", "->open:status", item.tValueByName(LOG));

		update(item, STATUS, "closed");
		assertEquals("open->closed:status", item.tValueByName(LOG));
	}

	public void testNoCallOnCreation() {
		TLClass type = (TLClass) TLModelUtil.findType(ITEM);
		TLObject item;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			item = DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type);
			item.tUpdateByName(NAME, "b");
			item.tUpdateByName(STATUS, "open");
			tx.commit();
		}
		try {
			assertEquals("Only the creation is reported, not the change of the status.", "created:b",
				item.tValueByName(LOG));
		} finally {
			delete(item);
		}
	}

	public void testNoCallWithoutChange() {
		TLObject item = newObject(ITEM, "c");
		update(item, STATUS, "open");
		update(item, LOG, "reset");

		update(item, STATUS, "open");
		assertEquals("Setting the same value is no change.", "reset", item.tValueByName(LOG));
	}

	static void update(TLObject object, String attribute, Object value) {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			object.tUpdateByName(attribute, value);
			tx.commit();
		}
	}

	public static Test suite() {
		return suite(TestChangeHandlerByExpression.class);
	}

}
