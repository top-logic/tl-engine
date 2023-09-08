/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Setup that installs a custom configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomPropertiesSetup extends SimpleDecoratedTestSetup {

	/**
	 * Creates a {@link CustomPropertiesSetup}.
	 * 
	 * @param test
	 *        The test to wrap.
	 * @param fileName
	 *        The configuration file name to install.
	 */
	public CustomPropertiesSetup(Test test, String fileName) {
		this(test, fileName, false);
	}
	
	/**
	 * Creates a {@link CustomPropertiesSetup}.
	 * 
	 * @param test
	 *        The test to wrap.
	 * @param fileName
	 *        The configuration file name to install.
	 * @param useDefault
	 *        also use standard configuration
	 */
	public CustomPropertiesSetup(Test test, String fileName, boolean useDefault) {
		super(CustomPropertiesDecorator.newDecorator(fileName, useDefault), test);
	}

	/**
	 * Creates a {@link CustomPropertiesSetup} from the given test class. It is expected that near
	 * the source of the test class there is a ".properties.xml" with the same name as the test
	 * class. That file is taken as properties file.
	 * 
	 * @param testClass
	 *        Test class which is used to build a {@link TestSuite}. That test is used as inner
	 *        test.
	 * 
	 * @see CustomPropertiesSetup#CustomPropertiesSetup(Class, boolean)
	 */
	public CustomPropertiesSetup(Class<? extends Test> testClass) {
		this(testClass, false);
	}

	/**
	 * Creates a {@link CustomPropertiesSetup} from the given test class. It is expected that near
	 * the source of the test class there is a ".properties.xml" with the same name as the test
	 * class. That file is taken as additional properties file.
	 * 
	 * @param testClass
	 *        Test class which is used to build a {@link TestSuite}. That test is used as inner
	 *        test.
	 * @param useDefault
	 *        also use standard configuration
	 */
	public CustomPropertiesSetup(Class<? extends Test> testClass, boolean useDefault) {
		super(CustomPropertiesDecorator.newDecorator(testClass, useDefault), new TestSuite(testClass));
	}

}