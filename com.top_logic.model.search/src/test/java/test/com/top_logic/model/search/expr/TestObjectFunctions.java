/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import junit.framework.Test;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ObjectFunctions;

/**
 * Tests for the TL-Script functions identifying an object by a text.
 *
 * @see ObjectFunctions
 */
@SuppressWarnings("javadoc")
public class TestObjectFunctions extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestSearchExpression:A";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// The result of a script is filtered by the read rights of the current user.
		becomeUser(PersonManager.getManager().getRoot());
	}

	public void testIdAndResolve() throws Exception {
		TLObject object = newObject(TYPE, "identified");

		String id = (String) eval("obj -> objectId($obj)", object);
		assertEquals("The identifier is the local one, without the table.",
			object.tIdLocal().toExternalForm(), id);
		assertEquals(object, eval("id -> $id.objectResolve(`" + TYPE + "`)", id));
	}

	public void testKeyAndResolve() throws Exception {
		TLObject object = newObject(TYPE, "keyed");

		String key = (String) eval("obj -> objectKey($obj)", object);
		assertEquals("The key is the text form of the object's identifier.", object.tId().asString(), key);
		assertTrue("The key starts with the table storing the object: " + key,
			key.startsWith(object.tHandle().tTable().getName() + ":"));
		assertFalse("The key of a current object names no revision: " + key, key.contains("@"));
		assertEquals(object, eval("key -> objectResolveKey($key)", key));
	}

	public void testKeyOfHistoricObject() throws Exception {
		TLObject object = newObject(TYPE, "historic");
		TLObject historic = WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(), object);

		String key = (String) eval("obj -> objectKey($obj)", historic);
		assertTrue("The key of a historic object names its revision: " + key, key.contains("@"));
		assertEquals("A key naming a revision finds the object as it was then.", historic,
			eval("key -> objectResolveKey($key)", key));

		delete(object);
		assertEquals("A deleted object is still found in the revision it lived in.", historic,
			eval("key -> objectResolveKey($key)", key));
		assertNull("A deleted object is not found now.", eval("key -> objectResolveKey($key)", object.tId().asString()));
	}

	public void testKeyOfTransientObject() throws Exception {
		assertNull("A transient object has no key.", eval("objectKey(new(`" + TYPE + "`, transient: true))"));
		assertNull("A transient object has no identifier.",
			eval("objectId(new(`" + TYPE + "`, transient: true))"));
	}

	public void testResolveNoKey() throws Exception {
		assertNull(eval("objectResolveKey('nonsense')"));
		assertNull(eval("objectResolveKey('NoSuchTable:1')"));
		assertNull(eval("objectResolveKey('')"));
		assertNull(eval("objectResolveKey(null)"));
	}

	public static Test suite() {
		return suite(TestObjectFunctions.class, PersonManager.Module.INSTANCE);
	}

}
