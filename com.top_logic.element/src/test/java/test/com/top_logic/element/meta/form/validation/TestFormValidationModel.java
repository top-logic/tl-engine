/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.validation;

import junit.framework.TestCase;

import com.top_logic.element.meta.form.validation.FormValidationModel;

/**
 * Tests for {@link FormValidationModel}.
 */
public class TestFormValidationModel extends TestCase {

	/**
	 * Tests that isValid() returns true initially when no constraints exist.
	 */
	public void testEmptyModelIsValid() {
		FormValidationModel model = new FormValidationModel();
		assertTrue(model.isValid());
	}
}
