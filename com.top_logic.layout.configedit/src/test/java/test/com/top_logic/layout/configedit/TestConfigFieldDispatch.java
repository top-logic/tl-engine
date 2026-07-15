/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigFieldDispatch;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests for {@link ConfigFieldDispatch}.
 */
public class TestConfigFieldDispatch extends TestCase {

	/** Test configuration with various property types. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isFlag()}. */
		String FLAG = "flag";

		/** Property name for {@link #getRatio()}. */
		String RATIO = "ratio";

		@Name(TEXT)
		String getText();

		void setText(String value);

		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		void setCount(int value);

		@Name(FLAG)
		boolean isFlag();

		void setFlag(boolean value);

		@Name(RATIO)
		double getRatio();

		void setRatio(double value);
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Tests that a String property produces a ReactTextInputControl.
	 */
	public void testStringProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.TEXT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		ReactControl control = ConfigFieldDispatch.createPlainControl(createTestContext(), model);
		assertTrue("String property should create ReactTextInputControl",
			control instanceof ReactTextInputControl);
	}

	/**
	 * Tests that a boolean property produces a ReactCheckboxControl.
	 */
	public void testBooleanProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.FLAG);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		ReactControl control = ConfigFieldDispatch.createPlainControl(createTestContext(), model);
		assertTrue("boolean property should create ReactCheckboxControl",
			control instanceof ReactCheckboxControl);
	}

	/**
	 * Tests that an int property produces a ReactNumberInputControl.
	 */
	public void testIntProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		ReactControl control = ConfigFieldDispatch.createPlainControl(createTestContext(), model);
		assertTrue("int property should create ReactNumberInputControl",
			control instanceof ReactNumberInputControl);
	}

	/**
	 * Tests that a double property produces a ReactNumberInputControl.
	 */
	public void testDoubleProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.RATIO);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		ReactControl control = ConfigFieldDispatch.createPlainControl(createTestContext(), model);
		assertTrue("double property should create ReactNumberInputControl",
			control instanceof ReactNumberInputControl);
	}

	/**
	 * Suite requiring TypeIndex for TypedConfiguration.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigFieldDispatch.class, TypeIndex.Module.INSTANCE);
	}
}
