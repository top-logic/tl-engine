/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.CustomPropertiesSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Test for configured defaults for typed configurations.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeConfigurationDefaults extends BasicTestCase {

	static {
		/* Load these classes before reading defaults: The reason is the implementation default that
		 * after the configuration is read, the cached ConfigurationDescriptors are adapted. As this
		 * class tests that the defaults are applied correctly, it is necessary that the
		 * ConfigDescriptors are already created and cached. */
		TypedConfiguration.getConfigurationDescriptor(AConfig.class);
		TypedConfiguration.getConfigurationDescriptor(BConfig.class);
		// Do not load BSubConfig in advance to test, whether an inherited default is applied.
		// TypedConfiguration.getConfigurationDescriptor(BSubConfig.class);
		TypedConfiguration.getConfigurationDescriptor(BConfigWithProgrammaticDefault.class);
		TypedConfiguration.getConfigurationDescriptor(CConfig.class);
	}

	public interface AConfig extends ConfigurationItem {

		int getInt();

	}

	public interface BConfig extends AConfig {
		// just to have sub interface to configure differnt value.
	}

	public interface BSubConfig extends BConfig {
		// just to have sub interface to inherit configured value.
	}

	public interface BConfigWithProgrammaticDefault extends AConfig {

		int B_INT_DEFAULT = 17;

		@IntDefault(B_INT_DEFAULT)
		@Override
		int getInt();

	}

	public interface CConfig extends AConfig {

		String getString();

	}

	public interface DynamicDefault extends ConfigurationItem {

		@ComplexDefault(Counter.class)
		int getX();

		class Counter extends DefaultValueProvider {

			private int _cnt = 0;

			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return Integer.valueOf(_cnt++);
			}

		}

	}

	public void testDirectConfiguredDefault() {
		AConfig a = TypedConfiguration.newConfigItem(AConfig.class);
		assertEquals("Configured default not applied.", 15, a.getInt());
	}

	public void testOverrideConfiguredDefault() {
		BConfig b = TypedConfiguration.newConfigItem(BConfig.class);
		assertEquals("Specialized configured default does not override general configured default.", 987546, b.getInt());
	}

	public void testInheritedOverrideConfiguredDefault() {
		BSubConfig b = TypedConfiguration.newConfigItem(BSubConfig.class);
		assertEquals("Ticket #14459: Specialized configured default is not inherited.", 987546, b.getInt());
	}

	public void testInheritenceDefaultClash() {
		BConfigWithProgrammaticDefault b = TypedConfiguration.newConfigItem(BConfigWithProgrammaticDefault.class);
		assertEquals("Default of specialized interface overridden.", BConfigWithProgrammaticDefault.B_INT_DEFAULT,
			b.getInt());
	}

	/**
	 * Tests that configuration of defaults of super interface are also used.
	 */
	public void testInheritenceDefault() {
		CConfig c = TypedConfiguration.newConfigItem(CConfig.class);
		assertEquals("Configured default of super interface not applied.", 15, c.getInt());
		assertEquals("Default sub interface not applied.", "configuredStringDefault", c.getString());
	}

	public void testDynamicDefault() {
		DynamicDefault v1 = TypedConfiguration.newConfigItem(DynamicDefault.class);
		DynamicDefault v2 = TypedConfiguration.newConfigItem(DynamicDefault.class);

		assertEquals(0, v1.getX());
		assertEquals(1, v2.getX());
	}

	public static Test suite() {
		return BasicTestSetup
			.createBasicTestSetup(new CustomPropertiesSetup(TestTypeConfigurationDefaults.class, true));
	}

}
