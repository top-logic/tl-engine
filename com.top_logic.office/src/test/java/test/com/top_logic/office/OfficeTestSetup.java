/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.office;

import junit.extensions.TestSetup;
import junit.framework.Test;
import test.com.top_logic.ModuleLicenceTestSetup;

/**
 * Factory class to create {@link TestSetup} for office tests.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OfficeTestSetup {

	/**
	 * Sets up the module for the given test.
	 * 
	 * @see ModuleLicenceTestSetup#setupModule(Test)
	 */
	public static Test createOfficeTestSetup(Test wrapped) {
		return ModuleLicenceTestSetup.setupModule(wrapped);
	}

	/**
	 * Sets up the module for the given test class.
	 * 
	 * @see ModuleLicenceTestSetup#setupModule(Class)
	 */
	public static Test createOfficeTestSetup(Class<?> testClass) {
		return ModuleLicenceTestSetup.setupModule(testClass);
	}

}

