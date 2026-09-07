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
import com.top_logic.basic.config.equal.EqualityByValue;
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

	/**
	 * Test configuration interface with value equality, unlike {@link TestConfig}: two instances
	 * with the same property values are {@link Object#equals(Object) equal} and share a
	 * {@link Object#hashCode() hash code}. This is the fixture that actually pins the index's use
	 * of {@link java.util.IdentityHashMap} at the outer level - two content-equal
	 * {@link TestConfig} instances are already told apart by the default identity
	 * {@code equals}/{@code hashCode}, so a plain {@link java.util.HashMap} would pass that case
	 * too.
	 */
	public interface ValueEqualTestConfig extends EqualityByValue {

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

	/**
	 * Two {@link EqualityByValue} items with the same content are still two separate fields - the
	 * index must not confuse them, even though {@link EqualityByValue#equals(Object)} says they
	 * are the same item. This is what actually requires the outer map to be an
	 * {@link java.util.IdentityHashMap}: unlike {@link #testTwoEqualItemsAreToldApart()}, a plain
	 * {@link java.util.HashMap} would merge these two registrations into one entry.
	 */
	public void testTwoContentEqualEqualityByValueItemsAreToldApart() {
		ValueEqualTestConfig first = TypedConfiguration.newConfigItem(ValueEqualTestConfig.class);
		first.setName("same");
		ValueEqualTestConfig second = TypedConfiguration.newConfigItem(ValueEqualTestConfig.class);
		second.setName("same");
		assertEquals("Precondition: the two items must be value-equal.", first, second);

		PropertyDescriptor property = first.descriptor().getProperty(ValueEqualTestConfig.NAME);
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
