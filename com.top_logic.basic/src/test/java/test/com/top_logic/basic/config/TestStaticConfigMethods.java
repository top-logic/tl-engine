/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test of using static and default methods in {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestStaticConfigMethods extends AbstractTypedConfigurationTestCase {

	public static interface ConfigWithStaticMethod extends ConfigurationItem {

		static void staticMethod() {
			// something is done here
		}

		String INT_NAME = "int";

		@Name(INT_NAME)
		static int getInt() {
			return 0;
		}

	}

	public static interface ConfigWithDefaultMethod extends ConfigurationItem {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		default int findInteger() {
			return 0;
		}
	}

	public static interface ConfigWithDefaultMethodExt extends ConfigWithDefaultMethod {
		// empty intermediate config item
	}

	public static interface ConfigWithDefaultMethodExt2 extends ConfigWithDefaultMethodExt {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default int findInteger() {
			return 2;
		}
	}

	public void testDefaultMethods() {
		assertEquals(0, TypedConfiguration.newConfigItem(ConfigWithDefaultMethod.class).findInteger());
		assertEquals("Intermediate items do not need LOOKUP constant", 0,
			TypedConfiguration.newConfigItem(ConfigWithDefaultMethodExt.class).findInteger());
		assertEquals(2, TypedConfiguration.newConfigItem(ConfigWithDefaultMethodExt2.class).findInteger());
	}

	public void testStaticMethods() {
		ConfigurationDescriptor descr = TypedConfiguration.getConfigurationDescriptor(ConfigWithStaticMethod.class);
		assertNull("Static methods are ignored by typed configuration.",
			descr.getProperty(ConfigWithStaticMethod.INT_NAME));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName =
			new HashMap<>();
		return globalDescriptorsByLocalName;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestStaticConfigMethods}.
	 */
	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestStaticConfigMethods.class);
	}

}
