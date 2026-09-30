/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.layout.view.form.AttributeOptions;

/**
 * Tests that {@link AttributeOptions} offers only options the current user may read.
 */
public class TestAttributeOptionsReadFilter extends AbstractModelAccessTest {

	/**
	 * The options of a reference exclude the objects the user may not read.
	 */
	public void testReferenceOptionsExcludeUnreadable() {
		becomeUser(_responsible);
		List<?> options = referenceOptions();

		assertTrue(options.contains(_category));
		assertFalse("An unreadable object is no option.", options.contains(_hiddenCategory));
	}

	/**
	 * A user bypassing the access rights is offered all objects.
	 */
	public void testReferenceOptionsOfRoot() {
		becomeUser(_root);
		List<?> options = referenceOptions();

		assertTrue(options.contains(_category));
		assertTrue(options.contains(_hiddenCategory));
	}

	/**
	 * The options of a value that no attribute holds exclude the objects the user may not read.
	 */
	public void testTypeOptionsExcludeUnreadable() {
		becomeUser(_responsible);
		List<?> options = AttributeOptions.optionsFor(type(CATEGORY));

		assertTrue(options.contains(_category));
		assertFalse("An unreadable object is no option.", options.contains(_hiddenCategory));
	}

	private List<?> referenceOptions() {
		return AttributeOptions.optionsFor(_project, part(PROJECT, CATEGORY_REF), null, dependency -> {
			// The dependencies of a reference's options are not of interest here.
		});
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestAttributeOptionsReadFilter.class);
	}

}
