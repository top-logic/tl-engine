/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.constraint.SizeConstraintCheck;
import com.top_logic.model.annotate.TLSize;

/**
 * Test case for the {@link TLSize} evaluation in {@link AttributeOperations}.
 */
@SuppressWarnings("javadoc")
public class TestAttributeOperations extends TestCase {

	public void testNoAnnotation() {
		assertEquals(0, AttributeOperations.getLowerBound((TLSize) null));
		assertEquals(255, AttributeOperations.getUpperBound((TLSize) null));
	}

	/**
	 * A bare <code>&lt;size-constraint/&gt;</code> must not impose an upper bound.
	 *
	 * <p>
	 * Ticket #29465: {@link TLSize#NO_UPPER_BOUND} was truncated to <code>-1</code>, which made
	 * every non-empty text too long.
	 * </p>
	 */
	public void testUnboundedSize() {
		TLSize size = TypedConfiguration.newConfigItem(TLSize.class);

		assertEquals(0, AttributeOperations.getLowerBound(size));
		assertEquals(Integer.MAX_VALUE, AttributeOperations.getUpperBound(size));

		assertNull(check(size).checkValue("Some text that must be accepted."));
	}

	public void testExplicitBounds() {
		TLSize size = TypedConfiguration.newConfigItem(TLSize.class);
		size.setLowerBound(2);
		size.setUpperBound(5);

		assertEquals(2, AttributeOperations.getLowerBound(size));
		assertEquals(5, AttributeOperations.getUpperBound(size));

		SizeConstraintCheck check = check(size);
		assertNotNull(check.checkValue("a"));
		assertNull(check.checkValue("abc"));
		assertNotNull(check.checkValue("abcdef"));
	}

	public void testLowerBoundOnly() {
		TLSize size = TypedConfiguration.newConfigItem(TLSize.class);
		size.setLowerBound(3);

		assertEquals(3, AttributeOperations.getLowerBound(size));
		assertEquals(Integer.MAX_VALUE, AttributeOperations.getUpperBound(size));

		SizeConstraintCheck check = check(size);
		assertNotNull(check.checkValue("ab"));
		assertNull(check.checkValue("abcdefghij"));
	}

	private static SizeConstraintCheck check(TLSize size) {
		return new SizeConstraintCheck(AttributeOperations.getLowerBound(size),
			AttributeOperations.getUpperBound(size));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAttributeOperations.class, TypeIndex.Module.INSTANCE));
	}
}
