/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import junit.framework.Test;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.revision.ModifiedRevision;

/**
 * Tests for the TL-Script function {@link ModifiedRevision}.
 */
@SuppressWarnings("javadoc")
public class TestModifiedRevision extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestSearchExpression:A";

	private static final String OTHER = "other";

	private static final String OTHERS = "others";

	private static final String NAME = "name";

	public void testSingleReference() throws Exception {
		TLObject item = newObject(TYPE, "item");
		TLObject target = newObject(TYPE, "target");
		assertMoves(item, x -> x.tUpdateByName(OTHER, target));
		assertMoves(item, x -> x.tUpdateByName(OTHER, null));
	}

	public void testMultipleReference() throws Exception {
		TLObject item = newObject(TYPE, "item");
		TLObject target = newObject(TYPE, "target");
		assertMoves(item, x -> add(x, OTHERS, target));
		assertMoves(item, x -> remove(x, OTHERS, target));
	}

	public void testUnrelatedChange() throws Exception {
		TLObject item = newObject(TYPE, "item");
		TLObject other = newObject(TYPE, "other");
		assertStays(item, other, x -> x.tUpdateByName(NAME, "other-2"));
		assertStays(item, other, x -> x.tUpdateByName(OTHER, item));
	}

	private void assertMoves(TLObject item, Consumer<TLObject> change) throws Exception {
		Revision before = modifiedRevision(item);
		Revision changeRevision = update(item, change);
		assertTrue(changeRevision.compareTo(before) > 0);
		assertEquals(changeRevision, modifiedRevision(item));
	}

	private void assertStays(TLObject item, TLObject changed, Consumer<TLObject> change) throws Exception {
		Revision before = modifiedRevision(item);
		update(changed, change);
		assertEquals(before, modifiedRevision(item));
	}

	private Revision modifiedRevision(TLObject item) throws Exception {
		return (Revision) eval("x -> $x.modifiedRevision()", item);
	}

	private static Revision update(TLObject item, Consumer<TLObject> change) {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			change.accept(item);
			tx.commit();
			return tx.getCommitRevision();
		}
	}

	private static void add(TLObject item, String attribute, TLObject value) {
		List<Object> values = new ArrayList<>((Collection<?>) item.tValueByName(attribute));
		values.add(value);
		item.tUpdateByName(attribute, values);
	}

	private static void remove(TLObject item, String attribute, TLObject value) {
		List<Object> values = new ArrayList<>((Collection<?>) item.tValueByName(attribute));
		values.remove(value);
		item.tUpdateByName(attribute, values);
	}

	public static Test suite() {
		return suite(TestModifiedRevision.class);
	}

}
