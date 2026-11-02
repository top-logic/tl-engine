/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.model.security.CheckedObjects.Outcome;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Test of the evaluation of checked objects in {@link CheckedObjects}: a single object, a
 * collection, <code>null</code> and an empty collection.
 *
 * <p>
 * The checks on persistent objects are tested with the model based access rights in
 * <code>test.com.top_logic.model.search.expr.TestOperationDeclarations</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCheckedObjects extends TestCase {

	private final List<Object> _checked = new ArrayList<>();

	/**
	 * Allows strings starting with "a", denies other strings, finds every other value invalid.
	 */
	private final Function<Object, Decision> _elementCheck = element -> {
		_checked.add(element);
		if (!(element instanceof String string)) {
			return Decision.invalid(element);
		}
		return string.startsWith("a") ? Decision.ALLOWED : Decision.denied(element);
	};

	public void testNullIsNoObject() {
		assertSame(Decision.NO_OBJECT, CheckedObjects.check(null, _elementCheck));
		assertEquals(Collections.emptyList(), _checked);
	}

	public void testEmptyCollectionIsNoObject() {
		assertSame(Decision.NO_OBJECT, CheckedObjects.check(Collections.emptyList(), _elementCheck));
		assertSame(Decision.NO_OBJECT, CheckedObjects.check(Collections.emptySet(), _elementCheck));
		assertEquals(Collections.emptyList(), _checked);
	}

	public void testSingleObject() {
		assertSame(Decision.ALLOWED, CheckedObjects.check("a1", _elementCheck));

		Decision denied = CheckedObjects.check("b1", _elementCheck);
		assertEquals(Outcome.DENIED, denied.getOutcome());
		assertEquals("b1", denied.getElement());
		assertFalse(denied.isAllowed());
	}

	public void testCollectionAllAllowed() {
		assertSame(Decision.ALLOWED, CheckedObjects.check(Arrays.asList("a1", "a2", "a3"), _elementCheck));
		assertEquals(Arrays.asList("a1", "a2", "a3"), _checked);
	}

	public void testCollectionOneDenied() {
		Decision decision = CheckedObjects.check(Arrays.asList("a1", "b2", "a3"), _elementCheck);
		assertEquals(Outcome.DENIED, decision.getOutcome());
		assertEquals("b2", decision.getElement());
		assertEquals("The check stops at the first element not allowed.", Arrays.asList("a1", "b2"), _checked);
	}

	public void testCollectionWithInvalidElement() {
		Decision decision = CheckedObjects.check(Arrays.asList("a1", Integer.valueOf(42)), _elementCheck);
		assertEquals(Outcome.INVALID, decision.getOutcome());
		assertEquals(Integer.valueOf(42), decision.getElement());

		Decision withNull = CheckedObjects.check(Arrays.asList("a1", null), _elementCheck);
		assertEquals("A null element is checked as an element.", Outcome.INVALID, withNull.getOutcome());
		assertNull(withNull.getElement());
	}

	public void testValueThatIsNoObjectIsInvalid() {
		Decision decision = CheckedObjects.check(null, "no object", SimpleBoundCommandGroup.READ);
		assertEquals(Outcome.INVALID, decision.getOutcome());
		assertEquals("no object", decision.getElement());

		Decision stored = CheckedObjects.checkStored(null, Arrays.asList(Integer.valueOf(1)), SimpleBoundCommandGroup.READ);
		assertEquals(Outcome.INVALID, stored.getOutcome());
		assertSame(Decision.NO_OBJECT, CheckedObjects.checkStored(null, null, SimpleBoundCommandGroup.READ));
	}

}
