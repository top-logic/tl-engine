/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Factory for {@link TestSetupDecorator} that installs a custom configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CustomPropertiesDecorator {

	/**
	 * Create the default filename for a custom configuration file for the given class.
	 *
	 * @see #createFileName(Class, String)
	 */
	public static String createFileName(Class<?> testClass) {
		return createFileName(testClass, customPropertiesFileName(testClass));
	}

	static String customPropertiesFileName(Class<?> testClass) {
		return testClass.getSimpleName() + ".properties.xml";
	}

	/**
	 * Creates a path to the file with the given filename next to the given test class.
	 * 
	 * <p>
	 * The path must be resolved to a {@link File} using the {@link FileManager}.
	 * </p>
	 */
	public static String createFileName(Class<?> testClass, String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(ModuleLayoutConstants.PATH_TO_MODULE_ROOT);
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(ModuleLayoutConstants.SRC_TEST_DIR);
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(testClass.getPackage().getName().replace('.', File.separatorChar));
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(fileName);
		return stringBuilder.toString();
	}

	/**
	 * Creates a {@link TestSetupDecorator} installing custom configuration for the given test
	 * classes.
	 * 
	 * @param testClasses
	 *        The test classes to use to resolve custom configuration files.
	 * @param withDefaultConfig
	 *        Whether the new configuration are increments for the default configuration.
	 */
	public static TestSetupDecorator newDecorator(Class<?>[] testClasses, boolean withDefaultConfig) {
		return new ClassBasedPropertiesDecorator(testClasses, withDefaultConfig);
	}

	/**
	 * Creates a {@link TestSetupDecorator} installing custom configuration for the given test
	 * class.
	 * 
	 * @see #newDecorator(Class[], boolean)
	 */
	public static TestSetupDecorator newDecorator(Class<?> testClass, boolean withDefaultConfig) {
		return newDecorator(new Class<?>[] { testClass }, withDefaultConfig);
	}

	/**
	 * Creates a {@link TestSetupDecorator} installing the configuration derived from the file with
	 * the given name.
	 * 
	 * @param fileName
	 *        Name of the file containing the custom configuration. The file is resolved using the
	 *        {@link FileManager}.
	 * @param withDefaultConfig
	 *        Whether the new configuration is an increment for the default configuration.
	 */
	public static TestSetupDecorator newDecorator(String fileName, boolean withDefaultConfig) {
		return new FileBasedPropertiesDecorator(fileName, withDefaultConfig);
	}

}

