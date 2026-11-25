/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.providers.AttributeByExpression;
import com.top_logic.model.search.providers.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for the result of an {@link AttributeByExpression} of a primitive type.
 *
 * <p>
 * A script result the storage mapping of the attribute type can convert is delivered as converted
 * value. A result it cannot convert is reported as {@link TopLogicException} naming the attribute,
 * not as a bare {@link ClassCastException}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDerivedPrimitiveResult extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestDerivedPrimitiveResult:Item";

	public void testStringResult() {
		TLObject item = newObject(TYPE, "A");
		assertEquals("Item A", item.tValueByName("text"));
	}

	public void testNumberResultConverted() {
		TLObject item = newObject(TYPE, "A");
		assertEquals(Integer.valueOf(42), item.tValueByName("number"));
	}

	public void testResKeyForStringAttribute() {
		assertIncompatibleResult(newObject(TYPE, "A"), "textFromResKey");
	}

	public void testStringForNumberAttribute() {
		assertIncompatibleResult(newObject(TYPE, "A"), "numberFromString");
	}

	private static void assertIncompatibleResult(TLObject item, String attribute) {
		try {
			Object value = item.tValueByName(attribute);
			fail("Incompatible script result must not be delivered: " + value);
		} catch (TopLogicException ex) {
			ResKey errorKey = ex.getErrorKey();
			assertEquals(
				I18NConstants.ERROR_SCRIPT_RESULT_OF_INCOMPATIBLE_TYPE__ATTR_EXPECTED_ACTUAL.fill(null, null, null).plain(),
				errorKey.plain());
			assertEquals(TYPE + "#" + attribute, errorKey.arguments()[0]);
		}
	}

	public static Test suite() {
		return suite(TestDerivedPrimitiveResult.class);
	}

}
