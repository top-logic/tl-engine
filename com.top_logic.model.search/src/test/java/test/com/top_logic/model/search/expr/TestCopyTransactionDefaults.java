/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.parser.ParseException;

/**
 * Test of the TL-Script function {@code copy()} with the option {@code skipTransactionDefaults}
 * for attributes whose {@link DefaultProvider} is computed in the transaction creating an object.
 *
 * @see CopyOperation#skipTransactionDefaults(boolean)
 */
@SuppressWarnings("javadoc")
public class TestCopyTransactionDefaults extends AbstractSearchExpressionTest {

	private static final String TYPE = "`TestSearchExpression:WithTransactionDefaults`";

	private static final String NAME = "`TestSearchExpression:WithTransactionDefaults#name`";

	private static final String NUMBER = "`TestSearchExpression:WithTransactionDefaults#number`";

	private static final String COMPUTED = "`TestSearchExpression:WithTransactionDefaults#computed`";

	private final List<TLObject> _persistent = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		becomeUser(PersonManager.getManager().getRoot());
	}

	@Override
	protected void tearDown() throws Exception {
		for (TLObject object : _persistent) {
			if (object.tValid()) {
				delete(object);
			}
		}
		_persistent.clear();
		super.tearDown();
	}

	public void testDraftHasNoTransactionDefaults() throws ParseException {
		TLObject draft = draft("draft");

		assertNull(get(draft, NUMBER));
		assertNull(get(draft, COMPUTED));
	}

	public void testPersistDraftKeepsTransactionDefaults() throws ParseException {
		TLObject draft = draft("draft");

		TLObject created = persistent(execute(
			search("draft -> $draft.copy(transient: false, skipTransactionDefaults: true)"), draft));

		assertFalse(created.tTransient());
		assertEquals("draft", get(created, NAME));
		assertNotNull(get(created, NUMBER));
		assertEquals("computed", get(created, COMPUTED));
	}

	public void testPersistDraftAllocatesNextSequenceNumber() throws ParseException {
		SearchExpression persist = search("draft -> $draft.copy(transient: false, skipTransactionDefaults: true)");

		Number first = (Number) get(persistent(execute(persist, draft("first"))), NUMBER);
		Number second = (Number) get(persistent(execute(persist, draft("second"))), NUMBER);

		assertNotNull(first);
		assertNotNull(second);
		assertEquals(first.longValue() + 1, second.longValue());
	}

	public void testPersistDraftWithoutOptionOverwritesTransactionDefaults() throws ParseException {
		TLObject draft = draft("draft");

		TLObject created = persistent(execute(search("draft -> $draft.copy(transient: false)"), draft));

		assertFalse(created.tTransient());
		assertEquals("draft", get(created, NAME));
		assertNull(get(created, NUMBER));
		assertNull(get(created, COMPUTED));
	}

	public void testFilterCannotCopyTransactionDefaults() throws ParseException {
		TLObject draft = draft("draft");

		TLObject created = persistent(execute(
			search("draft -> $draft.copy(filter: part -> value -> orig -> true, transient: false, skipTransactionDefaults: true)"), draft));

		assertEquals("draft", get(created, NAME));
		assertNotNull(get(created, NUMBER));
		assertEquals("computed", get(created, COMPUTED));
	}

	public void testTransientCopyReceivesValuesOfOriginal() throws ParseException {
		TLObject orig = persistent(execute(search("new(" + TYPE + ")..set(" + NAME + ", 'orig')")));
		Object number = get(orig, NUMBER);
		assertNotNull(number);
		assertEquals("computed", get(orig, COMPUTED));

		TLObject copy = (TLObject) execute(
			search("orig -> $orig.copy(transient: true, skipTransactionDefaults: true)"), orig);

		assertTrue(copy.tTransient());
		assertEquals("orig", get(copy, NAME));
		assertEquals(number, get(copy, NUMBER));
		assertEquals("computed", get(copy, COMPUTED));
	}

	private TLObject draft(String name) throws ParseException {
		TLObject draft = (TLObject) execute(
			search("name -> new(" + TYPE + ", transient: true)..set(" + NAME + ", $name)"), name);
		assertTrue(draft.tTransient());
		return draft;
	}

	private TLObject persistent(Object object) {
		TLObject result = (TLObject) object;
		_persistent.add(result);
		return result;
	}

	private static Object get(TLObject object, String part) throws ParseException {
		return execute(search("x -> $x.get(" + part + ")"), object);
	}

	public static Test suite() {
		return suite(TestCopyTransactionDefaults.class, PersonManager.Module.INSTANCE);
	}

}
