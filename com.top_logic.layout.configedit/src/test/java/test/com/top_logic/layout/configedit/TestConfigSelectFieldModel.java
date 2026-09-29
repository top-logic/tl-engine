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

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
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
	 * Suite requiring TypeIndex for TypedConfiguration.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigSelectFieldModel.class, TypeIndex.Module.INSTANCE);
	}
}
