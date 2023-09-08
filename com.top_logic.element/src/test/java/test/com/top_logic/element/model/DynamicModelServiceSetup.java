/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import static com.top_logic.basic.config.ConfigurationSchemaConstants.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.CustomConfigurationDecorator;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.SimpleDecoratedTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.MultiProperties;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.util.model.ModelService;

/**
 * Factory method for tests that have additional factory configuration for
 * {@link DynamicModelService}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicModelServiceSetup {

	private static class DynamicModelServiceEnhancer extends CustomConfigurationDecorator {

		private final Class<? extends Test> _testClass;

		public DynamicModelServiceEnhancer(Class<? extends Test> testClass) {
			_testClass = testClass;
		}

		@Override
		protected void installConfiguration() throws Exception {
			String config = "<application xmlns:" + CONFIG_NS_PREFIX + "=\"" + CONFIG_NS + "\">" +
				"<services>" +
				"<config service-class=\"" + ModelService.class.getName() + "\">" +
				"<instance class=\"" + DynamicModelService.class.getName() + "\">" +
				"<declarations>" +
				"<declaration file=\"" + factoryFile() + "\"/>" +
				"</declarations>" +
				"</instance>" +
				"</config>" +
				"</services>" +
				"</application>";

			BinaryContent typedConfig = BinaryDataFactory.createBinaryData(config.getBytes());
			MultiProperties.restartWithConfigs(null, typedConfig);
		}

		private String factoryFile() {
			String fileName = _testClass.getSimpleName() + ".factoryConfig.xml";
			return CustomPropertiesDecorator.createFileName(_testClass, fileName);
		}

	}

	/**
	 * Creates a {@link Test} for the given test class that enhances the {@link DynamicModelService}
	 * with a test structure.
	 * 
	 * <p>
	 * The test structure must be available in a file named "&lt;xxx&gt;.factoryConfig.xml" where
	 * "xxx" is the name of the test class. The configuration file is searchednext to the test
	 * class.
	 * </p>
	 */
	public static Test suite(Class<? extends Test> testClass) {
		Test kbTest = KBSetup.getKBTestWithoutSetups(testClass, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test innerTest = suite;
				return ServiceTestSetup.createSetup(innerTest, ModelService.Module.INSTANCE);
			}
		});
		TestSetupDecorator decorator = new DynamicModelServiceEnhancer(testClass);
		SimpleDecoratedTestSetup decoratedTest = new SimpleDecoratedTestSetup(decorator, kbTest);
		Test tlCommonSetups = KBSetup.wrapTLCommonSetups(decoratedTest);
		return TestUtils.doNotMerge(tlCommonSetups);
	}
}
