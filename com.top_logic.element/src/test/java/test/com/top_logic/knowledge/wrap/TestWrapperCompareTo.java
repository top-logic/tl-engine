/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.element.model.typed.AbstractTypedObjectTest;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;

/**
 * Test of the order {@link Wrapper#compareTo(Wrapper)} for persistent and transient objects.
 */
@SuppressWarnings("javadoc")
public class TestWrapperCompareTo extends AbstractTypedObjectTest {

	private static final String SAME_NAME = "a";

	private static final String OTHER_NAME = "b";

	private Wrapper _persistent1;

	private Wrapper _persistent2;

	private Wrapper _persistentOther;

	private Wrapper _transient1;

	private Wrapper _transient2;

	private Wrapper _transientOther;

	private List<Wrapper> _all;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_persistent1 = persistent(SAME_NAME);
			_persistent2 = persistent(SAME_NAME);
			_persistentOther = persistent(OTHER_NAME);
			tx.commit();
		}
		_transient1 = transientNode(SAME_NAME);
		_transient2 = transientNode(SAME_NAME);
		_transientOther = transientNode(OTHER_NAME);
		_all = List.of(_transient1, _persistentOther, _persistent2, _transientOther, _persistent1, _transient2);
	}

	private Wrapper persistent(String name) {
		TLObject object = newPersistent("NamedItem");
		object.tUpdateByName(TLNamed.NAME_ATTRIBUTE, name);
		assertTrue(object instanceof Wrapper);
		return (Wrapper) object;
	}

	private Wrapper transientNode(String name) {
		StructuredElement result = (StructuredElement) newTransient("Node");
		result.setElementName(name);
		assertTrue(result.tTransient());
		return result;
	}

	/**
	 * A list of persistent and transient objects with partly equal names is sorted by name, and
	 * the same order results from every initial order.
	 */
	public void testSortStable() {
		List<Wrapper> expected = sorted(_all);

		assertEquals(List.of(SAME_NAME, SAME_NAME, SAME_NAME, SAME_NAME, OTHER_NAME, OTHER_NAME),
			expected.stream().map(Wrapper::getName).toList());

		Random random = new Random(42);
		for (int n = 0; n < 20; n++) {
			List<Wrapper> shuffled = new ArrayList<>(_all);
			Collections.shuffle(shuffled, random);
			assertEquals(expected, sorted(shuffled));
		}
	}

	/**
	 * A persistent object is ordered before a transient object with the same name.
	 */
	public void testPersistentBeforeTransient() {
		assertTrue(_persistent1.compareTo(_transient1) < 0);
		assertTrue(_transient1.compareTo(_persistent1) > 0);
		assertTrue(_persistentOther.compareTo(_transientOther) < 0);
		assertTrue(_transientOther.compareTo(_persistentOther) > 0);

		List<Wrapper> expected = List.of(_persistent1, _persistent2);
		List<Wrapper> sameName = sorted(_all).subList(0, 2);
		assertTrue(sameName.containsAll(expected));
	}

	/**
	 * Two persistent objects with the same name are ordered by their object keys.
	 */
	public void testPersistentByObjectKey() {
		int expected = Integer.signum(_persistent1.tId().toString().compareTo(_persistent2.tId().toString()));
		assertTrue(expected != 0);
		assertEquals(expected, Integer.signum(_persistent1.compareTo(_persistent2)));
	}

	/**
	 * Two transient objects with the same name are ordered by their identity hash codes.
	 */
	public void testTransientByIdentity() {
		int expected =
			Integer.compare(System.identityHashCode(_transient1), System.identityHashCode(_transient2));
		assertEquals(expected, Integer.signum(_transient1.compareTo(_transient2)));
	}

	/**
	 * The order is antisymmetric, and an object is equal to itself only.
	 */
	public void testAntisymmetric() {
		for (Wrapper left : _all) {
			assertEquals(0, left.compareTo(left));
			for (Wrapper right : _all) {
				assertEquals(left + " / " + right, -Integer.signum(right.compareTo(left)),
					Integer.signum(left.compareTo(right)));
			}
		}
	}

	private static List<Wrapper> sorted(List<Wrapper> objects) {
		List<Wrapper> result = new ArrayList<>(objects);
		Collections.sort(result);
		return result;
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suite(TestWrapperCompareTo.class);
	}

}
