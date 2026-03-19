/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.constraint;

import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.constraint.MandatoryConstraintCheck;
import com.top_logic.element.meta.form.constraint.RangeConstraintCheck;
import com.top_logic.element.meta.form.constraint.SizeConstraintCheck;

/**
 * Tests for built-in {@link com.top_logic.model.annotate.util.ConstraintCheck} adapters.
 */
public class TestBuiltInConstraintChecks extends TestCase {

	public void testMandatoryRejectsNull() {
		assertNotNull(MandatoryConstraintCheck.INSTANCE.checkValue(null));
	}

	public void testMandatoryRejectsEmptyString() {
		assertNotNull(MandatoryConstraintCheck.INSTANCE.checkValue(""));
	}

	public void testMandatoryRejectsEmptyCollection() {
		assertNotNull(MandatoryConstraintCheck.INSTANCE.checkValue(Collections.emptyList()));
	}

	public void testMandatoryAcceptsNonEmpty() {
		assertNull(MandatoryConstraintCheck.INSTANCE.checkValue("hello"));
	}

	public void testSizeCheckAcceptsValidLength() {
		assertNull(new SizeConstraintCheck(3, 10).checkValue("hello"));
	}

	public void testSizeCheckRejectsTooShort() {
		assertNotNull(new SizeConstraintCheck(3, 10).checkValue("ab"));
	}

	public void testSizeCheckRejectsTooLong() {
		assertNotNull(new SizeConstraintCheck(0, 5).checkValue("toolongstring"));
	}

	public void testSizeCheckAcceptsNull() {
		assertNull(new SizeConstraintCheck(3, 10).checkValue(null));
	}

	public void testRangeCheckAcceptsInRange() {
		assertNull(new RangeConstraintCheck(1.0, 100.0).checkValue(50.0));
	}

	public void testRangeCheckRejectsBelowMin() {
		assertNotNull(new RangeConstraintCheck(1.0, 100.0).checkValue(0.5));
	}

	public void testRangeCheckRejectsAboveMax() {
		assertNotNull(new RangeConstraintCheck(1.0, 100.0).checkValue(101.0));
	}

	public void testRangeCheckAcceptsNull() {
		assertNull(new RangeConstraintCheck(1.0, 100.0).checkValue(null));
	}

	public void testRangeCheckMinOnly() {
		RangeConstraintCheck check = new RangeConstraintCheck(0.0, null);
		assertNotNull(check.checkValue(-1.0));
		assertNull(check.checkValue(1000.0));
	}

	public void testRangeCheckMaxOnly() {
		RangeConstraintCheck check = new RangeConstraintCheck(null, 100.0);
		assertNull(check.checkValue(-1000.0));
		assertNotNull(check.checkValue(101.0));
	}
}
