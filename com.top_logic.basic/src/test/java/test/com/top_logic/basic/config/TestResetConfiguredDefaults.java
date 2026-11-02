/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * Test that configured defaults of the {@link ApplicationConfig} are removed when it is shut down
 * or started with other defaults.
 */
@SuppressWarnings("javadoc")
public class TestResetConfiguredDefaults extends TestCase {

	public interface AConfig extends ConfigurationItem {

		@IntDefault(1)
		int getValue();

	}

	public interface BConfig extends AConfig {
		// Receives its own configured default.
	}

	public interface BSubConfig extends BConfig {
		// Not loaded before the defaults are installed.
	}

	public interface MandatoryConfig extends ConfigurationItem {

		@Mandatory
		String getName();

	}

	/**
	 * Whether {@link #setUp()} started the {@link TypeIndex}, which is required for reading the
	 * application configuration.
	 */
	private boolean _startedTypeIndex;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertFalse("Test requires the application configuration not to be loaded.",
			ApplicationConfig.Module.INSTANCE.isActive());
		if (!TypeIndex.Module.INSTANCE.isActive()) {
			ModuleUtil.INSTANCE.startUp(TypeIndex.Module.INSTANCE);
			_startedTypeIndex = true;
		}

		// Load before the defaults are installed.
		TypedConfiguration.getConfigurationDescriptor(AConfig.class);
		TypedConfiguration.getConfigurationDescriptor(BConfig.class);
		TypedConfiguration.getConfigurationDescriptor(MandatoryConfig.class);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_startedTypeIndex) {
			ModuleUtil.INSTANCE.shutDown(TypeIndex.Module.INSTANCE);
		}
		super.tearDown();
	}

	public void testResetOnShutDown() throws Exception {
		startUp(
			"<config interface='" + AConfig.class.getName() + "'><property name='value' value='15'/></config>"
				+ "<config interface='" + MandatoryConfig.class.getName()
				+ "'><property name='name' value='configured'/></config>");
		try {
			assertEquals(15, value(AConfig.class));
			assertEquals(15, value(BSubConfig.class));
			assertFalse(isNameMandatory());
		} finally {
			shutDown();
		}

		assertEquals(1, value(AConfig.class));
		assertEquals(1, value(BConfig.class));
		assertEquals("Descriptor loaded while the default was installed keeps the removed default.", 1, value(BSubConfig.class));
		assertTrue(isNameMandatory());
	}

	public void testRemovedSpecialDefault() throws Exception {
		startUp(
			"<config interface='" + AConfig.class.getName() + "'><property name='value' value='15'/></config>"
				+ "<config interface='" + BConfig.class.getName() + "'><property name='value' value='99'/></config>");
		try {
			assertEquals(15, value(AConfig.class));
			assertEquals(99, value(BConfig.class));
		} finally {
			shutDown();
		}

		startUp(
			"<config interface='" + AConfig.class.getName() + "'><property name='value' value='20'/></config>");
		try {
			assertEquals(20, value(AConfig.class));
			assertEquals("Removed default of sub interface still active.", 20, value(BConfig.class));
			assertEquals(20, value(BSubConfig.class));
		} finally {
			shutDown();
		}
	}

	private static int value(Class<? extends AConfig> type) {
		return TypedConfiguration.newConfigItem(type).getValue();
	}

	private static boolean isNameMandatory() {
		return TypedConfiguration.getConfigurationDescriptor(MandatoryConfig.class).getProperty("name").isMandatory();
	}

	private static void startUp(String defaults) throws ConfigurationException, ModuleException {
		String xml = "<application><defaults>" + defaults + "</defaults></application>";
		ApplicationConfig.Config config = (ApplicationConfig.Config) new ConfigurationReader(
			new DefaultInstantiationContext(TestResetConfiguredDefaults.class),
			Collections.singletonMap(ApplicationConfig.ROOT_TAG,
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class)))
					.setSource(CharacterContents.newContent(xml))
					.read();
		ApplicationConfig.Module.INSTANCE.startUp(config);
	}

	private static void shutDown() {
		ModuleUtil.INSTANCE.shutDown(ApplicationConfig.Module.INSTANCE);
	}

}
