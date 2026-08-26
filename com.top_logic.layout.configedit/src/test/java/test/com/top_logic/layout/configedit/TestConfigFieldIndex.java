/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;

/**
 * Tests for {@link ConfigFieldIndex}.
 */
public class TestConfigFieldIndex extends TestCase {

	/**
	 * Test configuration interface.
	 */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	public void testRegisterAndLookup() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);
		ConfigFieldIndex index = new ConfigFieldIndex();

		index.register(config, property, model);

		assertSame(model, index.lookup(config, property));
	}

	public void testAnUnregisteredFieldIsNotFound() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);

		assertNull(new ConfigFieldIndex().lookup(config, property));
	}

	/**
	 * Two items of the same type with the same content are still two fields - the index must not
	 * confuse them.
	 */
	public void testTwoEqualItemsAreToldApart() {
		TestConfig first = TypedConfiguration.newConfigItem(TestConfig.class);
		TestConfig second = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = first.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel firstModel = new ConfigFieldModel(first, property);
		ConfigFieldModel secondModel = new ConfigFieldModel(second, property);
		ConfigFieldIndex index = new ConfigFieldIndex();

		index.register(first, property, firstModel);
		index.register(second, property, secondModel);

		assertSame(firstModel, index.lookup(first, property));
		assertSame(secondModel, index.lookup(second, property));
	}

	/** Every render cycle refills the index, so what an earlier one put there must go. */
	public void testClearForgetsEverything() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldIndex index = new ConfigFieldIndex();
		index.register(config, property, new ConfigFieldModel(config, property));

		index.clear();

		assertNull(index.lookup(config, property));
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration}.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFieldIndex.class, TypeIndex.Module.INSTANCE));
	}
}
