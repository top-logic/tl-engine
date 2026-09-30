/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigSelectFieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;

/**
 * Tests for {@link ConfigSelectFieldModel}.
 */
public class TestConfigSelectFieldModel extends TestCase {

	/** Test enum. */
	public enum Color {
		/** Red. */
		RED,
		/** Green. */
		GREEN,
		/** Blue. */
		BLUE
	}

	/** Test configuration with an enum property. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		@Name(COLOR)
		Color getColor();

		void setColor(Color value);
	}

	/**
	 * Tests that enum constants are available as options.
	 */
	public void testOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		assertEquals(3, model.getOptions().size());
		assertEquals(Color.RED, model.getOptions().get(0));
		assertEquals(Color.GREEN, model.getOptions().get(1));
		assertEquals(Color.BLUE, model.getOptions().get(2));
		assertFalse(model.isMultiple());
	}

	/**
	 * Tests setting a value through the select model.
	 */
	public void testSetValueEnum() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		model.setValue(Color.GREEN);
		assertEquals(Color.GREEN, config.getColor());
		assertEquals(Color.GREEN, model.getValue());
	}

	/**
	 * Tests that setOptions fires the options listener.
	 */
	public void testOptionsListener() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		Object[] capturedOptions = new Object[1];
		model.addOptionsListener(new SelectFieldModel.SelectOptionsListener() {
			@Override
			public void onOptionsChanged(SelectFieldModel source, List<?> newOptions) {
				capturedOptions[0] = newOptions;
			}
		});

		List<Color> newOptions = Arrays.asList(Color.RED, Color.BLUE);
		model.setOptions(newOptions);

		assertNotNull("Options listener should have been called", capturedOptions[0]);
		assertEquals(2, ((List<?>) capturedOptions[0]).size());
	}

	/**
	 * {@link ConfigSelectFieldModel#setValue(Object)} forwards a non-{@link String} value
	 * unchanged to {@link com.top_logic.layout.configedit.ConfigFieldModel#setValue(Object)}, so
	 * the null-refusal for a technically mandatory property (see
	 * {@link com.top_logic.layout.configedit.ConfigFieldModel}) already applies here without any
	 * change of its own: clearing the (non-nullable) enum selection is refused as a field error,
	 * leaving the configuration untouched.
	 */
	public void testSetValueRejectsNullForNonNullableProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setColor(Color.BLUE);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		model.setValue(null);

		assertEquals("Clearing a non-nullable selection must not change its value.", Color.BLUE, config.getColor());
		assertNotNull("Clearing a non-nullable selection must be reported as a field error.",
			model.getInputError());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the label resolution the rejected-value error message uses.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigSelectFieldModel.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
