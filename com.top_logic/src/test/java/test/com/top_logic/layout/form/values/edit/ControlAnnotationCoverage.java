/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.top_logic.layout.form.values.edit.ControlAnnotationOptions;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * Check that a form stack maps every {@link ControlAnnotation} of the application to a control.
 */
public class ControlAnnotationCoverage {

	private static final String TEST_PACKAGE_PREFIX = "test.";

	/**
	 * Asserts that every annotation marked with {@link ControlAnnotation} in the application is
	 * either mapped or listed as exception, and that no exception is mapped.
	 *
	 * @param stack
	 *        Name of the form stack for the failure message.
	 * @param mapped
	 *        The annotations the form stack maps to a control.
	 * @param exceptions
	 *        Qualified names of annotations the form stack does not map, each with the reason. An
	 *        annotation of a module that is not part of the application is ignored.
	 *
	 * @implNote Annotations of tests (in the package {@value #TEST_PACKAGE_PREFIX}) are ignored:
	 *           the test jars on the class path of a test bring their own type index.
	 */
	public static void assertMapped(String stack, Collection<? extends Class<?>> mapped,
			Map<String, String> exceptions) {
		List<String> missing = new ArrayList<>();
		List<String> mappedExceptions = new ArrayList<>();
		for (Class<?> annotation : ControlAnnotationOptions.controlAnnotations()) {
			if (annotation.getName().startsWith(TEST_PACKAGE_PREFIX)) {
				continue;
			}
			boolean isMapped = mapped.contains(annotation);
			boolean isException = exceptions.containsKey(annotation.getName());
			if (isMapped && isException) {
				mappedExceptions.add(annotation.getName());
			} else if (!isMapped && !isException) {
				missing.add(annotation.getName());
			}
		}
		Assert.assertEquals("Control annotations without a control in the " + stack
			+ ". Map them, or list them as exception with the reason.", List.of(), missing);
		Assert.assertEquals("Control annotations listed as exception but mapped in the " + stack
			+ ". Remove them from the exceptions.", List.of(), mappedExceptions);
	}

}
