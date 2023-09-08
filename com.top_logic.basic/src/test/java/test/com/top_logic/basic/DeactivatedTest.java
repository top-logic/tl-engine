/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import test.com.top_logic.basic.junit.DirectoryLocalTestCollector;

/**
 * Annotation for test classes which must not be recognized during automatic
 * test suite creation.
 * 
 * @see DirectoryLocalTestCollector
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeactivatedTest {

	/**
	 * Returns the reason for the deactivation of the test class. 
	 */
	String value();
}
