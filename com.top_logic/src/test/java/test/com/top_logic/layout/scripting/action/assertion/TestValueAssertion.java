/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.action.assertion;

import static com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator.*;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion.Comparision;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Test for {@link ValueAssertion}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings({ "javadoc", "deprecation" })
public class TestValueAssertion extends BasicTestCase {

	public void testComparision() {
		assertCompare(intValue(0), intValue(0), Comparision.LOWER_OR_EQUAL);
		assertCompare(intValue(0), intValue(2), Comparision.LOWER_OR_EQUAL);
		assertNotCompare(intValue(2), intValue(0), Comparision.LOWER_OR_EQUAL);

		assertNotCompare(intValue(0), intValue(0), Comparision.LOWER_THAN);
		assertCompare(intValue(0), intValue(2), Comparision.LOWER_THAN);
		assertNotCompare(intValue(2), intValue(0), Comparision.LOWER_THAN);

		assertCompare(intValue(0), intValue(0), Comparision.GREATER_OR_EQUAL);
		assertNotCompare(intValue(0), intValue(2), Comparision.GREATER_OR_EQUAL);
		assertCompare(intValue(2), intValue(0), Comparision.GREATER_OR_EQUAL);

		assertNotCompare(intValue(0), intValue(0), Comparision.GREATER_THAN);
		assertNotCompare(intValue(0), intValue(2), Comparision.GREATER_THAN);
		assertCompare(intValue(2), intValue(0), Comparision.GREATER_THAN);

	}

	private void assertNotCompare(ValueRef actual, ValueRef expected, Comparision comparision) {
		assertCompare(actual, expected, comparision, true);
	}

	private void assertCompare(ValueRef actual, ValueRef expected, Comparision comparision) {
		assertCompare(actual, expected, comparision, false);
	}

	private void assertCompare(ValueRef actual, ValueRef expected, Comparision comparision, boolean expectedFailure) {
		ValueAssertion valueAssertion = ActionFactory.valueAssertion(expected, actual, comparision, false, "");
		Object actualValue = actual.visit(ValueResolver.DEFAULT, null);
		Object expectedValue = expected.visit(ValueResolver.DEFAULT, null);
		Comparision comparision2 = valueAssertion.getComparision();
		if (expectedFailure && comparision2.compare(valueAssertion, actualValue, expectedValue)) {
			fail("Expected " + comparision2 + " compares actual (" + actualValue + ") and expected (" + expectedValue
				+ ") to true");
		} else if (!expectedFailure && !comparision2.compare(valueAssertion, actualValue, expectedValue)) {
			fail("Expected " + comparision2 + " compares actual (" + actualValue + ") and expected (" + expectedValue
				+ ") to false");
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestValueAssertion}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestValueAssertion.class);
	}

}

